package com.example.spinnshot.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.spinnshot.data.Categories
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.components.ScreenScaffold
import com.example.spinnshot.ui.components.StepFooter
import com.example.spinnshot.ui.components.StepHeader
import com.example.spinnshot.ui.theme.BorderDim
import com.example.spinnshot.ui.theme.NeonFuchsia
import com.example.spinnshot.ui.theme.NightCard
import com.example.spinnshot.ui.theme.TextMuted

@Composable
fun CategoriesScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val selected by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val items = Categories.available + Categories.ALL

    ScreenScaffold(contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            StepHeader(
                kicker = "Paso 1 de 4",
                title = "Elige las\ncategorías",
                subtitle = "Selecciona una o varias. Tocar 'Todas' activa todo el banco de preguntas."
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { category ->
                    CategoryChip(
                        label = category,
                        selected = selected.contains(category),
                        onClick = { viewModel.toggleCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${selected.count { it != Categories.ALL }} categorías seleccionadas",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            StepFooter(
                primaryLabel = "Siguiente",
                onPrimary = onNext,
                primaryEnabled = selected.isNotEmpty(),
                secondaryLabel = "Atrás",
                onSecondary = onBack,
                primaryTag = "categories-next"
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) NeonFuchsia.copy(alpha = 0.18f) else NightCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) NeonFuchsia else BorderDim
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .testTag("cat-$label")
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = if (selected) NeonFuchsia else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
