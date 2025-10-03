package com.appynitty.kotlinsbalibrary.common.ui.my_location

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.location.awaitCurrentLocation
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.databinding.ActivityMyLocationBinding
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapEventListener
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapLayerChanger
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.googleMap.GoogleMapHelper
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.osm.OsmMapHelper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


@AndroidEntryPoint
class MyLocationActivity : AppCompatActivity(), SensorEventListener,
    MapEventListener {

    private lateinit var binding: ActivityMyLocationBinding
    private var mSensorManager: SensorManager? = null
    private var orientationMeter: Sensor? = null
    private val mRotationMatrix = FloatArray(16)
    private var lastBearingAngle = 0f
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentLocation: LatLng? = null
    private val viewModel: MyLocationViewModel by viewModels()
    private var marker: Marker? = null
    private var osmMarker: org.osmdroid.views.overlay.Marker? = null
    private var isSatelliteViewEnabled = true
    private lateinit var languageDataStore: LanguageDataStore
    private var isOpenStreetMap = true

    private lateinit var osmMapHelper: OsmMapHelper

    private lateinit var googleMapHelper: GoogleMapHelper
    private var googleMap: GoogleMap? = null

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let { context ->
            languageDataStore = LanguageDataStore(newBase.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            LanguageConfig.changeLanguage(context, appLanguage.languageId.toString())
        }
        super.attachBaseContext(newBase)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isOpenStreetMap) {
            OsmMapHelper.initMap(filesDir)
        }

        binding = ActivityMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLatitude = intent.getDoubleExtra("latitude", 0.0)
        currentLongitude = intent.getDoubleExtra("longitude", 0.0)
        if (currentLatitude != null)
            currentLocation = LatLng(currentLatitude!!, currentLongitude!!)

        initSensors()
        initMap()

        binding.satelliteViewBtn.setOnClickListener {

            isSatelliteViewEnabled = !isSatelliteViewEnabled

            if (isSatelliteViewEnabled) {
                binding.satelliteViewBtn.setImageResource(R.drawable.icn_satellite_view_after)
            } else {
                binding.satelliteViewBtn.setImageResource(R.drawable.icn_satellite_view_before)
            }

            if (isOpenStreetMap){
                MapLayerChanger(this, binding.openStreetMap).inflateMapLayer(!isSatelliteViewEnabled)
            }else{
                if (isSatelliteViewEnabled) {
                    googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                } else {
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
            }

        }

        binding.refreshButton.setOnClickListener {
            getInstantLocation()
        }

        viewModel.userLatLong.observe(this) {
            if (it.latitude.isNotEmpty()) {

                currentLocation = LatLng(
                    it.latitude.toDouble(),
                    it.longitude.toDouble()
                )

                if (isOpenStreetMap) {
                    if (osmMarker != null) {
                        osmMapHelper.moveCameraAt21Zoom(
                            binding.openStreetMap.controller,
                            it.latitude.toDouble(),
                            it.longitude.toDouble()
                        )
                        osmMarker?.position =
                            GeoPoint(it.latitude.toDouble(), it.longitude.toDouble())
                    }
                } else {
                    if (marker != null) {
                        val newLatLng = LatLng(
                            it.latitude.toDouble(),
                            it.longitude.toDouble()
                        )
                        marker?.position = newLatLng
                        changePositionSmoothly(marker, newLatLng)
                        googleMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                marker?.position!!,
                                19.0f
                            )
                        )
                    }
                }

            }
        }

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

    private fun initSensors() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager != null)
            orientationMeter = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    private fun initMap() {

        if (isOpenStreetMap) {
            osmMapHelper = OsmMapHelper(this)
            binding.openStreetMap.visibility = View.VISIBLE
            osmMapHelper.initMapView(this, binding.openStreetMap)

            binding.openStreetMap.overlays.add(RotationGestureOverlay(binding.openStreetMap))

            if (isOpenStreetMap) {
                osmMapHelper.attachMapFirstLoadedListener(binding.openStreetMap)
            }
        } else {
            googleMapHelper = GoogleMapHelper(this)
            binding.map.visibility = View.VISIBLE
            googleMapHelper.initMapView(supportFragmentManager, R.id.map)
        }

    }


    private fun updateCameraBearing(bearing: Float) {

        if (isOpenStreetMap) {
            binding.openStreetMap.mapOrientation = bearing
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

    private fun changePositionSmoothly(marker: Marker?, newLatLng: LatLng) {
        if (marker == null) {
            return
        }
        val animation = ValueAnimator.ofFloat(0f, 100f)
        var previousStep = 0f
        val deltaLatitude = newLatLng.latitude - marker.position.latitude
        val deltaLongitude = newLatLng.longitude - marker.position.longitude
        animation.duration = 1000
        animation.addUpdateListener { updatedAnimation ->
            val deltaStep = updatedAnimation.animatedValue as Float - previousStep
            previousStep = updatedAnimation.animatedValue as Float
            marker.position = LatLng(
                marker.position.latitude + deltaLatitude * deltaStep * 1 / 100,
                marker.position.longitude + deltaStep * deltaLongitude * 1 / 100
            )
        }
        animation.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /// no need to implement
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        var bitmap: Bitmap? = null
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(
            0,
            0,
            90,
            90
        )
        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(
                90,
                90,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap!!)
    }

    private fun getInstantLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val priority = Priority.PRIORITY_HIGH_ACCURACY
            lifecycleScope.launch {
                val location =
                    LocationServices.getFusedLocationProviderClient(this@MyLocationActivity)
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
                                binding.openStreetMap.controller,
                                currentLatitude,
                                currentLongitude
                            )
                            osmMarker?.position =
                                GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)

                        } else {
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation!!,
                                    19.0f
                                )
                            )
                            marker?.position = currentLocation!!
                        }
                    }
                }


            }
        }
    }

    override fun onMapCenterChanged(midLat: Double, midLng: Double, googleMap: GoogleMap?) {

    }

    override fun onMapFirstLoaded(googleMap: GoogleMap?) {

        if (isOpenStreetMap) {
            osmMapHelper.moveCamera(
                binding.openStreetMap.controller,
                currentLatitude,
                currentLongitude
            )
            if (currentLocation != null) {

                osmMarker = org.osmdroid.views.overlay.Marker(binding.openStreetMap)
                osmMarker?.position =
                    GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
                osmMarker?.setAnchor(
                    org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                    org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                )
                osmMarker?.icon =
                    ResourcesCompat.getDrawable(getResources(), R.drawable.location_person, null)
                binding.openStreetMap.overlays.add(osmMarker)
                //   binding.openStreetMap.invalidate()
                Handler(mainLooper).postDelayed(
                    {
                        osmMapHelper.moveCameraAt21Zoom(
                            binding.openStreetMap.controller,
                            currentLatitude,
                            currentLongitude
                        )
                    },
                    2000
                )
            }
        } else {
            this.googleMap = googleMap
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 19.0f))
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

            if (currentLocation != null) {
                marker = googleMap?.addMarker(
                    MarkerOptions().position(currentLocation!!)
                        .icon(
                            bitmapFromVector(
                                applicationContext, R.drawable.location_person
                            )
                        )
                )
            }
        }

    }

    override fun onOsmMapCenterChanged(midLat: Double, midLng: Double) {

    }

}