package io.github.tugakca.offerward.feature.applications.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.JobApplication
import java.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ApplicationDetailUiState {
    data object Loading : ApplicationDetailUiState
    data object NotFound : ApplicationDetailUiState

    /**
     * [notesDraft] is what the text field shows. It is seeded once from the database and then
     * owned by the UI, so saving a keystroke never moves the cursor or drops a character.
     */
    data class Loaded(val application: JobApplication, val notesDraft: String) : ApplicationDetailUiState
}

sealed interface ApplicationDetailAction {
    data class ChangeStatus(val status: ApplicationStatus) : ApplicationDetailAction
    data class NotesChanged(val notes: String) : ApplicationDetailAction
    data class SetInterview(val at: Instant?) : ApplicationDetailAction
    data object StopTracking : ApplicationDetailAction
}

sealed interface ApplicationDetailEffect {
    data object Closed : ApplicationDetailEffect
}

@HiltViewModel(assistedFactory = ApplicationDetailViewModel.Factory::class)
class ApplicationDetailViewModel @AssistedInject constructor(
    @Assisted private val jobId: String,
    private val repository: ApplicationRepository,
) : ViewModel() {

    private val state = MutableStateFlow<ApplicationDetailUiState>(ApplicationDetailUiState.Loading)
    val uiState: StateFlow<ApplicationDetailUiState> = state.asStateFlow()

    private val effectChannel = Channel<ApplicationDetailEffect>(Channel.BUFFERED)
    val effects: Flow<ApplicationDetailEffect> = effectChannel.receiveAsFlow()

    private val noteWrites = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private var stoppedTracking = false

    init {
        repository.observeApplication(jobId)
            .onEach { application -> state.update { reduce(it, application) } }
            .launchIn(viewModelScope)

        // Writes run one at a time and only the latest pending text is written,
        // so fast typing never queues up stale saves.
        noteWrites.conflate()
            .onEach { repository.updateNotes(jobId, it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ApplicationDetailAction) {
        when (action) {
            is ApplicationDetailAction.ChangeStatus -> viewModelScope.launch {
                repository.updateStatus(jobId, action.status)
            }

            is ApplicationDetailAction.NotesChanged -> {
                state.update { (it as? ApplicationDetailUiState.Loaded)?.copy(notesDraft = action.notes) ?: it }
                noteWrites.tryEmit(action.notes)
            }

            is ApplicationDetailAction.SetInterview -> viewModelScope.launch {
                repository.setInterviewAt(jobId, action.at)
            }

            ApplicationDetailAction.StopTracking -> viewModelScope.launch {
                stoppedTracking = true
                repository.untrack(jobId)
                effectChannel.send(ApplicationDetailEffect.Closed)
            }
        }
    }

    private fun reduce(current: ApplicationDetailUiState, application: JobApplication?): ApplicationDetailUiState =
        when {
            // Keep showing the last content while the screen closes, instead of flashing "not found".
            application == null && stoppedTracking -> current

            application == null -> ApplicationDetailUiState.NotFound

            current is ApplicationDetailUiState.Loaded -> current.copy(application = application)

            else -> ApplicationDetailUiState.Loaded(application, notesDraft = application.notes)
        }

    @AssistedFactory
    interface Factory {
        fun create(jobId: String): ApplicationDetailViewModel
    }
}
