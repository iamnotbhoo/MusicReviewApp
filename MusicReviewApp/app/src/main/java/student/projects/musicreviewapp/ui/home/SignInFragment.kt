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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import java.util.concurrent.Executor

class SignInFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())

        setupBiometricPrompt()

        val backButton = view.findViewById<View>(R.id.back_button)
        val signInButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.sign_in_button)
        val biometricButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.biometric_button)
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

        // Setup biometric button
        biometricButton.setOnClickListener {
            if (isBiometricSupported()) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                showToast("Biometric authentication not supported on this device")
            }
        }

        // Hide biometric button if not supported
        if (!isBiometricSupported()) {
            biometricButton.visibility = View.GONE
        }

        setupSignUpLink(signUpLink)
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Authentication successful!")
                    performBiometricSignIn()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Authenticate to access your MusicReviewApp account")
            .setDescription("Use your fingerprint to quickly sign in")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }

    private fun performBiometricSignIn() {
        // For biometric login, you might want to:
        // 1. Use stored credentials from secure storage
        // 2. Use a token-based approach
        // 3. Implement a biometric-specific authentication flow

        // For now, we'll simulate a successful login
        // In a real app, you'd retrieve stored credentials securely
        showToast("Biometric authentication successful!")
        navigateToHome()
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