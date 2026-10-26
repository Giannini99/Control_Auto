package br.com.sensorauto.ui.screen.config.bubble

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.sensorauto.ui.components.PrimaryButton
import br.com.sensorauto.ui.components.SectionCard
import br.com.sensorauto.ui.theme.GreenSuccess
import br.com.sensorauto.ui.theme.RedAlert
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleLevelScreen(
    onBack: () -> Unit,
    viewModel: BubbleLevelViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nível de Bolha", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LevelStatusBadge(isLevel = state.isLevel)

            Spacer(Modifier.height(4.dp))

            BubbleLevelWidget(
                pitchDeg = state.pitchDeg,
                rollDeg = state.rollDeg,
                isLevel = state.isLevel
            )

            Spacer(Modifier.height(4.dp))

            SectionCard(title = "Leitura dos Eixos") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AxisReadout(label = "Pitch (Horizontal)", value = state.pitchDeg)
                    AxisReadout(label = "Roll (Lateral)", value = state.rollDeg)
                }
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "Calibrar / Zerar Nível",
                onClick = { viewModel.calibrate() }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LevelStatusBadge(isLevel: Boolean) {
    val color = if (isLevel) GreenSuccess else RedAlert
    val icon = if (isLevel) Icons.Default.CheckCircle else Icons.Default.Warning
    val text = if (isLevel) "Dispositivo Nivelado" else "Ajuste a inclinação"

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AxisReadout(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "%.1f°".format(value),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
