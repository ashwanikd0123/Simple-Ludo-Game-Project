package com.example.simpleludogame.game.gamemodel

import android.content.Context
import com.example.simpleludogame.settings.SETTINGS_KEY

class GameConstants(context: Context) {
    companion object {
        const val BONUS_CHANCE_ON_PLAYER_CUT_KEY = "bonus_chance_on_player_cut"
        const val DARK_MODE_KEY = "dark_mode_setting"
        const val DEVELOPER_MODE_KEY = "developer_mode"

        const val MODE_NIGHT_FOLLOW_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_NIGHT = 2
    }

    val bonusChancePlayerCutActive: Boolean
    val developerModeActive: Boolean

    init {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        bonusChancePlayerCutActive = sharedPrefs.getBoolean(BONUS_CHANCE_ON_PLAYER_CUT_KEY, true)
        developerModeActive = sharedPrefs.getBoolean(DEVELOPER_MODE_KEY, false)
    }
}