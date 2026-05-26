package com.example.spinnshot.data

/** Snapshot of the whole game state. Immutable; replaced on every mutation. */
data class GameState(
    val players: List<Player> = emptyList(),
    val categories: Set<String> = emptySet(),
    val mode: GameMode = GameMode.SHOT_O_RETO,
    val totalRounds: Int = 5,
    val currentRound: Int = 1,
    /** Index in [players] pointing to whose turn it is to spin. */
    val currentSpinnerIndex: Int = 0,
    /** Player who spun in the latest action - kept for question screen context. */
    val lastSpinnerId: String? = null,
    /** Player selected by the roulette. */
    val selectedPlayerId: String? = null,
    val currentQuestion: Question? = null,
    val usedQuestionKeys: Set<String> = emptySet(),
    val finished: Boolean = false,
    /** When the players reached the tie-break in Verdad o Reto. */
    val tieBreak: Boolean = false
) {
    val isAdultRequired: Boolean get() = mode.usesAlcohol

    fun playerById(id: String?): Player? = players.firstOrNull { it.id == id }

    fun currentSpinner(): Player? = players.getOrNull(currentSpinnerIndex)

    fun selectedPlayer(): Player? = playerById(selectedPlayerId)

    fun ranking(): List<Player> =
        players.sortedWith(
            compareByDescending<Player> { it.points }
                .thenBy { it.shots }
                .thenBy { it.name }
        )
}
