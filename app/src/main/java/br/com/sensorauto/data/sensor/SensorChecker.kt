package br.com.sensorauto.data.sensor

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import br.com.sensorauto.domain.model.SensorType

class SensorChecker(
    private val sensorManager: SensorManager,
    private val packageManager: PackageManager
) {
    fun isAvailable(type: SensorType): Boolean = when (type) {
        SensorType.ACCEL  -> sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        SensorType.GYRO   -> sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
        SensorType.GPS    -> true
        SensorType.CAMERA -> packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        SensorType.MAGNETO -> sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
    }
}
