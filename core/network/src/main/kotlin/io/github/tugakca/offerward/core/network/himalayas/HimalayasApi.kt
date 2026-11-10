package io.github.tugakca.offerward.core.network.himalayas

import retrofit2.http.GET
import retrofit2.http.Query

/** Himalayas public job search API. Docs: https://himalayas.app/api */
internal interface HimalayasApi {
    @GET("jobs/api/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("sort") sort: String = "recent",
    ): HimalayasSearchResponse
}
