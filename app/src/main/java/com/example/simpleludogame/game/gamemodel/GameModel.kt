package com.example.simpleludogame.game.gamemodel

import com.example.simpleludogame.game.gamemodel.cell.Cell
import com.example.simpleludogame.game.gamemodel.cell.CellType

class GameModel {
    var board = Array<Array<Cell>>(15) { row ->
        Array<Cell>(15) { col ->
            Cell(row, col)
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
}