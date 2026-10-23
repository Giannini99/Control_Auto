package br.com.sensorauto.ui.screen.config.bubble

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.domain.model.Orientation
import br.com.sensorauto.domain.repository.ConfigRepository
import br.com.sensorauto.domain.usecase.ComputeOrientationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class BubbleLevelViewModel(
    private val sensorManager: SensorManager,
    private val configRepository: ConfigRepository,
    private val computeOrientation: ComputeOrientationUseCase
) : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow(BubbleLevelUiState())
    val uiState: StateFlow<BubbleLevelUiState> = _uiState.asStateFlow()

    private var mountOrientation = Orientation.VERTICAL
    private var rawPitch = 0f
    private var rawRoll = 0f

    init {
        viewModelScope.launch {
            val config = configRepository.load()
            mountOrientation = config.mountOrientation
        }
        registerSensor()
    }

    private fun registerSensor() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val (pitch, roll) = computeOrientation(x, y, z, mountOrientation)
            rawPitch = pitch
            rawRoll = roll

            val state = _uiState.value
            val adjustedPitch = pitch - state.offsetPitch
            val adjustedRoll = roll - state.offsetRoll
            val isLevel = abs(adjustedPitch) <= state.toleranceDeg && abs(adjustedRoll) <= state.toleranceDeg

            _uiState.update {
                it.copy(
                    pitchDeg = adjustedPitch,
                    rollDeg = adjustedRoll,
                    isLevel = isLevel
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    fun calibrate() {
        _uiState.update {
            it.copy(
                offsetPitch = rawPitch,
                offsetRoll = rawRoll,
                pitchDeg = 0f,
                rollDeg = 0f,
                isLevel = true
            )
        }
    }

    override fun onCleared() {
        sensorManager.unregisterListener(this)
    }
}
