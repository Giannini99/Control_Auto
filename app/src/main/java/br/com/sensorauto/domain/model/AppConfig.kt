package br.com.sensorauto.domain.model

data class AppConfig(
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehicleYear: String = "",
    val vehicleEngine: String = "",
    val gearType: GearType = GearType.MANUAL,
    val motorType: MotorType = MotorType.COMBUSTION,
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val deviceYear: String = "",
    val mountOrientation: Orientation = Orientation.VERTICAL,
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
