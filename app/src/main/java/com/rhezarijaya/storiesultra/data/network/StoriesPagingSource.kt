package com.rhezarijaya.storiesultra.data.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.ui.activity.main.Location

class StoriesPagingSource(private val apiService: APIService, private val bearerToken: String) :
    PagingSource<Int, Story>() {
    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStoriesPaged(
                APIUtils.formatBearerToken(bearerToken),
                position,
                params.loadSize,
                Location.LOCATION_OFF.isOn
            ).listStory

            LoadResult.Page(
                data = responseData,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }
}