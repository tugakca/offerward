package io.github.tugakca.offerward.core.data.repository

import io.github.tugakca.offerward.core.data.model.asEntity
import io.github.tugakca.offerward.core.data.model.asModel
import io.github.tugakca.offerward.core.data.model.isExpiredAt
import io.github.tugakca.offerward.core.data.settings.SyncSettings
import io.github.tugakca.offerward.core.database.dao.JobDao
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class OfflineFirstJobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val network: JobNetworkDataSource,
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

    override val countryCode: Flow<String> = settings.countryCode

    override suspend fun setCountryCode(code: String) = settings.setCountryCode(code)

    override suspend fun refresh(userInitiated: Boolean): RefreshResult = refreshMutex.withLock {
        val now = clock.instant()
        val minInterval = if (userInitiated) USER_REFRESH_INTERVAL else AUTO_REFRESH_INTERVAL
        val lastFetch = settings.lastJobsFetchAt()
        if (lastFetch != null && lastFetch.plus(minInterval).isAfter(now)) {
            return RefreshResult.Skipped(nextAllowedAt = lastFetch.plus(minInterval))
        }

        try {
            val country = settings.countryCode.first().ifBlank { null }
            val jobs = network.searchJobs(query = SEARCH_QUERY, countryCode = country, maxPages = MAX_PAGES)
                .filterNot { it.isExpiredAt(now) }
                .map { it.asEntity() }
            jobDao.replaceAll(jobs)
            settings.setLastJobsFetchAt(now)
            RefreshResult.Updated(jobCount = jobs.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keep the old cache: stale jobs are more useful than an empty screen.
            RefreshResult.Failed(e)
        }
    }

    internal companion object {
        /** The remote search is broad on purpose; the "Android only" chip narrows it on device. */
        const val SEARCH_QUERY = "android"
        const val MAX_PAGES = 3

        /** Himalayas refreshes its data daily, so polling more often than this gains nothing. */
        val AUTO_REFRESH_INTERVAL: Duration = Duration.ofHours(6)
        val USER_REFRESH_INTERVAL: Duration = Duration.ofMinutes(15)
    }
}
