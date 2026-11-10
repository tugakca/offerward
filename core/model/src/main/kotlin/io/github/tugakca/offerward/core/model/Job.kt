package io.github.tugakca.offerward.core.model

import java.time.Instant

/**
 * A remote job listing. [id] is the listing's stable URL at the source, so it survives refreshes.
 */
data class Job(
    val id: String,
    val source: JobSource,
    val title: String,
    val companyName: String,
    val companyLogoUrl: String?,
    val employmentType: String?,
    val seniority: List<String>,
    /** Countries the company hires from. Empty means the job is open worldwide. */
    val locationRestrictions: List<String>,
    val salary: Salary?,
    val excerpt: String,
    val descriptionHtml: String,
    val publishedAt: Instant,
    val expiresAt: Instant?,
    /** Where the Apply button goes. Must be used unchanged (every source's terms). */
    val applicationUrl: String,
    /** The listing's page at the source, shown for attribution. */
    val sourceUrl: String,
) {
    val isWorldwide: Boolean get() = locationRestrictions.isEmpty()
}

data class Salary(val min: Double?, val max: Double?, val currency: String?, val period: String?)
