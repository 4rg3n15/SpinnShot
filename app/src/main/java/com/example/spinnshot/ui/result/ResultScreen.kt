package com.example.spinnshot.ui.result

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.Player
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.GhostButton
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

@Composable
fun ResultScreen(viewModel: GameViewModel, onNewGame: () -> Unit) {
    val resolution by viewModel.resolution.collectAsStateWithLifecycle()
    val game by viewModel.game.collectAsStateWithLifecycle()
    val res = resolution ?: return

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "GAME OVER", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "Ranking final",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (res.needsTieBreak) {
                TieBreakBanner(
                    candidates = res.ranking.filter { it.points == res.ranking.first().points },
                    onResolve = { viewModel.resolveTieBreak(it) }
                )
            } else {
                WinnerCard(winner = res.winner)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("ORDEN FINAL", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(res.ranking, key = { _, p -> p.id }) { idx, p ->
                    RankRow(position = idx + 1, player = p, isWinner = res.winner?.id == p.id)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton(
                    text = "Compartir",
                    onClick = { /* future: share intent */ },
                    modifier = Modifier.weight(1f),
                    testTag = "share-btn"
                )
                NeonGradientButton(
                    text = "Nueva partida",
                    onClick = onNewGame,
                    modifier = Modifier.weight(1f),
                    testTag = "new-game-btn"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Modo: ${game?.mode?.displayName.orEmpty()} · ${game?.totalRounds ?: 0} rondas · Resultados subidos a SpinnShot Cloud",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WinnerCard(winner: Player?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonFuchsia.copy(alpha = 0.2f))),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text("GANADOR", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
            Text(
                text = winner?.name ?: "—",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${winner?.points ?: 0} puntos · ${winner?.shots ?: 0} shots",
                color = TextSecondary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TieBreakBanner(candidates: List<Player>, onResolve: (Player) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = NightCard,
        border = BorderStroke(1.dp, NeonFuchsia),
        modifier = Modifier.fillMaxWidth().testTag("tie-break-banner")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("EMPATE", color = NeonFuchsia, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "Ronda de desempate",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Selecciona manualmente quién ganó el desempate después de jugar una ronda extra.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            candidates.forEach { c ->
                Surface(
                    onClick = { onResolve(c) },
                    shape = RoundedCornerShape(14.dp),
                    color = NeonPurple.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, BorderDim),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("tie-candidate-${c.name}")
                ) {
                    Text(
                        text = c.name,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RankRow(position: Int, player: Player, isWinner: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isWinner) NeonFuchsia.copy(alpha = 0.18f) else NightCard,
        border = BorderStroke(1.dp, if (isWinner) NeonFuchsia else BorderDim)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = position.toString(),
                color = if (isWinner) NeonFuchsia else TextMuted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(end = 14.dp)
            )
            Text(
                text = player.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "${player.points} pts",
                color = NeonFuchsia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = "${player.shots} shots",
                color = NeonCyan,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
