package com.example.spinnshot.ui.setup

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.Player
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.components.StepFooter
import com.example.spinnshot.ui.components.StepHeader
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NeonPurple
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.TextMuted

@Composable
fun PlayersScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val players by viewModel.draftPlayers.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            StepHeader(
                kicker = "Paso 3 de 4",
                title = "Quiénes\njuegan",
                subtitle = "Mínimo 2 jugadores. Sin nombres vacíos ni repetidos."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    label = { Text("Nombre del jugador") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.addDraftPlayer(name); name = ""
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonFuchsia,
                        cursorColor = NeonFuchsia
                    ),
                    modifier = Modifier.weight(1f).testTag("player-name-input")
                )
                NeonGradientButton(
                    text = "+",
                    onClick = { viewModel.addDraftPlayer(name); name = "" },
                    testTag = "add-player-btn"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerRow(player, onRemove = { viewModel.removeDraftPlayer(player.id) })
                }
            }

            Text(
                text = "${players.size}/12 jugadores",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            StepFooter(
                primaryLabel = "Siguiente",
                onPrimary = onNext,
                primaryEnabled = players.size >= 2,
                secondaryLabel = "Atrás",
                onSecondary = onBack,
                primaryTag = "players-next"
            )
        }
    }
}

@Composable
private fun PlayerRow(player: Player, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NightCard,
        border = BorderStroke(1.dp, BorderDim),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .let { it }
            ) {
                Text(
                    text = player.name.take(1).uppercase(),
                    color = NeonPurple,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove, modifier = Modifier.testTag("remove-${player.name}")) {
                Icon(Icons.Outlined.Close, contentDescription = "Eliminar", tint = TextMuted)
            }
        }
    }
}
