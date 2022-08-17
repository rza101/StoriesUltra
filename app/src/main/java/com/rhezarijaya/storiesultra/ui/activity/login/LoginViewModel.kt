package com.rhezarijaya.storiesultra.ui.activity.login

import androidx.lifecycle.*
import com.rhezarijaya.storiesultra.data.repository.AuthRepository

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun login(email: String, password: String) = authRepository.login(email, password)

    fun saveLoginInfo(name: String, userId: String, token: String) =
        authRepository.saveLoginInfo(name, userId, token)
}