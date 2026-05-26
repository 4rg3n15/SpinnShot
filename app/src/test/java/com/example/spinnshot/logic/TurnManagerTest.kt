package com.example.spinnshot.logic

import com.example.spinnshot.data.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TurnManagerTest {

    private val players = listOf(
        Player(name = "A"),
        Player(name = "B"),
        Player(name = "C"),
        Player(name = "D")
    )

    @Test
    fun `shuffle keeps every player`() {
        val shuffled = TurnManager.shuffleInitialOrder(players, Random(42))
        assertEquals(players.size, shuffled.size)
        assertEquals(players.map { it.name }.toSet(), shuffled.map { it.name }.toSet())
    }

    @Test
    fun `next spinner wraps and advances round`() {
        val (idx0, advance0) = TurnManager.nextSpinner(0, 4)
        assertEquals(1, idx0)
        assertFalse(advance0)

        val (idx3, advance3) = TurnManager.nextSpinner(3, 4)
        assertEquals(0, idx3)
        assertTrue(advance3)
    }

    @Test
    fun `picked player is never spinner when more than 2 players`() {
        repeat(40) { iter ->
            val spinnerIdx = iter % 4
            val picked = TurnManager.pickAnsweringPlayer(players, spinnerIdx, Random(iter.toLong()))
            assertNotEquals(players[spinnerIdx].name, picked.name)
        }
    }

    @Test
    fun `with two players picks the other`() {
        val two = players.take(2)
        val picked = TurnManager.pickAnsweringPlayer(two, 0, Random(1))
        assertEquals("B", picked.name)
    }
}
