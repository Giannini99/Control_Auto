package br.com.sensorauto.ui.screen.config.equipment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import br.com.sensorauto.ui.theme.LocalThemePreferences
import br.com.sensorauto.domain.model.GearType
import br.com.sensorauto.domain.model.MotorType
import br.com.sensorauto.domain.model.Orientation
import br.com.sensorauto.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun EquipmentConfigScreen(
    viewModel: EquipmentConfigViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val config = state.config
    val appearance = LocalThemePreferences.current
    val effectiveDarkTheme = if (appearance.followSystem) isSystemInDarkTheme()
                             else appearance.darkTheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Configuração da Coleta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Defina os parâmetros de telemetria antes de iniciar a sessão.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = "Veículo & Suporte", content = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = config.vehicleBrand,
                    onValueChange = viewModel::updateVehicleBrand,
                    label = { Text("Marca") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = config.vehicleModel,
                    onValueChange = viewModel::updateVehicleModel,
                    label = { Text("Modelo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = config.vehicleYear,
                    onValueChange = viewModel::updateVehicleYear,
                    label = { Text("Ano") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = config.vehicleEngine,
                    onValueChange = viewModel::updateVehicleEngine,
                    label = { Text("Motorização") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Câmbio",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LabeledSwitch(
                        leftLabel = "Manual",
                        rightLabel = "Auto",
                        checked = config.gearType == GearType.AUTO,
                        onCheckedChange = { on ->
                            viewModel.updateGearType(if (on) GearType.AUTO else GearType.MANUAL)
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Motor",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LabeledSwitch(
                        leftLabel = "Comb.",
                        rightLabel = "Elét.",
                        checked = config.motorType == MotorType.ELECTRIC,
                        onCheckedChange = { on ->
                            viewModel.updateMotorType(if (on) MotorType.ELECTRIC else MotorType.COMBUSTION)
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Orientação do Suporte",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrientationCard(
                    label = "Vertical",
                    rotation = 0f,
                    isSelected = config.mountOrientation == Orientation.VERTICAL,
                    onClick = { viewModel.updateOrientation(Orientation.VERTICAL) },
                    modifier = Modifier.weight(1f)
                )
                OrientationCard(
                    label = "Horizontal",
                    rotation = 90f,
                    isSelected = config.mountOrientation == Orientation.HORIZONTAL,
                    onClick = { viewModel.updateOrientation(Orientation.HORIZONTAL) },
                    modifier = Modifier.weight(1f)
                )
            }
        })

        SectionCard(
            title = "Dispositivo Móvel",
            headerAction = {
                TextButton(onClick = viewModel::autoDetectDevice) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Auto-Detect", style = MaterialTheme.typography.labelSmall)
                }
            },
            content = {
                OutlinedTextField(
                    value = config.deviceBrand,
                    onValueChange = viewModel::updateDeviceBrand,
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.deviceModel,
                        onValueChange = viewModel::updateDeviceModel,
                        label = { Text("Modelo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = config.deviceYear,
                        onValueChange = viewModel::updateDeviceYear,
                        label = { Text("Ano") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        )

        SectionCard(title = "Aparência") {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = appearance.followSystem,
                    role = Role.Switch,
                    onValueChange = appearance::updateFollowSystem
                ).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Usar tema do celular", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (appearance.followSystem) "Tema do celular ativo"
                        else "Tema do celular desativado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = appearance.followSystem,
                    onCheckedChange = null
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = effectiveDarkTheme,
                    enabled = !appearance.followSystem,
                    role = Role.Switch,
                    onValueChange = appearance::updateDarkTheme
                ).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Tema escuro", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (appearance.followSystem) "Automático: seguindo o celular"
                        else if (effectiveDarkTheme) "Tema escuro ativo" else "Tema claro ativo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = effectiveDarkTheme,
                    onCheckedChange = null,
                    enabled = !appearance.followSystem
                )
            }
            Text(
                "A aparência é salva automaticamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { viewModel.save(onSuccess = {}) },
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Salvar", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LabeledSwitch(
    leftLabel: String,
    rightLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (!checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (!checked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = rightLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (checked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrientationCard(
    label: String,
    rotation: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}


