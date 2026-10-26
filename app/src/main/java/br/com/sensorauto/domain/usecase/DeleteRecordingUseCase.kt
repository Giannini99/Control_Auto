package br.com.sensorauto.domain.usecase

import android.app.Application
import br.com.sensorauto.domain.repository.RecordingRepository
import java.io.File

class DeleteRecordingUseCase(
    private val repository: RecordingRepository,
    private val application: Application
) {
    suspend operator fun invoke(id: Long) {
        val recording = repository.getById(id)
        if (recording != null) {
            // Deleta do banco
            repository.delete(id)
            
            // Deleta arquivos físicos no armazenamento externo
            val baseDir = application.getExternalFilesDir(null) ?: application.filesDir
            val sessionDir = File(baseDir, "sessions/${recording.name}")
            if (sessionDir.exists()) {
                sessionDir.deleteRecursively()
            }
        }
    }
}
