package com.rhezarijaya.storiesultra.ui.activity.main

import androidx.lifecycle.*
import androidx.paging.*
import com.rhezarijaya.storiesultra.data.network.*
import com.rhezarijaya.storiesultra.data.network.model.StoryResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.data.network.model.Story
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    private val mainStoriesData: LiveData<PagingData<Story>> =
        storyRepository.getPagedStories(getBearerToken().value ?: "")
            .cachedIn(viewModelScope)

    fun getBearerToken() = authRepository.getBearerToken()

    fun getMainStories(): LiveData<PagingData<Story>> {
        return mainStoriesData
    }

    fun getMapsStories(bearerToken: String, size: Int?) = storyRepository.getStories(
        bearerToken,
        null,
        size,
        Location.LOCATION_ON
    )

    fun logout() = authRepository.logout()
}