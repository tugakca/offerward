package io.github.tugakca.offerward.core.network.himalayas

import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import javax.inject.Inject

internal class HimalayasJobDataSource @Inject constructor(private val api: HimalayasApi) : JobNetworkDataSource {

    override val source = JobSource.HIMALAYAS

    override suspend fun fetchLatestJobs(): List<NetworkJob> {
        // Results can shift between pages while we read them, so de-duplicate by guid
        // (as the API docs advise). An empty or short page means there are no more results.
        val jobs = LinkedHashMap<String, HimalayasJob>()
        for (page in 1..MAX_PAGES) {
            val pageJobs = api.search(query = SEARCH_QUERY, page = page).jobs
            if (pageJobs.isEmpty()) break
            pageJobs.forEach { jobs.putIfAbsent(it.guid, it) }
            if (pageJobs.size < PAGE_SIZE) break
        }
        return jobs.values.map { it.asNetworkJob() }
    }

    internal companion object {
        /**
         * Newest software jobs from every country. Measured on the live API: "developer" sorted by
         * recency returns ~95% tech roles, while the unfiltered feed is mostly non-tech.
         */
        const val SEARCH_QUERY = "developer"
        const val MAX_PAGES = 5
        const val PAGE_SIZE = 20
    }
}
