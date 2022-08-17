package com.rhezarijaya.storiesultra.ui.activity.register

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesultra.data.repository.AuthRepository

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun register(
        name: String,
        email: String,
        password: String
    ) = authRepository.register(name, email, password)
}