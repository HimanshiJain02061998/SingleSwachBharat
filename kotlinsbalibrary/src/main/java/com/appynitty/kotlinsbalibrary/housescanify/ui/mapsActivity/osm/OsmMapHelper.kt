package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.osm

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.view.MotionEvent
import androidx.core.content.res.ResourcesCompat
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapEventListener
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapLayerChanger
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.IOrientationConsumer
import org.osmdroid.views.overlay.compass.IOrientationProvider
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File


private const val TAG = "OsmMapHelper"

class OsmMapHelper(private val listener: MapEventListener) {

    companion object {
        fun initMap(filesDir: File) {
            Configuration.getInstance().osmdroidTileCache = filesDir
            Configuration.getInstance().userAgentValue = CommonUtils.PACKAGE_NAME
        }
    }

    fun moveCamera(
        mapController: IMapController,
        currentLatitude: Double?,
        currentLongitude: Double?
    ) {
        if (currentLatitude != null && currentLongitude != null) {
            val geoPoint = GeoPoint(currentLatitude, currentLongitude)
            mapController.setCenter(geoPoint)
            mapController.zoomTo(16, 1000)
            mapController.animateTo(geoPoint)
        }
    }

    fun moveCameraAt21Zoom(
        mapController: IMapController,
        currentLatitude: Double?,
        currentLongitude: Double?
    ) {
        if (currentLatitude != null && currentLongitude != null) {
            val geoPoint = GeoPoint(currentLatitude, currentLongitude)
            mapController.setCenter(geoPoint)
            mapController.zoomTo(21, 1000)
            mapController.animateTo(geoPoint)
        }
    }

    fun initMapView(context: Context, osmMap: MapView) {
        osmMap.tileProvider.tileCache.protectedTileComputers.clear()
        osmMap.tileProvider.tileCache.setAutoEnsureCapacity(false)
        osmMap.setMultiTouchControls(true)

        MapLayerChanger(context, osmMap).inflateMapLayer(false)


    }

    fun attachMapFirstLoadedListener(osmMap: MapView) {
        osmMap.addOnFirstLayoutListener { _, _, _, _, _ ->
            listener.onMapFirstLoaded()
        }
    }

    fun attachMapListener(osmMap: MapView) {

        osmMap.addMapListener(object : MapListener{
            override fun onScroll(p0: ScrollEvent?): Boolean {
                val midLatLng = osmMap.mapCenter
                listener.onOsmMapCenterChanged(midLatLng.latitude ,midLatLng.longitude)
                return false
            }

            override fun onZoom(p0: ZoomEvent?): Boolean {
                val midLatLng = osmMap.mapCenter
                listener.onOsmMapCenterChanged(midLatLng.latitude ,midLatLng.longitude)
                return false
            }

        })
        osmMap.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val midLatLng = osmMap.mapCenter
                listener.onMapCenterChanged(midLatLng.latitude, midLatLng.longitude)
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                val midLatLng = osmMap.mapCenter
                listener.onMapCenterChanged(midLatLng.latitude, midLatLng.longitude)
                return false
            }

        },100))
    }

    fun addMarker(
        context : Context,
        osmMap: MapView,
        latLng: LatLng,
        title: String?,
        drawableId : Int
    ){
        val marker = Marker(osmMap)
        marker.position = GeoPoint(latLng.latitude, latLng.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        marker.icon = ResourcesCompat.getDrawable(
            context.resources, drawableId, null
        )
        osmMap.overlays.add(marker)
    }
    fun drawPolyline(
        osmMap: MapView,
        polyLine: Polyline,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ) {
        polyLine.outlinePaint.color = Color.rgb(194, 24, 7)
        polyLine.outlinePaint.strokeWidth = 15f
        osmMap.overlayManager.add(polyLine)
        polyLine.addPoint(startPoint)
        polyLine.addPoint(endPoint)
        polyLine.outlinePaint.setPathEffect(DashPathEffect(floatArrayOf(10f, 20f), 0f))

    }


}