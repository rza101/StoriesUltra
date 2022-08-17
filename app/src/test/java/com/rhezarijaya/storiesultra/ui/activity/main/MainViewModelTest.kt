package com.rhezarijaya.storiesultra.ui.activity.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.StoriesPagingSource
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.network.model.StoryResponse
import com.rhezarijaya.storiesultra.data.repository.AuthRepository
import com.rhezarijaya.storiesultra.data.repository.StoryRepository
import com.rhezarijaya.storiesultra.getOrAwaitValue
import com.rhezarijaya.storiesultra.ui.adapter.StoryListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var mainViewModel: MainViewModel

    private val dummyBearerToken: String = DummyDataGenerator.generateDummyBearerToken()
    private val dummyPage: Int? = null
    private val dummySize: Int = 50

    @Before
    fun setup() {
        storyRepository = mock(StoryRepository::class.java)
        mainViewModel = MainViewModel(authRepository, storyRepository)
    }

    @Test
    fun `when getting bearer token should not null and equal dummy`() = runTest {
        val expectedData = MutableLiveData<String?>()
        expectedData.value = dummyBearerToken

        `when`(
            authRepository.getBearerToken()
        ).thenReturn(expectedData)

        val actualData = mainViewModel.getBearerToken().getOrAwaitValue()

        verify(authRepository).getBearerToken()

        assertNotNull(actualData)
        assertEquals(dummyBearerToken, actualData)
    }

    @Test
    fun `when successfully getting data for main menu should not null and equal dummy`() =
        runTest {
            val expectedData = MutableLiveData<PagingData<Story>>()
            expectedData.value =
                StoriesPagingSource.snapshot(DummyDataGenerator.generateDummyStories())

            `when`(storyRepository.getPagedStories(DummyDataGenerator.generateDummyBearerToken())).thenReturn(
                expectedData
            )

            val actualData =
                mainViewModel.getMainStories(DummyDataGenerator.generateDummyBearerToken())
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

            verify(storyRepository).getPagedStories(DummyDataGenerator.generateDummyBearerToken())

            assertNotNull(actualData)
            assertEquals(DummyDataGenerator.generateDummyStories(), differ.snapshot())
            assertEquals(DummyDataGenerator.generateDummyStories().size, differ.snapshot().size)
        }

    @Test
    fun `when getting data for maps not done yet should not null and is ResultLoading`() = runTest {
        val expectedData = MutableLiveData<Result<StoryResponse>>()
        expectedData.value = Result.Loading

        `when`(
            storyRepository.getStories(
                dummyBearerToken,
                dummyPage,
                dummySize,
                Location.LOCATION_ON
            )
        ).thenReturn(expectedData)

        val actualData = mainViewModel.getMapsStories(
            dummyBearerToken,
            dummySize
        ).getOrAwaitValue()

        verify(storyRepository).getStories(
            dummyBearerToken,
            dummyPage,
            dummySize,
            Location.LOCATION_ON
        )

        assertNotNull(actualData)
        assert(actualData is Result.Loading)
    }

    @Test
    fun `when successfully getting data for maps should not null and equal dummy`() = runTest {
        val expectedData = MutableLiveData<Result<StoryResponse>>()
        expectedData.value = Result.Success(DummyDataGenerator.generateDummySuccessStoryResponse())

        `when`(
            storyRepository.getStories(
                dummyBearerToken,
                dummyPage,
                dummySize,
                Location.LOCATION_ON
            )
        ).thenReturn(expectedData)

        val actualData = mainViewModel.getMapsStories(
            dummyBearerToken,
            dummySize
        ).getOrAwaitValue()

        verify(storyRepository).getStories(
            dummyBearerToken,
            dummyPage,
            dummySize,
            Location.LOCATION_ON
        )

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            Result.Success(DummyDataGenerator.generateDummySuccessStoryResponse()),
            actualData
        )
    }

    @Test
    fun `when not success getting data for maps should not null and is ResultError`() = runTest {
        val expectedData = MutableLiveData<Result<StoryResponse>>()
        expectedData.value = Result.Error(DummyDataGenerator.generateDummyException())

        `when`(
            storyRepository.getStories(
                dummyBearerToken,
                dummyPage,
                dummySize,
                Location.LOCATION_ON
            )
        ).thenReturn(expectedData)

        val actualData = mainViewModel.getMapsStories(
            dummyBearerToken,
            dummySize
        ).getOrAwaitValue()

        verify(storyRepository).getStories(
            dummyBearerToken,
            dummyPage,
            dummySize,
            Location.LOCATION_ON
        )

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }

    @Test
    fun `when logout should call logout method on AuthRepository`() = runTest {
        mainViewModel.logout()
        verify(authRepository).logout()
    }
}
