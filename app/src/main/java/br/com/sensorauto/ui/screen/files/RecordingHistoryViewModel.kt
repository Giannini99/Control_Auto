package br.com.sensorauto.ui.screen.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.usecase.DeleteRecordingUseCase
import br.com.sensorauto.domain.usecase.ObserveRecordingsUseCase
import br.com.sensorauto.domain.usecase.UpdateRecordingNameUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordingHistoryUiState(
    val recordings: List<Recording> = emptyList(),
    val isLoading: Boolean = true
)

class RecordingHistoryViewModel(
    observeRecordings: ObserveRecordingsUseCase,
    private val deleteRecording: DeleteRecordingUseCase,
    private val updateRecordingName: UpdateRecordingNameUseCase
) : ViewModel() {

    val uiState: StateFlow<RecordingHistoryUiState> = observeRecordings()
        .map { RecordingHistoryUiState(recordings = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordingHistoryUiState()
        )

    fun delete(id: Long) {
        viewModelScope.launch { deleteRecording(id) }
    }

    fun rename(id: Long, newName: String) {
        viewModelScope.launch {
            updateRecordingName(id, newName)
        }
    }
}
