package com.example.simpleludogame.game.gamemodel.dicemodel

class Dice() {

    companion object {
        var diceBehavior: DiceBehavior = DiceBehavior.CANCEL_ON_THIRD_SIX
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