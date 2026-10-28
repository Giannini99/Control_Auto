package br.com.sensorauto.domain.repository

import br.com.sensorauto.domain.model.Recording
import kotlinx.coroutines.flow.Flow

interface RecordingRepository {
    fun observeAll(): Flow<List<Recording>>
    suspend fun getById(id: Long): Recording?
    suspend fun save(recording: Recording): Long
    suspend fun updateName(id: Long, name: String)
    suspend fun delete(id: Long)
}
