package com.rhezarijaya.storiesultra.ui.activity.main

import androidx.lifecycle.*
import androidx.paging.*
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.repository.AuthRepository
import com.rhezarijaya.storiesultra.data.repository.StoryRepository

class MainViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    fun getBearerToken() = authRepository.getBearerToken()

    fun getMainStories(bearerToken: String) =
        storyRepository.getPagedStories(bearerToken).cachedIn(viewModelScope)

    fun getMapsStories(bearerToken: String, size: Int?) = storyRepository.getStories(
        bearerToken,
        null,
        size,
        Location.LOCATION_ON
    )

    fun logout() = authRepository.logout()
}