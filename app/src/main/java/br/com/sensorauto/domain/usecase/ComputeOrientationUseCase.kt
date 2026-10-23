package br.com.sensorauto.domain.usecase

import br.com.sensorauto.domain.model.Orientation
import kotlin.math.atan2
import kotlin.math.sqrt

class ComputeOrientationUseCase {
    operator fun invoke(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        orientation: Orientation
    ): Pair<Float, Float> {
        val radToDeg = 180.0f / Math.PI.toFloat()
        return when (orientation) {
            Orientation.HORIZONTAL -> {
                val roll = atan2(accelY, sqrt(accelX * accelX + accelZ * accelZ)) * radToDeg
                val pitch = atan2(-accelX, sqrt(accelY * accelY + accelZ * accelZ)) * radToDeg
                Pair(pitch, roll)
            }
            Orientation.VERTICAL -> {
                val roll = atan2(accelX, sqrt(accelY * accelY + accelZ * accelZ)) * radToDeg
                val pitch = atan2(-accelY, sqrt(accelX * accelX + accelZ * accelZ)) * radToDeg
                Pair(pitch, roll)
            }
        }
    }
}
