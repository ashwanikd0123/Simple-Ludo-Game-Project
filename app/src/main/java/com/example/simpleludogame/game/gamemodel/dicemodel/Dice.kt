package com.example.simpleludogame.game.gamemodel.dicemodel

class Dice {
    companion object {
        var sixCount = 0
        fun getRandomNumber(): Int {
            val value = (Math.random() * 6.0 + 1.0).toInt()
            if (value != 6) {
                sixCount = 0
                return value
            }

            sixCount++
            if (sixCount == 4) {
                sixCount = 0
                return  (Math.random() * 5.0 + 1.0).toInt()
            }

            return value
        }
    }
}