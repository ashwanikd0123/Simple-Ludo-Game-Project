package com.example.simpleludogame.game.gamemodel.dicemodel

class Dice {
    companion object {
        fun getRandomNumber(): Int {
            return (Math.random() * 6.0 + 1.0).toInt()
        }
    }
}