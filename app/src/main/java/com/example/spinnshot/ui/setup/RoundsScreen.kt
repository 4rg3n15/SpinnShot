package com.example.spinnshot.ui.setup

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.components.StepFooter
import com.example.spinnshot.ui.components.StepHeader
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NeonPurple
import com.example.spinnshot.ui.theme.TextSecondary

@Composable
fun RoundsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    val rounds by viewModel.rounds.collectAsStateWithLifecycle()
    val available = viewModel.availableQuestionCount()

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            StepHeader(
                kicker = "Paso 4 de 4",
                title = "Cuántas\nrondas",
                subtitle = "Cada ronda significa que todos giran al menos una vez."
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = rounds.toString(),
                        color = NeonFuchsia,
                        fontWeight = FontWeight.Black,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize * 2.6f
                    )
                    Text(
                        text = "rondas",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepBtn(label = "−", onClick = { viewModel.setRounds(rounds - 1) }, tag = "rounds-minus")
                        Spacer(modifier = Modifier.size(28.dp))
                        StepBtn(label = "+", onClick = { viewModel.setRounds(rounds + 1) }, tag = "rounds-plus")
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "Banco actual: $available preguntas filtradas",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            StepFooter(
                primaryLabel = "Empezar partida",
                onPrimary = onStart,
                primaryEnabled = viewModel.canStartGame(),
                secondaryLabel = "Atrás",
                onSecondary = onBack,
                primaryTag = "start-game"
            )
        }
    }
}

@Composable
private fun StepBtn(label: String, onClick: () -> Unit, tag: String) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    listOf(NeonPurple.copy(alpha = 0.45f), BorderDim)
                )
            )
            .testTag(tag)
    ) {
        Text(text = label, color = NeonFuchsia, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineLarge)
    }
}
