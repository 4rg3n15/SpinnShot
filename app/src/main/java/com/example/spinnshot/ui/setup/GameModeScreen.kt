package com.example.spinnshot.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.GameMode
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.components.StepFooter
import com.example.spinnshot.ui.components.StepHeader
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonCyan
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.TextMuted
import com.example.spinnshot.ui.theme.TextSecondary

@Composable
fun GameModeScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val isAdult by viewModel.isAdult.collectAsStateWithLifecycle()
    val selected by viewModel.selectedMode.collectAsStateWithLifecycle()
    val modes = GameMode.values().toList()

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            StepHeader(
                kicker = "Paso 2 de 4",
                title = "Modo\nde juego",
                subtitle = if (!isAdult) "Eres menor de edad: los modos con alcohol están bloqueados."
                else "Cada modo tiene su propia tabla de penalizaciones."
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(modes) { mode ->
                    val locked = mode.usesAlcohol && !isAdult
                    ModeCard(
                        mode = mode,
                        selected = selected == mode,
                        locked = locked,
                        onClick = { if (!locked) viewModel.setMode(mode) }
                    )
                }
            }

            StepFooter(
                primaryLabel = "Siguiente",
                onPrimary = onNext,
                primaryEnabled = selected != null,
                secondaryLabel = "Atrás",
                onSecondary = onBack,
                primaryTag = "mode-next"
            )
        }
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    val border = when {
        locked -> BorderDim
        selected -> NeonFuchsia
        else -> BorderDim
    }
    Surface(
        onClick = onClick,
        enabled = !locked,
        shape = RoundedCornerShape(20.dp),
        color = NightCard,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth().testTag("mode-${mode.name}")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (mode.usesAlcohol) "CON ALCOHOL" else "SIN ALCOHOL",
                color = if (mode.usesAlcohol) NeonFuchsia else NeonCyan,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = mode.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (locked) {
                Text(
                    text = "🔒 Bloqueado por validación de edad",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
