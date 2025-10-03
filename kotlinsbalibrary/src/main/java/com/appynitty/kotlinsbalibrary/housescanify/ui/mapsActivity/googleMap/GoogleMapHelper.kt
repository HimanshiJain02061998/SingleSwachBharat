package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.googleMap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapEventListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions

class GoogleMapHelper(private val listener: MapEventListener) : OnMapReadyCallback {

    fun initMapView(supportFragmentManager: FragmentManager , mapId : Int) {
        val mapFragment = supportFragmentManager.findFragmentById(mapId) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = true
        listener.onMapFirstLoaded(googleMap)

        googleMap.setOnCameraIdleListener{
            val midLatLng = googleMap.cameraPosition.target
            listener.onMapCenterChanged(midLatLng.latitude, midLatLng.longitude,googleMap)
        }
    }

}