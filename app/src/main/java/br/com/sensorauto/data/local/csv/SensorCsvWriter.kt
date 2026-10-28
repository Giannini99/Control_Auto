package br.com.sensorauto.data.local.csv

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
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
    
    var photosDir: File? = null
        private set

    companion object {
        private const val TAG = "SensorCsvWriter"
        /**
         * Retorna a pasta raiz de sessões de forma consistente em todo o app.
         * Prioriza o armazenamento externo para facilitar o acesso via PC/USB.
         */
        fun getSessionsRoot(context: Context): File {
            val root = context.getExternalFilesDir(null) ?: context.filesDir
            val sessionsDir = File(root, "sessions")
            if (!sessionsDir.exists()) {
                val created = sessionsDir.mkdirs()
                Log.d(TAG, "Pasta raiz 'sessions' criada: $created em ${sessionsDir.absolutePath}")
            }
            return sessionsDir
        }
    }

    fun openSession(sessionName: String, config: AppConfig) {
        try {
            val dir = File(getSessionsRoot(context), sessionName)
            if (!dir.exists()) dir.mkdirs()
            sessionDir = dir
            
            Log.d(TAG, "Abrindo sessão: $sessionName em ${dir.absolutePath}")

            if (config.cameraEnabled) {
                photosDir = File(dir, "photos").also { if (!it.exists()) it.mkdirs() }
            } else {
                photosDir = null
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
                val file = File(dir, "magnetometro.csv")
                magnetoWriter = BufferedWriter(FileWriter(file)).also {
                    it.write("timestamp_ms,x,y,z")
                    it.newLine()
                }
                currentFiles.add(file.absolutePath)
            }
            
            Log.d(TAG, "Sessão aberta com sucesso. Arquivos preparados: ${currentFiles.size}")
        } catch (e: Exception) {
            Log.e(TAG, "ERRO CRÍTICO AO ABRIR ARQUIVOS DA SESSÃO", e)
        }
    }

    fun writeAccel(timestampMs: Long, x: Float, y: Float, z: Float) {
        accelWriter?.run { 
            try {
                write("$timestampMs,$x,$y,$z")
                newLine() 
            } catch (e: Exception) { Log.e(TAG, "Erro no writeAccel", e) }
        }
    }

    fun writeGyro(timestampMs: Long, x: Float, y: Float, z: Float) {
        gyroWriter?.run { 
            try {
                write("$timestampMs,$x,$y,$z")
                newLine() 
            } catch (e: Exception) { Log.e(TAG, "Erro no writeGyro", e) }
        }
    }

    fun writeGps(timestampMs: Long, lat: Double, lon: Double, speedKmh: Float) {
        gpsWriter?.run { 
            try {
                write("$timestampMs,$lat,$lon,$speedKmh")
                newLine() 
            } catch (e: Exception) { Log.e(TAG, "Erro no writeGps", e) }
        }
    }

    fun writeMagneto(timestampMs: Long, x: Float, y: Float, z: Float) {
        magnetoWriter?.run { 
            try {
                write("$timestampMs,$x,$y,$z")
                newLine() 
            } catch (e: Exception) { Log.e(TAG, "Erro no writeMagneto", e) }
        }
    }

    fun closeSession() {
        try {
            accelWriter?.close(); accelWriter = null
            gyroWriter?.close();  gyroWriter  = null
            gpsWriter?.close();   gpsWriter   = null
            magnetoWriter?.close(); magnetoWriter = null
            photosDir = null
            
            Log.d(TAG, "Sessão fechada e buffers limpos.")
            
            if (currentFiles.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, currentFiles.toTypedArray(), null, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fechar sessão", e)
        }
    }
}
