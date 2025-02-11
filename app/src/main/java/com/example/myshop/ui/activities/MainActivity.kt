package com.example.myshop.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myshop.R
import com.example.myshop.databinding.ActivityMainBinding
import com.example.myshop.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up Bottom Navigation
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            .setupWithNavController(navController)

        // Handle intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when {
                // Handle deep link
                intent.data?.host == "myshop.example.com" -> handleDeepLink(intent.data)
                
                // Handle notification click
                intent.hasExtra(Constants.EXTRA_ORDER_ID) -> {
                    val orderId = intent.getStringExtra(Constants.EXTRA_ORDER_ID)
                    val notificationType = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TYPE)
                    
                    when (notificationType) {
                        Constants.NOTIFICATION_TYPE_ORDER_STATUS -> {
                            orderId?.let { id ->
                                navController.navigate(
                                    R.id.navigation_order_details,
                                    Bundle().apply {
                                        putString("orderId", id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleDeepLink(uri: Uri?) {
        uri?.let {
            when {
                uri.path?.startsWith("/order/") == true -> {
                    val orderId = uri.lastPathSegment
                    orderId?.let { id ->
                        navController.navigate(
                            R.id.navigation_order_details,
                            Bundle().apply {
                                putString("orderId", id)
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
