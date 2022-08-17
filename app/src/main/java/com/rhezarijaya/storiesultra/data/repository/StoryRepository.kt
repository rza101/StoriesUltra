package com.rhezarijaya.storiesultra.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.rhezarijaya.storiesultra.data.network.APIService
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.StoriesPagingSource
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.network.model.StoryResponse
import com.rhezarijaya.storiesultra.ui.activity.main.Location
import com.rhezarijaya.storiesultra.util.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(private val apiService: APIService, ) {
    fun getPagedStories(bearerToken: String): LiveData<PagingData<Story>> {
        // initial load size dijadikan sama karena ada data duplikat
        return Pager(
            config = PagingConfig(
                pageSize = Constants.STORY_PAGING_PAGE_SIZE,
                prefetchDistance = Constants.STORY_PAGING_PREFETCH_DISTANCE,
                initialLoadSize = Constants.STORY_PAGING_PAGE_SIZE
            ),
            pagingSourceFactory = {
                StoriesPagingSource(apiService, bearerToken)
            }
        ).liveData
    }

    fun getStories(
        bearerToken: String,
        page: Int?,
        size: Int?,
        location: Location
    ): LiveData<Result<StoryResponse>> {
        return liveData {
            emit(Result.Loading)

            try {
                emit(
                    Result.Success(
                        apiService.getStories(
                            APIUtils.formatBearerToken(bearerToken),
                            page,
                            size,
                            location.isOn
                        )
                    )
                )
            } catch (exception: Exception) {
                emit(Result.Error(exception))
            }
        }
    }

    fun submit(
        bearerToken: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): LiveData<Result<CreateStoryResponse>> {
        return liveData {
            emit(Result.Loading)

            try {
                emit(
                    Result.Success(
                        apiService.postStory(
                            authorization = APIUtils.formatBearerToken(bearerToken),
                            description, photo, lat, lon
                        )
                    )
                )
            } catch (exception: Exception) {
                emit(Result.Error(exception))
            }
        }
    }
}