package br.com.sensorauto.data.repository

import br.com.sensorauto.data.local.csv.ConfigCsvManager
import br.com.sensorauto.data.local.db.dao.ConfigDao
import br.com.sensorauto.data.local.db.entity.ConfigEntity
import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.model.GearType
import br.com.sensorauto.domain.model.MotorType
import br.com.sensorauto.domain.model.Orientation
import br.com.sensorauto.domain.repository.ConfigRepository

class ConfigRepositoryImpl(
    private val dao: ConfigDao,
    private val csvManager: ConfigCsvManager
) : ConfigRepository {

    override suspend fun load(): AppConfig {
        return dao.get()?.toDomain() ?: AppConfig()
    }

    override suspend fun save(config: AppConfig) {
        dao.save(config.toEntity())
        csvManager.save(config)
    }

    private fun ConfigEntity.toDomain() = AppConfig(
        vehicleBrand = vehicleBrand,
        vehicleModel = vehicleModel,
        vehicleYear = vehicleYear,
        vehicleEngine = vehicleEngine,
        gearType = runCatching { GearType.valueOf(gearType) }.getOrDefault(GearType.MANUAL),
        motorType = runCatching { MotorType.valueOf(motorType) }.getOrDefault(MotorType.COMBUSTION),
        deviceBrand = deviceBrand,
        deviceModel = deviceModel,
        deviceYear = deviceYear,
        mountOrientation = runCatching { Orientation.valueOf(mountOrientation) }.getOrDefault(Orientation.VERTICAL),
        accelEnabled = accelEnabled,
        accelHz = accelHz,
        gyroEnabled = gyroEnabled,
        gyroHz = gyroHz,
        gpsEnabled = gpsEnabled,
        gpsHz = gpsHz,
        cameraEnabled = cameraEnabled,
        cameraIntervalSeconds = cameraIntervalSeconds,
        segmentMeters = segmentMeters,
        magnetoEnabled = magnetoEnabled,
        magnetoHz = magnetoHz
    )

    private fun AppConfig.toEntity() = ConfigEntity(
        id = 1,
        vehicleBrand = vehicleBrand,
        vehicleModel = vehicleModel,
        vehicleYear = vehicleYear,
        vehicleEngine = vehicleEngine,
        gearType = gearType.name,
        motorType = motorType.name,
        deviceBrand = deviceBrand,
        deviceModel = deviceModel,
        deviceYear = deviceYear,
        mountOrientation = mountOrientation.name,
        accelEnabled = accelEnabled,
        accelHz = accelHz,
        gyroEnabled = gyroEnabled,
        gyroHz = gyroHz,
        gpsEnabled = gpsEnabled,
        gpsHz = gpsHz,
        cameraEnabled = cameraEnabled,
        cameraIntervalSeconds = cameraIntervalSeconds,
        segmentMeters = segmentMeters,
        magnetoEnabled = magnetoEnabled,
        magnetoHz = magnetoHz
    )
}
