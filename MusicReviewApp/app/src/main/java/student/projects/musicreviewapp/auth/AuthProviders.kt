package student.projects.musicreviewapp.auth

import android.content.Context
import android.widget.LinearLayout
import student.projects.musicreviewapp.R

class AuthProviders(context: Context) : LinearLayout(context) {

    init {
        inflate(context, R.layout.layout_auth_providers, this)

        // Initialize the buttons
        val signInWithDemo = findViewById<SignInWithDemo>(R.id.sign_in_demo)
        val signInWithGoogle = findViewById<SignInWithGoogle>(R.id.sign_in_google)

        // You can add any additional setup here
    }
}