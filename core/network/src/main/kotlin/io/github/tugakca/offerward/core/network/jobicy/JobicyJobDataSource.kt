package io.github.tugakca.offerward.core.network.jobicy

import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import javax.inject.Inject

internal class JobicyJobDataSource @Inject constructor(private val api: JobicyApi) : JobNetworkDataSource {

    override val source = JobSource.JOBICY

    override suspend fun fetchLatestJobs(): List<NetworkJob> {
        // Cursor paging, newest first. Jobs can move between pages, so de-duplicate by id.
        val jobs = LinkedHashMap<Long, JobicyJob>()
        var cursor: String? = null
        for (page in 1..MAX_PAGES) {
            val response = api.jobs(industry = INDUSTRY, count = PAGE_SIZE, cursor = cursor)
            response.jobs.forEach { jobs.putIfAbsent(it.id, it) }
            cursor = response.nextCursor ?: break
        }
        return jobs.values.map { it.asNetworkJob() }
    }

    internal companion object {
        /**
         * "Software Engineering" on the site. Measured on the live API: ~85% engineering roles,
         * the rest mostly localisation and rating jobs.
         */
        const val INDUSTRY = "engineering"
        const val PAGE_SIZE = 100
        const val MAX_PAGES = 3
    }
}
