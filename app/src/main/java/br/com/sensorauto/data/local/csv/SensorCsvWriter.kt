package br.com.sensorauto.data.local.csv

import android.content.Context
import br.com.sensorauto.domain.model.AppConfig
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class SensorCsvWriter(private val context: Context) {

    private var accelWriter: BufferedWriter? = null
    private var gyroWriter: BufferedWriter? = null
    private var gpsWriter: BufferedWriter? = null
    private var magnetoWriter: BufferedWriter? = null
    var sessionDir: File? = null
        private set

    fun openSession(sessionName: String, config: AppConfig) {
        val dir = File(context.filesDir, "sessions/$sessionName").also {
            it.mkdirs()
            sessionDir = it
        }
        
        if (config.accelEnabled) {
            accelWriter = BufferedWriter(FileWriter(File(dir, "acelerometro.csv"))).also {
                it.write("timestamp_ms,x,y,z")
                it.newLine()
            }
        }
        
        if (config.gyroEnabled) {
            gyroWriter = BufferedWriter(FileWriter(File(dir, "giroscopio.csv"))).also {
                it.write("timestamp_ms,x,y,z")
                it.newLine()
            }
        }
        
        if (config.gpsEnabled) {
            gpsWriter = BufferedWriter(FileWriter(File(dir, "gps.csv"))).also {
                it.write("timestamp_ms,lat,lon,speed_kmh")
                it.newLine()
            }
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
    }
}
