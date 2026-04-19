package com.example.simpleludogame.game.gamemodel

import android.content.Context
import com.example.simpleludogame.settings.SETTINGS_KEY

class GameConstants(context: Context) {
    companion object {
        const val BONUS_CHANCE_ON_PLAYER_CUT_KEY = "bonus_chance_on_player_cut"
    }

    val bonusChancePlayerCutActive: Boolean

    init {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        bonusChancePlayerCutActive = sharedPrefs.getBoolean(BONUS_CHANCE_ON_PLAYER_CUT_KEY, true)
    }
}