package com.example.myshop.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myshop.R
import com.example.myshop.databinding.ActivityAuthBinding
import com.example.myshop.models.User
import com.example.myshop.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val firebaseHelper = FirebaseHelper.getInstance()
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Initially show login layout
        updateAuthMode(isLoginMode)
    }

    private fun setupListeners() {
        // Switch between login and register modes
        binding.switchAuthModeButton.setOnClickListener {
            isLoginMode = !isLoginMode
            updateAuthMode(isLoginMode)
        }

        // Handle authentication button click
        binding.authButton.setOnClickListener {
            if (validateInputs()) {
                if (isLoginMode) {
                    handleLogin()
                } else {
                    handleRegistration()
                }
            }
        }

        // Handle forgot password
        binding.forgotPasswordButton.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun updateAuthMode(isLogin: Boolean) {
        with(binding) {
            // Update button and link text
            authButton.setText(if (isLogin) R.string.btn_login else R.string.btn_register)
            switchAuthModeButton.setText(
                if (isLogin) R.string.text_no_account else R.string.text_have_account
            )

            // Show/hide registration-specific fields
            fullNameLayout.visibility = if (isLogin) View.GONE else View.VISIBLE
            phoneLayout.visibility = if (isLogin) View.GONE else View.VISIBLE
            confirmPasswordLayout.visibility = if (isLogin) View.GONE else View.VISIBLE
            forgotPasswordButton.visibility = if (isLogin) View.VISIBLE else View.GONE
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        with(binding) {
            // Email validation
            if (emailInput.text.toString().trim().isEmpty()) {
                emailLayout.error = getString(R.string.error_required_field)
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.text.toString()).matches()) {
                emailLayout.error = getString(R.string.error_invalid_email)
                isValid = false
            } else {
                emailLayout.error = null
            }

            // Password validation
            if (passwordInput.text.toString().length < 6) {
                passwordLayout.error = getString(R.string.error_password_short)
                isValid = false
            } else {
                passwordLayout.error = null
            }

            // Additional validations for registration
            if (!isLoginMode) {
                // Full name validation
                if (fullNameInput.text.toString().trim().isEmpty()) {
                    fullNameLayout.error = getString(R.string.error_required_field)
                    isValid = false
                }

                // Phone validation
                if (phoneInput.text.toString().trim().isEmpty()) {
                    phoneLayout.error = getString(R.string.error_required_field)
                    isValid = false
                }

                // Confirm password validation
                if (passwordInput.text.toString() != confirmPasswordInput.text.toString()) {
                    confirmPasswordLayout.error = getString(R.string.error_passwords_not_match)
                    isValid = false
                }
            }
        }
        return isValid
    }

    private fun handleLogin() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.error_login_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun handleRegistration() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val fullName = binding.fullNameInput.text.toString()
        val phone = binding.phoneInput.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = User(
                    userId = authResult.user?.uid ?: "",
                    email = email,
                    fullName = fullName,
                    phoneNumber = phone
                )
                
                // Save additional user information to Firestore
                firebaseHelper.createUserProfile(user)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(R.string.success_registration),
                            Toast.LENGTH_SHORT
                        ).show()
                        startMainActivity()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.error_registration_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.error_registration_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun handleForgotPassword() {
        val email = binding.emailInput.text.toString()
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.error_required_field)
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Password reset email sent",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to send reset email",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
