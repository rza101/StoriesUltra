package com.rhezarijaya.storiesultra.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.rhezarijaya.storiesultra.data.network.APIService
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.network.model.RegisterResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import kotlinx.coroutines.runBlocking

class AuthRepository(
    private val apiService: APIService,
    private val appPreferences: AppPreferences
) {
    fun getBearerToken(): LiveData<String?> {
        return appPreferences.getTokenPrefs().asLiveData()
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> {
        return liveData {
            emit(Result.Loading)

            try {
                emit(Result.Success(apiService.login(email, password)))
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
                        apiService.register(name, email, password)
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