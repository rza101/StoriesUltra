package com.rhezarijaya.storiesultra.ui.activity.register

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.data.network.APIUtils
import com.rhezarijaya.storiesultra.data.network.Result
import com.rhezarijaya.storiesultra.data.network.model.RegisterResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.ActivityRegisterBinding
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.util.Constants
import retrofit2.HttpException

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appPreferences = AppPreferences.getInstance(dataStore)
        val registerViewModel =
            ViewModelProvider(
                this@RegisterActivity,
                ViewModelFactory(APIUtils.getAPIService(), appPreferences)
            )[RegisterViewModel::class.java]

        binding.registerBtnRegister.setOnClickListener {
            if (!binding.registerNameField.isError &&
                !binding.registerEmailField.isError &&
                !binding.registerPasswordField.isError
            ) {
                registerViewModel.register(
                    binding.registerNameField.text.toString(),
                    binding.registerEmailField.text.toString(),
                    binding.registerPasswordField.text.toString()
                ).observe(this) { result ->
                    if (result != null) {
                        setLoading(result is Result.Loading)

                        when (result) {
                            is Result.Success -> {
                                if (!(result.data.error as Boolean)) {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.register_success),
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        result.data.message ?: getString(R.string.register_error),
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    finish()
                                }
                            }

                            is Result.Error -> {
                                var message: String = getString(R.string.register_error)

                                try {
                                    Gson().fromJson(
                                        (result.error as HttpException).response()?.errorBody()
                                            ?.string(),
                                        RegisterResponse::class.java
                                    ).message?.let {
                                        message = it
                                    }
                                } catch (e: Exception) {
                                }

                                Toast.makeText(
                                    this@RegisterActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.register_form_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            registerProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            registerBtnRegister.isEnabled = !isLoading
            registerNameField.isEnabled = !isLoading
            registerEmailField.isEnabled = !isLoading
            registerPasswordField.isEnabled = !isLoading
        }
    }
}