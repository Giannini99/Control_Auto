package br.com.sensorauto.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 1,
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehicleYear: String = "",
    val vehicleEngine: String = "",
    val gearType: String = "MANUAL",
    val motorType: String = "COMBUSTION",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val deviceYear: String = "",
    val mountOrientation: String = "VERTICAL",
    val accelEnabled: Boolean = true,
    val accelHz: Int = 50,
    val gyroEnabled: Boolean = true,
    val gyroHz: Int = 50,
    val gpsEnabled: Boolean = true,
    val gpsHz: Int = 1,
    val cameraEnabled: Boolean = false,
    val cameraIntervalSeconds: Float = 2.0f,
    val segmentMeters: Int = 10,
    val magnetoEnabled: Boolean = true,
    val magnetoHz: Int = 50
)
