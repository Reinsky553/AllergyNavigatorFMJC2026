package com.example.allergynavigator

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.navOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // NavHost
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mapFragment, R.id.loginFragment, R.id.onboardingFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val prefs = Prefs(this)

        if (!prefs.isOnboardingShown) {
            if (navController.currentDestination?.id != R.id.onboardingFragment) {
                navController.navigate(R.id.onboardingFragment)
            }
        } else if (!prefs.isLoggedIn) {
            if (navController.currentDestination?.id != R.id.loginFragment) {
                val options = navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
                navController.navigate(R.id.loginFragment, null, options)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}