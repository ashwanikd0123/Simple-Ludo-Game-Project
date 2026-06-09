package com.example.simpleludogame.game.gamemodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: GameViewModel
    private val dice: Dice = mock()
    private val gameConstants: GameConstants = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameConstants.bonusChancePlayerCutActive).thenReturn(true)
        viewModel = GameViewModel(dice, gameConstants)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initGame initializes state`() {
        viewModel.initGame(2)
        assertEquals(0, viewModel.currentPlayer.value)
        assertEquals(1, viewModel.diceVal.value)
        assertEquals(false, viewModel.isMoving.value)
        assertEquals(false, viewModel.gameEnd.value)
        assertEquals(0, viewModel.playerRanking)
    }

    @Test
    fun `test rollDice when not moving`() {
        viewModel.initGame(2)
        whenever(dice.roll(0)).thenReturn(6)
        
        viewModel.rollDice()
        
        verify(dice).roll(0)
        assertEquals(6, viewModel.diceVal.value)
        assertTrue(viewModel.isMoving.value!!)
    }

    @Test
    fun `test rollDice when already moving does nothing`() {
        viewModel.initGame(2)
        whenever(dice.roll(0)).thenReturn(6)
        
        // Force isMoving to true by rolling once
        viewModel.rollDice()
        
        // Try to roll again
        viewModel.rollDice()
        
        // Verify dice.roll was only called once
        verify(dice, times(1)).roll(0)
    }

    @Test
    fun `test handleRollResult with 0 moves next player`() {
        viewModel.initGame(2)
        whenever(dice.roll(0)).thenReturn(0)
        
        viewModel.rollDice()
        
        assertEquals(1, viewModel.currentPlayer.value)
        assertTrue(viewModel.playerChanceCut.value!!)
    }

    @Test
    fun `test updateCurrentPlayer`() {
        viewModel.initGame(4)
        // Manually move next player (internally) and update
        // Since gameModel is private, we rely on public methods
        whenever(dice.roll(0)).thenReturn(0)
        viewModel.rollDice()
        
        assertEquals(1, viewModel.currentPlayer.value)
        
        viewModel.updateCurrentPlayer()
        assertEquals(1, viewModel.currentPlayer.value)
    }

    @Test
    fun `test handleRollResult with no movable pawns moves next player`() = runTest {
        viewModel.initGame(2)
        // Green player (index 0) has all pawns in house, needs a 6 to move.
        // Roll a 2
        whenever(dice.roll(0)).thenReturn(2)
        
        viewModel.rollDice()
        
        // Wait for the delay in handleRollResult
        advanceUntilIdle()
        
        assertEquals(1, viewModel.currentPlayer.value)
    }

    @Test
    fun `test handleRollResult with single movable pawn auto moves`() = runTest {
        viewModel.initGame(2)
        // Give player 0 a pawn at start position so it can move with any number
        val player = viewModel.getCurrentPlayer()!!
        val pawn = player.pawns[0]
        player.moveOneUnit(pawn) // move from house to start
        
        whenever(dice.roll(0)).thenReturn(1)
        
        viewModel.rollDice()
        
        // It should auto-call movePawn(pawn)
        // movePawn has its own delays
        advanceUntilIdle()
        
        // Check if pawn moved (at least once)
        // After movePawn completes, it might move to next player or stay if bonus
        // Roll was 1, so should move to next player
        assertEquals(1, viewModel.currentPlayer.value)
    }

    @Test
    fun `test movePawn with bonus chance on 6`() = runTest {
        viewModel.initGame(2)
        val player = viewModel.getCurrentPlayer()!!
        val pawn = player.pawns[0]
        player.moveOneUnit(pawn) // move from house to start
        
        whenever(dice.roll(0)).thenReturn(6)
        viewModel.rollDice()
        
        advanceUntilIdle()
        
        // Should STILL be player 0 because 6 gives bonus chance
        assertEquals(0, viewModel.currentPlayer.value)
    }

    @Test
    fun `test hasPrevGameEnded returns correct status`() {
        viewModel.initGame(2)
        assertFalse(viewModel.hasPrevGameEnded())
        
        // Win the game for player 0
        val players = viewModel.getAllPlayers()
        players[0].setStatus(PlayerStatus.WON)
        // Only one player left playing (player 1) -> Game ends
        assertTrue(viewModel.hasPrevGameEnded())
    }

    @Test
    fun `test getAllPlayers returns correct size`() {
        viewModel.initGame(3)
        val players = viewModel.getAllPlayers()
        assertEquals(3, players.size)
    }
}
