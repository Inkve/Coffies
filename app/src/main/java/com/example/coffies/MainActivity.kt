package com.example.coffies

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeetype.insertDefaultCoffeeTypesIfNeeded
import com.example.coffies.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            insertDefaultCoffeeTypesIfNeeded(db)
        }

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_main,
                R.id.navigation_analytics,
                R.id.navigation_history,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setOnItemSelectedListener { item ->
            val destinationId = when (item.itemId) {
                R.id.navigation_main -> R.id.navigation_main
                R.id.navigation_analytics -> R.id.navigation_analytics
                R.id.navigation_history -> R.id.navigation_history
                R.id.navigation_settings -> R.id.navigation_settings
                else -> null
            }

            destinationId?.let {
                if (navController.currentDestination?.id != it) {
                    navController.popBackStack(it, false)
                    navController.navigate(it)
                } else {
                    navController.popBackStack(it, false)
                }
                true
            } ?: false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
