package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesultra.data.repository.AuthRepository
import com.rhezarijaya.storiesultra.data.repository.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CreateStoryViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    fun getBearerToken() = authRepository.getBearerToken()

    fun submit(
        bearerToken: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ) = storyRepository.submit(bearerToken, description, photo, lat, lon)
}