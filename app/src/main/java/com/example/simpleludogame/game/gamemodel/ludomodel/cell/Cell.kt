package com.example.simpleludogame.game.gamemodel.ludomodel.cell

import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn

class Cell(val row: Int, val col: Int) {
    var type: CellType = CellType.UNFILLED
    lateinit var next: Cell
    var pawns = mutableListOf<Pawn>()

    fun addPawn(pawn: Pawn) {
        pawns.add(pawn)
    }

    fun removePawn(pawn: Pawn) {
        pawns.remove(pawn)
    }

    fun releaseAllPawns(except: Pawn) {
        if (type != CellType.NORMAL) {
            return
        }

        for (pawn in pawns.toList()) {
            if (pawn.player == except.player) {
                continue
            }
            pawn.setCell(inHouseCell)
        }
    }
}

val inHouseCell = Cell(-1, -1)