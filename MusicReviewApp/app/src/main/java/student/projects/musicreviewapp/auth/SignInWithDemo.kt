package student.projects.musicreviewapp.auth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R

class SignInWithDemo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val auth = Firebase.auth

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_sign_in_demo, this, true)

        val demoButton = findViewById<MaterialButton>(R.id.demo_button)
        demoButton.setOnClickListener {
            signInWithDemoAccount()
        }
    }

    private fun signInWithDemoAccount() {
        val email = "testwithemail@mail.com"
        val password = "mypassword"

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    println("Demo sign in successful")
                } else {
                    // Sign in failed
                    val errorCode = task.exception?.message
                    val errorMessage = task.exception?.localizedMessage
                    println("Error signing in with demo: $errorCode, $errorMessage")
                }
            }
    }
}