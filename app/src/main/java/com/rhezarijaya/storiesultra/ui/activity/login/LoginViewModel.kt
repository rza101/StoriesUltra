package com.rhezarijaya.storiesultra.ui.activity.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.data.SingleEvent
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    private val loginData: MutableLiveData<LoginResponse> = MutableLiveData()
    private val loginError: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun getLoginData(): LiveData<LoginResponse> {
        return loginData
    }

    fun getLoginError(): LiveData<SingleEvent<String>> {
        return loginError
    }

    fun isLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun login(email: String, password: String) {
        isLoading.value = true

        APIUtils.getAPIService().login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isLoading.value = false

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error as Boolean) {
                            loginError.value = SingleEvent(it.message as String)
                        } else {
                            loginData.value = response.body() as LoginResponse

                            viewModelScope.launch {
                                response.body()?.loginData?.let { loginData ->
                                    appPreferences.saveNamePrefs(loginData.name ?: "")
                                    appPreferences.saveUserIDPrefs(loginData.userId ?: "")
                                    appPreferences.saveTokenPrefs(loginData.token ?: "")
                                }
                            }
                        }
                    } ?: run {
                        loginError.value = SingleEvent(Constants.UNEXPECTED_DATA_ERROR)
                    }
                } else {
                    val body: LoginResponse? =
                        Gson().fromJson(response.errorBody()?.string(), LoginResponse::class.java)

                    loginError.value =
                        SingleEvent(body?.message ?: APIUtils.formatResponseCode(response.code()))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isLoading.value = false
                loginError.value = SingleEvent(Constants.UNEXPECTED_ERROR)
            }
        })
    }
}