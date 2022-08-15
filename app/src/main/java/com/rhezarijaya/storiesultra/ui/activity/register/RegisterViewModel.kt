package com.rhezarijaya.storiesultra.ui.activity.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.data.network.model.RegisterResponse
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.data.SingleEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    private val registerData: MutableLiveData<RegisterResponse> = MutableLiveData()
    private val registerError: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun getRegisterData(): LiveData<RegisterResponse> {
        return registerData
    }

    fun getRegisterError(): LiveData<SingleEvent<String>> {
        return registerError
    }

    fun isLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun register(name: String, email: String, password: String) {
        isLoading.value = true

        APIUtils.getAPIService().register(name, email, password)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    isLoading.value = false

                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.error as Boolean) {
                                registerError.value = SingleEvent(it.message as String)
                            } else {
                                registerData.value = response.body() as RegisterResponse
                            }
                        } ?: run {
                            registerError.value = SingleEvent(Constants.UNEXPECTED_DATA_ERROR)
                        }
                    } else {
                        val body: RegisterResponse? = Gson().fromJson(
                            response.errorBody()?.string(),
                            RegisterResponse::class.java
                        )

                        registerError.value =
                            SingleEvent(body?.message ?: APIUtils.formatResponseCode(response.code()))
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    isLoading.value = false
                    registerError.value = SingleEvent(Constants.UNEXPECTED_ERROR)
                }
            })
    }
}