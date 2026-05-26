package com.example.spinnshot.ui.question

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.Player
import com.example.spinnshot.data.Question
import com.example.spinnshot.logic.TurnOutcome
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.GhostButton
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonCyan
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NeonPurple
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.NightDeep
import com.example.spinnshot.ui.theme.TextMuted
import com.example.spinnshot.ui.theme.TextPrimary
import com.example.spinnshot.ui.theme.TextSecondary

private sealed class QStep {
    data object Question : QStep()
    data object Revealed : QStep()
    data class Result(val outcome: TurnOutcome, val title: String, val body: String) : QStep()
    /** Two-choice screen after answering wrong. */
    data class WrongChoice(val mode: GameMode) : QStep()
    /** Dare/Truth narrative card (cumple / no cumple). */
    data class Challenge(val outcomeIfCumple: TurnOutcome, val outcomeIfNoCumple: TurnOutcome, val title: String) : QStep()
}

@Composable
fun QuestionScreen(viewModel: GameViewModel, onComplete: () -> Unit) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val state = game ?: return
    val phrase by viewModel.phrase.collectAsStateWithLifecycle()

    var activeQuestion by remember { mutableStateOf(state.currentQuestion) }
    var activeSelected by remember { mutableStateOf(state.selectedPlayer()) }
    var activeSpinner by remember {
        mutableStateOf(state.playerById(state.lastSpinnerId) ?: state.players.firstOrNull())
    }

    LaunchedEffect(state.currentQuestion, state.selectedPlayerId, state.lastSpinnerId) {
        val question = state.currentQuestion
        val selected = state.selectedPlayer()
        if (question != null && selected != null) {
            activeQuestion = question
            activeSelected = selected
            activeSpinner = state.playerById(state.lastSpinnerId) ?: state.players.firstOrNull()
        }
    }

    val question = activeQuestion ?: return
    val selected = activeSelected ?: return
    val spinner = activeSpinner ?: selected

    var step by remember(question) { mutableStateOf<QStep>(QStep.Question) }

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(player = selected, phrase = phrase)

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = step) {
                QStep.Question -> Flashcard(
                    front = true,
                    title = question.categoria.uppercase(),
                    text = question.pregunta
                )
                QStep.Revealed -> Flashcard(
                    front = false,
                    title = "RESPUESTA",
                    text = question.respuesta
                )
                is QStep.Result -> ResultCard(s.title, s.body)
                is QStep.WrongChoice -> WrongChoiceCard(mode = s.mode) { outcome ->
                    handleWrongChoice(state.mode, outcome, spinner, selected, viewModel) { newStep ->
                        step = newStep
                    }
                }
                is QStep.Challenge -> ChallengeCard(
                    title = s.title,
                    onCumple = {
                        step = QStep.Result(
                            outcome = s.outcomeIfCumple,
                            title = "Cumplió ✨",
                            body = viewModel.describeOutcome(s.outcomeIfCumple)
                        )
                        viewModel.applyOutcome(s.outcomeIfCumple)
                    },
                    onNoCumple = {
                        step = QStep.Result(
                            outcome = s.outcomeIfNoCumple,
                            title = "No cumplió 😬",
                            body = viewModel.describeOutcome(s.outcomeIfNoCumple)
                        )
                        viewModel.applyOutcome(s.outcomeIfNoCumple)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                QStep.Question -> NeonGradientButton(
                    text = "Revelar",
                    onClick = { step = QStep.Revealed },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reveal-btn"
                )
                QStep.Revealed -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GhostButton(
                        text = "Incorrecta",
                        onClick = {
                            // Modes diverge: Verdad o Reto = +1 only on correct; wrong leads to choice
                            step = QStep.WrongChoice(state.mode)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "incorrect-btn"
                    )
                    NeonGradientButton(
                        text = "Correcta",
                        onClick = {
                            val outcome = TurnOutcome.Correct
                            step = QStep.Result(
                                outcome = outcome,
                                title = "¡Correcto! 🎉",
                                body = viewModel.describeOutcome(outcome)
                            )
                            viewModel.applyOutcome(outcome)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "correct-btn"
                    )
                }
                is QStep.Result -> NeonGradientButton(
                    text = "Siguiente",
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "next-after-result"
                )
                else -> Unit
            }
        }
    }
}

