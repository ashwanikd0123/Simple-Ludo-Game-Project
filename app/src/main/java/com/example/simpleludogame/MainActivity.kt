package com.example.simpleludogame

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.simpleludogame.databinding.ActivityMainBinding
import com.example.simpleludogame.game.gamemodel.GameConstants
import com.example.simpleludogame.game.gamemodel.GameViewModel
import com.example.simpleludogame.settings.SETTINGS_KEY
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModels()

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        val fragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = fragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateTheme()
    }

    fun updateTheme() {
        val sharedPrefs = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        val mode = sharedPrefs.getInt(GameConstants.DARK_MODE_KEY, GameConstants.MODE_NIGHT_FOLLOW_SYSTEM)
        when (mode) {
            GameConstants.MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            GameConstants.MODE_NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}