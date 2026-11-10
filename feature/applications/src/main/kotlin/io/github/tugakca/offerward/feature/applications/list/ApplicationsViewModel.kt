package io.github.tugakca.offerward.feature.applications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.JobApplication
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ApplicationsUiState {
    data object Loading : ApplicationsUiState
    data object Empty : ApplicationsUiState
    data class Loaded(val sections: List<ApplicationSection>) : ApplicationsUiState
}

data class ApplicationSection(val status: ApplicationStatus, val applications: List<JobApplication>)

/** The screen is read-only (taps navigate), so the state is a straight projection of the data. */
@HiltViewModel
class ApplicationsViewModel @Inject constructor(applicationRepository: ApplicationRepository) : ViewModel() {
    val uiState: StateFlow<ApplicationsUiState> = applicationRepository.observeApplications()
        .map { applications ->
            if (applications.isEmpty()) {
                ApplicationsUiState.Empty
            } else {
                ApplicationsUiState.Loaded(
                    sectionsOf(applications),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApplicationsUiState.Loading)
}

/**
 * Most urgent first: upcoming interviews, then applications waiting for an answer, then jobs
 * still to apply to. Closed applications (offer, rejected) go last.
 */
internal val sectionOrder = listOf(
    ApplicationStatus.INTERVIEW,
    ApplicationStatus.APPLIED,
    ApplicationStatus.SAVED,
    ApplicationStatus.OFFER,
    ApplicationStatus.REJECTED,
)

internal fun sectionsOf(applications: List<JobApplication>): List<ApplicationSection> {
    val byStatus = applications.groupBy { it.status }
    return sectionOrder.mapNotNull { status ->
        val inSection = byStatus[status].orEmpty()
        if (inSection.isEmpty()) return@mapNotNull null
        val sorted = if (status == ApplicationStatus.INTERVIEW) {
            // Soonest interview first; ones without a date yet go to the end of the group.
            inSection.sortedWith(compareBy(nullsLast()) { it.interviewAt })
        } else {
            inSection.sortedByDescending { it.updatedAt }
        }
        ApplicationSection(status, sorted)
    }
}
