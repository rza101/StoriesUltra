package com.rhezarijaya.storiesultra.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.databinding.ItemStoryBinding
import com.rhezarijaya.storiesultra.ui.OnItemClick

class StoryListAdapter(private val onItemClick: OnItemClick<Story, ItemStoryBinding>) :
    PagingDataAdapter<Story, StoryListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        val context = holder.itemView.context

        if (data != null) {
            Glide.with(context)
                .load(data.photoUrl)
                .placeholder(R.drawable.ic_baseline_broken_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .into(holder.binding.itemStoryIvImg)
            holder.binding.itemStoryIvImg.contentDescription = data.description
            holder.binding.itemStoryTvName.text = data.name
            holder.binding.itemStoryTvCreatedAt.text = String.format(
                context.getString(R.string.created_at_format),
                APIUtils.formatCreatedAt(data.createdAt ?: "-")
            )

            holder.itemView.setOnClickListener {
                onItemClick.onClick(data, holder.binding)
            }
        }
    }

    inner class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id?.equals(newItem.id) as Boolean
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}