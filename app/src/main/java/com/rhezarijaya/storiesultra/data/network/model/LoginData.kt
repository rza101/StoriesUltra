package com.rhezarijaya.storiesultra.data.network.model

import com.google.gson.annotations.SerializedName

data class LoginData(
    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("userId")
    val userId: String? = null,

    @field:SerializedName("token")
    val token: String? = null
)