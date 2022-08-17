package com.rhezarijaya.storiesultra.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rhezarijaya.storiesultra.DummyDataGenerator
import com.rhezarijaya.storiesultra.MainDispatcherRule
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.APIService
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var apiService: APIService

    @Mock
    private lateinit var appPreferences: AppPreferences

    private lateinit var authRepository: AuthRepository

    private val dummyBearerToken = DummyDataGenerator.generateDummyBearerToken()
    private val dummyName = "name"
    private val dummyUserId = "userid"
    private val dummyEmail = "email"
    private val dummyPassword = "password"

    @Before
    fun setup() {
        authRepository = AuthRepository(apiService, appPreferences)
    }

    @Test
    fun `when getting bearer token should not null and equal dummy`() = runTest {
        val expectedData = flowOf(dummyBearerToken)

        `when`(appPreferences.getTokenPrefs()).thenReturn(expectedData)

        val actualData = authRepository.getBearerToken().getOrAwaitValue()

        verify(appPreferences).getTokenPrefs()

        assertNotNull(actualData)
        assertEquals(dummyBearerToken, actualData)
    }

    @Test
    fun `when login success data should not null and is ResultSuccess`() = runTest {
        val expectedData = DummyDataGenerator.generateDummySuccessLoginResponse()

        `when`(apiService.login(dummyEmail, dummyPassword)).thenReturn(expectedData)

        val actualData = authRepository.login(dummyEmail, dummyPassword).apply {
            getOrAwaitValue() // konsumsi dahulu Result.Loading
        }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).login(dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            DummyDataGenerator.generateDummySuccessLoginResponse(),
            (actualData as Result.Success).data
        )
    }

    @Test
    fun `when login failed data should not null and is ResultError`() = runTest {
        `when`(
            apiService.login(
                dummyEmail,
                dummyPassword
            )
        ).then {
            throw DummyDataGenerator.generateDummyException()
        }

        val actualData = authRepository.login(dummyEmail, dummyPassword).apply {
            getOrAwaitValue() // konsumsi dahulu Result.Loading
        }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).login(dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }

    @Test
    fun `when logout should call clearPerfs method on AppPreferences`() = runTest {
        authRepository.logout()
        verify(appPreferences).clearPrefs()
    }

    @Test
    fun `when register success data should not null and is ResultSuccess`() = runTest {
        val expectedData = DummyDataGenerator.generateDummySuccessRegisterResponse()

        `when`(apiService.register(dummyName, dummyEmail, dummyPassword)).thenReturn(expectedData)

        val actualData = authRepository.register(dummyName, dummyEmail, dummyPassword).apply {
            getOrAwaitValue() // konsumsi dahulu Result.Loading
        }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).register(dummyName, dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Success)
        assertEquals(
            DummyDataGenerator.generateDummySuccessRegisterResponse(),
            (actualData as Result.Success).data
        )
    }

    @Test
    fun `when register failed data should not null and is ResultError`() = runTest {
        `when`(
            apiService.register(dummyName, dummyEmail, dummyPassword)
        ).then {
            throw DummyDataGenerator.generateDummyException()
        }

        val actualData = authRepository.register(dummyName, dummyEmail, dummyPassword).apply {
            getOrAwaitValue() // konsumsi dahulu Result.Loading
        }.getOrAwaitValue() // dapat Result.Success atau Result.Error

        verify(apiService).register(dummyName, dummyEmail, dummyPassword)

        assertNotNull(actualData)
        assert(actualData is Result.Error)
    }

    @Test
    fun `when saving saving login info should call save prefs method on AppPreferences`() =
        runTest {
            authRepository.saveLoginInfo(dummyName, dummyUserId, dummyBearerToken)
            verify(appPreferences).saveNamePrefs(dummyName)
            verify(appPreferences).saveUserIDPrefs(dummyUserId)
            verify(appPreferences).saveTokenPrefs(dummyBearerToken)
        }
}