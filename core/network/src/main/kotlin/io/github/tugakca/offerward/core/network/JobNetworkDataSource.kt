package io.github.tugakca.offerward.core.network

import io.github.tugakca.offerward.core.network.model.NetworkJob

/** Where remote jobs come from. An interface so the data layer can be tested with a fake. */
interface JobNetworkDataSource {
    /**
     * Returns jobs matching [query] that people in [countryCode] can apply to, newest first,
     * reading at most [maxPages] pages. Throws on network or parsing errors.
     */
    suspend fun searchJobs(query: String, countryCode: String?, maxPages: Int): List<NetworkJob>
}
