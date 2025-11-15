package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences

class AuthManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MusicReviewApp", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PASSWORD = "user_password_" // Prefix for storing passwords
    }

    fun signUp(email: String, password: String, username: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            return false
        }

        // Check if user already exists
        if (sharedPreferences.contains("${KEY_USER_PASSWORD}$email")) {
            return false
        }

        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, username)
            // Store password with email as key
            putString("${KEY_USER_PASSWORD}$email", password)
            apply()
        }

        return true
    }

    // Add the missing signIn method
    fun signIn(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            return false
        }

        // Retrieve stored password for this email
        val storedPassword = sharedPreferences.getString("${KEY_USER_PASSWORD}$email", null)

        // Check if password matches
        if (storedPassword == password) {
            // Successful login
            with(sharedPreferences.edit()) {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_USER_EMAIL, email)
                // Get the username from stored data or use email prefix as fallback
                val username = sharedPreferences.getString(KEY_USER_NAME, email.split("@")[0])
                putString(KEY_USER_NAME, username)
                apply()
            }
            return true
        }

        return false
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUser(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun getCurrentEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            apply()
        }
    }
}