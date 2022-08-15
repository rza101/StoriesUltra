package com.rhezarijaya.storiesultra.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.databinding.ItemStoryLoadingBinding
import com.rhezarijaya.storiesultra.ui.OnPagingError

class StoryFooterLoadingAdapter(
    private val onPagingError: OnPagingError
) :
    LoadStateAdapter<StoryFooterLoadingAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.binding.itemStoryLoadingProgressBar.isVisible = loadState is LoadState.Loading

        if (loadState is LoadState.Error) {
            onPagingError.onError(
                loadState.error.localizedMessage
                    ?: holder.itemView.context.getString(R.string.paging_error)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder(
            ItemStoryLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class ViewHolder(val binding: ItemStoryLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}