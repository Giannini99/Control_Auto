package br.com.sensorauto.ui.screen.files

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.data.local.csv.SensorCsvWriter
import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.RecordingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class RecordingFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isDirectory: Boolean = false
)

class RecordingDetailViewModel(
    private val recordingId: Long,
    private val repository: RecordingRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _recording = MutableStateFlow<Recording?>(null)
    val recording: StateFlow<Recording?> = _recording.asStateFlow()

    private val _files = MutableStateFlow<List<RecordingFile>>(emptyList())
    val files: StateFlow<List<RecordingFile>> = _files.asStateFlow()

    private val _isZipping = MutableStateFlow(false)
    val isZipping: StateFlow<Boolean> = _isZipping.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val rec = repository.getById(recordingId)
            _recording.value = rec
            
            if (rec != null) {
                scanFiles(rec.name)
            }
        }
    }

    private fun scanFiles(sessionName: String) {
        val context = getApplication<Application>()
        // UNIFICAÇÃO: Usa o diretório compartilhado (Externo) definido no SensorCsvWriter
        val sessionsRoot = SensorCsvWriter.getSessionsRoot(context)
        val sessionDir = File(sessionsRoot, sessionName)
        
        Log.d("RecordingDetailVM", "Buscando arquivos em: ${sessionDir.absolutePath}")
        
        if (sessionDir.exists() && sessionDir.isDirectory) {
            val foundFiles = sessionDir.listFiles()?.map { file ->
                RecordingFile(
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = if (file.isDirectory) 0 else file.length(),
                    isDirectory = file.isDirectory
                )
            }?.sortedBy { it.name } ?: emptyList()
            
            _files.value = foundFiles
            Log.d("RecordingDetailVM", "Arquivos encontrados: ${foundFiles.size}")
        } else {
            Log.w("RecordingDetailVM", "Diretório da sessão não encontrado ou vazio: ${sessionDir.absolutePath}")
            _files.value = emptyList()
        }
    }

    fun shareSessionAsZip() {
        val currentRecording = _recording.value ?: return
        
        viewModelScope.launch {
            _isZipping.value = true
            try {
                val zipFile = createZipFromSession(currentRecording.name)
                if (zipFile != null) {
                    startShareIntent(zipFile)
                }
            } finally {
                _isZipping.value = false
            }
        }
    }

    private suspend fun createZipFromSession(sessionName: String): File? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val sessionsRoot = SensorCsvWriter.getSessionsRoot(context)
        val sessionDir = File(sessionsRoot, sessionName)
        if (!sessionDir.exists()) return@withContext null

        val cacheDir = File(context.cacheDir, "shared_sessions")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val zipFile = File(cacheDir, "$sessionName.zip")
        
        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                zipDirectory(sessionDir, sessionName, zos)
            }
            zipFile
        } catch (e: Exception) {
            Log.e("RecordingDetailVM", "Erro ao criar ZIP", e)
            null
        }
    }

    private fun zipDirectory(dir: File, baseName: String, zos: ZipOutputStream) {
        dir.listFiles()?.forEach { file ->
            val entryName = "$baseName/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, entryName, zos)
            } else {
                FileInputStream(file).use { fis ->
                    zos.putNextEntry(ZipEntry(entryName))
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }

    fun openFile(recordingFile: RecordingFile) {
        val context = getApplication<Application>()
        val file = File(recordingFile.path)
        if (!file.exists() || file.isDirectory) return

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = if (file.name.endsWith(".csv")) "text/csv" else "image/jpeg"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                shareFile(uri, mimeType)
            }
        } catch (e: Exception) {
            Log.e("RecordingDetailVM", "Erro ao abrir arquivo", e)
        }
    }

    private fun startShareIntent(file: File) {
        val context = getApplication<Application>()
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        shareFile(uri, "application/zip", "Compartilhar sessão (ZIP)")
    }

    private fun shareFile(uri: Uri, mimeType: String, chooserTitle: String = "Abrir com...") {
        val context = getApplication<Application>()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, chooserTitle)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
