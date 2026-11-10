package io.github.tugakca.offerward.feature.discover.list

import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import java.time.Instant

/** Everything the Discover screen draws, in one immutable object. */
data class DiscoverUiState(
    val filter: JobFilter = JobFilter(),
    val jobs: List<Job> = emptyList(),
    /** False until the cache has answered once, so an empty list is never mistaken for "no jobs". */
    val hasLoadedJobs: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastRefreshFailed: Boolean = false,
) {
    val isFiltered: Boolean get() = filter != JobFilter()

    val content: DiscoverContent
        get() = when {
            jobs.isNotEmpty() -> DiscoverContent.Jobs
            !hasLoadedJobs || (isRefreshing && !isFiltered) -> DiscoverContent.Loading
            isFiltered -> DiscoverContent.NoMatches
            lastRefreshFailed -> DiscoverContent.Error
            else -> DiscoverContent.Empty
        }
}

enum class DiscoverContent { Loading, Jobs, NoMatches, Empty, Error }

/** What the user can do on the screen. The only way into the ViewModel. */
sealed interface DiscoverAction {
    data class QueryChanged(val query: String) : DiscoverAction
    data object ToggleAndroidOnly : DiscoverAction
    data object ToggleSeniorOnly : DiscoverAction
    data object ToggleWorldwideOnly : DiscoverAction
    data object ClearFilters : DiscoverAction
    data object Refresh : DiscoverAction
}

/** One-off events that are not state: they must be shown once, not on every recomposition. */
sealed interface DiscoverEffect {
    data object RefreshFailed : DiscoverEffect
    data class AlreadyUpToDate(val nextRefreshAt: Instant) : DiscoverEffect
}

/** Results of the ViewModel's work, folded into the state by [reduce]. */
internal sealed interface DiscoverMutation {
    data class FilterChanged(val filter: JobFilter) : DiscoverMutation
    data class JobsLoaded(val jobs: List<Job>) : DiscoverMutation
    data object RefreshStarted : DiscoverMutation
    data class RefreshFinished(val result: RefreshResult) : DiscoverMutation
}

/** Pure state transition: no coroutines, no Android, trivially unit-testable. */
internal fun reduce(state: DiscoverUiState, mutation: DiscoverMutation): DiscoverUiState = when (mutation) {
    is DiscoverMutation.FilterChanged -> state.copy(filter = mutation.filter)

    is DiscoverMutation.JobsLoaded -> state.copy(jobs = mutation.jobs, hasLoadedJobs = true)

    DiscoverMutation.RefreshStarted -> state.copy(isRefreshing = true)

    is DiscoverMutation.RefreshFinished -> state.copy(
        isRefreshing = false,
        lastRefreshFailed = mutation.result is RefreshResult.Failed,
    )
}
