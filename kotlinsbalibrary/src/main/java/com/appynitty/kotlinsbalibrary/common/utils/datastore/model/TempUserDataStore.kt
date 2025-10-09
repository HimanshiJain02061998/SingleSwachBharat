package com.appynitty.kotlinsbalibrary.common.utils.datastore.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("temp_user_pref")

class TempUserDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val userDataStore = context.dataStore

    companion object {

        private val USER_ID_KEY = stringPreferencesKey(name = "user_id_key")

        //it is use to identify user is from house scanify or ghanta gadi
        private val USER_TYPE_ID = stringPreferencesKey(name = "user_type_id_key")
        private val EMP_TYPE_KEY = stringPreferencesKey(name = "employee_type_key")

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


    suspend fun clearUserDatastore() {
        userDataStore.edit {
            it.clear()
        }
    }

}