package com.example.myshop.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myshop.R
import com.example.myshop.databinding.ActivityMainBinding
import com.example.myshop.utils.FirebaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val firebaseHelper = FirebaseHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        checkUserAuthentication()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNav.setupWithNavController(navController)

        // Define top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_categories,
                R.id.navigation_cart,
                R.id.navigation_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Handle navigation visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_categories,
                R.id.navigation_cart,
                R.id.navigation_profile -> {
                    binding.navView.visibility = View.VISIBLE
                }
                else -> {
                    binding.navView.visibility = View.GONE
                }
            }
        }
    }

    private fun checkUserAuthentication() {
        val currentUser = firebaseHelper.getCurrentUser()
        if (currentUser == null) {
            // User is not logged in, navigate to AuthActivity
            startAuthActivity()
            finish()
        } else {
            // Check if user is admin and update UI accordingly
            firebaseHelper.getUserProfile(currentUser.uid)
                .addOnSuccessListener { user ->
                    if (user?.isAdmin() == true) {
                        // Show admin menu items or navigate to admin panel
                        showAdminFeatures()
                    }
                }
        }
    }

    private fun startAuthActivity() {
        startActivity(android.content.Intent(this, AuthActivity::class.java))
    }

    private fun showAdminFeatures() {
        // Add admin panel to bottom navigation or show admin menu
        binding.navView.menu.add(0, R.id.navigation_admin_panel, 4, R.string.title_admin_panel)
            .setIcon(R.drawable.ic_admin)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
