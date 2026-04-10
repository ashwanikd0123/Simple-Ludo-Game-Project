package com.example.simpleludogame.gamemodel

class Cell(val row: Int, val col: Int) {
    var type: CellType = CellType.UNFILLED
    lateinit var next: Cell
}