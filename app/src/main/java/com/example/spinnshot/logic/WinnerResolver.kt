package com.example.spinnshot.logic

import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.Player

data class GameResolution(
    val ranking: List<Player>,
    val winner: Player?,
    val needsTieBreak: Boolean
)

/** Determines the winner respecting each mode's tie-breaking rules. */
object WinnerResolver {

    fun resolve(players: List<Player>, mode: GameMode): GameResolution {
        if (players.isEmpty()) {
            return GameResolution(emptyList(), null, needsTieBreak = false)
        }

        val ranking = when (mode) {
            GameMode.SHOT_O_RETO, GameMode.VERDAD_O_SHOT ->
                players.sortedWith(
                    compareByDescending<Player> { it.points }
                        .thenBy { it.shots }
                        .thenBy { it.name }
                )
            GameMode.VERDAD_O_RETO ->
                players.sortedWith(
                    compareByDescending<Player> { it.points }
                        .thenBy { it.name }
                )
        }

        val topPoints = ranking.first().points
        val topGroup = ranking.filter { it.points == topPoints }

        val needsTieBreak = mode == GameMode.VERDAD_O_RETO && topGroup.size > 1

        val winner: Player? = when {
            needsTieBreak -> null
            mode == GameMode.VERDAD_O_RETO -> ranking.first()
            else -> {
                // tie-break by fewer shots already applied via comparator.
                ranking.first()
            }
        }
        return GameResolution(ranking, winner, needsTieBreak)
    }
}
