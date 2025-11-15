package student.projects.musicreviewapp.components.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import student.projects.musicreviewapp.R

class SignInButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onClick: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_sign_in_button, this, true)

        val signInButton = findViewById<MaterialButton>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            onClick?.invoke()
        }
    }
}