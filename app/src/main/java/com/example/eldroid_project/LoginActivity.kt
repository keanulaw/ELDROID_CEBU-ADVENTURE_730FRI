package com.example.eldroid_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        setupRetrofit()
        setupLoginButton()
    }

    private fun setupRetrofit() {
        try {
            // Initialize Retrofit with logging
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            apiService = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)

            Log.d("LoginActivity", "Retrofit setup successful")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error setting up Retrofit", e)
            Toast.makeText(this, "Error setting up network: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLoginButton() {
        loginButton.setOnClickListener {
            try {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                Log.d("LoginActivity", "Login attempt with email: $email")

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Toast.makeText(this, "Attempting login...", Toast.LENGTH_SHORT).show()
                performLogin(email, password)
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error in login button click", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LoginActivity", "Starting API call")
                val loginRequest = LoginRequest(email, password)
                Log.d("LoginActivity", "Request: $loginRequest")

                val response = apiService.login(loginRequest)
                Log.d("LoginActivity", "Response code: ${response.code()}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Log.d("LoginActivity", "Response body: $loginResponse")

                        when (loginResponse?.status) {
                            "success" -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate to HomeActivity
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finish()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    loginResponse?.message ?: "Unknown error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e("LoginActivity", "Error body: $errorBody")
                            Toast.makeText(
                                this@LoginActivity,
                                "Login failed: ${
                                    try {
                                        JSONObject(errorBody ?: "").getString("message")
                                    } catch (e: Exception) {
                                        response.message()
                                    }
                                }",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error parsing error response", e)
                            Toast.makeText(
                                this@LoginActivity,
                                "Login failed: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Network error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}