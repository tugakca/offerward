package io.github.tugakca.offerward.core.network.jobicy

import retrofit2.http.GET
import retrofit2.http.Query

/** Jobicy public jobs API. No key needed. Docs: https://jobi.cy/apidocs */
internal interface JobicyApi {
    @GET("api/jobs")
    suspend fun jobs(
        @Query("industry") industry: String,
        @Query("count") count: Int,
        /** Opaque token from the previous page's nextCursor. Null for the first page. */
        @Query("cursor") cursor: String?,
    ): JobicyResponse
}
