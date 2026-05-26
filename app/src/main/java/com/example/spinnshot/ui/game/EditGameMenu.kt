package com.example.spinnshot.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.Categories
import com.example.spinnshot.data.GameMode
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.GhostButton
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.NightDeep
import com.example.spinnshot.ui.theme.TextMuted
import com.example.spinnshot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGameMenu(
    open: Boolean,
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    onTerminate: () -> Unit
) {
    if (!open) return
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val game by viewModel.game.collectAsStateWithLifecycle()
    val state = game ?: return
    val isAdult by viewModel.isAdult.collectAsStateWithLifecycle()
    var newPlayer by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        containerColor = NightDeep,
        modifier = Modifier.testTag("edit-menu-sheet")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "Editar juego",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Los cambios se aplican sin reiniciar la partida.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Section(title = "Categorías") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Categories.available + Categories.ALL) { cat ->
                        val selected = state.categories.contains(cat)
                        Surface(
                            onClick = { viewModel.toggleCategory(cat); viewModel.editCategories(viewModel.selectedCategories.value) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) NeonFuchsia.copy(alpha = 0.18f) else NightCard,
                            border = BorderStroke(1.dp, if (selected) NeonFuchsia else BorderDim)
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (selected) NeonFuchsia else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Section(title = "Añadir jugador") {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPlayer,
                        onValueChange = { newPlayer = it.take(20) },
                        label = { Text("Nombre") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.editAddPlayer(newPlayer); newPlayer = ""
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonFuchsia,
                            cursorColor = NeonFuchsia
                        ),
                        modifier = Modifier.weight(1f).testTag("edit-new-player")
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    NeonGradientButton(
                        text = "Añadir",
                        onClick = { viewModel.editAddPlayer(newPlayer); newPlayer = "" },
                        testTag = "edit-add-player-btn"
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.players.joinToString { it.name },
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Section(title = "Rondas (actual: ${state.totalRounds})") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton(text = "+1 ronda", onClick = { viewModel.editExtendRounds(1) }, testTag = "extend-rounds-1")
                    GhostButton(text = "+3 rondas", onClick = { viewModel.editExtendRounds(3) }, testTag = "extend-rounds-3")
                }
            }

            Section(title = "Cambiar modo") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameMode.values().forEach { mode ->
                        val locked = mode.usesAlcohol && !isAdult
                        Surface(
                            onClick = { if (!locked) viewModel.editChangeMode(mode) },
                            enabled = !locked,
                            shape = RoundedCornerShape(16.dp),
                            color = if (state.mode == mode) NeonFuchsia.copy(alpha = 0.18f) else NightCard,
                            border = BorderStroke(1.dp, if (state.mode == mode) NeonFuchsia else BorderDim)
                        ) {
                            Text(
                                text = mode.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (state.mode == mode) NeonFuchsia else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            NeonGradientButton(
                text = "Terminar juego",
                onClick = onTerminate,
                modifier = Modifier.fillMaxWidth(),
                testTag = "terminate-btn"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = title.uppercase(),
        color = NeonFuchsia,
        style = MaterialTheme.typography.labelSmall
    )
    Spacer(modifier = Modifier.height(8.dp))
    content()
}
