package com.appynitty.kotlinsbalibrary.common.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appynitty.kotlinsbalibrary.common.dao.GisLocDao
import com.appynitty.kotlinsbalibrary.common.dao.LocationDao
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.dao.UserDetailsDao
import com.appynitty.kotlinsbalibrary.common.model.DateConverters
import com.appynitty.kotlinsbalibrary.common.model.GisLatLong
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.model.request.LocationApiRequest
import com.appynitty.kotlinsbalibrary.common.model.response.NearestLatLng
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedData
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao.TripDao
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao.TripHouseDao
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.ListTypeConverter
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripHouseData
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.UserTravelLoc
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.PropertyTypeDao
import com.appynitty.kotlinsbalibrary.housescanify.model.EmpHouseOnMap
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType


@TypeConverters(ListTypeConverter::class, DateConverters::class)
@Database(
    entities = [UserData::class, GarbageCollectionData::class,
        LocationApiRequest::class, ArchivedData::class,
        GisLatLong::class, TripRequest::class,
        TripHouseData::class, EmpGarbageCollectionRequest::class,
        EmpHouseOnMap::class,
        NearestLatLng::class,
        PropertyType::class,
        UserTravelLoc::class],
    version = 3,
    exportSchema = false
)
abstract class SbaDatabase : RoomDatabase() {

    abstract fun userDetailsDao(): UserDetailsDao
    abstract fun userTravelLocDao(): UserTravelLocDao
    abstract fun nearestLatLngDao(): NearestLatLngDao
    abstract fun garbageCollectionDao(): GarbageCollectionDao
    abstract fun locationDao(): LocationDao
    abstract fun archivedDao(): ArchivedDao
    abstract fun gisLocationDao(): GisLocDao
    abstract fun tripDao(): TripDao
    abstract fun tripHouseDao(): TripHouseDao
    abstract fun empGcDao(): EmpGcDao
    abstract fun empHouseOnMapDao(): EmpHouseOnMapDao
    abstract fun propertyTypeDao(): PropertyTypeDao

}

