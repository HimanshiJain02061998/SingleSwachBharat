package com.appynitty.kotlinsbalibrary.common.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.dao.LocationDao
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.dao.UserDetailsDao
import com.appynitty.kotlinsbalibrary.common.model.GisLatLong
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.model.request.LocationApiRequest
import com.appynitty.kotlinsbalibrary.common.model.response.LocationApiResponse
import com.appynitty.kotlinsbalibrary.common.model.response.NearestLatLng
import com.appynitty.kotlinsbalibrary.common.repository.LocationRepository
import com.appynitty.kotlinsbalibrary.common.repository.NearestLatLngRepository
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.UserTravelLoc
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.GarbageCollectionResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt


private const val TAG = "GisLocationServiceTest"

@AndroidEntryPoint
class GisLocationService : LifecycleService(), SensorEventListener {

    @Inject
    lateinit var garbageCollectionRepo: GarbageCollectionRepo

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var userDetailsDao: UserDetailsDao

    //todo -> uncomment when gis will be on
//    @Inject
//    lateinit var gisLocDao: GisLocDao
//    @Inject
//    lateinit var gisApi: GisApi

    @Inject
    lateinit var locationDao: LocationDao

    @Inject
    lateinit var nearestLatLngDao: NearestLatLngDao

    @Inject
    lateinit var userTravelLocDao: UserTravelLocDao

    @Inject
    lateinit var nearestLatLngRepository: NearestLatLngRepository

    private lateinit var userDataStore: UserDataStore
    private lateinit var sessionDataStore: SessionDataStore

    private var userData: UserData? = null
    private var latitude: String? = null
    private var longitude: String? = null

    private var internetLiveData: ConnectivityStatus? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var mTimer: Timer? = null
    private var userId: String = ""

    private var isDutyOn: Boolean = true
    private var gisStartTs: String = ""

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var stepSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private lateinit var mGravity: FloatArray
    private var mAccel = 0f
    private var mAccelCurrent = 0f
    private var mAccelLast = 0f

    private var hitCount = 0
    private var hitSum = 0.0
    private var hitResult = 0.0
    private var isWalking = false
    private var stepList = ArrayList<String>()
    private val sampleSize =
        50 // change this sample size as you want, higher is more precise but slow measure.
    private val threshold = 0.2 // change this threshold as you want, higher is more spike movement

    private var gisNotifyInterval = (1000 * 60 * 3   //for 2 minutes
            ).toLong()

    private var locApiNotifyInterval = (1000 * 60 * 5    // for 10 minutes
            ).toLong()

    private lateinit var kalmanFilter: KalmanFilter
    private lateinit var oldLocationList: ArrayList<Location>
    private lateinit var noAccuracyLocationList: ArrayList<Location>
    private lateinit var inaccurateLocationList: ArrayList<Location>
    private lateinit var kalmanNGLocationList: ArrayList<Location>
    private lateinit var locationList: ArrayList<GisLatLong>

    private var isInternetOn = false


    //location request for fused location client
    private val locationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setMinUpdateDistanceMeters(5f)
            .build()

