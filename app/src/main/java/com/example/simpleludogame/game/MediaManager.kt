package com.example.simpleludogame.game

import android.content.Context
import android.media.MediaPlayer
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.simpleludogame.R
import com.example.simpleludogame.settings.SETTINGS_KEY

class MediaManager(context: Context) {

    companion object {
        const val SOUND_KEY = "sound_effects"
        const val VIBRATION_KEY = "vibration"
    }

    val isSoundOn: Boolean
    val isVibrationOn: Boolean

    init {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        isSoundOn = sharedPrefs.getBoolean(SOUND_KEY, true)
        isVibrationOn = sharedPrefs.getBoolean(VIBRATION_KEY, true)
    }

    enum class Event {
        DICE_ROLL, PAWN_MOVEMENT, PAWN_CUT, PAWN_ENTER_GOAL
    }

    private val diceRollMediaPlayer = MediaPlayer.create(context, R.raw.dice_roll)
    private val pawnMovementPlayer = MediaPlayer.create(context, R.raw.pawn_move)
    private val pawnCutPlayer = MediaPlayer.create(context, R.raw.pawn_cut)
    private val pawnEnterGoalPlayer = MediaPlayer.create(context, R.raw.pawn_enter_goal)

    fun playMedia(view: View, event: Event) {
        when (event) {
            Event.DICE_ROLL -> diceRoll(view)
            Event.PAWN_MOVEMENT -> pawnMovement(view)
            Event.PAWN_CUT -> pawnCut(view)
            Event.PAWN_ENTER_GOAL -> pawnEnterGoal(view)
        }
    }

    private fun diceRoll(view: View) {
        if (isSoundOn) {
            diceRollMediaPlayer?.start()
        }
        if (isVibrationOn) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun pawnMovement(view: View) {
        if (isSoundOn) {
            pawnMovementPlayer?.start()
        }
        if (isVibrationOn) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun pawnCut(view: View) {
        if (isSoundOn) {
            pawnCutPlayer?.start()
        }
        if (isVibrationOn) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun pawnEnterGoal(view: View) {
        if (isSoundOn) {
            pawnEnterGoalPlayer?.start()
        }
        if (isVibrationOn) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

}
