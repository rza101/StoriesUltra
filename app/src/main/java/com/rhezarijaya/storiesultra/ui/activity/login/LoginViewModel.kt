package com.rhezarijaya.storiesultra.ui.activity.login

import androidx.lifecycle.*
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.AuthRepository
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.StoryRepository
import com.rhezarijaya.storiesultra.util.Constants
import kotlinx.coroutines.runBlocking

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun login(email: String, password: String) = authRepository.login(email, password)

    fun saveLoginInfo(name: String, userId: String, token: String) =
        authRepository.saveLoginInfo(name, userId, token)
}