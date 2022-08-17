package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.repository.AuthRepository
import com.rhezarijaya.storiesultra.data.repository.StoryRepository
import com.rhezarijaya.storiesultra.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CreateStoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var createStoryViewModel: CreateStoryViewModel

    private val dummyBearerToken: String = DummyDataGenerator.generateDummyBearerToken()
    private val dummyDescription: RequestBody = DummyDataGenerator.generateDummyDescription()
    private val dummyFile: MultipartBody.Part = DummyDataGenerator.generateDummyFileMultipart()

    @Before
    fun setup() {
        createStoryViewModel = CreateStoryViewModel(authRepository, storyRepository)
    }

    @Test
    fun `when getting bearer token should not null and equal dummy`() = runTest {
        val expectedData = MutableLiveData<String?>()
        expectedData.value = dummyBearerToken

        `when`(
            authRepository.getBearerToken()
        ).thenReturn(expectedData)

        val actualData = createStoryViewModel.getBearerToken().getOrAwaitValue()

        verify(authRepository).getBearerToken()

        assertNotNull(actualData)
        assertEquals(dummyBearerToken, actualData)
    }

    @Test
    fun `when submitting not done yet data should not null and is ResultLoading`() = runTest {
        val expectedData = MutableLiveData<Result<CreateStoryResponse>>()
        expectedData.value = Result.Loading

        `when`(
            storyRepository.submit(
                dummyBearerToken,
                dummyDescription,
                dummyFile,
                null,
                null
            )
        ).thenReturn(expectedData)

        val actualData = storyRepository.submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        ).getOrAwaitValue()

        verify(storyRepository).submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        )

        assertNotNull(actualData)
        assert(actualData is Result.Loading)
    }

    @Test
    fun `when submit is success data should not null and is ResultSuccess`() = runTest {
        val expectedData = MutableLiveData<Result<CreateStoryResponse>>()
        expectedData.value =
            Result.Success(DummyDataGenerator.generateDummySuccessCreateStoryResponse())

        `when`(
            storyRepository.submit(
                dummyBearerToken,
                dummyDescription,
                dummyFile,
                null,
                null
            )
        ).thenReturn(expectedData)

        val actualData = createStoryViewModel.submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        ).getOrAwaitValue()

        verify(storyRepository).submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        )

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            Result.Success(DummyDataGenerator.generateDummySuccessCreateStoryResponse()),
            actualData
        )
    }

    @Test
    fun `when submit is failed data should not null and is ResultError`() = runTest {
        val expectedData = MutableLiveData<Result<CreateStoryResponse>>()
        expectedData.value = Result.Error(DummyDataGenerator.generateDummyException())

        `when`(
            storyRepository.submit(
                dummyBearerToken,
                dummyDescription,
                dummyFile,
                null,
                null
            )
        ).thenReturn(expectedData)

        val actualData = createStoryViewModel.submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        ).getOrAwaitValue()

        verify(storyRepository).submit(
            dummyBearerToken,
            dummyDescription,
            dummyFile,
            null,
            null
        )

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }
}