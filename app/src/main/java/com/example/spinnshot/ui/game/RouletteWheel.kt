package com.example.spinnshot.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.spinnshot.data.Player
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NightBlack
import com.example.spinnshot.ui.theme.TextPrimary
import com.example.spinnshot.ui.theme.rouletteGradient

/**
 * Draws the roulette wheel and animates the rotation when [spinTrigger] increments.
 * The wheel always lands such that [selectedIndex] is under the pin at the top.
 */
@Composable
fun RouletteWheel(
    players: List<Player>,
    spinTrigger: Int,
    selectedIndex: Int?,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(spinTrigger) {
        if (spinTrigger == 0 || selectedIndex == null || players.isEmpty()) return@LaunchedEffect
        val segment = 360f / players.size
        // We want the pin (top) to point at `selectedIndex`.
        // The center of segment i is at angle (i * segment) measured from 0 deg.
        // Initial drawing puts segment 0 at top, growing clockwise.
        val targetAngle = -selectedIndex * segment
        val current = rotation.value % 360f
        val base = rotation.value - current
        val extraSpins = 360f * 5
        rotation.snapTo(rotation.value)
        rotation.animateTo(
            targetValue = base + extraSpins + targetAngle,
            animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier.size(280.dp), contentAlignment = Alignment.Center) {
        val palette = rouletteGradient(players.size.coerceAtLeast(1))

        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            rotate(degrees = rotation.value, pivot = center) {
                val sweep = 360f / players.size.coerceAtLeast(1)
                players.forEachIndexed { idx, _ ->
                    val color = palette[idx % palette.size]
                    drawArc(
                        color = color,
                        startAngle = -90f + idx * sweep,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    drawArc(
                        color = NightBlack.copy(alpha = 0.4f),
                        startAngle = -90f + idx * sweep,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                // Player labels
                players.forEachIndexed { idx, player ->
                    val angle = -90f + idx * sweep + sweep / 2f
                    val rad = Math.toRadians(angle.toDouble())
                    val textRadius = radius * 0.62f
                    val x = center.x + (textRadius * kotlin.math.cos(rad)).toFloat()
                    val y = center.y + (textRadius * kotlin.math.sin(rad)).toFloat()
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = TextPrimary.toArgb()
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 12.dp.toPx()
                            isAntiAlias = true
                            isFakeBoldText = true
                        }
                        save()
                        rotate(angle + 90f, x, y)
                        drawText(player.name.take(10), x, y, paint)
                        restore()
                    }
                }
            }

            // Outer ring
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.Transparent, NeonFuchsia.copy(alpha = 0.45f)),
                    center = center,
                    radius = radius * 1.1f
                ),
                radius = radius * 1.04f,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner hub
            drawCircle(color = NightBlack, radius = radius * 0.18f, center = center)
            drawCircle(
                color = NeonFuchsia,
                radius = radius * 0.18f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Pin (pointer) at the top
            val pinPath = Path().apply {
                moveTo(center.x - 14.dp.toPx(), center.y - radius - 6.dp.toPx())
                lineTo(center.x + 14.dp.toPx(), center.y - radius - 6.dp.toPx())
                lineTo(center.x, center.y - radius + 18.dp.toPx())
                close()
            }
            drawPath(path = pinPath, color = NeonFuchsia)
        }
    }
}

// Use Compose's built-in toArgb() extension on Color.
