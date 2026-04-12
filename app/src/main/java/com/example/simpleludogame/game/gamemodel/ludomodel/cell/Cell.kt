package com.example.simpleludogame.game.gamemodel.ludomodel.cell

import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn

class Cell(val row: Int, val col: Int) {
    var type: CellType = CellType.UNFILLED
    lateinit var next: Cell
    var pawns = mutableListOf<Pawn>()

    fun addPawn(pawn: Pawn) {
        if (pawns.size == 1 && pawns[0].player != pawn.player) {
            pawns[0].setCell(inHouseCell)
        }
        pawns.add(pawn)
    }

    fun removePawn(pawn: Pawn) {
        pawns.remove(pawn)
    }
}

val inHouseCell = Cell(-1, -1)