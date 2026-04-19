package com.example.simpleludogame.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.simpleludogame.R
import com.example.simpleludogame.databinding.FragmentSettingsBinding
import com.example.simpleludogame.game.MediaManager
import com.example.simpleludogame.game.gamemodel.GameConstants
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice

val SETTINGS_KEY = "game_settings"

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setupSettingsList()
        return binding.root
    }

    private fun setupSettingsList() {
        val sharedPrefs = requireContext().getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        
        val items = listOf(
            SettingItem.Header(getString(R.string.game_settings_header)),
            SettingItem.Option(
                getString(R.string.three_sixes_rule),
                listOf(
                    getString(R.string.off),
                    getString(R.string.turn_cancel),
                    getString(R.string.convert_third_six)
                ),
                sharedPrefs.getInt(Dice.DICE_BEHAVIOR_KEY, 0)
            ) { index ->
                sharedPrefs.edit().putInt(Dice.DICE_BEHAVIOR_KEY, index).apply()
            },
            SettingItem.Option(
                getString(R.string.cut_gives_bonus),
                listOf(getString(R.string.off), getString(R.string.on)),
                if (sharedPrefs.getBoolean(GameConstants.BONUS_CHANCE_ON_PLAYER_CUT_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(GameConstants.BONUS_CHANCE_ON_PLAYER_CUT_KEY, index == 1).apply()
            },
            SettingItem.Header(getString(R.string.audio_feel_settings_header)),
            SettingItem.Option(
                getString(R.string.sound_effects),
                listOf(getString(R.string.off), getString(R.string.on)),
                if (sharedPrefs.getBoolean(MediaManager.SOUND_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(MediaManager.SOUND_KEY, index == 1).apply()
            },
            SettingItem.Option(
                getString(R.string.vibration),
                listOf(getString(R.string.off), getString(R.string.on)),
                if (sharedPrefs.getBoolean(MediaManager.VIBRATION_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(MediaManager.VIBRATION_KEY, index == 1).apply()
            },
            SettingItem.Option(
                getString(R.string.dark_mode),
                listOf(getString(R.string.system), getString(R.string.off), getString(R.string.on)),
                sharedPrefs.getInt(GameConstants.DARK_MODE_KEY, 0)
            ) { index ->
                sharedPrefs.edit().putInt(GameConstants.DARK_MODE_KEY, index).apply()
                updateTheme(index)
            }
        )

        binding.settingsListView.adapter = SettingsAdapter(requireContext(), items)
    }

    fun updateTheme(mode: Int) {
        when (mode) {
            GameConstants.MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            GameConstants.MODE_NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
