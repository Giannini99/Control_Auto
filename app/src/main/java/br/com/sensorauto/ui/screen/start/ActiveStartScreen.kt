package br.com.sensorauto.ui.screen.start

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.sensorauto.ui.components.SensorChart
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveStartScreen(viewModel: ActiveStartViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        
        if (cameraGranted) {
            viewModel.setupCamera(context, lifecycleOwner)
        }
        
        viewModel.startSession(
            hasLocationPermission = locationGranted,
            hasCameraPermission = cameraGranted
        )
    }

    LaunchedEffect(Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            viewModel.setupCamera(context, lifecycleOwner)
        }
    }

    fun onStartClicked() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission && hasCameraPermission) {
            viewModel.startSession(hasLocationPermission = true, hasCameraPermission = true)
        } else {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "IDENTIFICAÇÃO DA SESSÃO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = state.sessionName.ifEmpty { "—" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TEMPO DECORRIDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = state.elapsedMs.toTimestamp(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryCell(
                        icon = Icons.Default.Route,
                        label = "Distância",
                        value = "%.1f".format(state.distanceKm),
                        unit = "km",
                        modifier = Modifier.weight(1f)
                    )
                    CellDivider()
                    TelemetryCell(
                        icon = Icons.Default.Speed,
                        label = "Velocidade",
                        value = "%.0f".format(state.speedKmh),
                        unit = "km/h",
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryCell(
                        icon = Icons.Default.Sensors,
                        label = "Acelerômetro",
                        value = state.accelSamples.lastOrNull()
                            ?.let { "%.2f".format(it[0]) } ?: "—",
                        unit = "m/s²",
                        modifier = Modifier.weight(1f)
                    )
                    CellDivider()
                    TelemetryCell(
                        icon = Icons.Default.BatteryFull,
                        label = "Bateria",
                        value = "${state.batteryPercent}",
                        unit = "%",
                        valueColor = if (state.batteryPercent < 20) Color(0xFFEB5757)
                                     else Color(0xFF27AE60),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                SensorChart(
                    title = "Dinâmica (Acelerômetro)",
                    icon = Icons.Default.StackedLineChart,
                    badge = "X / Y / Z",
                    series = listOf(
                        state.accelSamples.map { it[0] },
                        state.accelSamples.map { it[1] },
                        state.accelSamples.map { it[2] }
                    ),
                    seriesColors = listOf(
                        Color(0xFFF5A623),
                        Color(0xFFEB5757),
                        Color(0xFF27AE60)
                    )
                )

                HorizontalDivider()

                SensorChart(
                    title = "Rotação (Giroscópio)",
                    icon = Icons.Default.Cached,
                    badge = "Roll / Pitch / Yaw",
                    series = listOf(
                        state.gyroSamples.map { it[0] },
                        state.gyroSamples.map { it[1] },
                        state.gyroSamples.map { it[2] }
                    ),
                    seriesColors = listOf(
                        Color(0xFF00658A),
                        Color(0xFF27AE60),
                        Color(0xFF9C27B0)
                    )
                )
            }
        }

        HorizontalDivider()
        
        // Indicador de Câmera Ativa
        if (state.status == StartStatus.RUNNING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Captura de fotos ativa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.status) {
                StartStatus.IDLE -> {
                    Button(
                        onClick = ::onStartClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("INICIAR SESSÃO", style = MaterialTheme.typography.titleMedium)
                    }
                }
                StartStatus.RUNNING -> {
                    OutlinedButton(
                        onClick = viewModel::pauseSession,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("PAUSAR", style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = viewModel::stopSession,
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("FINALIZAR", style = MaterialTheme.typography.titleMedium)
                    }
                }
                StartStatus.PAUSED -> {
                    OutlinedButton(
                        onClick = viewModel::stopSession,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("FINALIZAR", style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = viewModel::resumeSession,
                        modifier = Modifier.weight(2f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("RETOMAR", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TelemetryCell(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun CellDivider() {
    Surface(
        modifier = Modifier
            .width(1.dp)
            .height(80.dp)
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    ) {}
}

private fun Long.toTimestamp(): String {
    val totalSeconds = this / 1000
    val centis = (this % 1000) / 10
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d.%02d".format(h, m, s, centis)
}
