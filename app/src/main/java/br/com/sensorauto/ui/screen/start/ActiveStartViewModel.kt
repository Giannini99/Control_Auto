package br.com.sensorauto.ui.screen.start

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.data.local.csv.SensorCsvWriter
import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.ConfigRepository
import br.com.sensorauto.domain.usecase.SaveRecordingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val MAX_SAMPLES = 300
private const val TIMER_INTERVAL_MS = 50L
private const val BATTERY_POLL_MS = 10_000L

class ActiveStartViewModel(
    private val sensorManager: SensorManager,
    private val locationManager: LocationManager,
    private val batteryManager: BatteryManager,
    private val configRepository: ConfigRepository,
    private val saveRecording: SaveRecordingUseCase,
    private val csvWriter: SensorCsvWriter
) : ViewModel(), SensorEventListener, LocationListener {

    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var batteryJob: Job? = null
    private var lastLocation: Location? = null
    private var sessionCounter = 1
    private var deviceId: String = ""
    private var activeConfig: AppConfig? = null

    private val accelBuffer = ArrayDeque<FloatArray>(MAX_SAMPLES)
    private val gyroBuffer  = ArrayDeque<FloatArray>(MAX_SAMPLES)

    init {
        // Sensores em modo preview (Hz padrão UI)
        registerSensors()
        startBatteryPolling()
        viewModelScope.launch {
            val config = configRepository.load()
            deviceId = if (config.deviceBrand.isEmpty()) {
                "${Build.MANUFACTURER} ${Build.MODEL}".trim()
            } else {
                "${config.deviceBrand} ${config.deviceModel}".trim()
            }
        }
    }

    fun startSession(hasLocationPermission: Boolean = false) {
        viewModelScope.launch {
            val config = configRepository.load()
            activeConfig = config
            
            // Re-registra sensores com a frequência e habilitados do usuário
            registerSensors(config)
            
            val d = LocalDate.now()
            val name = "%02d%02d%04d-start-%02d".format(d.dayOfMonth, d.monthValue, d.year, sessionCounter)
            val startMs = System.currentTimeMillis()
            
            _uiState.update {
                it.copy(
                    sessionName = name,
                    status = StartStatus.RUNNING,
                    elapsedMs = 0L,
                    distanceKm = 0f,
                    speedKmh = 0f,
                    maxSpeedKmh = 0f,
                    dataPoints = 0,
                    sessionStartMs = startMs,
                    totalSpeedSum = 0f,
                    speedReadings = 0
                )
            }
            csvWriter.openSession(name, config)
            startTimer()
            if (hasLocationPermission) requestLocation(config)
        }
    }

    fun pauseSession() {
        _uiState.update { it.copy(status = StartStatus.PAUSED) }
        timerJob?.cancel()
    }

    fun resumeSession() {
        _uiState.update { it.copy(status = StartStatus.RUNNING) }
        startTimer()
    }

    fun stopSession() {
        timerJob?.cancel()
        locationManager.removeUpdates(this)
        lastLocation = null

        val state = _uiState.value
        if (state.sessionName.isNotEmpty()) {
            val avgSpeed = if (state.speedReadings > 0)
                state.totalSpeedSum / state.speedReadings else 0f

            viewModelScope.launch {
                saveRecording(
                    Recording(
                        id          = 0,
                        name        = state.sessionName,
                        dateMillis  = state.sessionStartMs,
                        distanceKm  = state.distanceKm,
                        durationMs  = state.elapsedMs,
                        avgSpeedKmh = avgSpeed,
                        maxSpeedKmh = state.maxSpeedKmh,
                        dataPoints  = state.dataPoints,
                        deviceId    = deviceId
                    )
                )
            }
        }
        csvWriter.closeSession()
        activeConfig = null
        sessionCounter++
        
        // Retorna sensores para modo preview (UI)
        registerSensors()
        
        _uiState.update {
            it.copy(
                sessionName = "",
                status = StartStatus.IDLE,
                elapsedMs = 0L,
                distanceKm = 0f,
                speedKmh = 0f,
                maxSpeedKmh = 0f,
                dataPoints = 0,
                sessionStartMs = 0L,
                totalSpeedSum = 0f,
                speedReadings = 0
            )
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_INTERVAL_MS)
                _uiState.update { it.copy(elapsedMs = it.elapsedMs + TIMER_INTERVAL_MS) }
            }
        }
    }

    private fun startBatteryPolling() {
        batteryJob = viewModelScope.launch {
            while (isActive) {
                val pct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                _uiState.update { it.copy(batteryPercent = pct) }
                delay(BATTERY_POLL_MS)
            }
        }
    }

    private fun registerSensors(config: AppConfig? = null) {
        sensorManager.unregisterListener(this)
        
        // Se config for null, estamos em modo PREVIEW (Hz da UI, todos ativos)
        val accelDelay = if (config != null) 1_000_000 / config.accelHz else SensorManager.SENSOR_DELAY_UI
        val gyroDelay = if (config != null) 1_000_000 / config.gyroHz else SensorManager.SENSOR_DELAY_UI
        val magnetoDelay = if (config != null) 1_000_000 / config.magnetoHz else SensorManager.SENSOR_DELAY_UI
        
        val accelEnabled = config?.accelEnabled ?: true
        val gyroEnabled = config?.gyroEnabled ?: true
        val magnetoEnabled = config?.magnetoEnabled ?: true

        if (accelEnabled) {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(this, it, accelDelay)
            }
        }
        if (gyroEnabled) {
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
                sensorManager.registerListener(this, it, gyroDelay)
            }
        }
        if (magnetoEnabled) {
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
                sensorManager.registerListener(this, it, magnetoDelay)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val ts = System.currentTimeMillis()
        val sample = floatArrayOf(event.values[0], event.values[1], event.values[2])
        val isRecording = _uiState.value.status == StartStatus.RUNNING

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                pushSample(accelBuffer, sample) { copy(accelSamples = accelBuffer.toList()) }
                // Só grava se estiver rodando E o sensor estiver habilitado na config ativa
                if (isRecording && (activeConfig?.accelEnabled != false)) {
                    csvWriter.writeAccel(ts, sample[0], sample[1], sample[2])
                    _uiState.update { it.copy(dataPoints = it.dataPoints + 1) }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                pushSample(gyroBuffer, sample) { copy(gyroSamples = gyroBuffer.toList()) }
                if (isRecording && (activeConfig?.gyroEnabled != false)) {
                    csvWriter.writeGyro(ts, sample[0], sample[1], sample[2])
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (isRecording && (activeConfig?.magnetoEnabled != false)) {
                    csvWriter.writeMagneto(ts, sample[0], sample[1], sample[2])
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun pushSample(
        buffer: ArrayDeque<FloatArray>,
        sample: FloatArray,
        update: StartUiState.() -> StartUiState
    ) {
        if (buffer.size >= MAX_SAMPLES) buffer.removeFirst()
        buffer.addLast(sample)
        _uiState.update { it.update() }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation(config: AppConfig? = null) {
        locationManager.removeUpdates(this)
        
        val enabled = config?.gpsEnabled ?: true
        if (enabled && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Converte Hz para milissegundos
            val interval = if (config != null) (1000 / config.gpsHz).toLong() else 1000L
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        val prev = lastLocation
        val addedKm = if (prev != null) prev.distanceTo(location) / 1000f else 0f
        lastLocation = location
        val speedKmh = location.speed * 3.6f
        val ts = System.currentTimeMillis()

        // Só grava no CSV se o GPS estiver habilitado na config da sessão
        if (_uiState.value.status == StartStatus.RUNNING && (activeConfig?.gpsEnabled != false)) {
            csvWriter.writeGps(ts, location.latitude, location.longitude, speedKmh)
        }

        _uiState.update {
            it.copy(
                distanceKm   = it.distanceKm + addedKm,
                speedKmh     = speedKmh,
                maxSpeedKmh  = maxOf(it.maxSpeedKmh, speedKmh),
                totalSpeedSum = it.totalSpeedSum + speedKmh,
                speedReadings = it.speedReadings + 1
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        batteryJob?.cancel()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        csvWriter.closeSession()
    }
}
