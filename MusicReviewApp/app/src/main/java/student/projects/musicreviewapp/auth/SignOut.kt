package student.projects.musicreviewapp.auth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R

class SignOut @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val auth = Firebase.auth
    var onSignOut: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_sign_out, this, true)

        val signOutButton = findViewById<MaterialButton>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            onSignOut()
        }
    }

    private fun onSignOut() {
        auth.signOut()
        onSignOut?.invoke()
    }
}