package com.rhezarijaya.storiesultra.ui.activity.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.databinding.ActivityDetailBinding
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.util.Constants

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val story = intent.getParcelableExtra<Story>(Constants.INTENT_MAIN_TO_DETAIL)

        story?.let {
            Glide.with(this)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_baseline_broken_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .into(binding.detailIvPhoto)
            binding.detailIvPhoto.contentDescription = story.description

            binding.detailTvName.text = story.name
            binding.detailTvCreatedAt.text = String.format(
                getString(R.string.created_at_format),
                APIUtils.formatCreatedAt(story.createdAt ?: "")
            )
            binding.detailTvDescription.text = story.description
            binding.detailTvLocation.text = String.format(
                getString(R.string.location_format),
                if (story.lat != null && story.lon != null) {
                    String.format(
                        getString(R.string.coordinate_format),
                        story.lat,
                        story.lon
                    )
                } else {
                    "-"
                }
            )
        } ?: run {
            Toast.makeText(this, getString(R.string.data_invalid), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}