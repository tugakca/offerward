package io.github.tugakca.offerward.core.data.repository

import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.RefreshResult
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first access to remote jobs. Reads always come from the local cache; [refresh] is the
 * only thing that touches the network.
 */
interface JobRepository {
    fun observeJobs(filter: JobFilter): Flow<List<Job>>

    fun observeJob(id: String): Flow<Job?>

    /** ISO country code the remote search is scoped to; empty means any country. */
    val countryCode: Flow<String>

    suspend fun setCountryCode(code: String)

    /**
     * Refreshes the cache unless it is still fresh. [userInitiated] (pull-to-refresh) uses a
     * shorter minimum interval than automatic refreshes. Never throws; failures are returned.
     */
    suspend fun refresh(userInitiated: Boolean): RefreshResult
}
