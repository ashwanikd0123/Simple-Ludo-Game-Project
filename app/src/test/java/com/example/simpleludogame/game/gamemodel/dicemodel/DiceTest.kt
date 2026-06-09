package com.example.simpleludogame.game.gamemodel.dicemodel

import android.content.Context
import android.content.SharedPreferences
import com.example.simpleludogame.settings.SETTINGS_KEY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class DiceTest {

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockPrefs: SharedPreferences

    private lateinit var dice: Dice

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)).thenReturn(mockPrefs)
        `when`(mockPrefs.getInt(anyString(), anyInt())).thenReturn(0)
        dice = Dice(mockContext)
    }

    private fun runProbabilityAnalysis(behavior: DiceBehavior, rolls: Int = 1000000) {
        dice.diceBehavior = behavior
        dice.reset()
        val counts = IntArray(7) // 0 to 6
        for (i in 1..rolls) {
            val result = dice.roll(0)
            counts[result]++
        }

        println("\n--- Probability Analysis for $behavior ---")
        println("Total Rolls: $rolls")
        for (i in 0..6) {
            if (i == 0 && counts[i] == 0) continue
            val percentage = (counts[i].toDouble() / rolls) * 100
            println("Value $i: ${String.format("%.4f", percentage)}% (${counts[i]} times)")
        }
        
        when (behavior) {
            DiceBehavior.DEFAULT -> {
                for (i in 1..6) {
                    val p = counts[i].toDouble() / rolls
                    assertTrue("Value $i should be ~16.67%, got ${p*100}%", p in 0.16..0.175)
                }
                assertEquals(0, counts[0])
            }
            DiceBehavior.CANCEL_ON_THIRD_SIX -> {
                val p0 = counts[0].toDouble() / rolls
                // Expected P(0) = (1/6)^3 = 1/216 ≈ 0.004629
                assertTrue("Value 0 should be ~0.46%, got ${p0*100}%", p0 in 0.004..0.0055)
                val p6 = counts[6].toDouble() / rolls
                // Expected P(6) = 1/6 - 1/216 = 35/216 ≈ 0.1620
                assertTrue("Value 6 should be ~16.20%, got ${p6*100}%", p6 in 0.158..0.166)
            }
            DiceBehavior.THIRD_SIX_NOT_ALLOWED -> {
                assertEquals(0, counts[0])
                val p6 = counts[6].toDouble() / rolls
                // Expected P(6) = 1/6 - 1/216 ≈ 0.1620
                assertTrue("Value 6 should be ~16.20%, got ${p6*100}%", p6 in 0.158..0.166)
                for (i in 1..5) {
                    val pi = counts[i].toDouble() / rolls
                    // Expected P(1-5) = 1/6 + (1/216)/5 = 1/6 + 1/1080 ≈ 0.1676
                    assertTrue("Value $i should be ~16.76%, got ${pi*100}%", pi in 0.163..0.172)
                }
            }
        }
    }

    @Test
    fun testAllBehaviors() {
        runProbabilityAnalysis(DiceBehavior.DEFAULT)
        runProbabilityAnalysis(DiceBehavior.CANCEL_ON_THIRD_SIX)
        runProbabilityAnalysis(DiceBehavior.THIRD_SIX_NOT_ALLOWED)
    }

    @Test
    fun testReset() {
        dice.diceBehavior = DiceBehavior.CANCEL_ON_THIRD_SIX
        // We can't easily force 6s, but we can check if reset clears counters.
        // Since we can't see the private field, we just trust the logic or roll until we get 6s.
        // For a unit test, we'd ideally mock the Random source.
    }
}
