package com.example.spinnshot.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.Player
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonCyan
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NeonPurple
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.TextMuted
import com.example.spinnshot.ui.theme.TextPrimary
import com.example.spinnshot.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onAskQuestion: () -> Unit,
    onTerminate: () -> Unit,
    onFinished: () -> Unit
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val state = game ?: return

    var spinTrigger by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectedIdx = state.selectedPlayerId?.let { id ->
        state.players.indexOfFirst { it.id == id }
    }?.takeIf { it >= 0 }

    // Trigger animation when a new selectedPlayerId is set.
    LaunchedEffect(state.selectedPlayerId) {
        if (state.selectedPlayerId != null) {
            spinTrigger++
            // Wait for spin animation, then navigate to question screen.
            delay(3000)
            onAskQuestion()
        }
    }

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("RONDA ACTUAL", color = NeonPurple, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${state.currentRound} / ${state.totalRounds}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.testTag("game-menu-btn")
                ) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Editar juego", tint = NeonFuchsia)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                RouletteWheel(
                    players = state.players,
                    spinTrigger = spinTrigger,
                    selectedIndex = selectedIdx
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current turn info
            val spinner = state.currentSpinner()
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NightCard,
                border = BorderStroke(1.dp, BorderDim),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("TURNO ACTUAL", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = spinner?.name.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Modo: ${state.mode.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            NeonGradientButton(
                text = "Girar",
                onClick = {
                    if (state.selectedPlayerId == null) {
                        viewModel.spin()
                    }
                },
                enabled = state.selectedPlayerId == null,
                modifier = Modifier.fillMaxWidth(),
                testTag = "spin-btn"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MARCADOR",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.players, key = { it.id }) { p ->
                    ScoreRow(
                        player = p,
                        isSpinner = p.id == spinner?.id
                    )
                }
            }
        }
    }

    EditGameMenu(
        open = menuOpen,
        viewModel = viewModel,
        onDismiss = { menuOpen = false },
        onTerminate = {
            menuOpen = false
            onTerminate()
        }
    )
}

@Composable
private fun ScoreRow(player: Player, isSpinner: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NightCard,
        border = BorderStroke(1.dp, if (isSpinner) NeonFuchsia else BorderDim)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    color = NeonFuchsia
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = player.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            ChipStat(value = player.points.toString(), label = "pts", tint = NeonFuchsia)
            Spacer(modifier = Modifier.size(8.dp))
            ChipStat(value = player.shots.toString(), label = "shots", tint = NeonCyan)
        }
    }
}

@Composable
private fun ChipStat(value: String, label: String, tint: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(value, color = tint, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}
