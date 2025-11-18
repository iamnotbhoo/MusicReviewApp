package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager

class SignUpFragment : Fragment() {

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager()

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        val signUpButton = view.findViewById<MaterialButton>(R.id.sign_up_button)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val usernameEditText = view.findViewById<TextInputEditText>(R.id.username_edit_text)
        val signInLink = view.findViewById<TextView>(R.id.sign_in_link)

        backButton.setOnClickListener { findNavController().popBackStack() }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            if (password.length < 12) {
                showToast("Password must be at least 12 characters")
                return@setOnClickListener
            }

            authManager.signUp(email, password, username) { success, error ->
                if (success) {
                    showToast("Account created successfully!")
                    navigateToHome()
                } else {
                    showToast("Sign up failed: ${error ?: "Unknown error"}")
                }
            }
        }

        setupSignInLink(signInLink)
    }

    private fun setupSignInLink(textView: TextView) {
        val fullText = "Already have an account? Sign In"
        val spannableString = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { navigateToSignIn() }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.purple_link, null)
                ds.isUnderlineText = false
            }
        }
        val startIndex = fullText.indexOf("Sign In")
        val endIndex = startIndex + "Sign In".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun navigateToSignIn() { findNavController().navigate(R.id.action_signUpFragment_to_signInFragment) }

    private fun navigateToHome() { findNavController().navigate(R.id.action_signUpFragment_to_homeFragment) }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
