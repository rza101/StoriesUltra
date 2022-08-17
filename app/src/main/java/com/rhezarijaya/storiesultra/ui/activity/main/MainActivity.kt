package com.rhezarijaya.storiesultra.ui.activity.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIService
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.ActivityMainBinding
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.ui.activity.login.LoginActivity
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.util.Utils

class MainActivity : AppCompatActivity() {
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Utils.checkPermission(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CODE
            )
        }

        val appPreferences = AppPreferences.getInstance(dataStore)
        mainViewModel =
            ViewModelProvider(
                this@MainActivity,
                ViewModelFactory(APIUtils.getAPIService(), appPreferences)
            )[MainViewModel::class.java]

        setupWithNavController(
            binding.mainBottomNavView,
            findNavController(this, R.id.main_nav_host)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_main_logout) {
            mainViewModel.logout()

            Toast.makeText(
                this@MainActivity,
                getString(R.string.logout_success),
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Utils.checkPermission(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, getString(R.string.camera_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.camera_not_granted), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1111
        const val INTENT_CREATE_STORY = 2222
    }
}