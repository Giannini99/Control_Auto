package br.com.sensorauto.data.repository

import br.com.sensorauto.data.local.db.dao.RecordingDao
import br.com.sensorauto.data.local.db.entity.RecordingEntity
import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecordingRepositoryImpl(private val dao: RecordingDao) : RecordingRepository {

    override fun observeAll(): Flow<List<Recording>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Recording? =
        dao.getById(id)?.toDomain()

    override suspend fun save(recording: Recording): Long =
        dao.insert(recording.toEntity())

    override suspend fun updateName(id: Long, name: String) =
        dao.updateName(id, name)

    override suspend fun delete(id: Long) = dao.deleteById(id)

    private fun Recording.toEntity() = RecordingEntity(
        id          = id,
        name        = name,
        dateMillis  = dateMillis,
        distanceKm  = distanceKm,
        durationMs  = durationMs,
        avgSpeedKmh = avgSpeedKmh,
        maxSpeedKmh = maxSpeedKmh,
        dataPoints  = dataPoints,
        deviceId    = deviceId
    )

    private fun RecordingEntity.toDomain() = Recording(
        id           = id,
        name         = name,
        dateMillis   = dateMillis,
        distanceKm   = distanceKm,
        durationMs   = durationMs,
        avgSpeedKmh  = avgSpeedKmh,
        maxSpeedKmh  = maxSpeedKmh,
        dataPoints   = dataPoints,
        deviceId     = deviceId
    )
}
