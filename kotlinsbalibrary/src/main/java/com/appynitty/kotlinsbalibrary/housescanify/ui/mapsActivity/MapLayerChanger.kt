package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity

import android.content.Context
import android.util.Log
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView

private const val TAG = "MapLayerChanger"
class MapLayerChanger(private val context: Context, private val map : MapView) {

    fun inflateMapLayer(isSatelliteViewEnabled : Boolean) {

        if (!isSatelliteViewEnabled) {
            try {
                val googleOnlineTile: OnlineTileSourceBase = object : OnlineTileSourceBase(
                    "GoogleNormal",
                    1,
                    20,
                    256,
                    ".png",
                    arrayOf("")
                ) {
                    override fun getTileURLString(pMapTileIndex: Long): String {
                        val i = MapTileIndex.getX(pMapTileIndex)
                        val j = MapTileIndex.getY(pMapTileIndex)
                        val k = MapTileIndex.getZoom(pMapTileIndex)
                       // return "https://mt0.google.com/vt/lyrs=y&hl=en&x=$i&y=$j&z=$k"
                        return "https://mt0.google.com/vt/lyrs=s&hl=en&x=$i&y=$j&z=$k"
                    }
                }

                TileSourceFactory.addTileSource(googleOnlineTile)
                map.setTileSource(googleOnlineTile)

            } catch (t: Throwable) {
                Log.d(TAG, "onViewCreated: ${t.message}")
                map.overlayManager.tilesOverlay.setColorFilter(null)
                map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }

        } else {

            try {

                map.overlayManager.tilesOverlay.setColorFilter(null)
                map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
//                val googleOnlineTile: OnlineTileSourceBase = object : OnlineTileSourceBase(
//                    "GoogleNormal",
//                    1,
//                    20,
//                    256,
//                    ".png",
//                    arrayOf("")
//                ) {
//                    override fun getTileURLString(pMapTileIndex: Long): String {
//                        val i = MapTileIndex.getX(pMapTileIndex)
//                        val j = MapTileIndex.getY(pMapTileIndex)
//                        val k = MapTileIndex.getZoom(pMapTileIndex)
//                        return "https://mt0.google.com/vt/lyrs=y&hl=en&x=$i&y=$j&z=$k"
//                        //return "https://mt0.google.com/vt/lyrs=t&hl=en&x=$i&y=$j&z=$k"
//                    }
//                }
//
//                TileSourceFactory.addTileSource(googleOnlineTile)
//                map.setTileSource(googleOnlineTile)

            } catch (t: Throwable) {
                Log.d(TAG, "onViewCreated: ${t.message}")
                map.overlayManager.tilesOverlay.setColorFilter(null)
                map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            }


        }
    }
}