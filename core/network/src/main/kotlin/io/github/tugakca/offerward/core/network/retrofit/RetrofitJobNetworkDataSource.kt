package io.github.tugakca.offerward.core.network.retrofit

import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import javax.inject.Inject

internal class RetrofitJobNetworkDataSource @Inject constructor(private val api: HimalayasApi) : JobNetworkDataSource {

    override suspend fun searchJobs(query: String, countryCode: String?, maxPages: Int): List<NetworkJob> {
        // Results can shift between pages while we read them, so de-duplicate by guid
        // (as the API docs advise). An empty page means there are no more results.
        val jobs = LinkedHashMap<String, NetworkJob>()
        for (page in 1..maxPages) {
            val pageJobs = api.search(query = query, countryCode = countryCode, page = page).jobs
            if (pageJobs.isEmpty()) break
            pageJobs.forEach { jobs.putIfAbsent(it.guid, it) }
            if (pageJobs.size < PAGE_SIZE) break
        }
        return jobs.values.toList()
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
