package com.appynitty.kotlinsbalibrary.common.utils.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("language_pref")

class LanguageDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val languageDataStore = context.dataStore

    companion object {
        private val Language_Name = stringPreferencesKey(name = "language_name")
        private val Language_ID = stringPreferencesKey(name = "language_id")
    }

    suspend fun savePreferredLanguage(appLanguage: AppLanguage) {
        languageDataStore.edit { preferences ->
            preferences[Language_ID] = appLanguage.languageId
            preferences[Language_Name] = appLanguage.languageName
        }
    }

    private val getPreferredLanguage: Flow<AppLanguage> = languageDataStore.data
        .map { preferences ->
            AppLanguage(
                preferences[Language_ID] ?: "mr",
                preferences[Language_Name] ?: "Marathi"
            )
        }
    val currentLanguage: AppLanguage
        get() = runBlocking { getPreferredLanguage.first() }
}