package com.example.spinnshot.data

/** Game mode definitions. */
enum class GameMode(
    val displayName: String,
    val description: String,
    val usesAlcohol: Boolean
) {
    SHOT_O_RETO(
        displayName = "Shot o Reto",
        description = "Si fallas eliges: tomar un shot o aceptar un reto del jugador que giró.",
        usesAlcohol = true
    ),
    VERDAD_O_RETO(
        displayName = "Verdad o Reto",
        description = "Sin alcohol. Al fallar eliges entre decir una verdad o cumplir un reto.",
        usesAlcohol = false
    ),
    VERDAD_O_SHOT(
        displayName = "Verdad o Shot",
        description = "Si fallas eliges entre confesar una verdad o tomar un shot.",
        usesAlcohol = true
    );

    companion object {
        fun allowedForUser(isAdult: Boolean): List<GameMode> =
            if (isAdult) values().toList() else values().filter { !it.usesAlcohol }
    }
}
