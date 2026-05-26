package com.example.spinnshot.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Night party palette - purples, fuchsias, neon accents
val NightBlack = Color(0xFF07000F)
val NightDeep = Color(0xFF110024)
val NightCard = Color(0xFF1A0033)
val NightSurface = Color(0xFF22043F)

val NeonPurple = Color(0xFFA855F7)
val NeonFuchsia = Color(0xFFE879F9)
val NeonPink = Color(0xFFF0ABFC)
val NeonCyan = Color(0xFF22D3EE)
val NeonViolet = Color(0xFF6366F1)

val TextPrimary = Color(0xFFF5F3FF)
val TextSecondary = Color(0xFFC4B5FD)
val TextMuted = Color(0xFF8B7AB8)

val BorderDim = Color(0x40A855F7)

fun nightGradient() = Brush.verticalGradient(
    colors = listOf(NightBlack, NightDeep, NightBlack)
)

fun neonButtonGradient() = Brush.horizontalGradient(
    colors = listOf(NeonPurple, NeonFuchsia)
)

fun rouletteGradient(playerCount: Int): List<Color> {
    val base = listOf(NeonPurple, NeonFuchsia, NeonViolet, NeonCyan, NeonPink, NightSurface)
    return List(playerCount) { base[it % base.size] }
}
