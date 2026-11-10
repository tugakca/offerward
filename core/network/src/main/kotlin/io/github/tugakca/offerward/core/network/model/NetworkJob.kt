package io.github.tugakca.offerward.core.network.model

import kotlinx.serialization.Serializable

/** A job as the Himalayas API returns it. Times are Unix seconds. */
@Serializable
data class NetworkJob(
    val guid: String,
    val title: String,
    val companyName: String,
    val companyLogo: String? = null,
    val employmentType: String? = null,
    val seniority: List<String> = emptyList(),
    val locationRestrictions: List<String> = emptyList(),
    val minSalary: Double? = null,
    val maxSalary: Double? = null,
    val currency: String? = null,
    val salaryPeriod: String? = null,
    val excerpt: String = "",
    val description: String = "",
    val pubDate: Long,
    val expiryDate: Long? = null,
    val applicationLink: String,
)

@Serializable
data class NetworkJobSearchResponse(val jobs: List<NetworkJob> = emptyList())
