package com.rhezarijaya.storiesultra.data.network

import com.rhezarijaya.storiesultra.BuildConfig
import com.rhezarijaya.storiesultra.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object APIUtils {
    fun formatBearerToken(token: String): String {
        return "Bearer $token"
    }

    fun formatCreatedAt(dateString: String): String {
        var result = ""

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("GMT")

            val date = sdf.parse(dateString)

            result =
                date?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(it)
                }.toString()
        } catch (e: Exception) {
        }

        return result
    }

    fun getAPIService(): APIService {
        return Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        if(BuildConfig.DEBUG){
                            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                        } else {
                            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
                        }
                    )
                    .build()
            )
            .build()
            .create(APIService::class.java)
    }
}