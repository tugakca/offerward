package io.github.tugakca.offerward.feature.applications.undo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.model.JobApplication
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Connects "stopped tracking" events to the repository, so an undo restores the application as it was. */
@HiltViewModel
internal class StopTrackingUndoViewModel @Inject constructor(
    events: StopTrackingEvents,
    private val repository: ApplicationRepository,
) : ViewModel() {
    val stopped: Flow<JobApplication> = events.stopped

    fun undo(application: JobApplication) {
        viewModelScope.launch { repository.restore(application) }
    }
}
