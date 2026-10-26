package br.com.sensorauto.data.local.csv

import android.content.Context
import android.media.MediaScannerConnection
import br.com.sensorauto.domain.model.AppConfig
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class SensorCsvWriter(private val context: Context) {

    private var accelWriter: BufferedWriter? = null
    private var gyroWriter: BufferedWriter? = null
    private var gpsWriter: BufferedWriter? = null
    private var magnetoWriter: BufferedWriter? = null
    private val currentFiles = mutableListOf<String>()
    
    var sessionDir: File? = null
        private set

    fun openSession(sessionName: String, config: AppConfig) {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(baseDir, "sessions/$sessionName").also {
            it.mkdirs()
            sessionDir = it
        }
        
        currentFiles.clear()

        if (config.accelEnabled) {
            val file = File(dir, "acelerometro.csv")
            accelWriter = BufferedWriter(FileWriter(file)).also {
                it.write("timestamp_ms,x,y,z")
                it.newLine()
            }
            currentFiles.add(file.absolutePath)
        }
        
        if (config.gyroEnabled) {
            val file = File(dir, "giroscopio.csv")
            gyroWriter = BufferedWriter(FileWriter(file)).also {
                it.write("timestamp_ms,x,y,z")
                it.newLine()
            }
            currentFiles.add(file.absolutePath)
        }
        
        if (config.gpsEnabled) {
            val file = File(dir, "gps.csv")
            gpsWriter = BufferedWriter(FileWriter(file)).also {
                it.write("timestamp_ms,lat,lon,speed_kmh")
                it.newLine()
            }
            currentFiles.add(file.absolutePath)
        }

        if (config.magnetoEnabled) {
            magnetoWriter = BufferedWriter(FileWriter(File(dir, "magnetometro.csv"))).also {
                it.write("timestamp_ms,x,y,z")
                it.newLine()
            }
        }
    }

    fun writeAccel(timestampMs: Long, x: Float, y: Float, z: Float) {
        accelWriter?.run { write("$timestampMs,$x,$y,$z"); newLine() }
    }

    fun writeGyro(timestampMs: Long, x: Float, y: Float, z: Float) {
        gyroWriter?.run { write("$timestampMs,$x,$y,$z"); newLine() }
    }

    fun writeGps(timestampMs: Long, lat: Double, lon: Double, speedKmh: Float) {
        gpsWriter?.run { write("$timestampMs,$lat,$lon,$speedKmh"); newLine() }
    }

    fun writeMagneto(timestampMs: Long, x: Float, y: Float, z: Float) {
        magnetoWriter?.run { write("$timestampMs,$x,$y,$z"); newLine() }
    }

    fun closeSession() {
        accelWriter?.close(); accelWriter = null
        gyroWriter?.close();  gyroWriter  = null
        gpsWriter?.close();   gpsWriter   = null
        magnetoWriter?.close(); magnetoWriter = null
        
        // Notifica o sistema sobre os novos arquivos para que apareçam no explorador
        if (currentFiles.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, currentFiles.toTypedArray(), null, null)
        }
    }
}
