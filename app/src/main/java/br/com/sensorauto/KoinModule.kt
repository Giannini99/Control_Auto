package br.com.sensorauto

import android.hardware.SensorManager
import android.location.LocationManager
import android.os.BatteryManager
import br.com.sensorauto.data.local.csv.ConfigCsvManager
import br.com.sensorauto.data.local.csv.SensorCsvWriter
import br.com.sensorauto.data.local.db.AppDatabase
import br.com.sensorauto.data.repository.ConfigRepositoryImpl
import br.com.sensorauto.data.repository.RecordingRepositoryImpl
import br.com.sensorauto.data.sensor.SensorChecker
import br.com.sensorauto.domain.repository.ConfigRepository
import br.com.sensorauto.domain.repository.RecordingRepository
import br.com.sensorauto.domain.usecase.DeleteRecordingUseCase
import br.com.sensorauto.domain.usecase.ObserveRecordingsUseCase
import br.com.sensorauto.domain.usecase.SaveConfigUseCase
import br.com.sensorauto.domain.usecase.SaveRecordingUseCase
import br.com.sensorauto.ui.screen.config.equipment.EquipmentConfigViewModel
import br.com.sensorauto.ui.screen.config.sensors.SensorConfigViewModel
import br.com.sensorauto.ui.screen.files.RecordingDetailViewModel
import br.com.sensorauto.ui.screen.files.RecordingHistoryViewModel
import br.com.sensorauto.ui.screen.start.ActiveStartViewModel
import br.com.sensorauto.domain.usecase.ComputeOrientationUseCase
import br.com.sensorauto.domain.usecase.UpdateRecordingNameUseCase
import br.com.sensorauto.ui.screen.config.bubble.BubbleLevelViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().configDao() }
    single { get<AppDatabase>().recordingDao() }
    single { ConfigCsvManager(androidContext()) }
    single { SensorCsvWriter(androidContext()) }
    single { androidContext().getSystemService(SensorManager::class.java)!! }
    single { androidContext().getSystemService(LocationManager::class.java)!! }
    single { androidContext().getSystemService(BatteryManager::class.java)!! }
    single {
        SensorChecker(
            sensorManager = get(),
            packageManager = androidContext().packageManager
        )
    }
    single<ConfigRepository> { ConfigRepositoryImpl(get(), get()) }
    single<RecordingRepository> { RecordingRepositoryImpl(get()) }
    factory { SaveConfigUseCase(get()) }
    factory { SaveRecordingUseCase(get()) }
    factory { ObserveRecordingsUseCase(get()) }
    factory { DeleteRecordingUseCase(get(), androidApplication()) }
    factory { UpdateRecordingNameUseCase(get(), androidApplication()) }
    factory { ComputeOrientationUseCase() }
    viewModel { BubbleLevelViewModel(get(), get(), get()) }
    viewModel { EquipmentConfigViewModel(get(), get()) }
    viewModel { SensorConfigViewModel(get(), get(), get()) }
    viewModel { ActiveStartViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RecordingHistoryViewModel(get(), get(), get()) }
    viewModel { params -> RecordingDetailViewModel(params.get(), get(), androidApplication()) }
}