    //location call back for fused location client
    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {

            val location = locationResult.lastLocation
            if (location != null) {
                val requiredAccuracyInMeter = 15// adjust your need
                if (location.hasAccuracy() && location.accuracy <= requiredAccuracyInMeter) {

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()

                    scope.launch {
                        val prevLatLong = userDataStore.getUserLatLong.first()
                        var distance = "0"
                        if (prevLatLong.latitude != "") {
                            val prevLocation = Location("PrevLocation")
                            prevLocation.latitude = prevLatLong.latitude.toDouble()
                            prevLocation.longitude = prevLatLong.longitude.toDouble()

                            distance = prevLocation.distanceTo(location).toString()

                            val arr: Array<String> =
                                distance.split("\\.".toRegex()).toTypedArray()
                            val intArr = IntArray(5)
                            intArr[0] = arr[0].toInt()
                            distance = intArr[0].toString()

                            val nf: NumberFormat = NumberFormat.getInstance(
                                Locale(
                                    "en", "US"
                                )
                            )

                            val sDistance: String = nf.format(distance.toInt())
                            distance = String.format(distance, sDistance)
                        }
                        userDataStore.saveUserLatLong(
                            UserLatLong(
                                location.latitude.toString(),
                                location.longitude.toString(),
                                distance
                            )
                        )
                    }

                    if (location.speed < 1.5) {
                        if (stepList.size > 2) {
                            stepList.clear()
                            if (isWalking) {
                                scope.launch {
                                    val latLong = GisLatLong(
                                        0, location.latitude, location.longitude, gisStartTs
                                    )
                                    //todo -> uncomment when gis will be on
                                 //   gisLocDao.insertGisLatLong(latLong)
                                    val userTravel =
                                        UserTravelLoc(0, location.latitude, location.longitude)
                                    userTravelLocDao.insertUserTravelLatLong(userTravel)
                                    saveLocationsIntoRoomDb(location)
                                }
                                gotNewLocationCheckForNearest(location)
                                isWalking = false
                            }
                        }
                    } else {
                        stepList.clear()
                        if (isWalking) {
                            scope.launch {
                                val latLong = GisLatLong(
                                    0, location.latitude, location.longitude, gisStartTs
                                )
                                //todo -> uncomment when gis will be on
                               // gisLocDao.insertGisLatLong(latLong)
                                val userTravel =
                                    UserTravelLoc(0, location.latitude, location.longitude)
                                userTravelLocDao.insertUserTravelLatLong(userTravel)
                                gotNewLocationCheckForNearest(location)
                                saveLocationsIntoRoomDb(location)
                            }
                            isWalking = false
                        }
                    }

                }
            }
        }
    }

    private fun submitGarbageCollectionForNotScanned(actualNearestHouseList : ArrayList<NearestLatLng>) {
      scope.launch {
          val vehicleNumber = userDataStore.getUserVehicleDetails.first().vehicleNumber
          val notScannedGarbageCollectionData = ArrayList<GarbageCollectionData>()
          val userId = userDataStore.getUserEssentials.first().userId
          if (userData != null){
              actualNearestHouseList.forEach {
                  val garbageCollectionData = GarbageCollectionData(
                      0,
                      it.referenceId,
                      userId,
                      it.houseLat,
                      it.houseLong,
                      vehicleNumber,
                      "1",
                      "4",
                      DateTimeUtils.getScanningServerDate(),
                      CommonUtils.getBatteryStatus(application).toString(),
                      "0",
                      isLocation = false,
                      isOffline = true,
                      empType = "N",
                      note = "",
                      gpBeforeImage = "",
                      gpAfterImage = "",
                      gpBeforeImageTime = null,
                      totalGcWeight = "0.0",
                      totalDryWeight = "0.0",
                      totalWetWeight = "0.0"
                  )
                  notScannedGarbageCollectionData.add(garbageCollectionData)
              }
              if (notScannedGarbageCollectionData.isNotEmpty()){

                  try {
                      val response = garbageCollectionRepo.saveGarbageCollectionOfflineData(
                          CommonUtils.APP_ID,
                          "0",
                          CommonUtils.getBatteryStatus(application),
                          CommonUtils.CONTENT_TYPE,
                          notScannedGarbageCollectionData
                      )
                      handleNotScannedResponse(response)
                      Toast.makeText(
                          this@GisLocationService,
                          "Not scanned Api Hit ${notScannedGarbageCollectionData.size}",
                          Toast.LENGTH_LONG
                      ).show()
                  }catch (t : Throwable){
                      Log.e(TAG, "submitGarbageCollectionForNotScanned: ${t.message}" )
                  }
              }
          }
          userTravelLocDao.deleteAllUserTravelLatLongs()
      }
    }

    private fun handleNotScannedResponse(response: Response<List<GarbageCollectionResponse>>) {
        scope.launch {
            response.body()?.forEach {
                it.referenceID?.let { it1 -> nearestLatLngDao.deleteNearestHouseById(it1) }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {

        super.onCreate()

        userDataStore = UserDataStore(this)
        sessionDataStore = SessionDataStore(this)

        prepareData()

        kalmanFilter = KalmanFilter(3f)
        oldLocationList = ArrayList()
        noAccuracyLocationList = ArrayList()
        inaccurateLocationList = ArrayList()
        kalmanNGLocationList = ArrayList()
        locationList = ArrayList()

        mAccel = 0.00f
        mAccelCurrent = SensorManager.GRAVITY_EARTH
        mAccelLast = SensorManager.GRAVITY_EARTH

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)


        if (mAccelerometer == null) {
            Toast.makeText(this, "No accelerometer detected on this device", Toast.LENGTH_SHORT)
                .show()
        } else {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (stepSensor == null) {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(this, "No step sensor detected on this device", Toast.LENGTH_SHORT)
                .show()

            mSensorManager.registerListener(
                this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            // Rate suitable for the user interface
            mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sessionDataStore.getIsUserDutyOn.asLiveData().observe(this) {
            isDutyOn = it
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1, Notification()
        )
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Permission required", Toast.LENGTH_LONG).show()
            return
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }

        scope.launch {
            sessionDataStore.getGisStartTs.collect {
                gisStartTs = it
            }
        }

        internetLiveData = ConnectivityStatus(this)
        internetLiveData?.observeForever {
            isInternetOn = it
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {

        val notificationChannelId = "Location channel id"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        val notification: Notification =
            notificationBuilder.setOngoing(true).setContentTitle(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_notification_description))
                .setSmallIcon(R.drawable.ic_noti_icon)
                .setColor(resources.getColor(R.color.colorPrimary, resources.newTheme())).build()

        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        mTimer = Timer()
        mTimer!!.schedule(
            TimerTaskToSendGisLocation(), 10, gisNotifyInterval
        )
        mTimer!!.schedule(TimerTaskToSendLocation(), 10, locApiNotifyInterval)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        mSensorManager.unregisterListener(this)
        if (internetLiveData != null) internetLiveData?.removeObservers(this)

    }

    inner class TimerTaskToSendLocation : TimerTask() {
        override fun run() {
            sendLocation()
        }
    }

    inner class TimerTaskToSendGisLocation : TimerTask() {
        override fun run() {
            scope.launch {
                //todo -> uncomment when gis will be on
//                val tempList = gisLocDao.getAllGisLatLongs().first()
//                if (tempList.size > 2) {
//                    sendGisLocation(tempList)
//                }
                val userType = userDataStore.getUserEssentials.first()
                if (userType.userTypeId == "0" && userType.employeeType == "N") {
                    nearestLocationsCheck()
                }

            }
        }
    }

    private fun gotNewLocationCheckForNearest(location: Location) =
        scope.launch {
            val userType = userDataStore.getUserEssentials.first()
            if (userType.userTypeId == "0" && userType.employeeType == "N") {
                val lastScannedHouseLocation = userDataStore.getLastGhantaGadiScanLatLong.first()
                if (lastScannedHouseLocation.latitude.isNotEmpty() && lastScannedHouseLocation.longitude.isNotEmpty()) {
                    val prevLocation = Location("LastGhantaGadiLocation")
                    prevLocation.latitude = lastScannedHouseLocation.latitude.toDouble()
                    prevLocation.longitude = lastScannedHouseLocation.longitude.toDouble()
                    val distance = prevLocation.distanceTo(location)
                    if (distance < 50f) {
                        return@launch
                    } else {
                        getNearestHouseLocation()
                    }
                } else {
                    getNearestHouseLocation()
                }
            } else {
                return@launch
            }
        }

    private fun getNearestHouseLocation() = scope.launch {
        val userId = userDataStore.getUserEssentials.first().userId
        val userLatLng = userDataStore.getUserLatLong.first()
        val latitude = userLatLng.latitude
        val longitude = userLatLng.longitude
        userDataStore.saveLastGhantaGadiScanLatLong(UserLatLong(latitude, longitude, "0"))
        try {
            val response = nearestLatLngRepository.getNearestLatLongs(
                CommonUtils.APP_ID,
                latitude,
                longitude,
                userId.toInt()
            )

            if (response.isSuccessful) {
                GlobalScope.launch {
                  //  nearestLatLngDao.deleteAllNearestHouses()
                    response.body()?.forEach {
                        val existingHouse = nearestLatLngDao.getNearestHouseByRefId(it.referenceId)
                        if (existingHouse == null) {
                            nearestLatLngDao.insertNearestHouse(it)
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "getNearestLatLongs: ${t.message.toString()}")
        }
    }

    private fun nearestLocationsCheck() {
        scope.launch {
            val nearestLatLngs = nearestLatLngDao.getNearestHouses().first()
            if (nearestLatLngs.isNotEmpty()) {

                val actualNearestHouseList = ArrayList<NearestLatLng>()
                val userTravelLatLongs = userTravelLocDao.getAllUserTravelLatLongs().first()
                nearestLatLngs.forEach { nearestHouse ->

                    val nearestHouseLocation = Location("HouseLocation")
                    nearestHouseLocation.latitude = nearestHouse.houseLat.toDouble()
                    nearestHouseLocation.longitude = nearestHouse.houseLong.toDouble()
                    userTravelLatLongs.forEach { latLng ->
                        val gisLocation = Location("GisLocation")
                        gisLocation.latitude = latLng.latitude
                        gisLocation.longitude = latLng.longitude

                        Log.i(
                            "CheckLastDistanceAndThis",
                            "nearestLocationsCheck: ${nearestHouse.referenceId} away ${
                                nearestHouseLocation.distanceTo(gisLocation)
                            }"
                        )
                        if (nearestHouseLocation.distanceTo(gisLocation) < 25) {
                            actualNearestHouseList.add(
                                NearestLatLng(
                                    nearestHouse.referenceId,
                                    nearestHouse.houseLat,
                                    nearestHouse.houseLong,
                                    "0"
                                )
                            )
                        }
                    }
                }
                if (actualNearestHouseList.isNotEmpty())
                    submitGarbageCollectionForNotScanned(actualNearestHouseList)
            }

        }
    }

    private fun saveLocationsIntoRoomDb(location: Location) {

        val locationApiRequest = LocationApiRequest(
            0,
            userData!!.userId,
            location.latitude.toString(),
            location.longitude.toString(),
            DateTimeUtils.getGisServiceTimeStamp(),
            "",
            !isInternetOn
        )
        scope.launch {
            locationDao.insertLocation(locationApiRequest)
        }

    }

    private fun sendLocation() {

        if (isDutyOn) {

            if (userData != null) {
                if (latitude != null && longitude != null) {
                    scope.launch {

                        if (isInternetOn) {
                            val locList = locationDao.getAllLocationData().first()
                            if(locList.isNotEmpty()){
                                try {
                                    if (userData != null){
                                        val response = locationRepository.saveUserLocationToApi(
                                            CommonUtils.APP_ID,
                                            CommonUtils.CONTENT_TYPE,
                                            userData!!.userTypeId,
                                            userData!!.employeeType,
                                            CommonUtils.getBatteryStatus(application),
                                            locList
                                        )
                                        handleLocationResponse(response)
                                    }
                                } catch (t: Throwable) {
                                    when (t) {
                                        is IOException -> Log.e(TAG, "sendLocation: ${t.message}")
                                        else -> Log.e(TAG, "sendLocation: ${t.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun handleLocationResponse(response: Response<List<LocationApiResponse>>) {
        if (response.isSuccessful) {
            if (!response.body().isNullOrEmpty()) {
                response.body()?.forEach {
                    scope.launch {
                        locationDao.deleteLocationById(it.offlineId.toString())
                    }
                }
            }
        }
    }

    private fun prepareData() {
        scope.launch {
            userDetailsDao.gerUserData().collect {
                userData = it
            }
        }
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
        }
        val sessionDataStore = SessionDataStore(applicationContext)
        sessionDataStore.getIsUserDutyOn.asLiveData().observe(this) {
            isDutyOn = it
        }
    }

//todo -> uncomment when gis will be on
/*    private fun sendGisLocation(tempList: List<GisLatLong>) {

        Log.d(TAG, "sendGisLocation: $tempList")
        val offlineIds = StringBuilder()
        val mLineString = StringBuilder("Linestring (")

        for (i in tempList.indices) {
            if (i == (tempList.size - 1)) {

                mLineString.append(tempList[i].longitude)
                mLineString.append(" ")
                mLineString.append(tempList[i].latitude)
                mLineString.append(")")

                offlineIds.append(tempList[i].id)
            } else {

                mLineString.append(tempList[i].longitude)
                mLineString.append(" ")
                mLineString.append(tempList[i].latitude)
                mLineString.append(",")

                offlineIds.append("${tempList[i].id},")
            }
        }

        scope.launch {

            val trailId = sessionDataStore.getGisTrailId.first()

            val isRunning = if (isDutyOn) 1 else 0

            userId = userDataStore.getUserEssentials.first().userId

            if (userId.isNotEmpty()) {

            val gisLatLong = GisLocRequest(
                trailId,
                gisStartTs,
                gisStartTs,
                DateTimeUtils.getGisServiceTimeStamp(),
                DateTimeUtils.getGisServiceTimeStamp(),
                userId.toInt(),
                userId.toInt(),
                mLineString.toString(),
                offlineIds.toString(),
                isRunning
            )
            val mList = ArrayList<GisLocRequest>()
            mList.add(gisLatLong)

            val userData = userDataStore.getUserEssentials.first()
            val userTypeId = userData.userTypeId

            if (userTypeId == "0") {
                try {
                    val response = gisApi.saveGarbageMapGisLocations(CommonUtils.APP_ID, gisLatLong)
                    handleGisResponse(response)
                } catch (t: Throwable) {
                    when (t) {
                        is IOException -> Log.e(TAG, "sendGisLocation: ${t.message}")
                        else -> Log.e(TAG, "sendGisLocation: ${t.message}")
                    }
                }
            } else if (userTypeId == "1") {
                try {
                    val response = gisApi.saveHouseMapGisLocations(CommonUtils.APP_ID, gisLatLong)
                    handleGisResponse(response)
                } catch (t: Throwable) {
                    when (t) {
                        is IOException -> Log.e(TAG, "sendGisLocation: ${t.message}")
                        else -> Log.e(TAG, "sendGisLocation: ${t.message}")
                    }
                }
            }
        }
        }
    }*/
//todo -> uncomment when gis will be on
/*    private fun handleGisResponse(response: Response<GisLocResponse?>) {
        if (response.isSuccessful) {
            val gisLocResponse = response.body()
            if (gisLocResponse != null) {
                val offlineIds = gisLocResponse.offlineId
                if (offlineIds.isNotEmpty()) {
                    val offlineIdsList: List<String>? = offlineIds.split(",").map { it.trim() }
                    if (offlineIdsList?.isNotEmpty() == true) {
                        offlineIdsList.forEach {
                            scope.launch {
                                gisLocDao.deleteLocationById(it)
                            }
                        }
                    }
                }
            }
        }
    }*/

    override fun onSensorChanged(event: SensorEvent?) {

        if (event != null) {

            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepList.add(event.values[0].toString())
            } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                stepList.add(event.values[0].toString())
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values.clone()
                // Shake detection
                val x = mGravity[0].toDouble()
                val y = mGravity[1].toDouble()
                val z = mGravity[2].toDouble()
                mAccelLast = mAccelCurrent
                mAccelCurrent = sqrt(x * x + y * y + z * z).toFloat()
                val delta = (mAccelCurrent - mAccelLast).toDouble()
                mAccel = (mAccel * 0.9f + delta).toFloat()
                if (hitCount <= sampleSize) {
                    hitCount++
                    hitSum += abs(mAccel)
                } else {
                    hitResult = hitSum / sampleSize
                    isWalking = hitResult > threshold

                    hitCount = 0
                    hitSum = 0.0
                    hitResult = 0.0
                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


}