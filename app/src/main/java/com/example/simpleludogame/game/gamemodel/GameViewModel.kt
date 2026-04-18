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
    var cutPawnCount = MutableLiveData<Int>(0)
    var pawnEnteredGoal = MutableLiveData<Boolean>(false)
    
    var playerRanking = 1

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)
        currentPlayer = gameModel.currentPlayer
        playerRanking = 0
        gameEnd.value = false
        isMoving.value = false
        selectablePawns.value = emptyList()
        cutPawnCount.value = 0
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

        isMoving.value = true

        val currentPlayer = gameModel.getCurrentPlayer() ?: return
        val movablePawns = currentPlayer.canMove(num)
        if (movablePawns.isEmpty()) {
            viewModelScope.launch {
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 500L)
                moveNextPlayer()
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

        viewModelScope.launch {
            delay(500L)

            for (i in 1..(moves - 1)) {
                currentPlayer.moveOneUnit(pawn)
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 50L)
            }

            cutPawnCount.value = currentPlayer.resolveNextCell(pawn)
            currentPlayer.moveOneUnit(pawn)

            if (pawn.cell.value?.type == CellType.GOAL) {
                pawnEnteredGoal.value = true
            }

            // short delay before next move
            delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 100L)

            var shouldMoveToNextPlayer = true

            // if was able to cut pawn then player will get second chance
            if (cutPawnCount.value!! > 0) {
                shouldMoveToNextPlayer = false
            }

            // when last number was 6 or pawn reached goal player gets second chance
            if (pawn.cell.value?.type == CellType.GOAL || num == 6) {
                shouldMoveToNextPlayer = false
            }

            // if player has won then move to next player
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
            } else {
                isMoving.value = false
            }
        }

        selectablePawns.value = emptyList()
    }

    private fun moveNextPlayer() {
        gameEnd.value = gameModel.gameEnd()
        gameModel.moveToNextPlayer()
        isMoving.postValue(false)
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel.players
    }
}