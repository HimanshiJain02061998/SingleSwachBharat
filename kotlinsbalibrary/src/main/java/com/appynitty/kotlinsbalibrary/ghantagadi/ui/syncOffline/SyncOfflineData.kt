package com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline

data class SyncOfflineData(
    val userType: Int,
    val date: String,
    var offlineGarbageCollectionCount: Int = 0,
    var offlineDumpCount: Int,
    var streetCount: Int,
    var liquidCount: Int,
    var dumpCollectionPoint: Int,
    var masterPlateCollection: Int
)
