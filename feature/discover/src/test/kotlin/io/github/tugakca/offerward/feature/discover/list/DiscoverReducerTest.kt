package io.github.tugakca.offerward.feature.discover.list

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import java.io.IOException
import java.time.Instant
import org.junit.Test

class DiscoverReducerTest {

    @Test
    fun beforeTheCacheAnswersTheScreenIsLoading() {
        assertThat(DiscoverUiState().content).isEqualTo(DiscoverContent.Loading)
    }

    @Test
    fun anEmptyCacheWhileTheFirstRefreshRunsIsStillLoading() {
        val state = DiscoverUiState()
            .reduceAll(DiscoverMutation.JobsLoaded(emptyList()), DiscoverMutation.RefreshStarted)

        assertThat(state.content).isEqualTo(DiscoverContent.Loading)
    }

    @Test
    fun jobsWinOverEveryOtherStatus() {
        val state = DiscoverUiState().reduceAll(
            DiscoverMutation.RefreshStarted,
            DiscoverMutation.JobsLoaded(listOf(previewJob)),
            DiscoverMutation.RefreshFinished(RefreshResult.Failed(IOException())),
        )

        assertThat(state.content).isEqualTo(DiscoverContent.Jobs)
        assertThat(state.lastRefreshFailed).isTrue()
    }

    @Test
    fun aFailedFirstRefreshWithNothingCachedIsAnError() {
        val state = DiscoverUiState().reduceAll(
            DiscoverMutation.JobsLoaded(emptyList()),
            DiscoverMutation.RefreshStarted,
            DiscoverMutation.RefreshFinished(RefreshResult.Failed(IOException())),
        )

        assertThat(state.content).isEqualTo(DiscoverContent.Error)
    }

    @Test
    fun noResultsWithFiltersOnIsNoMatchesNotAnError() {
        val state = DiscoverUiState().reduceAll(
            DiscoverMutation.FilterChanged(JobFilter(androidOnly = true)),
            DiscoverMutation.JobsLoaded(emptyList()),
            DiscoverMutation.RefreshFinished(RefreshResult.Failed(IOException())),
        )

        assertThat(state.content).isEqualTo(DiscoverContent.NoMatches)
    }

    @Test
    fun aSkippedRefreshIsNotAFailure() {
        val state = DiscoverUiState().reduceAll(
            DiscoverMutation.RefreshStarted,
            DiscoverMutation.RefreshFinished(RefreshResult.Skipped(Instant.EPOCH)),
        )

        assertThat(state.isRefreshing).isFalse()
        assertThat(state.lastRefreshFailed).isFalse()
    }

    private fun DiscoverUiState.reduceAll(vararg mutations: DiscoverMutation) =
        mutations.fold(this) { state, mutation -> reduce(state, mutation) }
}
