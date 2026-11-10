package io.github.tugakca.offerward.core.network.retrofit

import io.github.tugakca.offerward.core.network.model.NetworkJobSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Himalayas public job search API. Docs: https://himalayas.app/api */
internal interface HimalayasApi {
    @GET("jobs/api/search")
    suspend fun search(
        @Query("q") query: String,
        /** ISO 3166 alpha-2. Also returns worldwide jobs open to that country. */
        @Query("country") countryCode: String?,
        @Query("page") page: Int,
        @Query("sort") sort: String = "recent",
    ): NetworkJobSearchResponse
}
