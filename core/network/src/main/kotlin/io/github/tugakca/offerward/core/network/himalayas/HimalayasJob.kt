package io.github.tugakca.offerward.core.network.himalayas

import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import java.time.Instant
import kotlinx.serialization.Serializable

/** A job as the Himalayas API returns it. Times are Unix seconds. */
@Serializable
internal data class HimalayasJob(
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
internal data class HimalayasSearchResponse(val jobs: List<HimalayasJob> = emptyList())

internal fun HimalayasJob.asNetworkJob() = NetworkJob(
    source = JobSource.HIMALAYAS,
    id = guid,
    title = title,
    companyName = companyName,
    companyLogoUrl = companyLogo,
    employmentType = employmentType,
    seniority = seniority,
    locationRestrictions = locationRestrictions,
    salaryMin = minSalary,
    salaryMax = maxSalary,
    salaryCurrency = currency,
    salaryPeriod = salaryPeriod,
    excerpt = excerpt,
    descriptionHtml = description,
    publishedAt = Instant.ofEpochSecond(pubDate),
    expiresAt = expiryDate?.let(Instant::ofEpochSecond),
    applicationUrl = applicationLink,
)
