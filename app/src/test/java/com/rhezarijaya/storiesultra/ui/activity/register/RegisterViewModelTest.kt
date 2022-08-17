package com.rhezarijaya.storiesultra.ui.activity.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.RegisterResponse
import com.rhezarijaya.storiesultra.data.repository.AuthRepository
import com.rhezarijaya.storiesultra.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
class RegisterViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository
    private lateinit var registerViewModel: RegisterViewModel

    private val dummyName = "name"
    private val dummyEmail = "email"
    private val dummyPassword = "password"

    @Before
    fun setup() {
        registerViewModel = RegisterViewModel(authRepository)
    }

    @Test
    fun `when register not done yet data should not null and is ResultLoading`() = runTest {
        val expectedData = MutableLiveData<Result<RegisterResponse>>()
        expectedData.value = Result.Loading

        `when`(authRepository.register(dummyName, dummyEmail, dummyPassword)).thenReturn(
            expectedData
        )

        val actualData =
            registerViewModel.register(dummyName, dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).register(dummyName, dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Loading)
    }

    @Test
    fun `when register success data should not null and is ResultSuccess`() = runTest {
        val expectedData = MutableLiveData<Result<RegisterResponse>>()
        expectedData.value =
            Result.Success(DummyDataGenerator.generateDummySuccessRegisterResponse())

        `when`(authRepository.register(dummyName, dummyEmail, dummyPassword)).thenReturn(
            expectedData
        )

        val actualData =
            registerViewModel.register(dummyName, dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).register(dummyName, dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            Result.Success(DummyDataGenerator.generateDummySuccessRegisterResponse()),
            actualData
        )
    }

    @Test
    fun `when register failed data should not null and is ResultError`() = runTest {
        val expectedData = MutableLiveData<Result<RegisterResponse>>()
        expectedData.value = Result.Error(DummyDataGenerator.generateDummyException())

        `when`(authRepository.register(dummyName, dummyEmail, dummyPassword)).thenReturn(
            expectedData
        )

        val actualData =
            registerViewModel.register(dummyName, dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).register(dummyName, dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }
}