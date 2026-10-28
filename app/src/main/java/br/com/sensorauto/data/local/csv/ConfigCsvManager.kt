package br.com.sensorauto.data.local.csv

import android.content.Context
import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.model.GearType
import br.com.sensorauto.domain.model.MotorType
import br.com.sensorauto.domain.model.Orientation
import java.io.File

class ConfigCsvManager(private val context: Context) {

    private val configFile: File
        get() = File(context.filesDir, "config.csv")

    private val header = "vehicleBrand,vehicleModel,vehicleYear,vehicleEngine," +
            "gearType,motorType,deviceBrand,deviceModel,deviceYear," +
            "mountOrientation,accelEnabled,accelHz,gyroEnabled,gyroHz," +
            "gpsEnabled,gpsHz,cameraEnabled,cameraIntervalSeconds,segmentMeters," +
            "magnetoEnabled,magnetoHz"

    fun save(config: AppConfig) {
        configFile.bufferedWriter().use { writer ->
            writer.write(header)
            writer.newLine()
            writer.write(config.toCsvRow())
        }
    }

    fun load(): AppConfig? {
        if (!configFile.exists()) return null
        val lines = configFile.readLines()
        if (lines.size < 2) return null
        return parseCsvRow(lines[1])
    }

    private fun AppConfig.toCsvRow(): String = listOf(
        vehicleBrand.escapeCsv(), vehicleModel.escapeCsv(),
        vehicleYear.escapeCsv(), vehicleEngine.escapeCsv(),
        gearType.name, motorType.name,
        deviceBrand.escapeCsv(), deviceModel.escapeCsv(), deviceYear.escapeCsv(),
        mountOrientation.name,
        accelEnabled.toString(), accelHz.toString(),
        gyroEnabled.toString(), gyroHz.toString(),
        gpsEnabled.toString(), gpsHz.toString(),
        cameraEnabled.toString(), cameraIntervalSeconds.toString(), segmentMeters.toString(),
        magnetoEnabled.toString(), magnetoHz.toString()
    ).joinToString(",")

    private fun parseCsvRow(row: String): AppConfig {
        val parts = row.splitCsv()
        if (parts.size < 17) return AppConfig()
        return AppConfig(
            vehicleBrand = parts[0].unescapeCsv(),
            vehicleModel = parts[1].unescapeCsv(),
            vehicleYear = parts[2].unescapeCsv(),
            vehicleEngine = parts[3].unescapeCsv(),
            gearType = runCatching { GearType.valueOf(parts[4]) }.getOrDefault(GearType.MANUAL),
            motorType = runCatching { MotorType.valueOf(parts[5]) }.getOrDefault(MotorType.COMBUSTION),
            deviceBrand = parts[6].unescapeCsv(),
            deviceModel = parts[7].unescapeCsv(),
            deviceYear = parts[8].unescapeCsv(),
            mountOrientation = runCatching { Orientation.valueOf(parts[9]) }.getOrDefault(Orientation.VERTICAL),
            accelEnabled = parts[10].toBoolean(),
            accelHz = parts[11].toIntOrNull() ?: 50,
            gyroEnabled = parts[12].toBoolean(),
            gyroHz = parts[13].toIntOrNull() ?: 50,
            gpsEnabled = parts[14].toBoolean(),
            gpsHz = parts[15].toIntOrNull() ?: 1,
            cameraEnabled = parts[16].toBoolean(),
            cameraIntervalSeconds = if (parts.size > 17) parts[17].toFloatOrNull() ?: 2.0f else 2.0f,
            segmentMeters = if (parts.size > 18) parts[18].toIntOrNull() ?: 10 else 10,
            magnetoEnabled = if (parts.size > 19) parts[19].toBoolean() else true,
            magnetoHz = if (parts.size > 20) parts[20].toIntOrNull() ?: 50 else 50
        )
    }

    private fun String.escapeCsv(): String =
        if (contains(',') || contains('"') || contains('\n'))
            "\"${replace("\"", "\"\"")}\"" else this

    private fun String.unescapeCsv(): String =
        if (startsWith('"') && endsWith('"'))
            substring(1, length - 1).replace("\"\"", "\"") else this

    private fun String.splitCsv(): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (char in this) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
