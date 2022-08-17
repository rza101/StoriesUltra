package com.rhezarijaya.storiesultra.ui.activity.login

import android.content.Context
import android.content.Intent
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
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.databinding.ActivityLoginBinding
import com.rhezarijaya.storiesultra.ui.ViewModelFactory
import com.rhezarijaya.storiesultra.ui.activity.main.MainActivity
import com.rhezarijaya.storiesultra.ui.activity.register.RegisterActivity
import com.rhezarijaya.storiesultra.util.Constants
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appPreferences = AppPreferences.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(
                this@LoginActivity,
                ViewModelFactory(APIUtils.getAPIService(), appPreferences)
            )[LoginViewModel::class.java]

        binding.loginTvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtnLogin.setOnClickListener {
            if (!binding.loginEmailField.isError && !binding.loginPasswordField.isError) {
                loginViewModel.login(
                    binding.loginEmailField.text.toString(),
                    binding.loginPasswordField.text.toString()
                ).observe(this) { result ->
                    if (result != null) {
                        setLoading(result is Result.Loading)

                        when (result) {
                            is Result.Success -> {
                                if (!result.data.error!! && result.data.loginData != null) {
                                    result.data.loginData.apply {
                                        if (name != null && userId != null && token != null) {
                                            loginViewModel.saveLoginInfo(name, userId, token)
                                        }
                                    }

                                    Toast.makeText(
                                        this@LoginActivity,
                                        getString(R.string.login_success),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        result.data.message ?: getString(R.string.login_error),
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
                                        LoginResponse::class.java
                                    ).message?.let {
                                        message = it
                                    }
                                } catch (e: Exception) {
                                }

                                Toast.makeText(
                                    this@LoginActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_form_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginBtnLogin.isEnabled = !isLoading
            loginEmailField.isEnabled = !isLoading
            loginPasswordField.isEnabled = !isLoading
            loginTvRegister.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }
}