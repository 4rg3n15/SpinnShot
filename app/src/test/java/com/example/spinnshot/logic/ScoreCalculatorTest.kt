package com.example.spinnshot.logic

import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    private val base = Player(name = "Test", points = 5, shots = 2)

    @Test
    fun `correct in shot or reto adds point removes shot`() {
        val r = ScoreCalculator.apply(base, GameMode.SHOT_O_RETO, TurnOutcome.Correct)
        assertEquals(6, r.points)
        assertEquals(1, r.shots)
    }

    @Test
    fun `shots never go negative on correct answer`() {
        val zero = base.copy(shots = 0)
        val r = ScoreCalculator.apply(zero, GameMode.SHOT_O_RETO, TurnOutcome.Correct)
        assertEquals(0, r.shots)
        assertEquals(6, r.points)
    }

    @Test
    fun `shot punishment adds shot and removes point`() {
        val r = ScoreCalculator.apply(base, GameMode.SHOT_O_RETO, TurnOutcome.ShotOReto_Shot)
        assertEquals(4, r.points)
        assertEquals(3, r.shots)
    }

    @Test
    fun `reto no cumple doubles penalty`() {
        val r = ScoreCalculator.apply(base, GameMode.SHOT_O_RETO, TurnOutcome.ShotOReto_RetoNoCumple)
        assertEquals(3, r.points)
        assertEquals(4, r.shots)
    }

    @Test
    fun `verdad o reto correct adds only point`() {
        val r = ScoreCalculator.apply(base, GameMode.VERDAD_O_RETO, TurnOutcome.Correct)
        assertEquals(6, r.points)
        assertEquals(2, r.shots)  // shots unchanged in non-alcohol mode
    }

    @Test
    fun `verdad o reto reto no cumple removes 3 points`() {
        val r = ScoreCalculator.apply(base, GameMode.VERDAD_O_RETO, TurnOutcome.VerdadOReto_RetoNoCumple)
        assertEquals(2, r.points)
    }

    @Test
    fun `verdad o shot verdad no cumple doubles`() {
        val r = ScoreCalculator.apply(base, GameMode.VERDAD_O_SHOT, TurnOutcome.VerdadOShot_VerdadNoCumple)
        assertEquals(3, r.points)
        assertEquals(4, r.shots)
    }
}
