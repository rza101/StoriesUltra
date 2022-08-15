package com.rhezarijaya.storiesultra.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.network.model.RegisterResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import kotlinx.coroutines.runBlocking

class AuthRepository(private val appPreferences: AppPreferences) {
    fun getBearerToken(): LiveData<String?> {
        return appPreferences.getTokenPrefs().asLiveData()
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> {
        return liveData {
            emit(Result.Loading)

            try {
                emit(Result.Success(APIUtils.getAPIService().login(email, password)))
            } catch (exception: Exception) {
                emit(Result.Error(exception))
            }
        }
    }

    fun logout() = runBlocking {
        appPreferences.clearPrefs()
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> {
        return liveData {
            emit(Result.Loading)

            try {
                emit(
                    Result.Success(
                        APIUtils.getAPIService().register(name, email, password)
                    )
                )
            } catch (exception: Exception) {
                emit(Result.Error(exception))
            }
        }
    }

    fun saveLoginInfo(name: String, userId: String, token: String) = runBlocking {
        appPreferences.saveNamePrefs(name)
        appPreferences.saveUserIDPrefs(userId)
        appPreferences.saveTokenPrefs(token)
    }
}