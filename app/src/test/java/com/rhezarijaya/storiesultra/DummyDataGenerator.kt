package com.rhezarijaya.storiesultra

import com.rhezarijaya.storiesultra.data.network.model.*

object DummyDataGenerator {
    fun generateDummySuccessCreateStoryResponse() =
        CreateStoryResponse(error = false, message = "Submit success")

    fun generateDummySuccessLoginResponse() = LoginResponse(
        error = false,
        message = "Submit success",
        loginData = LoginData(name = "name", userId = "123", token = "abc123")
    )

    fun generateDummySuccessRegisterResponse() = RegisterResponse(
        error = false,
        message = "Register success"
    )

    fun generateDummySuccessStoryResponse() =
        StoryResponse(error = false, message = "success", listStory = generateDummyStories())

    fun generateDummyException() = Exception("exception")

    fun generateDummyBearerToken() = "bearer token"

    fun generateDummyStories(): List<Story> {
        val stories: MutableList<Story> = arrayListOf()

        for (i in 1..50) {
            stories.add(
                Story(
                    id = "story-$i",
                    name = "Dicoding ($i)",
                    description = "Jika sudah cinta programming, maka belajar ngoding pun terasa lebih mudah dan enjoy ($i)",
                    photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1648719567500_ssbAAkGs.png",
                    createdAt = "2022-03-31T09:39:27.502Z",
                    lat = -6.0019502,
                    lon = 106.0662807
                )
            )
        }

        return stories
    }
}