package com.example.spinnshot.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val points: Int = 0,
    val shots: Int = 0
) {
    fun addPoints(delta: Int): Player = copy(points = points + delta)

    /** Shots can never go below zero. */
    fun addShots(delta: Int): Player = copy(shots = (shots + delta).coerceAtLeast(0))
}
