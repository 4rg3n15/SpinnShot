package com.example.spinnshot.logic

import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WinnerResolverTest {

    @Test
    fun `winner with more points`() {
        val players = listOf(
            Player(name = "A", points = 5, shots = 2),
            Player(name = "B", points = 3, shots = 1)
        )
        val r = WinnerResolver.resolve(players, GameMode.SHOT_O_RETO)
        assertEquals("A", r.winner?.name)
    }

    @Test
    fun `tiebreak in shot or reto picks fewer shots`() {
        val players = listOf(
            Player(name = "A", points = 5, shots = 5),
            Player(name = "B", points = 5, shots = 1)
        )
        val r = WinnerResolver.resolve(players, GameMode.SHOT_O_RETO)
        assertEquals("B", r.winner?.name)
    }

    @Test
    fun `verdad o reto ties trigger tie break`() {
        val players = listOf(
            Player(name = "A", points = 4),
            Player(name = "B", points = 4)
        )
        val r = WinnerResolver.resolve(players, GameMode.VERDAD_O_RETO)
        assertTrue(r.needsTieBreak)
        assertNull(r.winner)
    }
}
