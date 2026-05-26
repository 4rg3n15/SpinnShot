package com.example.spinnshot.ui.agevalidation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.NeonGradientButton
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.components.StepHeader
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.TextMuted
import java.time.LocalDate
import java.time.Period

@Composable
fun AgeValidationScreen(
    viewModel: GameViewModel,
    onValidated: () -> Unit
) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val canValidate by remember {
        derivedStateOf {
            day.toIntOrNull() in 1..31 &&
                    month.toIntOrNull() in 1..12 &&
                    (year.toIntOrNull() ?: 0) in 1900..LocalDate.now().year
        }
    }

    ScreenScaffold(contentPadding = PaddingValues(24.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            StepHeader(
                kicker = "Verificación",
                title = "Indícanos\ntu edad",
                subtitle = "Necesitamos confirmar tu fecha de nacimiento. Los modos con alcohol están bloqueados para menores de 18."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateField(
                    value = day,
                    onChange = { if (it.length <= 2) day = it.filter(Char::isDigit) },
                    label = "Día",
                    modifier = Modifier.weight(1f),
                    tag = "age-day"
                )
                DateField(
                    value = month,
                    onChange = { if (it.length <= 2) month = it.filter(Char::isDigit) },
                    label = "Mes",
                    modifier = Modifier.weight(1f),
                    tag = "age-month"
                )
                DateField(
                    value = year,
                    onChange = { if (it.length <= 4) year = it.filter(Char::isDigit) },
                    label = "Año",
                    modifier = Modifier.weight(1.4f),
                    tag = "age-year"
                )
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message!!,
                    color = NeonFuchsia,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            NeonGradientButton(
                text = "Validar",
                onClick = {
                    val parsed = runCatching {
                        LocalDate.of(year.toInt(), month.toInt(), day.toInt())
                    }.getOrNull()
                    if (parsed == null) {
                        message = "Fecha inválida. Revisa los datos."
                        return@NeonGradientButton
                    }
                    val age = Period.between(parsed, LocalDate.now()).years
                    val adult = age >= 18
                    viewModel.setAdult(adult)
                    message = if (adult) {
                        "✅ Tienes $age años. Acceso completo."
                    } else {
                        "⚠️ Tienes $age años. Modos con alcohol bloqueados."
                    }
                    onValidated()
                },
                enabled = canValidate,
                modifier = Modifier.fillMaxWidth().testTag("validate-age-btn"),
                testTag = "validate-age-button"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tu fecha no se almacena. Sólo se usa para habilitar los modos.",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun DateField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonFuchsia,
            cursorColor = NeonFuchsia
        ),
        modifier = modifier.testTag(tag)
    )
}
