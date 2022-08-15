package com.rhezarijaya.storiesultra.ui.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.rhezarijaya.storiesultra.R
import com.rhezarijaya.storiesultra.databinding.ActivityLoginBinding
import com.rhezarijaya.storiesultra.data.network.model.LoginResponse
import com.rhezarijaya.storiesultra.ui.activity.main.MainActivity
import com.rhezarijaya.storiesultra.ui.activity.register.RegisterActivity
import com.rhezarijaya.storiesultra.data.preferences.AppPreferences
import com.rhezarijaya.storiesultra.util.Constants
import com.rhezarijaya.storiesultra.data.SingleEvent
import com.rhezarijaya.storiesultra.ui.ViewModelFactory

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
                ViewModelFactory(appPreferences)
            )[LoginViewModel::class.java]

        binding.loginTvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtnLogin.setOnClickListener {
            if (!binding.loginEmailField.isError && !binding.loginPasswordField.isError) {
                loginViewModel.login(
                    binding.loginEmailField.text.toString(),
                    binding.loginPasswordField.text.toString()
                )
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_form_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        loginViewModel.getLoginData().observe(this@LoginActivity) { loginData: LoginResponse ->
            if (!(loginData.error as Boolean)) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_success),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

        loginViewModel.getLoginError()
            .observe(this@LoginActivity) { loginError: SingleEvent<String> ->
                loginError.getData()?.let {
                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                }
            }

        loginViewModel.isLoading().observe(this@LoginActivity) { isLoading: Boolean ->
            // TODO boleh pakai binding apply agar tdk berulang
            binding.apply {
                loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                loginBtnLogin.isEnabled = !isLoading
                loginEmailField.isEnabled = !isLoading
                loginPasswordField.isEnabled = !isLoading
                loginTvRegister.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }
}