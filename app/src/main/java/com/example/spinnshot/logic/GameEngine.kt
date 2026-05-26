package com.example.spinnshot.logic

import com.example.spinnshot.data.GameState
import com.example.spinnshot.data.Player
import com.example.spinnshot.data.Question
import kotlin.random.Random

/**
 * High level orchestrator for the SpinnShot game lifecycle.
 *
 * The engine is intentionally stateless: every public method receives the
 * current [GameState] and returns a new one. State is owned by the
 * [com.example.spinnshot.ui.GameViewModel].
 */
object GameEngine {

    private fun questionKey(q: Question): String = "${q.categoria}|${q.pregunta}"

    /** Selects a new random question from [pool] respecting the "no repeats" rule. */
    fun pickQuestion(
        state: GameState,
        pool: List<Question>,
        random: Random = Random.Default
    ): Pair<Question?, Set<String>> {
        if (pool.isEmpty()) return null to state.usedQuestionKeys
        val unused = pool.filter { questionKey(it) !in state.usedQuestionKeys }
        val (chosen, newUsed) = when {
            unused.isNotEmpty() -> {
                val q = unused.random(random)
                q to state.usedQuestionKeys + questionKey(q)
            }
            else -> {
                // Pool exhausted - reshuffle (start a new cycle).
                val q = pool.random(random)
                q to setOf(questionKey(q))
            }
        }
        return chosen to newUsed
    }

    /** Performs a roulette spin and prepares the next question. */
    fun spin(
        state: GameState,
        pool: List<Question>,
        random: Random = Random.Default
    ): GameState {
        require(state.players.size >= 2) { "Mínimo 2 jugadores" }
        val spinner = state.players.getOrNull(state.currentSpinnerIndex) ?: state.players.first()
        val selected = TurnManager.pickAnsweringPlayer(state.players, state.currentSpinnerIndex, random)
        val (question, used) = pickQuestion(state, pool, random)
        return state.copy(
            lastSpinnerId = spinner.id,
            selectedPlayerId = selected.id,
            currentQuestion = question,
            usedQuestionKeys = used
        )
    }

    /** Applies the outcome to the selected player and advances the turn. */
    fun applyOutcomeAndAdvance(state: GameState, outcome: TurnOutcome): GameState {
        val selected = state.selectedPlayer() ?: return state
        val updated = ScoreCalculator.apply(selected, state.mode, outcome)
        val newPlayers = state.players.map { if (it.id == updated.id) updated else it }

        val (nextIdx, advancedRound) = TurnManager.nextSpinner(
            state.currentSpinnerIndex,
            newPlayers.size
        )
        val newRound = if (advancedRound) state.currentRound + 1 else state.currentRound
        val finished = newRound > state.totalRounds

        return state.copy(
            players = newPlayers,
            currentSpinnerIndex = nextIdx,
            currentRound = newRound.coerceAtMost(state.totalRounds),
            finished = finished,
            selectedPlayerId = null,
            currentQuestion = null
        )
    }

    /** Adds a new player at the end of the rotation - safe mid-game. */
    fun addPlayer(state: GameState, name: String): GameState {
        if (name.isBlank()) return state
        val player = Player(name = name.trim())
        return state.copy(players = state.players + player)
    }

    fun removePlayer(state: GameState, id: String): GameState {
        if (state.players.size <= 2) return state // never go below 2
        val newPlayers = state.players.filterNot { it.id == id }
        val newIdx = state.currentSpinnerIndex.coerceAtMost(newPlayers.size - 1)
        return state.copy(players = newPlayers, currentSpinnerIndex = newIdx)
    }

    fun updateCategories(state: GameState, categories: Set<String>): GameState =
        state.copy(categories = categories)

    fun updateMode(state: GameState, mode: com.example.spinnshot.data.GameMode): GameState =
        state.copy(mode = mode)

    fun extendRounds(state: GameState, delta: Int): GameState =
        state.copy(totalRounds = (state.totalRounds + delta).coerceAtLeast(state.currentRound))

    /** Generates a fresh game preserving config defaults. */
    fun startNew(
        players: List<Player>,
        categories: Set<String>,
        mode: com.example.spinnshot.data.GameMode,
        rounds: Int,
        random: Random = Random.Default
    ): GameState {
        val shuffled = TurnManager.shuffleInitialOrder(players, random)
        return GameState(
            players = shuffled,
            categories = categories,
            mode = mode,
            totalRounds = rounds,
            currentRound = 1,
            currentSpinnerIndex = 0
        )
    }
}
