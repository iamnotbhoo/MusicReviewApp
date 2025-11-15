package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import student.projects.musicreviewapp.R

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getStartedButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.get_started_button)
        val signInButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.sign_in_button)

        getStartedButton.setOnClickListener {
            navigateToSignUp()
        }

        signInButton.setOnClickListener {
            navigateToSignIn()
        }
    }

    private fun navigateToSignUp() {
        findNavController().navigate(R.id.action_welcomeFragment_to_signUpFragment)
    }

    private fun navigateToSignIn() {
        // For now, navigate to sign up as placeholder
        // You can create a separate sign in fragment later
        findNavController().navigate(R.id.action_welcomeFragment_to_signUpFragment)
    }
}