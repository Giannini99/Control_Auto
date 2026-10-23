package br.com.sensorauto.ui.screen.config.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.data.sensor.SensorChecker
import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.model.SensorType
import br.com.sensorauto.domain.repository.ConfigRepository
import br.com.sensorauto.domain.usecase.SaveConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SensorConfigViewModel(
    private val repository: ConfigRepository,
    private val saveConfigUseCase: SaveConfigUseCase,
    private val sensorChecker: SensorChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorConfigUiState())
    val uiState: StateFlow<SensorConfigUiState> = _uiState.asStateFlow()

    private var config = AppConfig()

    init {
        viewModelScope.launch {
            config = repository.load()
            refresh()
        }
    }

    private fun refresh() {
        _uiState.update { it.copy(sensors = buildItems()) }
    }

    private fun buildItems(): List<SensorConfigItem> = listOf(
        SensorConfigItem(
            type = SensorType.ACCEL,
            label = "Acelerômetro",
            description = "Captura variações de força ao longo dos 3 eixos. Essencial para calcular IRI.",
            isAvailable = sensorChecker.isAvailable(SensorType.ACCEL),
            isEnabled = config.accelEnabled,
            frequencyHz = config.accelHz,
            freqRange = 10..200
        ),
        SensorConfigItem(
            type = SensorType.GYRO,
            label = "Giroscópio",
            description = "Mede rotação angular. Complementa o acelerômetro na detecção de inclinações.",
            isAvailable = sensorChecker.isAvailable(SensorType.GYRO),
            isEnabled = config.gyroEnabled,
            frequencyHz = config.gyroHz,
            freqRange = 10..200
        ),
        SensorConfigItem(
            type = SensorType.GPS,
            label = "GPS",
            description = "Registra a posição geográfica ao longo do trajeto.",
            isAvailable = sensorChecker.isAvailable(SensorType.GPS),
            isEnabled = config.gpsEnabled,
            frequencyHz = config.gpsHz,
            freqRange = 1..10
        ),
        SensorConfigItem(
            type = SensorType.MAGNETO,
            label = "Magnetômetro",
            description = "Mede o campo magnético ambiente ao longo dos 3 eixos (bússola e orientação).",
            isAvailable = sensorChecker.isAvailable(SensorType.MAGNETO),
            isEnabled = config.magnetoEnabled,
            frequencyHz = config.magnetoHz,
            freqRange = 10..200
        ),
        SensorConfigItem(
            type = SensorType.CAMERA,
            label = "Câmera",
            description = "Tira fotos em intervalos regulares durante a inspeção para análise visual posterior.",
            isAvailable = sensorChecker.isAvailable(SensorType.CAMERA),
            isEnabled = config.cameraEnabled,
            frequencyHz = (config.cameraIntervalSeconds * 10).toInt().coerceIn(5..100),
            freqRange = 5..100,
            isIntervalSensor = true
        )
    )

    fun toggleSensor(type: SensorType, enabled: Boolean) {
        config = when (type) {
            SensorType.ACCEL   -> config.copy(accelEnabled = enabled)
            SensorType.GYRO    -> config.copy(gyroEnabled = enabled)
            SensorType.GPS     -> config.copy(gpsEnabled = enabled)
            SensorType.CAMERA  -> config.copy(cameraEnabled = enabled)
            SensorType.MAGNETO -> config.copy(magnetoEnabled = enabled)
        }
        refresh()
    }

    fun updateFrequency(type: SensorType, hz: Int) {
        config = when (type) {
            SensorType.ACCEL   -> config.copy(accelHz = hz)
            SensorType.GYRO    -> config.copy(gyroHz = hz)
            SensorType.GPS     -> config.copy(gpsHz = hz)
            SensorType.CAMERA  -> config.copy(cameraIntervalSeconds = hz / 10f)
            SensorType.MAGNETO -> config.copy(magnetoHz = hz)
        }
        refresh()
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            saveConfigUseCase(config)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
