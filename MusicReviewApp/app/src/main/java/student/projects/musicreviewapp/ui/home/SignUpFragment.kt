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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        authManager = AuthManager(requireContext())

        val backButton = view.findViewById<View>(R.id.back_button)
        val signUpButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.sign_up_button)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val signInLink = view.findViewById<TextView>(R.id.sign_in_link)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            performSignUp(email, password)
        }

        setupSignInLink(signInLink)
    }

    private fun setupSignInLink(textView: TextView) {
        val fullText = "Already have an account? Sign In"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToSignIn()
            }

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

    private fun performSignUp(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return
        }

        val success = authManager.signUp(email, password, email.split("@")[0])

        if (success) {
            showToast("Account created successfully!")
            navigateToHome()
        } else {
            showToast("Sign up failed. Email may already exist.")
        }
    }

    private fun navigateToSignIn() {
        findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}