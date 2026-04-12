package com.example.simpleludogame.game.gamemodel

import androidx.lifecycle.MutableLiveData
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.Cell
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.CellType
import com.example.simpleludogame.game.gamemodel.ludomodel.player.Player
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus

class GameModel(val playerCount: Int) {
    var board = Array<Array<Cell>>(15) { row ->
        Array<Cell>(15) { col ->
            Cell(row, col)
        }
    }

    private val greenPlayer = Player(PlayerColors.GREEN, board[6][1], board[6][0], board[7][1]); // upper left green
    private val yellowPlayer = Player(PlayerColors.YELLOW, board[1][8], board[0][8], board[1][7]); // upper right yellow
    private val bluePlayer = Player(PlayerColors.BLUE, board[8][13], board[8][14], board[7][13]); // lower right blue
    private val redPlayer = Player(PlayerColors.RED, board[13][6], board[14][6], board[13][7]); // lower left red

    init {
        initPlayers()
        initBoard()
    }

    lateinit var players: Array<Player>

    var currentPlayer = MutableLiveData<Int>(0)

    fun initPlayers() {
        when (playerCount) {
            1 -> players = arrayOf(
                    greenPlayer
                )
            2 -> players = arrayOf(
                    yellowPlayer,
                    redPlayer
                )
            3 -> players = arrayOf(
                    yellowPlayer,
                    bluePlayer,
                    redPlayer
                )
            else -> players = arrayOf(
                    greenPlayer,
                    yellowPlayer,
                    bluePlayer,
                    redPlayer
                )
        }
    }

    fun initBoard() {
        val mainPathCoords = mutableListOf<Pair<Int, Int>>()
        // Green's side to Yellow's side
        for (r in 6..6) for (c in 0..5) mainPathCoords.add(r to c)
        for (r in 5 downTo 0) mainPathCoords.add(r to 6)
        for (c in 7..8) mainPathCoords.add(0 to c)
        for (r in 1..5) mainPathCoords.add(r to 8)

        // Yellow's side to Blue's side
        for (c in 9..14) mainPathCoords.add(6 to c)
        for (r in 7..8) mainPathCoords.add(r to 14)
        for (c in 13 downTo 9) mainPathCoords.add(8 to c)

        // Blue's side to Red's side
        for (r in 9..14) mainPathCoords.add(r to 8)
        for (c in 7 downTo 6) mainPathCoords.add(14 to c)
        for (r in 13 downTo 9) mainPathCoords.add(r to 6)

        // Red's side back to Green's
        for (c in 5 downTo 0) mainPathCoords.add(8 to c)
        for (r in 7 downTo 7) mainPathCoords.add(r to 0) // Final link

        // Link the outer track
        for (i in 0 until mainPathCoords.size) {
            val current = mainPathCoords[i]
            val next = mainPathCoords[(i + 1) % mainPathCoords.size]
            board[current.first][current.second].next = board[next.first][next.second]
        }

        // Red Home (Left to Right)
        for (c in 1..5) {
            board[7][c].next = board[7][c + 1]
        }

        // Yellow Home (Top to Bottom)
        for (r in 1..5) {
            board[r][7].next = board[r + 1][7]
        }

        // Blue Home (Right to Left)
        for (c in 13 downTo 9) {
            board[7][c].next = board[7][c - 1]
        }

        // Green Home (Bottom to Top)
        for (r in 13 downTo 9) {
            board[r][7].next = board[r - 1][7]
        }

        for (r in 6..8) {
            for (c in 6..8) {
                board[r][c].type = CellType.GOAL
            }
        }

        for (r in 0..5) {
            for (c in 0..5) {
                board[r][c].type = CellType.INVALID
            }
        }

        for (r in 9..14) {
            for (c in 9..14) {
                board[r][c].type = CellType.INVALID
            }
        }

        for (r in 0..5) {
            for (c in 9..14) {
                board[r][c].type = CellType.INVALID
            }
        }

        for (r in 9..14) {
            for (c in 0..5) {
                board[r][c].type = CellType.INVALID
            }
        }

        for (r in 0..14) {
            for (c in 0..14) {
                if (board[r][c].type == CellType.UNFILLED) {
                    board[r][c].type = CellType.NORMAL;
                }
            }
        }

        board[6][1].type = CellType.STAR
        board[2][6].type = CellType.STAR
        board[1][8].type = CellType.STAR
        board[6][12].type = CellType.STAR
        board[8][13].type = CellType.STAR
        board[12][8].type = CellType.STAR
        board[13][6].type = CellType.STAR
        board[8][2].type = CellType.STAR
    }

    fun gameEnd(): Boolean {
        var playingCount = 0
        for (player in players) {
            if (player.getStatus() == PlayerStatus.PLAYING) {
                playingCount++
            }
        }
        return playingCount > 1
    }

    fun moveToNextPlayer(): Boolean {
        var curValue = (currentPlayer.value!! + 1) % players.size
        var count = 0
        while (players[curValue].getStatus() != PlayerStatus.PLAYING) {
            curValue = (curValue + 1) % players.size
            count++
            if (count == playerCount) {
                return false
            }
        }
        currentPlayer.value = curValue
        return true
    }

    fun getCurrentPlayer(): Player? {
        currentPlayer.value?.let { return players[it] }
        return null
    }
}