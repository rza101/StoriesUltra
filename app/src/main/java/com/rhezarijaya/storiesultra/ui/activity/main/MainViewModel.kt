package com.rhezarijaya.storiesultra.ui.activity.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.network.model.StoryResponse
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.data.SingleEvent
import com.rhezarijaya.storiesultra.data.network.StoriesPagingSource
import com.rhezarijaya.storiesultra.data.network.model.Story
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    private val mainStoriesData: LiveData<PagingData<Story>> =
        getPagedStories().cachedIn(viewModelScope)

    private val mapsStoriesData: MutableLiveData<StoryResponse> = MutableLiveData()
    private val mapsStoriesError: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    private val isMapsStoriesLoading: MutableLiveData<Boolean> = MutableLiveData()

    private fun getPagedStories(): LiveData<PagingData<Story>> {
        var bearerToken: String

        runBlocking {
            bearerToken = appPreferences.getTokenPrefs().first() ?: ""
        }

        // initial load size dijadikan sama karena ada data duplikat
        return Pager(
            config = PagingConfig(
                pageSize = Constants.STORY_PAGING_PAGE_SIZE,
                prefetchDistance = Constants.STORY_PAGING_PREFETCH_DISTANCE,
                initialLoadSize = Constants.STORY_PAGING_PAGE_SIZE
            ),
            pagingSourceFactory = {
                StoriesPagingSource(APIUtils.getAPIService(), bearerToken)
            }
        ).liveData
    }

    fun getMainStoriesData(): LiveData<PagingData<Story>> {
        return mainStoriesData
    }

    fun getMapsStoriesData(): LiveData<StoryResponse> {
        return mapsStoriesData
    }

    fun getMapsStoriesError(): LiveData<SingleEvent<String>> {
        return mapsStoriesError
    }

    fun isMapsStoriesLoading(): LiveData<Boolean> {
        return isMapsStoriesLoading
    }

    fun loadMapsStories(size: Int?) {
        isMapsStoriesLoading.value = true

        var bearerToken: String

        runBlocking {
            bearerToken = appPreferences.getTokenPrefs().first() ?: ""
        }

        APIUtils.getAPIService().getStories(
            APIUtils.formatBearerToken(bearerToken),
            null,
            size,
            Location.LOCATION_ON.isOn
        ).enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                isMapsStoriesLoading.value = false

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error as Boolean) {
                            mapsStoriesError.value = SingleEvent(it.message as String)
                        } else {
                            mapsStoriesData.value = response.body() as StoryResponse
                        }
                    } ?: run {
                        mapsStoriesError.value = SingleEvent(Constants.UNEXPECTED_DATA_ERROR)
                    }
                } else {
                    val body: LoginResponse? =
                        Gson().fromJson(response.errorBody()?.string(), LoginResponse::class.java)

                    mapsStoriesError.value =
                        SingleEvent(body?.message ?: APIUtils.formatResponseCode(response.code()))
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                isMapsStoriesLoading.value = false
                mapsStoriesError.value = SingleEvent(Constants.UNEXPECTED_ERROR)
            }
        })
    }

    fun logout() {
        viewModelScope.launch {
            appPreferences.clearPrefs()
        }
    }
}