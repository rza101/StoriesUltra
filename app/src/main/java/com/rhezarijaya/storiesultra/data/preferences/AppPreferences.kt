package com.rhezarijaya.storiesultra.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rhezarijaya.storiesultra.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    private val NAME_KEY = stringPreferencesKey(Constants.NAME_PREFERENCES)
    private val USERID_KEY = stringPreferencesKey(Constants.USERID_PREFERENCES)
    private val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_PREFERENCES)

    fun getNamePrefs(): Flow<String?> {
        return dataStore.data.map {
            it[NAME_KEY]
        }
    }

    suspend fun saveNamePrefs(name: String) {
        dataStore.edit {
            it[NAME_KEY] = name
        }
    }

    fun getUserIDPrefs(): Flow<String?> {
        return dataStore.data.map {
            it[USERID_KEY]
        }
    }

    suspend fun saveUserIDPrefs(userId: String) {
        dataStore.edit {
            it[USERID_KEY] = userId
        }
    }

    fun getTokenPrefs(): Flow<String?> {
        return dataStore.data.map {
            it[TOKEN_KEY]
        }
    }

    suspend fun saveTokenPrefs(token: String) {
        dataStore.edit {
            it[TOKEN_KEY] = token
        }
    }

    suspend fun clearPrefs() {
        dataStore.edit {
            it.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferences(dataStore)
                INSTANCE = instance
                return@synchronized instance
            }
        }
    }
}