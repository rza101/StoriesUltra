package com.rhezarijaya.storiesultra.ui.activity.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.data.SingleEvent
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.ui.OnSuccessCallback
import com.rhezarijaya.storiesultra.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateStoryViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val createError: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    fun isLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun getCreateError(): LiveData<SingleEvent<String>> {
        return createError
    }

    fun submit(
        onSuccessCallback: OnSuccessCallback<CreateStoryResponse>,
        description: String,
        photo: File,
        lat: Double?,
        lon: Double?
    ) {
        isLoading.value = true

        var bearerToken: String

        runBlocking {
            bearerToken = appPreferences.getTokenPrefs().first() ?: ""
        }

        APIUtils.getAPIService().postStory(
            authorization = APIUtils.formatBearerToken(bearerToken),
            description = description.toRequestBody("text/plain".toMediaType()),
            photo = MultipartBody.Part.createFormData(
                "photo",
                photo.name,
                photo.asRequestBody("image/jpeg".toMediaType())
            ),
            lat = lat?.toString()?.toRequestBody("text/plain".toMediaType()),
            lon = lon?.toString()?.toRequestBody("text/plain".toMediaType())
        ).enqueue(object : Callback<CreateStoryResponse> {
            override fun onResponse(
                call: Call<CreateStoryResponse>,
                response: Response<CreateStoryResponse>
            ) {
                isLoading.value = false

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error as Boolean) {
                            createError.value = SingleEvent(it.message as String)
                        } else {
                            onSuccessCallback.onSuccess(response.body()!!)
                        }
                    } ?: run {
                        createError.value = SingleEvent(Constants.UNEXPECTED_DATA_ERROR)
                    }
                } else {
                    val body: CreateStoryResponse? =
                        Gson().fromJson(
                            response.errorBody()?.string(),
                            CreateStoryResponse::class.java
                        )

                    createError.value =
                        SingleEvent(body?.message ?: APIUtils.formatResponseCode(response.code()))
                }
            }

            override fun onFailure(call: Call<CreateStoryResponse>, t: Throwable) {
                isLoading.value = false
                createError.value = SingleEvent(Constants.UNEXPECTED_ERROR)
            }
        })
    }
}