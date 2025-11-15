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

class SignInFragment : Fragment() {

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())

        val backButton = view.findViewById<View>(R.id.back_button)
        val signInButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.sign_in_button)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val signUpLink = view.findViewById<TextView>(R.id.sign_up_link)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            performSignIn(email, password)
        }

        setupSignUpLink(signUpLink)
    }

    private fun setupSignUpLink(textView: TextView) {
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToSignUp()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.purple_link, null)
                ds.isUnderlineText = false
            }
        }

        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun performSignIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        val success = authManager.signIn(email, password)

        if (success) {
            showToast("Signed in successfully!")
            navigateToHome()
        } else {
            showToast("Sign in failed. Please check your credentials.")
        }
    }

    private fun navigateToSignUp() {
        findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}