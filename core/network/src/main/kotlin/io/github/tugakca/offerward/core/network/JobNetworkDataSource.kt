package io.github.tugakca.offerward.core.network

import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.model.NetworkJob

/**
 * One job board. Each board has its own API, paging and quirks; implementations hide all of that
 * and return [NetworkJob]s, so the data layer treats every board the same way.
 */
interface JobNetworkDataSource {
    val source: JobSource

    /** The board's newest software jobs, newest first. Throws on network or parsing errors. */
    suspend fun fetchLatestJobs(): List<NetworkJob>
}
