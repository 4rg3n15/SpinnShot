package com.example.spinnshot.logic

import com.example.spinnshot.data.Player
import kotlin.random.Random

/** Encapsulates the spinning-turn rotation among the configured players. */
object TurnManager {

    /** Shuffles the initial player order with [random] for reproducibility in tests. */
    fun shuffleInitialOrder(players: List<Player>, random: Random = Random.Default): List<Player> {
        if (players.size <= 1) return players
        return players.shuffled(random)
    }

    /**
     * Selects which player will answer when the spinner is [spinnerIndex].
     * If more than 2 players, the spinner cannot be selected. With 2 players,
     * the other player is always picked. With a single player we return it
     * trivially.
     */
    fun pickAnsweringPlayer(
        players: List<Player>,
        spinnerIndex: Int,
        random: Random = Random.Default
    ): Player {
        if (players.isEmpty()) error("No players configured")
        if (players.size == 1) return players[0]
        val candidates = if (players.size == 2) {
            players.filterIndexed { idx, _ -> idx != spinnerIndex }
        } else {
            players.filterIndexed { idx, _ -> idx != spinnerIndex }
        }
        return candidates.random(random)
    }

    /**
     * Advances the spinner cursor. Returns a pair of
     * (newSpinnerIndex, advancedRound).
     * When the cursor wraps to 0, the round counter must be incremented by 1.
     */
    fun nextSpinner(currentIndex: Int, totalPlayers: Int): Pair<Int, Boolean> {
        if (totalPlayers <= 0) return 0 to false
        val next = (currentIndex + 1) % totalPlayers
        val advancedRound = next == 0
        return next to advancedRound
    }
}
