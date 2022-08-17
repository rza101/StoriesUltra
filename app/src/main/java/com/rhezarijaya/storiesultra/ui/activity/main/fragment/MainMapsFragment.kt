package com.rhezarijaya.storiesultra.ui.activity.main.fragment

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.StoryResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.FragmentMainMapsBinding
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.ui.activity.main.MainViewModel
import com.rhezarijaya.storiesultra.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException

class MainMapsFragment : Fragment(), OnMapReadyCallback {
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

    private lateinit var binding: FragmentMainMapsBinding
    private lateinit var mapView: MapView

    private lateinit var googleMap: GoogleMap
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainMapsBinding.inflate(inflater, container, false)
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

        var bearerToken: String

        runBlocking {
            bearerToken = mainViewModel.getBearerToken().asFlow().first() ?: ""
        }

        mapView = binding.fragmentMainMapsMapview

        binding.fragmentMainMapsFabRefresh.setOnClickListener {
            mainViewModel.getMapsStories(bearerToken, 50)
                .observe(requireActivity()) { result ->
                    if (result != null) {
                        setLoading(result is Result.Loading)

                        when (result) {
                            is Result.Success -> {
                                if (!result.data.error!!) {
                                    addMarkersAndAnimateCamera(result.data)
                                } else {
                                    Toast.makeText(
                                        requireActivity(),
                                        result.data.message ?: "Error getting data",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                            is Result.Error -> {
                                Toast.makeText(
                                    requireActivity(),
                                    result.error.localizedMessage,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
        }

        mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MainMapsFragment)
        }

        mainViewModel.getMapsStories(bearerToken, 50)
            .observe(requireActivity()) { result ->
                if (result != null) {
                    setLoading(result is Result.Loading)

                    when (result) {
                        is Result.Success -> {
                            if (!result.data.error!!) {
                                addMarkersAndAnimateCamera(result.data)
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    result.data.message ?: "Error getting data",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                        is Result.Error -> {
                            var message: String = getString(R.string.create_story_error)

                            try {
                                Gson().fromJson(
                                    (result.error as HttpException).response()?.errorBody()
                                        ?.string(),
                                    StoryResponse::class.java
                                ).message?.let {
                                    message = it
                                }
                            } catch (e: Exception) {
                            }

                            Toast.makeText(
                                requireActivity(),
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isIndoorLevelPickerEnabled = true
            isMapToolbarEnabled = false
            isZoomControlsEnabled = false
        }

        if ((requireActivity().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        ) {
            try {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_theme_night
                    )
                )
            } catch (exception: Resources.NotFoundException) {
            }
        }
    }

    private fun addMarkersAndAnimateCamera(storiesData: StoryResponse) {
        if (context != null && this::googleMap.isInitialized) {
            val boundBuilder = LatLngBounds.builder()

            storiesData.listStory.forEach { story ->
                story.let {
                    if (it.name != null && it.lat != null && it.lon != null) {
                        val coordinate = LatLng(it.lat, it.lon)

                        googleMap.addMarker(
                            MarkerOptions().apply {
                                icon(
                                    AppCompatResources.getDrawable(
                                        requireActivity(),
                                        R.drawable.ic_baseline_person_pin_circle_24
                                    )?.let { it1 ->
                                        BitmapDescriptorFactory.fromBitmap(
                                            it1.toBitmap()
                                        )
                                    }
                                )
                                position(coordinate)
                                snippet(
                                    String.format(
                                        requireActivity().getString(R.string.coordinate_format),
                                        it.lat,
                                        it.lon
                                    )
                                )
                                title(it.name)
                            }
                        )

                        boundBuilder.include(coordinate)
                    }
                }
            }

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundBuilder.build(),
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    250
                )
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.fragmentMainMapsFabRefresh.isEnabled = !isLoading
        binding.fragmentMainMapsProgressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}