package com.rhezarijaya.storiesultra.ui.activity.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
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
class LoginViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel

    private val dummyName: String = "name"
    private val dummyEmail: String = "email"
    private val dummyUserId: String = "userid"
    private val dummyPassword: String = "password"
    private val dummyToken: String = "token"

    @Before
    fun setup() {
        loginViewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `when login not done yet data should not null and is ResultLoading`() = runTest {
        val expectedData = MutableLiveData<Result<LoginResponse>>()
        expectedData.value = Result.Loading

        `when`(authRepository.login(dummyEmail, dummyPassword)).thenReturn(expectedData)

        val actualData = loginViewModel.login(dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).login(dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Loading)
    }

    @Test
    fun `when login success data should not null and is ResultSuccess`() = runTest {
        val expectedData = MutableLiveData<Result<LoginResponse>>()
        expectedData.value = Result.Success(DummyDataGenerator.generateDummySuccessLoginResponse())

        `when`(authRepository.login(dummyEmail, dummyPassword)).thenReturn(expectedData)

        val actualData = loginViewModel.login(dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).login(dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            Result.Success(DummyDataGenerator.generateDummySuccessLoginResponse()),
            actualData
        )
    }

    @Test
    fun `when login failed data should not null and is ResultError`() = runTest {
        val expectedData = MutableLiveData<Result<LoginResponse>>()
        expectedData.value = Result.Error(DummyDataGenerator.generateDummyException())

        `when`(authRepository.login(dummyEmail, dummyPassword)).thenReturn(expectedData)

        val actualData = loginViewModel.login(dummyEmail, dummyPassword).getOrAwaitValue()

        verify(authRepository).login(dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }

    @Test
    fun `when saving login info should call saveLoginInfo method on AuthRepository`() = runTest {
        loginViewModel.saveLoginInfo(dummyName, dummyUserId, dummyToken)
        verify(authRepository).saveLoginInfo(dummyName, dummyUserId, dummyToken)
    }
}