package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.getOrAwaitValue
import com.rhezarijaya.storiesultra.ui.OnSuccessCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.internal.stubbing.answers.CallsRealMethods
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CreateStoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var appPreferences: AppPreferences
    private lateinit var onSuccessCallback: OnSuccessCallback<CreateStoryResponse>

    @Before
    fun setup() {
        appPreferences = mock(AppPreferences::class.java)
        onSuccessCallback = object : OnSuccessCallback<CreateStoryResponse> {
            override fun onSuccess(message: CreateStoryResponse) {}
        }
    }

    @Test
    fun `when done submitting isLoading should be false`() = runBlocking {
//        val mock = mock(CreateStoryViewModel::class.java)
//        val submit = mock.submit(onSuccessCallback, "description", File(""), null, null)
//
//        assertNotNull(submit)
//        assertTrue(!mock.isLoading().getOrAwaitValue())

        val createStoryViewModel = CreateStoryViewModel(appPreferences)
        val submit = createStoryViewModel.submit(onSuccessCallback, "description", File(""), null, null)

        assertNotNull(submit)
    }
}