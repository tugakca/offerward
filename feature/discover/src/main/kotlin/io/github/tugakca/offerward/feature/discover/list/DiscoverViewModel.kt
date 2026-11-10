package io.github.tugakca.offerward.feature.discover.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.data.repository.JobRepository
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val applicationRepository: ApplicationRepository,
) : ViewModel() {

    private val state = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = state.asStateFlow()

    private val effectChannel = Channel<DiscoverEffect>(Channel.BUFFERED)
    val effects: Flow<DiscoverEffect> = effectChannel.receiveAsFlow()

    init {
        observeJobsForCurrentFilter()
        observeTrackedStatuses()
        refresh(userInitiated = false)
    }

    fun onAction(action: DiscoverAction) {
        when (action) {
            is DiscoverAction.QueryChanged -> updateFilter { it.copy(query = action.query) }
            DiscoverAction.ToggleAndroidOnly -> updateFilter { it.copy(androidOnly = !it.androidOnly) }
            DiscoverAction.ToggleSeniorOnly -> updateFilter { it.copy(seniorOnly = !it.seniorOnly) }
            DiscoverAction.ToggleWorldwideOnly -> updateFilter { it.copy(worldwideOnly = !it.worldwideOnly) }
            DiscoverAction.ClearFilters -> updateFilter { JobFilter() }
            DiscoverAction.Refresh -> refresh(userInitiated = true)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeJobsForCurrentFilter() {
        state.map { it.filter }
            .distinctUntilChanged()
            .flatMapLatest(jobRepository::observeJobs)
            .onEach { mutate(DiscoverMutation.JobsLoaded(it)) }
            .launchIn(viewModelScope)
    }

    private fun observeTrackedStatuses() {
        applicationRepository.observeApplications()
            .map { applications -> applications.associate { it.jobId to it.status } }
            .distinctUntilChanged()
            .onEach { mutate(DiscoverMutation.TrackedStatusesLoaded(it)) }
            .launchIn(viewModelScope)
    }

    private fun refresh(userInitiated: Boolean) {
        if (state.value.isRefreshing) return
        viewModelScope.launch {
            mutate(DiscoverMutation.RefreshStarted)
            val result = jobRepository.refresh(userInitiated)
            mutate(DiscoverMutation.RefreshFinished(result))
            when (result) {
                // With an empty cache the screen itself shows the error, so no snackbar on top.
                is RefreshResult.Failed -> if (state.value.jobs.isNotEmpty()) {
                    effectChannel.send(
                        DiscoverEffect.RefreshFailed,
                    )
                }

                is RefreshResult.Skipped -> if (userInitiated) {
                    effectChannel.send(
                        DiscoverEffect.AlreadyUpToDate(result.nextAllowedAt),
                    )
                }

                is RefreshResult.Updated -> if (result.unavailableSources.isNotEmpty()) {
                    effectChannel.send(
                        DiscoverEffect.SourcesUnavailable(result.unavailableSources),
                    )
                }
            }
        }
    }

    private inline fun updateFilter(transform: (JobFilter) -> JobFilter) =
        mutate(DiscoverMutation.FilterChanged(transform(state.value.filter)))

    private fun mutate(mutation: DiscoverMutation) = state.update { reduce(it, mutation) }
}
