package com.rhezarijaya.storiesultra.data.network.model

import com.google.gson.annotations.SerializedName

data class CreateStoryResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)
