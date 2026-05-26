package com.example.spinnshot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NeonPurple
import com.example.spinnshot.ui.theme.NightBlack
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.TextMuted
import com.example.spinnshot.ui.theme.nightGradient

/** Page chrome - night gradient background + inset-aware padding. */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack)
            .background(nightGradient())
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
    }
}

@Composable
fun NeonGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(CircleShape)
            .background(
                if (enabled) com.example.spinnshot.ui.theme.neonButtonGradient()
                else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(BorderDim, BorderDim))
            )
            .let { if (testTag != null) it.testTagId(testTag) else it }
    ) {
        Text(
            text = text.uppercase(),
            color = NightBlack,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = modifier
            .heightIn(min = 52.dp)
            .let { if (testTag != null) it.testTagId(testTag) else it }
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val border = if (selected) NeonFuchsia else BorderDim
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NightCard),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        NightCard,
                        NightBlack
                    )
                )
            ),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun StepHeader(
    kicker: String,
    title: String,
    subtitle: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = kicker.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NeonPurple
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StepFooter(
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    primaryTag: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (secondaryLabel != null && onSecondary != null) {
            GhostButton(
                text = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.weight(1f)
            )
        }
        NeonGradientButton(
            text = primaryLabel,
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier.weight(if (secondaryLabel != null) 1.5f else 1f),
            testTag = primaryTag
        )
    }
}

@Composable
fun NeonDot(size: Int = 12) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(NeonFuchsia)
    )
}

@Composable
fun CenteredFinePrint(text: String) {
    Text(
        text = text,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
    )
}

private fun Modifier.testTagId(tag: String): Modifier =
    this.then(
        androidx.compose.ui.semantics.semantics {
            this.testTag = tag
        }
    )
