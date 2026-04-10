package com.example.simpleludogame.game.gamemodel.cell

class Cell(val row: Int, val col: Int) {
    var type: CellType = CellType.UNFILLED
    lateinit var next: Cell
}