package student.projects.musicreviewapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager()
        navController = findNavController(R.id.nav_host_fragment)

        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set up the top-level destinations - these won't show up in back stack
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.profileFragment
            )
        )

        // Connect bottom navigation with nav controller
        bottomNavigationView.setupWithNavController(navController)

        // Show/hide bottom navigation based on authentication and destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.profileFragment -> {
                    // Show bottom nav for main app sections
                    bottomNavigationView.visibility = android.view.View.VISIBLE
                }
                else -> {
                    // Hide bottom nav for auth screens and welcome screen
                    bottomNavigationView.visibility = android.view.View.GONE
                }
            }
        }

        // Check if user is logged in and navigate accordingly
        if (authManager.isLoggedIn()) {
            // If user is logged in and we're on an auth screen, navigate to home
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.welcomeFragment ||
                currentDestination == R.id.signInFragment ||
                currentDestination == R.id.signUpFragment) {
                navController.navigate(R.id.homeFragment)
            }
        } else {
            // If user is not logged in and we're on a main app screen, navigate to welcome
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.homeFragment ||
                currentDestination == R.id.searchFragment ||
                currentDestination == R.id.profileFragment) {
                navController.navigate(R.id.welcomeFragment)
            }
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
