package com.example.spinnshot.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.TextSecondary

private data class OnboardingStep(
    val kicker: String,
    val title: String,
    val body: String,
    val cta: String
)

private val STEPS = listOf(
    OnboardingStep(
        kicker = "Capítulo 01",
        title = "Bienvenido a\nSpinnShot",
        body = "El juego social donde la ruleta decide quién brilla… y quién paga la ronda.",
        cta = "Siguiente"
    ),
    OnboardingStep(
        kicker = "Capítulo 02",
        title = "Diviértete\ny atrévete",
        body = "Preguntas, retos, verdades y shots. Pon a prueba a tus amigos sin perder el ritmo de la fiesta.",
        cta = "Siguiente"
    ),
    OnboardingStep(
        kicker = "Capítulo 03",
        title = "Empieza\nla fiesta",
        body = "Elige tu modo, gira la ruleta y deja que el destino haga lo suyo. Que comience SpinnShot.",
        cta = "Empezar"
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val step = STEPS[index]

    ScreenScaffold(contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Indicator(currentIndex = index, total = STEPS.size)

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut())
                },
                label = "onboarding-step"
            ) { current ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = current.kicker.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonFuchsia
                    )
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = current.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            NeonGradientButton(
                text = step.cta,
                onClick = {
                    if (index < STEPS.lastIndex) index++ else onFinished()
                },
                modifier = Modifier.fillMaxWidth().testTag("onboarding-cta"),
                testTag = "onboarding-cta-btn"
            )
        }
    }
}

@Composable
private fun Indicator(currentIndex: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val active = i == currentIndex
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .size(width = if (active) 36.dp else 18.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (active) NeonFuchsia else BorderDim)
            )
        }
    }
}
