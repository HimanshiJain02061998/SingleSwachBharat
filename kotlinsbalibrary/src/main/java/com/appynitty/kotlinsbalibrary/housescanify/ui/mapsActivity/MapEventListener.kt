package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity

import com.google.android.gms.maps.GoogleMap

interface MapEventListener {
    fun onMapCenterChanged(midLat: Double, midLng: Double, googleMap: GoogleMap? = null)
    fun onMapFirstLoaded(googleMap: GoogleMap? = null)

    fun onOsmMapCenterChanged(midLat: Double , midLng: Double)
}