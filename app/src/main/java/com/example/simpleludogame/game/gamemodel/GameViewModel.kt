package com.example.simpleludogame.game.gamemodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn
import com.example.simpleludogame.game.gamemodel.ludomodel.player.Player
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.CellType
import com.example.simpleludogame.ludoboardui.LudoBoardForeGroundView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel() : ViewModel() {
    private var gameModel: GameModel? = null

    private val _currentPlayer = MutableLiveData<Int>()
    val currentPlayer: LiveData<Int> = _currentPlayer

    private val _diceVal = MutableLiveData<Int>(1)
    val diceVal: LiveData<Int> = _diceVal

    private val _isMoving = MutableLiveData<Boolean>(false)
    val isMoving: LiveData<Boolean> = _isMoving

    private val _gameEnd = MutableLiveData<Boolean>(false)
    val gameEnd: LiveData<Boolean> = _gameEnd

    private val _selectablePawns = MutableLiveData<List<Pawn>>(emptyList())
    val selectablePawns: LiveData<List<Pawn>> = _selectablePawns

    private val _cutPawnCount = MutableLiveData<Int>(0)
    val cutPawnCount: LiveData<Int> = _cutPawnCount

    private val _pawnEnteredGoal = MutableLiveData<Boolean>(false)
    val pawnEnteredGoal: LiveData<Boolean> = _pawnEnteredGoal

    private val _playerChanceCut = MutableLiveData<Boolean>(false)
    val playerChanceCut: LiveData<Boolean> = _playerChanceCut

    private val _starCell = MutableLiveData<Boolean>(true)
    val starCell: LiveData<Boolean> = _starCell

    var playerRanking = 1
    lateinit var dice: Dice
    lateinit var gameConstants: GameConstants

    fun initGame(context: Context, playerCount: Int) {
        gameModel = GameModel(playerCount)
        dice = Dice(context)
        gameConstants = GameConstants(context)
        _starCell.value = false
        playerRanking = 0
        _gameEnd.value = false
        _isMoving.value = false
        _selectablePawns.value = emptyList()
        _cutPawnCount.value = 0
        updateCurrentPlayer()
    }
    
    fun hasPrevGameEnded(): Boolean {
        gameModel?.let { 
            return it.gameEnd()
        }
        return true
    }

    fun getCurrentPlayer(): Player? {
        return gameModel?.getCurrentPlayer()
    }

    fun rollDice() {
        if (_isMoving.value ?: true) {
            return
        }

        val num = dice.roll(currentPlayer.value!!)
        handleRollResult(num)
    }

    fun rollDiceCustom(num: Int) {
        if (_isMoving.value ?: true) {
            return
        }
        handleRollResult(num)
    }

    private fun handleRollResult(num: Int) {
        if (num == 0) {
            _playerChanceCut.value = true
            moveNextPlayer()
            return
        }

        _diceVal.value = num

        _isMoving.value = true

        val currentPlayer = gameModel?.getCurrentPlayer() ?: return
        val movablePawns = currentPlayer.canMove(num)
        if (movablePawns.isEmpty()) {
            viewModelScope.launch {
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 500L)
                moveNextPlayer()
            }
            return
        }

        _selectablePawns.value = movablePawns

        if (movablePawns.size == 1) {
            movePawn(movablePawns[0])
        }
    }

    fun updateCurrentPlayer() {
        gameModel?.let {
            _currentPlayer.value = it.currentPlayer
        }
    }

    fun movePawn(pawn: Pawn) {
        if (!_selectablePawns.value!!.contains(pawn)) {
            return
        }

        val num = _diceVal.value!!
        val currentPlayer = gameModel?.getCurrentPlayer() ?: return
        val moves = currentPlayer.getNumberOfMoves(pawn, num)

        viewModelScope.launch {
            delay(500L)

            for (i in 1..<moves) {
                currentPlayer.moveOneUnit(pawn)
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 50L)
            }

            _cutPawnCount.value = currentPlayer.resolveNextCell(pawn)
            currentPlayer.moveOneUnit(pawn)

            val finalCell = pawn.cell.value!!

            if (finalCell.type == CellType.GOAL) {
                _pawnEnteredGoal.value = true
            } else if (finalCell.type == CellType.STAR && finalCell != currentPlayer.startCell) {
                _starCell.value = true
                _starCell.value = false
            }

            // short delay before next move
            delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS.toLong() + 100L)

            var shouldMoveToNextPlayer = true

            // if was able to cut pawn then player will get second chance
            if (_cutPawnCount.value!! > 0 && gameConstants.bonusChancePlayerCutActive) {
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
                _isMoving.value = false
            }
        }

        _selectablePawns.value = emptyList()
    }

    private fun moveNextPlayer() {
        _gameEnd.value = gameModel?.gameEnd()
        gameModel?.moveToNextPlayer()
        updateCurrentPlayer()
        _isMoving.postValue(false)
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel?.players ?: emptyArray()
    }
}