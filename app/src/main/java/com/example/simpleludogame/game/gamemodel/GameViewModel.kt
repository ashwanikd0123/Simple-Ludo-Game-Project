package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.ViewModel

class GameViewModel(playerCount: Int) : ViewModel() {
    private val gameModel = GameModel(playerCount)



}