package com.example.spinnshot.logic

import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.Player

/** Possible outcomes within a single question turn. */
sealed class TurnOutcome {
    /** Correct answer flow. */
    data object Correct : TurnOutcome()

    // ---- Shot o Reto ----
    data object ShotOReto_Shot : TurnOutcome()                 // player chose Shot
    data object ShotOReto_RetoCumple : TurnOutcome()          // chose Reto, cumplió
    data object ShotOReto_RetoNoCumple : TurnOutcome()        // chose Reto, no cumplió

    // ---- Verdad o Reto ----
    data object VerdadOReto_VerdadCumple : TurnOutcome()
    data object VerdadOReto_VerdadNoCumple : TurnOutcome()
    data object VerdadOReto_RetoCumple : TurnOutcome()
    data object VerdadOReto_RetoNoCumple : TurnOutcome()

    // ---- Verdad o Shot ----
    data object VerdadOShot_Shot : TurnOutcome()
    data object VerdadOShot_VerdadCumple : TurnOutcome()
    data object VerdadOShot_VerdadNoCumple : TurnOutcome()
}

/** Pure score arithmetic - no UI / state outside [Player]. */
object ScoreCalculator {

    /**
     * Applies the score delta for the [selected] player.
     * Shots cannot go below zero (enforced inside [Player]).
     */
    fun apply(selected: Player, mode: GameMode, outcome: TurnOutcome): Player {
        return when (mode) {
            GameMode.SHOT_O_RETO -> applyShotOReto(selected, outcome)
            GameMode.VERDAD_O_RETO -> applyVerdadOReto(selected, outcome)
            GameMode.VERDAD_O_SHOT -> applyVerdadOShot(selected, outcome)
        }
    }

    /** Human readable description of the points/shots delta for the result card. */
    fun describe(mode: GameMode, outcome: TurnOutcome): String = when (mode to outcome) {
        GameMode.SHOT_O_RETO to TurnOutcome.Correct -> "+1 punto · -1 shot"
        GameMode.SHOT_O_RETO to TurnOutcome.ShotOReto_Shot -> "-1 punto · +1 shot"
        GameMode.SHOT_O_RETO to TurnOutcome.ShotOReto_RetoCumple -> "-1 punto · -1 shot"
        GameMode.SHOT_O_RETO to TurnOutcome.ShotOReto_RetoNoCumple -> "Doble shot · -2 puntos · +2 shots"

        GameMode.VERDAD_O_RETO to TurnOutcome.Correct -> "+1 punto"
        GameMode.VERDAD_O_RETO to TurnOutcome.VerdadOReto_VerdadCumple -> "-2 puntos"
        GameMode.VERDAD_O_RETO to TurnOutcome.VerdadOReto_VerdadNoCumple -> "Debe hacer un reto · -2 puntos"
        GameMode.VERDAD_O_RETO to TurnOutcome.VerdadOReto_RetoCumple -> "-1 punto"
        GameMode.VERDAD_O_RETO to TurnOutcome.VerdadOReto_RetoNoCumple -> "Debe decir una verdad · -3 puntos"

        GameMode.VERDAD_O_SHOT to TurnOutcome.Correct -> "+1 punto · -1 shot"
        GameMode.VERDAD_O_SHOT to TurnOutcome.VerdadOShot_Shot -> "-1 punto · +1 shot"
        GameMode.VERDAD_O_SHOT to TurnOutcome.VerdadOShot_VerdadCumple -> "-1 punto · -1 shot"
        GameMode.VERDAD_O_SHOT to TurnOutcome.VerdadOShot_VerdadNoCumple -> "Doble shot · -2 puntos · +2 shots"

        else -> ""
    }

    private fun applyShotOReto(p: Player, outcome: TurnOutcome): Player = when (outcome) {
        TurnOutcome.Correct -> p.addPoints(1).addShots(-1)
        TurnOutcome.ShotOReto_Shot -> p.addPoints(-1).addShots(1)
        TurnOutcome.ShotOReto_RetoCumple -> p.addPoints(-1).addShots(-1)
        TurnOutcome.ShotOReto_RetoNoCumple -> p.addPoints(-2).addShots(2)
        else -> p
    }

    private fun applyVerdadOReto(p: Player, outcome: TurnOutcome): Player = when (outcome) {
        TurnOutcome.Correct -> p.addPoints(1)
        TurnOutcome.VerdadOReto_VerdadCumple -> p.addPoints(-2)
        TurnOutcome.VerdadOReto_VerdadNoCumple -> p.addPoints(-2)
        TurnOutcome.VerdadOReto_RetoCumple -> p.addPoints(-1)
        TurnOutcome.VerdadOReto_RetoNoCumple -> p.addPoints(-3)
        else -> p
    }

    private fun applyVerdadOShot(p: Player, outcome: TurnOutcome): Player = when (outcome) {
        TurnOutcome.Correct -> p.addPoints(1).addShots(-1)
        TurnOutcome.VerdadOShot_Shot -> p.addPoints(-1).addShots(1)
        TurnOutcome.VerdadOShot_VerdadCumple -> p.addPoints(-1).addShots(-1)
        TurnOutcome.VerdadOShot_VerdadNoCumple -> p.addPoints(-2).addShots(2)
        else -> p
    }
}
