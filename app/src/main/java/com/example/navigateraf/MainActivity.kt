package com.example.navigateraf

import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.navigateraf.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        // Инициализируем NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)

        // Инициализация AppBarConfiguration для toolbar (если он есть)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()

        setupWithNavController(binding.navView, navController)
        setupWithNavController(Toolbar(baseContext), navController, appBarConfiguration)

        // Настройка NavController с BottomNavigationView
        val bottomNav = binding.navView
        bottomNav.setupWithNavController(navController)
        //TODO
        bottomNav.selectedItemId = R.id.counterFragment

        if (savedInstanceState == null) {
            navController.navigate(R.id.counterFragment)
        }
    }

    // Обработчик нажатий на кнопку "назад"
    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settingsFragment) {
            val navController = findNavController(this, R.id.nav_host_fragment_content_main)
            val currentDestination = navController.currentDestination
            if (currentDestination != null && currentDestination.id == R.id.settingsFragment) {
                return true
            }
            navController.navigate(R.id.settingsFragment)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}