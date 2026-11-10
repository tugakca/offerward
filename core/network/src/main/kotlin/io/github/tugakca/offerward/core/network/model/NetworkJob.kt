package io.github.tugakca.offerward.core.network.model

import io.github.tugakca.offerward.core.model.JobSource
import java.time.Instant

/** A job as any source returns it, after the source's own format has been translated. */
data class NetworkJob(
    val source: JobSource,
    /** The listing's URL at the source. Stable across refreshes and unique across sources. */
    val id: String,
    val title: String,
    val companyName: String,
    val companyLogoUrl: String?,
    val employmentType: String?,
    val seniority: List<String>,
    /** Countries or regions the company hires from. Empty means worldwide. */
    val locationRestrictions: List<String>,
    val salaryMin: Double?,
    val salaryMax: Double?,
    val salaryCurrency: String?,
    /** "annual", "monthly", "weekly" or "hourly", whatever the source calls it. */
    val salaryPeriod: String?,
    /** Plain text. */
    val excerpt: String,
    val descriptionHtml: String,
    val publishedAt: Instant,
    val expiresAt: Instant?,
    val applicationUrl: String,
)
