package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import com.example.simpleludogame.game.gamemodel.ludomodel.player.Player
import com.example.simpleludogame.ludoboardui.LudoBoardForeGroundView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class GameViewModel() : ViewModel() {
    private lateinit var gameModel: GameModel
    var diceVal = MutableLiveData<Int>(1)

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)
    }

    fun rollDice() {
        val num = Dice.getRandomNumber()
        diceVal.value = num
        val currentPlayer = gameModel.getCurrentPlayer() ?: return
        if (!currentPlayer.canMove(num).isEmpty()) {
            val pawn = currentPlayer.canMove(num)[0]
            viewModelScope.launch {
                for (i in 1..num) {
                    currentPlayer.moveOneUnit(pawn)
                    delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS)
                }
            }
        }
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel.players
    }
}