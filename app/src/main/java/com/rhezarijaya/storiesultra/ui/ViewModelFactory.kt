package com.rhezarijaya.storiesultra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rhezarijaya.storiesultra.data.network.AuthRepository
import com.rhezarijaya.storiesultra.data.network.StoryRepository
import com.rhezarijaya.storiesultra.ui.activity.create.CreateStoryViewModel
import com.rhezarijaya.storiesultra.ui.activity.login.LoginViewModel
import com.rhezarijaya.storiesultra.ui.activity.main.MainViewModel
import com.rhezarijaya.storiesultra.ui.activity.register.RegisterViewModel
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences

class ViewModelFactory(private val appPreferences: AppPreferences) :
    ViewModelProvider.NewInstanceFactory() {
    private val authRepository = AuthRepository(appPreferences)
    private val storyRepository = StoryRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoryViewModel::class.java)) {
            return CreateStoryViewModel(authRepository, storyRepository) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        } else if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(authRepository, storyRepository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }

        throw IllegalArgumentException(modelClass.name + " not found")
    }
}