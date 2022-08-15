package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.AuthRepository
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.StoryRepository
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

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

    @Before
    fun setup() {
        createStoryViewModel = CreateStoryViewModel(authRepository, storyRepository)
    }

    @Test
    fun `when submitting and not done yet`() = runTest {
        val expectedData = MutableLiveData<Result<CreateStoryResponse>>()
        expectedData.value = Result.Loading

        `when`(
            storyRepository.submit(
                "",
                "",
                File(""),
                null,
                null
            )
        ).thenReturn(expectedData)

        val actualData = createStoryViewModel.submit(
            "",
            "",
            File(""),
            null,
            null
        ).getOrAwaitValue()

        verify(storyRepository).submit(
            "",
            "",
            File(""),
            null,
            null
        )

        assertNotNull(actualData)
        assertEquals(expectedData.getOrAwaitValue(), actualData)
    }
}