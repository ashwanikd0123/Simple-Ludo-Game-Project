package com.example.simpleludogame.game.gamemodel.dicemodel

import android.content.Context
import com.example.simpleludogame.settings.SETTINGS_KEY
import kotlin.random.Random

class Dice(context: Context) {

    companion object {
        const val DICE_BEHAVIOR_KEY = "dice_behavior"
        private const val MAX_N = 10
    }

    var diceBehavior: DiceBehavior = DiceBehavior.CANCEL_ON_THIRD_SIX

    private val sixCounts = IntArray(4)
    private val queues = Array(4) { mutableListOf<Int>() }
    private val random = Random.Default

    init {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        diceBehavior = when (sharedPrefs.getInt(DICE_BEHAVIOR_KEY, 0)) {
            0 -> DiceBehavior.DEFAULT
            1 -> DiceBehavior.CANCEL_ON_THIRD_SIX
            else -> DiceBehavior.THIRD_SIX_NOT_ALLOWED
        }
        reset()
    }

    fun reset() {
        sixCounts.fill(0)
        for (i in 0 until 4) {
            queues[i].clear()
            makeNewQueue(i)
        }
    }

    private fun makeNewQueue(playerIndex: Int) {
        val n = 6 * random.nextInt(1, MAX_N + 1)
        val q = queues[playerIndex]
        for (i in 0 until n) {
            q.add((i % 6) + 1)
        }
        q.shuffle(random)
    }

    fun roll(playerIndex: Int): Int {
        if (queues[playerIndex].isEmpty()) {
            makeNewQueue(playerIndex)
        }
        val value = queues[playerIndex].removeAt(0)

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
            return random.nextInt(1, 6)
        }

        return 6
    }
}
