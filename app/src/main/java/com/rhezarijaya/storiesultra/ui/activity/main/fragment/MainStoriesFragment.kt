package com.rhezarijaya.storiesultra.ui.activity.main.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.model.Story
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.FragmentMainStoriesBinding
import com.rhezarijaya.storiesultra.databinding.ItemStoryBinding
import com.rhezarijaya.storiesultra.ui.OnItemClick
import com.rhezarijaya.storiesultra.ui.OnPagingError
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.ui.activity.create.CreateStoryActivity
import com.rhezarijaya.storiesultra.ui.activity.detail.DetailActivity
import com.rhezarijaya.storiesultra.ui.activity.main.MainActivity
import com.rhezarijaya.storiesultra.ui.activity.main.MainViewModel
import com.rhezarijaya.storiesultra.ui.adapter.StoryFooterLoadingAdapter
import com.rhezarijaya.storiesultra.ui.adapter.StoryListAdapter
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.util.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainStoriesFragment : Fragment() {
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

    private lateinit var binding: FragmentMainStoriesBinding
    private lateinit var mainViewModel: MainViewModel

    private val storyListAdapter = StoryListAdapter(object : OnItemClick<Story, ItemStoryBinding> {
        override fun onClick(data: Story, binding: ItemStoryBinding) {
            if (context != null) {
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra(Constants.INTENT_MAIN_TO_DETAIL, data)

                requireActivity().startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        Pair(
                            binding.itemStoryIvImg,
                            requireActivity().getString(R.string.transition_detail_photo)
                        ),
                        Pair(
                            binding.itemStoryTvName,
                            requireActivity().getString(R.string.transition_detail_name)
                        ),
                        Pair(
                            binding.itemStoryTvCreatedAt,
                            requireActivity().getString(R.string.transition_detail_created_at)
                        ),
                    ).toBundle()
                )
            }
        }
    })

    private val intentCreateStoryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == MainActivity.INTENT_CREATE_STORY) {
                refreshRecyclerView()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainStoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appPreferences = AppPreferences.getInstance(requireActivity().dataStore)
        mainViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelFactory(APIUtils.getAPIService(), appPreferences)
            )[MainViewModel::class.java]

        setAdapter()

        binding.fragmentMainStoriesSwipeRefreshLayout.setOnRefreshListener {
            refreshRecyclerView()
        }

        storyListAdapter.loadStateFlow.asLiveData().observe(requireActivity()) {
            binding.fragmentMainStoriesSwipeRefreshLayout.isRefreshing =
                it.source.refresh is LoadState.Loading
        }

        binding.fragmentMainStoriesFabCreate.setOnClickListener {
            if (!Utils.checkPermission(requireActivity(), Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    MainActivity.REQUEST_PERMISSION_CODE
                )
            } else {
                intentCreateStoryResult.launch(
                    Intent(
                        requireActivity(),
                        CreateStoryActivity::class.java
                    )
                )
            }
        }

        var bearerToken: String

        runBlocking {
            bearerToken = mainViewModel.getBearerToken().asFlow().first() ?: ""
        }

        mainViewModel.getMainStories(bearerToken).observe(requireActivity()) { storyPagingData ->
            storyListAdapter.submitData(lifecycle, storyPagingData)
            binding.fragmentMainStoriesRvStories.layoutManager?.scrollToPosition(0)
        }
    }

    private fun setAdapter() {
        binding.fragmentMainStoriesRvStories.adapter =
            storyListAdapter.withLoadStateFooter(
                footer = StoryFooterLoadingAdapter(
                    object : OnPagingError {
                        override fun onError(message: String) {
                            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
                        }
                    })
            )

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.fragmentMainStoriesRvStories.layoutManager =
                GridLayoutManager(requireActivity(), 2)
        } else {
            binding.fragmentMainStoriesRvStories.layoutManager =
                LinearLayoutManager(requireActivity())
        }
    }

    private fun refreshRecyclerView() {
        storyListAdapter.refresh()
        setAdapter() // ada bug dimana jika data sudah fetch sampai habis, setelah refresh tdk bisa fetch lagi
    }
}