private fun handleWrongChoice(
    mode: GameMode,
    outcome: TurnOutcome,
    spinner: Player,
    selected: Player,
    viewModel: GameViewModel,
    setStep: (QStep) -> Unit
) {
    when (mode) {
        GameMode.SHOT_O_RETO -> when (outcome) {
            TurnOutcome.ShotOReto_Shot -> {
                setStep(QStep.Result(outcome, "Toma tu shot 🥃", viewModel.describeOutcome(outcome)))
                viewModel.applyOutcome(outcome)
            }
            else -> setStep(
                QStep.Challenge(
                    outcomeIfCumple = TurnOutcome.ShotOReto_RetoCumple,
                    outcomeIfNoCumple = TurnOutcome.ShotOReto_RetoNoCumple,
                    title = "${spinner.name} reta a ${selected.name}"
                )
            )
        }
        GameMode.VERDAD_O_RETO -> when (outcome) {
            TurnOutcome.VerdadOReto_VerdadCumple -> setStep(
                QStep.Challenge(
                    outcomeIfCumple = TurnOutcome.VerdadOReto_VerdadCumple,
                    outcomeIfNoCumple = TurnOutcome.VerdadOReto_VerdadNoCumple,
                    title = "${spinner.name} pregunta a ${selected.name}"
                )
            )
            TurnOutcome.VerdadOReto_RetoCumple -> setStep(
                QStep.Challenge(
                    outcomeIfCumple = TurnOutcome.VerdadOReto_RetoCumple,
                    outcomeIfNoCumple = TurnOutcome.VerdadOReto_RetoNoCumple,
                    title = "${spinner.name} reta a ${selected.name}"
                )
            )
            else -> Unit
        }
        GameMode.VERDAD_O_SHOT -> when (outcome) {
            TurnOutcome.VerdadOShot_Shot -> {
                setStep(QStep.Result(outcome, "Toma tu shot 🥃", viewModel.describeOutcome(outcome)))
                viewModel.applyOutcome(outcome)
            }
            else -> setStep(
                QStep.Challenge(
                    outcomeIfCumple = TurnOutcome.VerdadOShot_VerdadCumple,
                    outcomeIfNoCumple = TurnOutcome.VerdadOShot_VerdadNoCumple,
                    title = "${spinner.name} pregunta a ${selected.name}"
                )
            )
        }
    }
}

@Composable
private fun Header(player: Player, phrase: String) {
    Column {
        Text(text = "TURNO DE", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
        Text(
            text = player.name,
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = phrase,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun Flashcard(front: Boolean, title: String, text: String) {
    val rotation by animateFloatAsState(
        targetValue = if (front) 0f else 180f,
        animationSpec = tween(420),
        label = "flashcard"
    )
    val displayBack = rotation > 90f

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (displayBack) NightCard else NightDeep,
        border = BorderStroke(1.dp, if (displayBack) NeonFuchsia else BorderDim),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12 * density }
            .testTag(if (front) "flashcard-front" else "flashcard-back")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        if (displayBack)
                            listOf(NightCard, NeonPurple.copy(alpha = 0.18f))
                        else
                            listOf(NightDeep, NightCard)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = if (displayBack) NeonFuchsia else NeonPurple,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { rotationY = if (displayBack) 180f else 0f }
            )
        }
    }
}

@Composable
private fun ResultCard(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.25f), NightCard))
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = body, style = MaterialTheme.typography.titleMedium, color = NeonFuchsia, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WrongChoiceCard(mode: GameMode, onChoose: (TurnOutcome) -> Unit) {
    Column {
        Text(
            text = "Elige tu castigo",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        when (mode) {
            GameMode.SHOT_O_RETO -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    title = "Shot",
                    subtitle = "-1 punto · +1 shot",
                    onClick = { onChoose(TurnOutcome.ShotOReto_Shot) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-shot"
                )
                ChoiceCard(
                    title = "Reto",
                    subtitle = "Acepta el desafío",
                    onClick = { onChoose(TurnOutcome.ShotOReto_RetoCumple) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-reto"
                )
            }
            GameMode.VERDAD_O_RETO -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    title = "Verdad",
                    subtitle = "-2 puntos",
                    onClick = { onChoose(TurnOutcome.VerdadOReto_VerdadCumple) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-verdad"
                )
                ChoiceCard(
                    title = "Reto",
                    subtitle = "-1 punto",
                    onClick = { onChoose(TurnOutcome.VerdadOReto_RetoCumple) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-reto"
                )
            }
            GameMode.VERDAD_O_SHOT -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    title = "Shot",
                    subtitle = "-1 punto · +1 shot",
                    onClick = { onChoose(TurnOutcome.VerdadOShot_Shot) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-shot"
                )
                ChoiceCard(
                    title = "Verdad",
                    subtitle = "Confiesa algo",
                    onClick = { onChoose(TurnOutcome.VerdadOShot_VerdadCumple) },
                    modifier = Modifier.weight(1f),
                    tag = "wrong-verdad"
                )
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = NightCard,
        border = BorderStroke(1.dp, BorderDim),
        modifier = modifier.height(160.dp).testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(NightCard, NeonPurple.copy(alpha = 0.2f))))
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title.uppercase(), color = NeonFuchsia, style = MaterialTheme.typography.labelSmall)
            Column {
                Text(text = title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Black)
                Text(text = subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    title: String,
    onCumple: () -> Unit,
    onNoCumple: () -> Unit
) {
    Column {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NightCard,
            border = BorderStroke(1.dp, NeonFuchsia),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(NightCard, NeonPurple.copy(alpha = 0.3f))))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                text = "No cumple",
                onClick = onNoCumple,
                modifier = Modifier.weight(1f),
                testTag = "no-cumple-btn"
            )
            NeonGradientButton(
                text = "Cumple",
                onClick = onCumple,
                modifier = Modifier.weight(1f),
                testTag = "cumple-btn"
            )
        }
    }
}
