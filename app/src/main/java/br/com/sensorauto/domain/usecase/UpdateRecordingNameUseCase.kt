package br.com.sensorauto.domain.usecase

import android.app.Application
import br.com.sensorauto.domain.repository.RecordingRepository
import java.io.File

class UpdateRecordingNameUseCase(
    private val repository: RecordingRepository,
    private val application: Application
) {
    suspend operator fun invoke(id: Long, newName: String) {
        val recording = repository.getById(id)
        if (recording != null && recording.name != newName) {
            val oldName = recording.name
            
            // Localiza o diretório base de forma consistente (Externo com Fallback para Interno)
            val baseDir = application.getExternalFilesDir(null) ?: application.filesDir
            val oldDir = File(baseDir, "sessions/$oldName")
            val newDir = File(baseDir, "sessions/$newName")
            
            if (oldDir.exists() && oldDir.isDirectory) {
                oldDir.renameTo(newDir)
            }
            
            // Atualiza no banco
            repository.updateName(id, newName)
        }
    }
}
