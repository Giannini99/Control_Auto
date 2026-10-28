package br.com.sensorauto.domain.usecase

import android.app.Application
import br.com.sensorauto.data.local.csv.SensorCsvWriter
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
            
            // Deleta arquivos físicos na pasta compartilhada (Externo)
            val sessionsRoot = SensorCsvWriter.getSessionsRoot(application)
            val sessionDir = File(sessionsRoot, recording.name)
            if (sessionDir.exists()) {
                sessionDir.deleteRecursively()
            }
        }
    }
}
