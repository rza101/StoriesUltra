package com.rhezarijaya.storiesultra.ui.activity.create

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.BuildConfig
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.CreateStoryResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.ActivityCreateStoryBinding
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.ui.activity.main.MainActivity
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.util.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.*
import java.util.concurrent.TimeUnit

class CreateStoryActivity : AppCompatActivity() {
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var createStoryViewModel: CreateStoryViewModel

    private lateinit var tempCameraImagePath: String
    private var currentImageFile: File? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {}
    private val locationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(1)
        maxWaitTime = TimeUnit.SECONDS.toMillis(1)
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }

    private var currentLocation: Location? = null

    private val intentCameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val resultImage = File(tempCameraImagePath)
                currentImageFile = resultImage

                Glide.with(this)
                    .load(resultImage)
                    .placeholder(R.drawable.ic_baseline_broken_image_24)
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .into(binding.createIvPreview)
            }
        }

    private val intentGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val resultImage = getFileFromUri(this, it.data?.data as Uri)
                currentImageFile = resultImage

                Glide.with(this)
                    .load(resultImage)
                    .placeholder(R.drawable.ic_baseline_broken_image_24)
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .into(binding.createIvPreview)
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> getLocation()
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> getLocation()
                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val appPreferences = AppPreferences.getInstance(dataStore)
        createStoryViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(APIUtils.getAPIService(), appPreferences)
            )[CreateStoryViewModel::class.java]

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@CreateStoryActivity)

        binding.createBtnCamera.setOnClickListener {
            val tempFile = createImageTempFile(this)

            tempCameraImagePath = tempFile.absolutePath
            val tempFileUri: Uri = FileProvider.getUriForFile(
                this@CreateStoryActivity,
                BuildConfig.APPLICATION_ID,
                tempFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                resolveActivity(packageManager)
                putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
            }

            intentCameraResult.launch(intent)
        }

        binding.createBtnGallery.setOnClickListener {
            intentGalleryResult.launch(
                Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
                    getString(R.string.choose_picture)
                )
            )
        }

        binding.createBtnClear.setOnClickListener {
            currentImageFile = null
            Glide.with(this)
                .load(
                    Utils.getPlaceholderImage(this)
                )
                .into(binding.createIvPreview)
        }

        binding.createBtnGetLocation.setOnClickListener {
            getLocation()
        }

        binding.createBtnSubmit.setOnClickListener {
            if (currentImageFile != null && !TextUtils.isEmpty(binding.createEtDescription.text.toString())) {
                val compressed = compressImageFile(currentImageFile!!)
                var bearerToken: String

                runBlocking {
                    bearerToken = createStoryViewModel.getBearerToken().asFlow().first() ?: ""
                }

                createStoryViewModel.submit(
                    bearerToken,
                    binding.createEtDescription.text.toString()
                        .toRequestBody("text/plain".toMediaType()),
                    MultipartBody.Part.createFormData(
                        "photo",
                        compressed.name,
                        compressed.asRequestBody("image/jpeg".toMediaType())
                    ),
                    currentLocation?.latitude?.toString()
                        ?.toRequestBody("text/plain".toMediaType()),
                    currentLocation?.longitude?.toString()
                        ?.toRequestBody("text/plain".toMediaType())
                ).observe(this) { result ->
                    if (result != null) {
                        setLoading(result is Result.Loading)

                        when (result) {
                            is Result.Success -> {
                                if (!result.data.error!!) {
                                    Toast.makeText(
                                        this@CreateStoryActivity,
                                        result.data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setResult(MainActivity.INTENT_CREATE_STORY)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        result.data.message
                                            ?: getString(R.string.create_story_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            is Result.Error -> {
                                var message: String = getString(R.string.create_story_error)

                                try {
                                    Gson().fromJson(
                                        (result.error as HttpException).response()?.errorBody()
                                            ?.string(),
                                        CreateStoryResponse::class.java
                                    ).message?.let {
                                        message = it
                                    }
                                } catch (e: Exception) {
                                }

                                Toast.makeText(
                                    this@CreateStoryActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.create_story_form_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        stopLocationUpdates()
        super.onPause()
    }

    private fun compressImageFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)

            val bmpPicByteArray = bmpStream.toByteArray()

            streamLength = bmpPicByteArray.size
            compressQuality -= 10
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun createImageTempFile(context: Context): File {
        return File.createTempFile(
            System.currentTimeMillis().toString() + "_" + (Math.random() * 1000).toInt().toString(),
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
    }

    private fun getFileFromUri(context: Context, uriToFile: Uri): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createImageTempFile(context)

        val inputStream = contentResolver.openInputStream(uriToFile) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)

        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        return myFile
    }

    private fun getLocation() {
        if (Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
            Utils.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location

                    binding.createTvCurrentCoordinate.text = String.format(
                        getString(R.string.coordinate_format),
                        location.latitude,
                        location.longitude
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            Toast.makeText(
                this,
                getString(R.string.location_permission_note),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            createProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            createBtnCamera.isEnabled = !isLoading
            createBtnGallery.isEnabled = !isLoading
            createBtnClear.isEnabled = !isLoading
            createEtDescription.isEnabled = !isLoading
            createBtnSubmit.isEnabled = !isLoading
        }
    }

    private fun startLocationUpdates() {
        if (Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
            Utils.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}