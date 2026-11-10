package io.github.tugakca.offerward.feature.discover.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.data.repository.JobRepository
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {

    private val repository = FakeJobRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun opensWithAnAutomaticRefreshAndShowsCachedJobs() = runTest {
        repository.jobs.value = listOf(job("1", "Android Developer"))

        val viewModel = DiscoverViewModel(repository)

        assertThat(repository.refreshCalls).containsExactly(false)
        assertThat(viewModel.uiState.value.jobs.map { it.id }).containsExactly("1")
        assertThat(viewModel.uiState.value.content).isEqualTo(DiscoverContent.Jobs)
    }

    @Test
    fun filterActionsReachTheRepository() = runTest {
        repository.jobs.value = listOf(job("1", "Android Developer"), job("2", "Backend Engineer"))
        val viewModel = DiscoverViewModel(repository)

        viewModel.onAction(DiscoverAction.ToggleAndroidOnly)
        viewModel.onAction(DiscoverAction.QueryChanged("dev"))

        assertThat(viewModel.uiState.value.filter).isEqualTo(JobFilter(query = "dev", androidOnly = true))
        assertThat(viewModel.uiState.value.jobs.map { it.id }).containsExactly("1")

        viewModel.onAction(DiscoverAction.ClearFilters)
        assertThat(viewModel.uiState.value.jobs).hasSize(2)
    }

    @Test
    fun pullToRefreshIsUserInitiated() = runTest {
        val viewModel = DiscoverViewModel(repository)

        viewModel.onAction(DiscoverAction.Refresh)

        assertThat(repository.refreshCalls).containsExactly(false, true).inOrder()
    }

    @Test
    fun aSkippedPullToRefreshTellsTheUserWhenTheNextOneIsAllowed() = runTest {
        val viewModel = DiscoverViewModel(repository)
        val next = Instant.parse("2026-11-10T13:00:00Z")
        repository.nextResult = RefreshResult.Skipped(next)

        viewModel.effects.test {
            viewModel.onAction(DiscoverAction.Refresh)
            assertThat(awaitItem()).isEqualTo(DiscoverEffect.AlreadyUpToDate(next))
        }
    }

    @Test
    fun aFailedRefreshOverCachedJobsShowsASnackbar() = runTest {
        repository.jobs.value = listOf(job("1", "Android Developer"))
        val viewModel = DiscoverViewModel(repository)
        repository.nextResult = RefreshResult.Failed(IOException())

        viewModel.effects.test {
            viewModel.onAction(DiscoverAction.Refresh)
            assertThat(awaitItem()).isEqualTo(DiscoverEffect.RefreshFailed)
        }
    }

    @Test
    fun aFailedFirstLoadShowsTheErrorStateInsteadOfASnackbar() = runTest {
        repository.nextResult = RefreshResult.Failed(IOException())

        val viewModel = DiscoverViewModel(repository)

        assertThat(viewModel.uiState.value.content).isEqualTo(DiscoverContent.Error)
        viewModel.effects.test { expectNoEvents() }
    }

    private class FakeJobRepository : JobRepository {
        val jobs = MutableStateFlow<List<Job>>(emptyList())
        var nextResult: RefreshResult = RefreshResult.Updated(0)
        val refreshCalls = mutableListOf<Boolean>()

        override fun observeJobs(filter: JobFilter): Flow<List<Job>> = jobs.map { list ->
            list.filter { job ->
                (filter.query.isEmpty() || job.title.contains(filter.query, ignoreCase = true)) &&
                    (!filter.androidOnly || job.title.contains("android", ignoreCase = true))
            }
        }

        override fun observeJob(id: String): Flow<Job?> = jobs.map { list -> list.find { it.id == id } }

        override suspend fun refresh(userInitiated: Boolean): RefreshResult {
            refreshCalls += userInitiated
            return nextResult
        }
    }

    private fun job(id: String, title: String) = previewJob.copy(id = id, title = title)
}
