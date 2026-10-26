package br.com.sensorauto.ui.screen.files

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RecordingFile(
    val name: String,
    val path: String,
    val sizeBytes: Long
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
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val sessionDir = File(baseDir, "sessions/$sessionName")
        
        if (sessionDir.exists() && sessionDir.isDirectory) {
            val foundFiles = sessionDir.listFiles()?.map { file ->
                RecordingFile(
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length()
                )
            }?.sortedBy { it.name } ?: emptyList()
            
            _files.value = foundFiles
        } else {
            _files.value = emptyList()
        }
    }

    fun openFile(recordingFile: RecordingFile) {
        val context = getApplication<Application>()
        val file = File(recordingFile.path)
        if (!file.exists()) return

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Tenta abrir o arquivo (Intent de Visualização)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Verifica se há algum app capaz de abrir o CSV
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Se não houver, tenta o compartilhamento genérico como fallback
                shareFile(uri)
            }
        } catch (e: Exception) {
            // Fallback para compartilhamento em caso de erro no VIEW
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                shareFile(uri)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun shareFile(uri: Uri) {
        val context = getApplication<Application>()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Abrir com...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
