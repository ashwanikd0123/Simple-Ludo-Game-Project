package com.example.simpleludogame.game.gamemodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    val dice: Dice,
    val gameConstants: GameConstants
) : ViewModel() {
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

    fun initGame(playerCount: Int) {
        gameModel = GameModel(playerCount)

        dice.reset()

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

        viewModelScope.launch(Dispatchers.Default) {
            val currentPlayer = gameModel?.getCurrentPlayer() ?: return@launch
            val movablePawns = currentPlayer.canMove(num)
            if (movablePawns.isEmpty()) {
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS + 500L)
                moveNextPlayer()
                return@launch
            }

            withContext(Dispatchers.Main) {
                _selectablePawns.value = movablePawns

                if (movablePawns.size == 1) {
                    movePawn(movablePawns[0])
                }
            }
        }
    }

    fun updateCurrentPlayer() {
        gameModel?.let {
            _currentPlayer.postValue(it.currentPlayer)
        }
    }

    fun movePawn(pawn: Pawn) {
        if (_selectablePawns.value?.contains(pawn) != true) {
            return
        }

        val num = _diceVal.value ?: return
        val currentPlayer = gameModel?.getCurrentPlayer() ?: return

        _selectablePawns.value = emptyList()

        viewModelScope.launch(Dispatchers.Default) {
            val moves = currentPlayer.getNumberOfMoves(pawn, num)

            delay(500L)

            for (i in 1..<moves) {
                withContext(Dispatchers.Main) {
                    currentPlayer.moveOneUnit(pawn)
                }
                delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS + 50L)
            }

            var cutCount = 0
            withContext(Dispatchers.Main) {
                cutCount = currentPlayer.resolveNextCell(pawn)
                _cutPawnCount.value = cutCount
                currentPlayer.moveOneUnit(pawn)
            }

            val finalCell = pawn.getCell()

            if (finalCell?.type == CellType.GOAL) {
                _pawnEnteredGoal.postValue(true)
            } else if (finalCell?.type == CellType.STAR && finalCell != currentPlayer.startCell) {
                withContext(Dispatchers.Main) {
                    _starCell.value = true
                    _starCell.value = false
                }
            }

            // short delay before next move
            delay(LudoBoardForeGroundView.PAWN_MOVE_ANIMATION_DURATION_MS + 100L)

            var shouldMoveToNextPlayer = true

            // if was able to cut pawn then player will get second chance
            if (cutCount > 0 && gameConstants.bonusChancePlayerCutActive) {
                shouldMoveToNextPlayer = false
            }

            // when last number was 6 or pawn reached goal player gets second chance
            if (finalCell?.type == CellType.GOAL || num == 6) {
                shouldMoveToNextPlayer = false
            }

            // if player has won then move to next player
            if (currentPlayer.hasWon()) {
                playerRanking++
                withContext(Dispatchers.Main) {
                    when (playerRanking) {
                        1 -> currentPlayer.setStatus(PlayerStatus.RANK_1)
                        2 -> currentPlayer.setStatus(PlayerStatus.RANK_2)
                        3 -> currentPlayer.setStatus(PlayerStatus.RANK_3)
                        else -> currentPlayer.setStatus(PlayerStatus.LOSE)
                    }
                }
                shouldMoveToNextPlayer = true
            }

            if (shouldMoveToNextPlayer) {
                moveNextPlayer()
            } else {
                _isMoving.postValue(false)
            }
        }
    }
    
    private fun moveNextPlayer() {
        _gameEnd.postValue(gameModel?.gameEnd())
        gameModel?.moveToNextPlayer()
        updateCurrentPlayer()
        _isMoving.postValue(false)
    }

    fun getAllPlayers(): Array<Player> {
        return gameModel?.players ?: emptyArray()
    }
}