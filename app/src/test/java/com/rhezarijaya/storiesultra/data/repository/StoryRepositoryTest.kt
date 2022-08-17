package com.rhezarijaya.storiesultra.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.APIService
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.StoriesPagingSource
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.getOrAwaitValue
import com.rhezarijaya.storiesultra.ui.activity.main.Location
import com.rhezarijaya.storiesultra.ui.adapter.StoryListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var apiService: APIService

    private lateinit var storyRepository: StoryRepository

    private val dummyAuthorization =
        APIUtils.formatBearerToken(DummyDataGenerator.generateDummyBearerToken())
    private val dummyBearerToken = DummyDataGenerator.generateDummyBearerToken()
    private val dummyPage: Int? = null
    private val dummySize: Int = 50

    private val dummyDescription: RequestBody =
        "description".toRequestBody("text/plain".toMediaType())
    private val dummyFile: MultipartBody.Part = MultipartBody.Part.createFormData(
        "photo",
        "filename",
        File("file").asRequestBody("image/jpeg".toMediaType())
    )

    @Before
    fun setup() {
        storyRepository = StoryRepository(apiService)
    }

    @Test
    fun `when successfully getting data for main menu should not null and equal dummy`() = runTest {
        val mockedClass = mock(StoryRepository::class.java)

        val expectedData = MutableLiveData<PagingData<Story>>()
        expectedData.value =
            StoriesPagingSource.snapshot(DummyDataGenerator.generateDummyStories())

        `when`(mockedClass.getPagedStories(DummyDataGenerator.generateDummyBearerToken())).thenReturn(
            expectedData
        )

        val actualData = mockedClass.getPagedStories(DummyDataGenerator.generateDummyBearerToken())
            .getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {}
                override fun onRemoved(position: Int, count: Int) {}
                override fun onMoved(fromPosition: Int, toPosition: Int) {}
                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            },
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualData)

        verify(mockedClass).getPagedStories(DummyDataGenerator.generateDummyBearerToken())

        assertNotNull(actualData)
        assertEquals(DummyDataGenerator.generateDummyStories(), differ.snapshot())
        assertEquals(DummyDataGenerator.generateDummyStories().size, differ.snapshot().size)
    }

    @Test
    fun `when successfully getting data for maps should not null and equal dummy`() = runTest {
        val expectedData = DummyDataGenerator.generateDummySuccessStoryResponse()

        `when`(
            apiService.getStories(
                dummyAuthorization,
                dummyPage,
                dummySize,
                Location.LOCATION_ON.isOn
            )
        ).thenReturn(expectedData)

        val actualData =
            storyRepository.getStories(dummyBearerToken, dummyPage, dummySize, Location.LOCATION_ON)
                .apply {
                    getOrAwaitValue() // konsumsi dahulu Result.Loading
                }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).getStories(
            dummyAuthorization,
            dummyPage,
            dummySize,
            Location.LOCATION_ON.isOn
        )

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            DummyDataGenerator.generateDummySuccessStoryResponse(),
            (actualData as Result.Success).data
        )
    }

    @Test
    fun `when not success getting data for maps should not null and is ResultError`() = runTest {
        `when`(
            apiService.getStories(
                dummyAuthorization,
                dummyPage,
                dummySize,
                Location.LOCATION_ON.isOn
            )
        ).then {
            throw DummyDataGenerator.generateDummyException()
        }

        val actualData =
            storyRepository.getStories(dummyBearerToken, dummyPage, dummySize, Location.LOCATION_ON)
                .apply {
                    getOrAwaitValue() // konsumsi dahulu Result.Loading
                }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).getStories(
            dummyAuthorization,
            dummyPage,
            dummySize,
            Location.LOCATION_ON.isOn
        )

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }

    @Test
    fun `when submit is success data should not null and is ResultSuccess`() = runTest {
        val expectedData = DummyDataGenerator.generateDummySuccessCreateStoryResponse()

        `when`(
            apiService.postStory(
                dummyAuthorization,
                dummyDescription,
                dummyFile,
                null,
                null
            )
        ).thenReturn(expectedData)

        val actualData =
            storyRepository.submit(
                dummyBearerToken,
                dummyDescription,
                dummyFile,
                null,
                null
            ).apply {
                getOrAwaitValue() // konsumsi dahulu Result.Loading
            }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).postStory(
            dummyAuthorization,
            dummyDescription,
            dummyFile,
            null,
            null
        )

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            DummyDataGenerator.generateDummySuccessCreateStoryResponse(),
            (actualData as Result.Success).data
        )
    }

    @Test
    fun `when submit is failed data should not null and is ResultError`() = runTest {
        `when`(
            apiService.postStory(
                dummyAuthorization,
                dummyDescription,
                dummyFile,
                null,
                null
            )
        ).then {
            throw DummyDataGenerator.generateDummyException()
        }

        val actualData =
            storyRepository.submit(
                dummyBearerToken,
                dummyDescription,
                dummyFile,
                null,
                null
            ).apply {
                getOrAwaitValue() // konsumsi dahulu Result.Loading
            }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).postStory(
            dummyAuthorization,
            dummyDescription,
            dummyFile,
            null,
            null
        )

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }
}