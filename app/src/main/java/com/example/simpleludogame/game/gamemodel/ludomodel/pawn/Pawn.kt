package com.example.simpleludogame.game.gamemodel.ludomodel.pawn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.Cell
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors

class Pawn(val player: PlayerColors) {
    private var _cell: MutableLiveData<Cell> = MutableLiveData<Cell>()
    var cell: LiveData<Cell> = _cell

    fun getCell(): Cell? {
        return _cell.value
    }

    fun setCell(cell: Cell) {
        getCell()?.removePawn(this)
        cell.addPawn(this)
        _cell.value = cell
    }

    fun setWithoutRemoval(cell: Cell) {
        cell.addPawn(this)
        _cell.value = cell
    }
}