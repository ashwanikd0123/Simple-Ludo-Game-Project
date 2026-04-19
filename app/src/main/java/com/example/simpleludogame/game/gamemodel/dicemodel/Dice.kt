package com.example.simpleludogame.game.gamemodel.dicemodel

import android.content.Context
import com.example.simpleludogame.game.MediaManager.Companion.SOUND_KEY
import com.example.simpleludogame.game.MediaManager.Companion.VIBRATION_KEY
import com.example.simpleludogame.settings.SETTINGS_KEY

class Dice(context: Context) {

    companion object {
        const val DICE_BEHAVIOR_KEY = "dice_behavior"
    }

    var diceBehavior: DiceBehavior = DiceBehavior.CANCEL_ON_THIRD_SIX

    init {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        diceBehavior = when (sharedPrefs.getInt(DICE_BEHAVIOR_KEY, 0)) {
            0 -> DiceBehavior.DEFAULT
            1 -> DiceBehavior.CANCEL_ON_THIRD_SIX
            else -> DiceBehavior.THIRD_SIX_NOT_ALLOWED
        }
    }

    private val sixCounts = IntArray(4)

    fun roll(playerIndex: Int): Int {
        val value = (1..6).random()

        if (diceBehavior == DiceBehavior.DEFAULT) {
            return value
        }

        if (value != 6) {
            sixCounts[playerIndex] = 0
            return value
        }

        sixCounts[playerIndex]++

        if (sixCounts[playerIndex] == 3) {
            sixCounts[playerIndex] = 0
            if (diceBehavior == DiceBehavior.CANCEL_ON_THIRD_SIX) {
                return 0
            }
            return (1..5).random()
        }

        return 6
    }
}