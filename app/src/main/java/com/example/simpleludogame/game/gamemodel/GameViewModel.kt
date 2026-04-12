package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.CellType
import com.example.simpleludogame.game.gamemodel.ludomodel.player.Player
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus
import com.example.simpleludogame.ludoboardui.LudoBoardForeGroundView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel() : ViewModel() {
    private lateinit var gameModel: GameModel

    lateinit var currentPlayer: LiveData<Int>
    var diceVal = MutableLiveData<Int>(1)
    var isMoving = MutableLiveData<Boolean>(false)

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)
        currentPlayer = gameModel.currentPlayer
    }

    fun getCurrentPlayer(): Player? {
        return gameModel.getCurrentPlayer()
    }

    fun rollDice() {
        if (isMoving.value ?: true) {
            return
        }

        val num = Dice.getRandomNumber()
        diceVal.value = num

        val currentPlayer = gameModel.getCurrentPlayer() ?: return
        if (!currentPlayer.canMove(num).isEmpty()) {
            val pawn = currentPlayer.canMove(num)[0]
            val moves = currentPlayer.getNumberOfMoves(pawn, num)

            isMoving.value = true
            viewModelScope.launch {
                for (i in 1..(moves - 1)) {
                    currentPlayer.moveOneUnit(pawn)
                    delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 100L)
                }

                currentPlayer.resolveNextCell(pawn)
                currentPlayer.moveOneUnit(pawn)

                // short delay before next move
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 200L)

                isMoving.postValue(false)

                var shouldMoveToNextPlayer = true

                // dice should not move when last number was 6 or pawn reached goal
                if (pawn.cell.value?.type == CellType.GOAL || num == 6) {
                    shouldMoveToNextPlayer = false
                }

                // dice should move when player has won
                if (currentPlayer.hasWon()) {
                    currentPlayer.setStatus(PlayerStatus.WON)
                    shouldMoveToNextPlayer = true
                }

                if (shouldMoveToNextPlayer) {
                    moveNextPlayer()
                }
            }
        } else {
            moveNextPlayer()
        }
    }

    private fun moveNextPlayer() {
        if (!gameModel.moveToNextPlayer()) {
            gameModel.gameEnd()
        }
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel.players
    }
}