package com.example.simpleludogame.game.gamemodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class GameModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test initPlayers with 1 player`() {
        val gameModel = GameModel(1)
        assertEquals(1, gameModel.players.size)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.GREEN, gameModel.players[0].colors)
    }

    @Test
    fun `test initPlayers with 2 players`() {
        val gameModel = GameModel(2)
        assertEquals(2, gameModel.players.size)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.YELLOW, gameModel.players[0].colors)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.RED, gameModel.players[1].colors)
    }

    @Test
    fun `test initPlayers with 3 players`() {
        val gameModel = GameModel(3)
        assertEquals(3, gameModel.players.size)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.YELLOW, gameModel.players[0].colors)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.BLUE, gameModel.players[1].colors)
        assertEquals(com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors.RED, gameModel.players[2].colors)
    }

    @Test
    fun `test initPlayers with 4 players`() {
        val gameModel = GameModel(4)
        assertEquals(4, gameModel.players.size)
    }

    @Test
    fun `test initBoard link connection`() {
        val gameModel = GameModel(4)
        // Green's first path cell (6,0) should lead to (6,1)
        val cell60 = gameModel.board[6][0]
        val cell61 = gameModel.board[6][1]
        assertEquals(cell61, cell60.next)
    }

    @Test
    fun `test moveToNextPlayer basic`() {
        val gameModel = GameModel(4)
        gameModel.currentPlayer = 0
        val moved = gameModel.moveToNextPlayer()
        assertTrue(moved)
        assertEquals(1, gameModel.currentPlayer)
    }

    @Test
    fun `test moveToNextPlayer skipping non-playing players`() {
        val gameModel = GameModel(4)
        gameModel.currentPlayer = 0
        // Set player 1 to WON
        gameModel.players[1].setStatus(PlayerStatus.WON)
        
        val moved = gameModel.moveToNextPlayer()
        assertTrue(moved)
        // Should skip index 1 and go to 2
        assertEquals(2, gameModel.currentPlayer)
    }

    @Test
    fun `test gameEnd when only one player remains`() {
        val gameModel = GameModel(2)
        assertEquals(false, gameModel.gameEnd())
        
        gameModel.players[0].setStatus(PlayerStatus.WON)
        // Only player 1 (RED) is PLAYING
        assertTrue(gameModel.gameEnd())
    }

    @Test
    fun `test getCurrentPlayer returns correct player`() {
        val gameModel = GameModel(2)
        gameModel.currentPlayer = 1
        assertEquals(gameModel.players[1], gameModel.getCurrentPlayer())
    }

    @Test
    fun `test moveToNextPlayer with single player`() {
        val gameModel = GameModel(1)
        assertEquals(0, gameModel.currentPlayer)
        val moved = gameModel.moveToNextPlayer()
        assertTrue(moved)
        assertEquals(0, gameModel.currentPlayer)
    }
}
