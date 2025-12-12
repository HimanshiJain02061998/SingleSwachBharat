package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.location.awaitCurrentLocation
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.databinding.ActivityMapsBinding
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpHouseOnMapResponse
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.googleMap.GoogleMapHelper
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.osm.OsmMapHelper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


private const val TAG = "MapsActivity"
private const val PATTERN_DASH_LENGTH_PX = 20f
private const val PATTERN_GAP_LENGTH_PX = 20f
private const val DISTANCE_LIMIT = 500

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), SensorEventListener,
    MapEventListener {

    private lateinit var binding: ActivityMapsBinding
    private var mSensorManager: SensorManager? = null
    private var orientationMeter: Sensor? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var updateLatitude: Double? = null
    private var updateLongitude: Double? = null
    private var currentLocation: LatLng? = null
    private lateinit var mDot: PatternItem
    private lateinit var mDash: PatternItem
    private lateinit var mGap: PatternItem
    private lateinit var alphaPatternPolygon: List<PatternItem>
    private val mRotationMatrix = FloatArray(16)
    private var lastBearingAngle = 0f
    private var houseOnMapList = arrayListOf<EmpHouseOnMapResponse>()
    private var submittedHouseList = arrayListOf<LatLng>()
    private val viewModel: MapViewModel by viewModels()
    private var lastScannedHouseLatLng: LatLng? = null
    private var polyline: Polyline? = null

    private var isOpenStreetMap = true
    private lateinit var osmMapHelper: OsmMapHelper
    private lateinit var googleMapHelper: GoogleMapHelper
    private var googleMap: GoogleMap? = null
    private var osmPolyLine: org.osmdroid.views.overlay.Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            finish()
        }
        if (isOpenStreetMap) {
            OsmMapHelper.initMap(filesDir)
        }
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isOpenStreetMap) {
            osmPolyLine = org.osmdroid.views.overlay.Polyline(binding.osmMap)
            osmMapHelper = OsmMapHelper(this)
        } else {
            googleMapHelper = GoogleMapHelper(this)
        }

        initVars()
        prepareData()
        registerClickEvents()
        registerMapListeners()
        observerLiveData()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)
    }

    override fun finish() {

        super.finish()
    }

    private fun registerMapListeners() {
        if (isOpenStreetMap) {
            osmMapHelper.attachMapFirstLoadedListener(binding.osmMap)
        }
    }

    private fun observerLiveData() {
        viewModel.lastScannedHouseLatLong.observe(this) {
            if (it != null) {
                if (it.latitude != null && it.latitude != "null" && it.latitude.isNotEmpty() && it.longitude.isNotEmpty()) {
                    try {
                        lastScannedHouseLatLng =
                            LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                    } catch (e: Exception) {
                        Log.i(TAG, "observerLiveData: ${e.message}")
                    }
                }
            }
        }
    }

    private fun getInstantLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val priority = Priority.PRIORITY_HIGH_ACCURACY
            lifecycleScope.launch {
                val location =
                    LocationServices.getFusedLocationProviderClient(this@MapsActivity)
                        .awaitCurrentLocation(priority)
                if (location != null) {
                    viewModel.saveUserLocation(
                        UserLatLong(
                            location.latitude.toString(), location.longitude.toString(), ""
                        )
                    )

                    withContext(Dispatchers.Main) {
                        currentLocation = LatLng(
                            location.latitude,
                            location.longitude
                        )

                        currentLatitude = currentLocation?.latitude
                        currentLongitude = currentLocation?.longitude

                        if (isOpenStreetMap) {
                            osmMapHelper.moveCameraAt21Zoom(
                                binding.osmMap.controller,
                                currentLatitude,
                                currentLongitude
                            )
                        } else {
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation!!,
                                    19.0f
                                )
                            )
                        }
                    }

                }

            }
        }
    }

    private fun registerClickEvents() {

        binding.confirmButton.setOnClickListener {
            val intent = intent

            val distanceFromLastHouse = calcDistance(lastScannedHouseLatLng,
                updateLatitude?.let { it1 -> updateLongitude?.let { it2 -> LatLng(it1, it2) } })

            if (distanceFromLastHouse > 100 && lastScannedHouseLatLng != null) {
                distanceDialogAlert(distanceFromLastHouse)
            } else {
                intent.putExtra("updateLat", updateLatitude.toString())
                intent.putExtra("updateLong", updateLongitude.toString())
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        binding.refreshLocationBtn.setOnClickListener {
            getInstantLocation()
        }
    }

    private fun distanceDialogAlert(distanceFromLastHouse: Double) {
        val positiveText = resources.getString(R.string.confirm)
        val negativeText = resources.getString(R.string.refresh_location)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.distance_alert))
        builder.setMessage(
            resources.getString(
                R.string.distance_alert_desc,
                distanceFromLastHouse.toInt()
            )
        )
        builder.setCancelable(false)
            .setPositiveButton(positiveText) { _: DialogInterface, _: Int ->
                intent.putExtra("updateLat", updateLatitude.toString())
                intent.putExtra("updateLong", updateLongitude.toString())
                setResult(RESULT_OK, intent)
                finish()
            }.setNegativeButton(negativeText) { _: DialogInterface, _: Int ->
                getInstantLocation()
            }.create()
            .show()
    }

    private fun initVars() {

        currentLatitude = intent.getDoubleExtra("latitude", 0.0)
        currentLongitude = intent.getDoubleExtra("longitude", 0.0)

        updateLatitude = currentLatitude
        updateLongitude = currentLongitude

        houseOnMapList = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableArrayListExtra(
                "houseOnMapList",
                EmpHouseOnMapResponse::class.java
            ) as ArrayList<EmpHouseOnMapResponse>
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableArrayListExtra(
                "houseOnMapList"
            )!!
        }

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager != null)
            orientationMeter = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        mDot = Dot()
        mDash = Dash(PATTERN_DASH_LENGTH_PX)
        mGap = Gap(PATTERN_GAP_LENGTH_PX)
        alphaPatternPolygon = listOf(mGap, mDot)

        if (isOpenStreetMap) {
            binding.osmMap.visibility = View.VISIBLE
            osmMapHelper.initMapView(this, binding.osmMap)



            binding.osmMap.overlays.add(RotationGestureOverlay(binding.osmMap))


            val mapNorthCompassOverlay = object : CompassOverlay(this, binding.osmMap) {
                override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                    mapView?.mapOrientation = 0f
                    return super.onSingleTapConfirmed(e, mapView)
                }

                override fun draw(c: Canvas?, pProjection: Projection?) {
                    drawCompass(c, -binding.osmMap.mapOrientation, pProjection?.screenRect)
                }

            }

            binding.osmMap.overlays.add(mapNorthCompassOverlay)

        } else {
            binding.googleMap.visibility = View.VISIBLE
            googleMapHelper.initMapView(supportFragmentManager, R.id.googleMap)
        }

    }

    private fun prepareData() {

        if (houseOnMapList.isNotEmpty()) {
            houseOnMapList.forEach { latLong ->
                val currentLocation =
                    latLong.latitude?.toDouble()
                        ?.let { latLong.longitude?.let { it1 -> LatLng(it, it1.toDouble()) } }
                if (currentLocation != null) {
                    submittedHouseList.add(currentLocation)
                }
            }
        }
        if (currentLatitude != null && currentLongitude != null)
            currentLocation = LatLng(currentLatitude!!, currentLongitude!!)

    }


    private fun updateCameraBearing(bearing: Float) {

        if (isOpenStreetMap) {
            binding.osmMap.mapOrientation = -bearing
        } else {
            if (googleMap == null) return
            val camPos = googleMap?.cameraPosition?.let {
                CameraPosition
                    .builder(
                        it // current Camera
                    )
                    .tilt(0f)
                    .bearing(bearing)
                    .build()
            }
            camPos?.let { CameraUpdateFactory.newCameraPosition(it) }
                ?.let { googleMap?.animateCamera(it) }
        }
    }


    override fun onMapCenterChanged(midLat: Double, midLng: Double, googleMap: GoogleMap?) {

        if (isOpenStreetMap) {
            osmPolyLine?.actualPoints?.clear()
            osmPolyLine?.let {
                osmMapHelper.drawPolyline(
                    binding.osmMap,
                    it,
                    GeoPoint(currentLatitude!!, currentLongitude!!),
                    GeoPoint(midLat, midLng)
                )
            }
        } else {

            updateLatitude = midLat
            updateLongitude = midLng
            polyline?.remove()
            val options = PolylineOptions().pattern(alphaPatternPolygon)
                .color(Color.RED).width(15f)
            polyline = googleMap?.addPolyline(
                options.add(
                    LatLng(currentLatitude!!, currentLongitude!!),
                    LatLng(midLat, midLng)
                )
            )
        }
        if (calcDistance(
                LatLng(
                    midLat,
                    midLng
                )
            ) > DISTANCE_LIMIT
        ) {
            if (isOpenStreetMap) {
                osmPolyLine?.actualPoints?.clear()
                osmMapHelper.moveCameraAt21Zoom(
                    binding.osmMap.controller,
                    currentLatitude,
                    currentLongitude
                )
            } else {
                polyline?.remove()
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 19.0f))
            }
            val text = getString(R.string.distance_warning, DISTANCE_LIMIT)
            CustomToast.showWarningToast(this@MapsActivity, text)
        }
    }

    override fun onMapFirstLoaded(googleMap: GoogleMap?) {

        if (isOpenStreetMap) {
            osmMapHelper.moveCamera(binding.osmMap.controller, currentLatitude, currentLongitude)
            Handler(mainLooper).postDelayed(
                {
                    osmMapHelper.attachMapListener(binding.osmMap)
                    osmMapHelper.moveCameraAt21Zoom(
                        binding.osmMap.controller,
                        currentLatitude,
                        currentLongitude
                    )
                },
                2000
            )
            addMarkersOnMap()
        } else {
            this.googleMap = googleMap
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        currentLatitude!!,
                        currentLongitude!!
                    ), 19.0f
                )
            )
            addMarkersOnMap(googleMap)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            googleMap?.isMyLocationEnabled = true
        }

    }

    override fun onOsmMapCenterChanged(midLat: Double, midLng: Double) {
        updateLatitude = midLat
        updateLongitude = midLng
    }

    private fun addMarkersOnMap(googleMap: GoogleMap? = null) {
        if (submittedHouseList.isNotEmpty()) {
            for (i in submittedHouseList.indices) {
                // below line is use to add marker to each location of our array list.
                when (houseOnMapList[i].gcType) {
                    1 -> {
                        //house
                        if (isOpenStreetMap) {
                            osmMapHelper.addMarker(
                                this,
                                binding.osmMap,
                                submittedHouseList[i],
                                houseOnMapList[i].referenceId,
                                R.drawable.icn_house
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(submittedHouseList[i])
                                    .title(houseOnMapList[i].referenceId).icon(
                                        bitmapFromVector(
                                            applicationContext, R.drawable.icn_house
                                        )
                                    )
                            )
                        }
                    }

                    3 -> {
                        //dump
                        if (isOpenStreetMap) {
                            osmMapHelper.addMarker(
                                this,
                                binding.osmMap,
                                submittedHouseList[i],
                                houseOnMapList[i].referenceId,
                                R.drawable.dump_yard_marker
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(submittedHouseList[i])
                                    .title(houseOnMapList[i].referenceId).icon(
                                        bitmapFromVector(
                                            applicationContext, R.drawable.dump_yard_marker
                                        )
                                    )
                            )
                        }


                    }

                    4 -> {
                        //liquid
                        if (isOpenStreetMap) {
                            osmMapHelper.addMarker(
                                this,
                                binding.osmMap,
                                submittedHouseList[i],
                                houseOnMapList[i].referenceId,
                                R.drawable.liquidwaste_marker
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(submittedHouseList[i])
                                    .title(houseOnMapList[i].referenceId).icon(
                                        bitmapFromVector(
                                            applicationContext, R.drawable.liquidwaste_marker
                                        )
                                    )
                            )
                        }


                    }

                    5 -> {
                        //street
                        if (isOpenStreetMap) {
                            osmMapHelper.addMarker(
                                this,
                                binding.osmMap,
                                submittedHouseList[i],
                                houseOnMapList[i].referenceId,
                                R.drawable.street_sweep_marker
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(submittedHouseList[i])
                                    .title(houseOnMapList[i].referenceId).icon(
                                        bitmapFromVector(
                                            applicationContext, R.drawable.street_sweep_marker
                                        )
                                    )
                            )
                        }

                    }

                    12 -> {
                        //street
                        if (isOpenStreetMap) {
                            osmMapHelper.addMarker(
                                this,
                                binding.osmMap,
                                submittedHouseList[i],
                                houseOnMapList[i].referenceId,
                                R.drawable.icn_masterplate
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(submittedHouseList[i])
                                    .title(houseOnMapList[i].referenceId).icon(
                                        bitmapFromVector(
                                            applicationContext, R.drawable.icn_masterplate
                                        )
                                    )
                            )
                        }

                    }
                }

            }
        }
    }


    override fun onSensorChanged(sensorEvent: SensorEvent?) {

        if (sensorEvent != null) {
            if (sensorEvent.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, sensorEvent.values
                )
                val orientation = FloatArray(3)
                SensorManager.getOrientation(mRotationMatrix, orientation)
                val bearing = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (bearing < 0) {
                    if (bearing < -20) {
                        if (lastBearingAngle == 0f) {
                            updateCameraBearing(bearing)
                            lastBearingAngle = bearing
                        } else {
                            if (kotlin.math.abs(
                                    kotlin.math.abs(lastBearingAngle) - kotlin.math.abs(
                                        bearing
                                    )
                                ) > 10
                            ) {
                                updateCameraBearing(bearing)
                                lastBearingAngle = bearing
                            }
                        }
                    }
                } else {
                    if (bearing > 20) {
                        if (lastBearingAngle == 0f) {
                            updateCameraBearing(bearing)
                            lastBearingAngle = bearing
                        } else {
                            if (kotlin.math.abs(
                                    kotlin.math.abs(lastBearingAngle) - kotlin.math.abs(
                                        bearing
                                    )
                                ) > 10
                            ) {
                                updateCameraBearing(bearing)
                                lastBearingAngle = bearing
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /// no need to implement
    }

    override fun onResume() {
        super.onResume()

        if (orientationMeter != null)
            mSensorManager?.registerListener(
                this,
                orientationMeter,
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
            )
    }


    private fun calcDistance(newPosition: LatLng?): Double {
        val startPoint = Location("locationA")
        startPoint.latitude = currentLatitude!!
        startPoint.longitude = currentLongitude!!
        val endPoint = Location("locationB")
        if (newPosition != null) {
            endPoint.latitude = newPosition.latitude
            endPoint.longitude = newPosition.longitude
        }
        return startPoint.distanceTo(endPoint).toDouble()
    }

    private fun calcDistance(newPosition: LatLng?, lastPosition: LatLng?): Double {
        val startPoint = Location("locationA")
        if (lastPosition != null && newPosition != null) {
            startPoint.latitude = lastPosition.latitude
            startPoint.longitude = lastPosition.longitude
            val endPoint = Location("locationB")
            endPoint.latitude = newPosition.latitude
            endPoint.longitude = newPosition.longitude

            return startPoint.distanceTo(endPoint).toDouble()
        }
        return 0.0
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        var bitmap: Bitmap? = null
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap!!)
    }


}