package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesultra.data.network.AuthRepository
import com.rhezarijaya.storiesultra.data.network.StoryRepository
import java.io.File

class CreateStoryViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    fun getBearerToken() = authRepository.getBearerToken()

    fun submit(
        bearerToken: String,
        description: String,
        photo: File,
        lat: Double?,
        lon: Double?
    ) = storyRepository.submit(bearerToken, description, photo, lat, lon)
}