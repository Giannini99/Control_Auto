package br.com.sensorauto.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.sensorauto.data.local.db.dao.ConfigDao
import br.com.sensorauto.data.local.db.dao.RecordingDao
import br.com.sensorauto.data.local.db.entity.ConfigEntity
import br.com.sensorauto.data.local.db.entity.RecordingEntity

@Database(entities = [ConfigEntity::class, RecordingEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sensorauto.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
    }
}
