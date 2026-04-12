package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.ViewModel

class GameViewModel() : ViewModel() {
    private lateinit var gameModel: GameModel

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)
    }

}