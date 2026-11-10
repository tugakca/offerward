package io.github.tugakca.offerward.core.data.repository

import io.github.tugakca.offerward.core.data.model.asEntity
import io.github.tugakca.offerward.core.data.model.asModel
import io.github.tugakca.offerward.core.data.model.isExpiredAt
import io.github.tugakca.offerward.core.data.model.withoutCrossSourceDuplicates
import io.github.tugakca.offerward.core.data.settings.SyncSettings
import io.github.tugakca.offerward.core.database.dao.JobDao
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.model.RefreshResult
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class OfflineFirstJobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val sources: Set<@JvmSuppressWildcards JobNetworkDataSource>,
    private val settings: SyncSettings,
    private val clock: Clock,
) : JobRepository {

    // Two screens asking to refresh at once must not both hit the network.
    private val refreshMutex = Mutex()

    override fun observeJobs(filter: JobFilter): Flow<List<Job>> = jobDao.observeJobs(
        query = filter.query.trim(),
        androidOnly = filter.androidOnly,
        seniorOnly = filter.seniorOnly,
        worldwideOnly = filter.worldwideOnly,
    ).map { jobs -> jobs.map { it.asModel() } }

    override fun observeJob(id: String): Flow<Job?> = jobDao.observeJob(id).map { it?.asModel() }

    override suspend fun refresh(userInitiated: Boolean): RefreshResult = refreshMutex.withLock {
        val now = clock.instant()
        val minInterval = if (userInitiated) USER_REFRESH_INTERVAL else AUTO_REFRESH_INTERVAL
        val lastFetch = settings.lastJobsFetchAt()
        if (lastFetch != null && lastFetch.plus(minInterval).isAfter(now)) {
            return RefreshResult.Skipped(nextAllowedAt = lastFetch.plus(minInterval))
        }

        val results = fetchFromAllSources()
        val failures = results.filterValues { it.isFailure }
        if (failures.size == results.size) {
            // Keep the old cache: stale jobs are more useful than an empty screen.
            return RefreshResult.Failed(checkNotNull(failures.values.first().exceptionOrNull()))
        }

        val fresh = results.values.flatMap { it.getOrNull().orEmpty() }
            .filterNot { it.isExpiredAt(now) }
            .map { it.asEntity() }
        // A board that is down keeps the jobs it had, so one outage does not empty half the list.
        val keptFromFailedSources = jobDao.getJobsFrom(failures.keys).filterNot { it.isExpiredAt(now) }
        val jobs = (fresh + keptFromFailedSources).withoutCrossSourceDuplicates()

        jobDao.replaceAll(jobs)
        settings.setLastJobsFetchAt(now)
        RefreshResult.Updated(jobCount = jobs.size, unavailableSources = failures.keys)
    }

    /** Asks every board at once; one slow or broken board neither delays nor cancels the others. */
    private suspend fun fetchFromAllSources(): Map<JobSource, Result<List<NetworkJob>>> = coroutineScope {
        sources.map { dataSource ->
            async {
                val result = try {
                    Result.success(dataSource.fetchLatestJobs())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(e)
                }
                dataSource.source to result
            }
        }.awaitAll().toMap()
    }

    internal companion object {
        /** The boards refresh their data a few times a day, so polling more often gains nothing. */
        val AUTO_REFRESH_INTERVAL: Duration = Duration.ofHours(6)
        val USER_REFRESH_INTERVAL: Duration = Duration.ofMinutes(15)
    }
}
