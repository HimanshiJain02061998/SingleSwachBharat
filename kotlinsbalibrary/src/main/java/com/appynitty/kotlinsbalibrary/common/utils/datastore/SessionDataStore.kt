package com.appynitty.kotlinsbalibrary.common.utils.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val TAG = "SessionDataStore"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session_pref")

class SessionDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val sessionDataStore = context.dataStore

    companion object {

        private val IS_USER_LOGGED_IN = booleanPreferencesKey(name = "is_user_login_key")
        private val IS_USER_DUTY_ON = booleanPreferencesKey(name = "is_user_duty_on_key")
        private val DUTY_ON_DATE = stringPreferencesKey(name = "duty_date_on_key")
        private val BEFORE_IMAGE_FILE_PATH = stringPreferencesKey(name = "before_image_file_path")
        private val DUMP_YARD_TRIP_NO = intPreferencesKey(name = "dump_trip_no")
        private val IS_FIRST_TRIP_HOUSE = booleanPreferencesKey(name = "is_first_trip_house")
        private val GIS_TRAIL_ID = stringPreferencesKey(name = "gis_trail_id")
        private val GIS_START_TS = stringPreferencesKey(name = "gis_start_ts")
        private val BEARER_TOKEN = stringPreferencesKey(name = "api_bearer_token")

    }


    suspend fun saveBearerToken(token: String) {
        sessionDataStore.edit {
            it[BEARER_TOKEN] = token
        }
    }

    val getBearerToken: Flow<String> = sessionDataStore.data
        .map {
            it[BEARER_TOKEN] ?: ""
        }

    suspend fun saveGisTrailId(trailId: String) {
        sessionDataStore.edit {
            it[GIS_TRAIL_ID] = trailId
        }
    }

    val getGisTrailId: Flow<String> = sessionDataStore.data
        .map {
            it[GIS_TRAIL_ID] ?: ""
        }

    suspend fun saveGisStartTs(startTime: String) {
        sessionDataStore.edit {
            it[GIS_START_TS] = startTime
        }
    }

    val getGisStartTs: Flow<String> = sessionDataStore.data
        .map {
            it[GIS_START_TS] ?: ""
        }

//    suspend fun saveIsFirstDumpTripHouse(isFirstTrip: Boolean) {
//
//        sessionDataStore.edit { preferences ->
//            preferences[IS_FIRST_TRIP_HOUSE] = isFirstTrip
//        }
//    }
//
//    val getIsFirstDumpTripHouse: Flow<Boolean> = sessionDataStore.data
//        .map { preferences ->
//            preferences[IS_FIRST_TRIP_HOUSE] ?: true
//        }

    suspend fun saveDumpYardTripNo(tripNo: Int) {
        sessionDataStore.edit { preferences ->
            preferences[DUMP_YARD_TRIP_NO] = tripNo
        }
    }

    val getDumpYardTripNo: Flow<Int> = sessionDataStore.data
        .map { preferences ->
            preferences[DUMP_YARD_TRIP_NO] ?: 0
        }

    suspend fun saveUserLoginSession(isLoggedIn: Boolean) {
        sessionDataStore.edit { preferences ->
            preferences[IS_USER_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun saveIsUserDutyOn(isDutyOn: Boolean) {
        Log.i(TAG, "saveIsUserDutyOn: save")
        sessionDataStore.edit { preferences ->
            preferences[IS_USER_DUTY_ON] = isDutyOn
        }
    }

    suspend fun saveUserDutyOnDate(dutyOnDate: String) {
        sessionDataStore.edit { preferences ->
            preferences[DUTY_ON_DATE] = dutyOnDate
        }
    }

    val getIsUserDutyOn: Flow<Boolean> = sessionDataStore.data
        .map { preferences ->
            preferences[IS_USER_DUTY_ON] ?: false
        }

    val getDutyOnDate: Flow<String> = sessionDataStore.data
        .map { preferences ->
            preferences[DUTY_ON_DATE] ?: ""
        }


    suspend fun saveBeforeImageFilePath(filePath: String) {
        sessionDataStore.edit { preferences ->
            preferences[BEFORE_IMAGE_FILE_PATH] = filePath
        }
    }

    val getBeforeImageFilePath: Flow<String> = sessionDataStore.data
        .map { preferences ->
            preferences[BEFORE_IMAGE_FILE_PATH] ?: ""
        }


    val getIsUserLoggedIn: Flow<Boolean> = sessionDataStore.data
        .map { preferences ->
            preferences[IS_USER_LOGGED_IN] ?: false
        }

    suspend fun clearSessionDatastore(){
        sessionDataStore.edit {
            it.clear()
        }
    }
}