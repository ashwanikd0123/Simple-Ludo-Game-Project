package com.example.simpleludogame.game.gamemodel.ludomodel.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.Cell
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.CellType
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.inHouseCell
import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn

class Player(val colors: PlayerColors, val startCell: Cell, val invalidCell: Cell, val inSafeCell: Cell) {
    var pawns = Array<Pawn>(4) {
        Pawn(colors).apply {
            setCell(inHouseCell)
        }
    }

    private var _status = MutableLiveData<PlayerStatus>(PlayerStatus.PLAYING)
    var status: LiveData<PlayerStatus> = _status

    fun getStatus(): PlayerStatus {
        return status.value!!
    }

    fun setStatus(status: PlayerStatus) {
        _status.value = status
    }

    fun hasWon(): Boolean {
        for (pawn in pawns) {
            if (pawn.getCell()!!.type != CellType.GOAL) {
                return false
            }
        }
        return true
    }

    fun canMove(number: Int): List<Pawn> {
        val res = mutableListOf<Pawn>()

        for (pawn in pawns) {
            if (pawn.getCell()!!.type == CellType.GOAL) {
                continue
            }

            if (pawn.getCell() == inHouseCell && number == 6) {
                res.add(pawn)
                continue
            }

            if (isPossible(pawn, number)) {
                res.add(pawn)
            }
        }
        return res
    }

    fun getNumberOfMoves(pawn: Pawn, number: Int): Int {
        if (pawn.getCell() == inHouseCell && number == 6) {
            return 1
        }
        return number
    }

    fun isPossible(pawn: Pawn, number: Int): Boolean {
        pawn.getCell()?.let {
            if (it == inHouseCell) return false
            var curPos = it
            for (i in 1 .. number) {
                curPos = getNextCell(curPos)
                if (curPos.type == CellType.GOAL) {
                    return i == number
                }
            }
            return true
        }
        return false
    }

    fun getNextCell(cell: Cell): Cell {
        if (cell.next == invalidCell) {
            return inSafeCell
        }
        return cell.next
    }

    fun moveOneUnit(pawn: Pawn) {
        pawn.getCell()?.let { cell ->
            val next = if (cell == inHouseCell) startCell else getNextCell(cell)
            pawn.setCell(next)
        }
        updateStatus()
    }

    fun resolveNextCell(pawn: Pawn) {
        pawn.getCell()?.let { cell ->
            val next = if (cell == inHouseCell) startCell else getNextCell(cell)
            next.releaseAllPawns(pawn)
        }
    }

    fun updateStatus() {
        for (pawn in pawns) {
            if (pawn.getCell()!!.type != CellType.GOAL) {
                return
            }
        }
        _status.value = PlayerStatus.WON
    }
}