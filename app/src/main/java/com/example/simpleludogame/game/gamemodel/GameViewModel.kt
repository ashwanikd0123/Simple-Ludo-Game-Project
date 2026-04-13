package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.CellType
import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn
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
    var gameEnd = MutableLiveData<Boolean>(false)
    var selectablePawns = MutableLiveData<List<Pawn>>()
    
    var playerRanking = 1

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)
        currentPlayer = gameModel.currentPlayer
        playerRanking = 0
        gameEnd.value = false
        isMoving.value = false
        selectablePawns.value = emptyList()
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
        val movablePawns = currentPlayer.canMove(num)
        if (movablePawns.isEmpty()) {
            isMoving.value = true
            viewModelScope.launch {
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 100L)
                moveNextPlayer()
                isMoving.postValue(false)
            }
            return
        }

        selectablePawns.value = movablePawns

        if (movablePawns.size == 1) {
            movePawn(movablePawns[0])
        }
    }

    fun movePawn(pawn: Pawn) {
        if (!selectablePawns.value!!.contains(pawn)) {
            return
        }

        val num = diceVal.value!!
        val currentPlayer = gameModel.getCurrentPlayer() ?: return
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
                playerRanking++
                when (playerRanking) {
                    1 -> currentPlayer.setStatus(PlayerStatus.RANK_1)
                    2 -> currentPlayer.setStatus(PlayerStatus.RANK_2)
                    3 -> currentPlayer.setStatus(PlayerStatus.RANK_3)
                    else -> currentPlayer.setStatus(PlayerStatus.LOSE)
                }
                shouldMoveToNextPlayer = true
            }

            if (shouldMoveToNextPlayer) {
                moveNextPlayer()
            }
        }

        selectablePawns.value = emptyList()
    }

    private fun moveNextPlayer() {
        gameEnd.value = gameModel.gameEnd()
        gameModel.moveToNextPlayer()
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel.players
    }
}