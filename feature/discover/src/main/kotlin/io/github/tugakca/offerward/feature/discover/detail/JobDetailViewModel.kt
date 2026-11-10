package io.github.tugakca.offerward.feature.discover.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.data.repository.JobRepository
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface JobDetailUiState {
    data object Loading : JobDetailUiState
    data object NotFound : JobDetailUiState
    data class Loaded(
        val job: Job,
        /** Null when the user is not tracking this job yet. */
        val applicationStatus: ApplicationStatus?,
    ) : JobDetailUiState
}

sealed interface JobDetailAction {
    data object Track : JobDetailAction
}

@HiltViewModel(assistedFactory = JobDetailViewModel.Factory::class)
class JobDetailViewModel @AssistedInject constructor(
    @Assisted private val jobId: String,
    jobRepository: JobRepository,
    private val applicationRepository: ApplicationRepository,
) : ViewModel() {

    val uiState: StateFlow<JobDetailUiState> = combine(
        jobRepository.observeJob(jobId),
        applicationRepository.observeApplication(jobId),
    ) { job, application ->
        if (job == null) JobDetailUiState.NotFound else JobDetailUiState.Loaded(job, application?.status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState.Loading)

    fun onAction(action: JobDetailAction) {
        when (action) {
            JobDetailAction.Track -> {
                val job = (uiState.value as? JobDetailUiState.Loaded)?.job ?: return
                viewModelScope.launch { applicationRepository.track(job) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(jobId: String): JobDetailViewModel
    }
}
