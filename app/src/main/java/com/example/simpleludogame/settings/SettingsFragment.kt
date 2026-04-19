package com.example.simpleludogame.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
            SettingItem.Header("Game Settings"),
            SettingItem.Option(
                "Three Sixes Rule",
                listOf("Off", "Turn Cancel", "Convert 3rd six to non-6"),
                sharedPrefs.getInt(Dice.DICE_BEHAVIOR_KEY, 0)
            ) { index ->
                sharedPrefs.edit().putInt(Dice.DICE_BEHAVIOR_KEY, index).apply()
            },
            SettingItem.Option(
                "Cut Gives Bonus Turn",
                listOf("Off", "On"),
                if (sharedPrefs.getBoolean(GameConstants.BONUS_CHANCE_ON_PLAYER_CUT_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(GameConstants.BONUS_CHANCE_ON_PLAYER_CUT_KEY, index == 1).apply()
            },
            SettingItem.Header("Audio / Feel Settings"),
            SettingItem.Option(
                "Sound Effects",
                listOf("Off", "On"),
                if (sharedPrefs.getBoolean(MediaManager.SOUND_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(MediaManager.SOUND_KEY, index == 1).apply()
            },
            SettingItem.Option(
                "Vibration",
                listOf("Off", "On"),
                if (sharedPrefs.getBoolean(MediaManager.VIBRATION_KEY, true)) 1 else 0
            ) { index ->
                sharedPrefs.edit().putBoolean(MediaManager.VIBRATION_KEY, index == 1).apply()
            }
        )

        binding.settingsListView.adapter = SettingsAdapter(requireContext(), items)
    }
}
