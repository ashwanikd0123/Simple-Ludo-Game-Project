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

    fun releaseAllPawns(except: Pawn): Int {
        if (type != CellType.NORMAL) {
            return 0
        }

        var res = 0
        for (pawn in pawns.toList()) {
            if (pawn.player == except.player) {
                continue
            }
            res++
            pawn.setCell(inHouseCell)
        }
        return res
    }
}

val inHouseCell = Cell(-1, -1)