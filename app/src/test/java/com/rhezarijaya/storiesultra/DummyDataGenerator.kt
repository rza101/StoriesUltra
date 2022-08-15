package com.rhezarijaya.storiesultra

import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.network.model.Story

object DummyDataGenerator {
    fun generateDummySuccessCreateResponse(): CreateStoryResponse {
        return CreateStoryResponse(error = false, message = "success")
    }

    fun generateDummyErrorCreateResponse(): CreateStoryResponse {
        return CreateStoryResponse(error = true, message = "error submitting")
    }

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