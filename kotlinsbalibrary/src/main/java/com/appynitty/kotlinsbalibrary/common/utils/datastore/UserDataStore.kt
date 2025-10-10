package com.appynitty.kotlinsbalibrary.common.utils.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserEssentials
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore("user_pref")

class UserDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val userDataStore = context.dataStore

    companion object {

        private val USER_ID_KEY = stringPreferencesKey(name = "user_id_key")

        //it is use to identify user is from house scanify or ghanta gadi
        private val USER_TYPE_ID = stringPreferencesKey(name = "user_type_id_key")
        private val EMP_TYPE_KEY = stringPreferencesKey(name = "employee_type_key")
        private val LAST_LATITUDE_KEY = stringPreferencesKey(name = "last_known_lat_key")
        private val LAST_LONGITUDE_KEY = stringPreferencesKey(name = "last_known_long_key")
        private val LAST_DISTANCE_KEY = stringPreferencesKey(name = "last_distance_key")
        private val USER_VEHICLE_TYPE_ID = stringPreferencesKey(name = "user_vehicle_type_id_key")
        private val USER_VEHICLE_TYPE = stringPreferencesKey(name = "user_vehicle_type_key")
        private val USER_VEHICLE_NUMBER = stringPreferencesKey(name = "user_vehicle_number_key")
        private val IS_BIFURCATION_ON = booleanPreferencesKey(name = "is_bifurcation_on")
        private val IS_VEHICLE_SCAN_ON = booleanPreferencesKey(name = "is_vehicle_scan_on")
        private val LAST_HOUSE_SCANIFY_SCAN_LAT = stringPreferencesKey(name = "last_house_scanify_scan_lat")
        private val LAST_HOUSE_SCANIFY_SCAN_LONG = stringPreferencesKey(name = "last_house_scanify_scan_long")
        private val LAST_HOUSE_SCANIFY_SCAN_ACCURACY = stringPreferencesKey(name = "last_house_scanify_scan_accuracy")
        private val LAST_GHANTA_GADI_SCAN_ACCURACY = stringPreferencesKey(name = "last_ghanta_gadi_scan_accuracy")
        private val LAST_GHANTA_GADI_SCAN_LAT = stringPreferencesKey(name = "last_ghanta_gadi_scan_lat")
        private val LAST_GHANTA_GADI_SCAN_LONG = stringPreferencesKey(name = "last_ghanta_gadi_scan_long")
        private val DIS_ID_KEY = intPreferencesKey(name = "dis_id_key")
        private val APP_ID = stringPreferencesKey(name = "app_id")
        private val ULB_NAME = stringPreferencesKey(name = "ulb_name")

    }
    suspend fun saveAppId(appId: String) {
        clearAppId()
        userDataStore.edit { preferences ->
            preferences[APP_ID] = appId
        }
    }

    suspend fun saveUlbName(ulbName: String) {
        clearUlbName()
        userDataStore.edit { preferences ->
            preferences[ULB_NAME] = ulbName
        }
    }

    suspend fun clearAppId() {
        userDataStore.edit { prefs ->
            prefs.remove(APP_ID)
        }
    }

    suspend fun clearUlbName() {
        userDataStore.edit { prefs ->
            prefs.remove(ULB_NAME)
        }
    }

    val getAppId: Flow<String> = userDataStore.data
        .map { preferences ->
            (preferences[APP_ID] ?: "")
        }

    val getUlbName: Flow<String> = userDataStore.data
        .map { preferences ->
            (preferences[ULB_NAME] ?: "")
        }

    suspend fun saveDisId(disId: Int) {
        userDataStore.edit { preferences ->
            preferences[DIS_ID_KEY] = disId
        }
    }
    val getDisId: Flow<Int> = userDataStore.data
        .map { preferences ->
            preferences[DIS_ID_KEY] ?: 0
        }
    suspend fun saveIsBifurcationOn(isBifurcationOn: Boolean) {
        userDataStore.edit { preferences ->
            preferences[IS_BIFURCATION_ON] = isBifurcationOn
        }
    }

    val getIsBifurcationOn: Flow<Boolean> = userDataStore.data
        .map {
            it[IS_BIFURCATION_ON] ?: true
        }


    suspend fun saveIsVehicleScanOn(isVehicleScanOn: Boolean) {
        userDataStore.edit { preferences ->
            preferences[IS_VEHICLE_SCAN_ON] = isVehicleScanOn
        }
    }

    val getIsVehicleScanOn: Flow<Boolean> =
        userDataStore.data.map {
            it[IS_VEHICLE_SCAN_ON] ?: true
        }

    suspend fun saveUserVehicleDetails(vehicleDetails: UserVehicleDetails) {
        userDataStore.edit { preferences ->
            preferences[USER_VEHICLE_TYPE_ID] = vehicleDetails.vehicleId
            preferences[USER_VEHICLE_TYPE] = vehicleDetails.vehicleTypeName
            preferences[USER_VEHICLE_NUMBER] = vehicleDetails.vehicleNumber
        }
    }

    val getUserVehicleDetails: Flow<UserVehicleDetails> = userDataStore.data
        .map { preferences ->
            UserVehicleDetails(
                preferences[USER_VEHICLE_TYPE_ID] ?: "",
                preferences[USER_VEHICLE_TYPE] ?: "",
                preferences[USER_VEHICLE_NUMBER] ?: ""
            )
        }

    suspend fun saveUserEssentials(userEssentials: UserEssentials) {
        userDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userEssentials.userId
            preferences[EMP_TYPE_KEY] = userEssentials.employeeType
            preferences[USER_TYPE_ID] = userEssentials.userTypeId

        }
    }

    val getUserEssentials: Flow<UserEssentials> = userDataStore.data
        .map { preferences ->
            UserEssentials(
                preferences[USER_ID_KEY] ?: "",
                preferences[EMP_TYPE_KEY] ?: "",
                preferences[USER_TYPE_ID] ?: "",

            )
        }


    suspend fun saveUserLatLong(userLatLong: UserLatLong) {
        userDataStore.edit { preferences ->
            preferences[LAST_LATITUDE_KEY] = userLatLong.latitude
            preferences[LAST_LONGITUDE_KEY] = userLatLong.longitude
            preferences[LAST_DISTANCE_KEY] = userLatLong.distance
        }
    }

    val getUserLatLong: Flow<UserLatLong> = userDataStore.data
        .map { preferences ->
            UserLatLong(
                preferences[LAST_LATITUDE_KEY] ?: "",
                preferences[LAST_LONGITUDE_KEY] ?: "",
                preferences[LAST_DISTANCE_KEY] ?: "0"
            )
        }

    suspend fun saveLastHouseScanifyLatLong(userLatLong: UserLatLong) {
        userDataStore.edit { preferences ->
            preferences[LAST_HOUSE_SCANIFY_SCAN_LAT] = userLatLong.latitude
            preferences[LAST_HOUSE_SCANIFY_SCAN_LONG] = userLatLong.longitude
            preferences[LAST_HOUSE_SCANIFY_SCAN_ACCURACY] = userLatLong.distance
        }
    }

    val getLastHouseScanifyLatLong: Flow<UserLatLong> = userDataStore.data
        .map { preferences ->
            UserLatLong(
                preferences[LAST_HOUSE_SCANIFY_SCAN_LAT] ?: "",
                preferences[LAST_HOUSE_SCANIFY_SCAN_LONG] ?: "",
                preferences[LAST_HOUSE_SCANIFY_SCAN_ACCURACY] ?: "0"
            )
        }

    suspend fun saveLastGhantaGadiScanLatLong(userLatLong: UserLatLong) {
        userDataStore.edit { preferences ->
            preferences[LAST_GHANTA_GADI_SCAN_LAT] = userLatLong.latitude
            preferences[LAST_GHANTA_GADI_SCAN_LONG] = userLatLong.longitude
            preferences[LAST_GHANTA_GADI_SCAN_ACCURACY] = userLatLong.distance
        }
    }

    val getLastGhantaGadiScanLatLong: Flow<UserLatLong> = userDataStore.data
        .map { preferences ->
            UserLatLong(
                preferences[LAST_GHANTA_GADI_SCAN_LAT] ?: "",
                preferences[LAST_GHANTA_GADI_SCAN_LONG] ?: "",
                preferences[LAST_GHANTA_GADI_SCAN_ACCURACY] ?: "0"
            )
        }

    suspend fun clearUserDatastore() {
        userDataStore.edit {
            it.clear()
        }
    }

}