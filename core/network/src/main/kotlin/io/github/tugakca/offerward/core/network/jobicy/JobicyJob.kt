package io.github.tugakca.offerward.core.network.jobicy

import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import java.time.OffsetDateTime
import kotlinx.serialization.Serializable

/** A job as the Jobicy API returns it. Docs: https://jobi.cy/apidocs */
@Serializable
internal data class JobicyJob(
    val id: Long,
    /** The listing on Jobicy. Also the Apply link for the public (free) API. */
    val url: String,
    val jobTitle: String,
    val companyName: String,
    val companyLogo: String? = null,
    val jobType: List<String> = emptyList(),
    /** "Anywhere", "Europe" or a comma-separated country list such as "Canada,  USA". */
    val jobGeo: String = "",
    /** "Senior", "Midweight", "Entry-Level, Junior"... or "Any". */
    val jobLevel: String = "",
    val jobExcerpt: String = "",
    val jobDescription: String = "",
    /** ISO 8601 with offset, for example 2026-11-10T14:03:49+00:00. */
    val pubDate: String,
    val salaryMin: Double? = null,
    val salaryMax: Double? = null,
    val salaryCurrency: String? = null,
    val salaryPeriod: String? = null,
)

@Serializable
internal data class JobicyResponse(val jobs: List<JobicyJob> = emptyList(), val nextCursor: String? = null)

internal fun JobicyJob.asNetworkJob() = NetworkJob(
    source = JobSource.JOBICY,
    id = url,
    title = decodeHtmlEntities(jobTitle).trim(),
    companyName = decodeHtmlEntities(companyName).trim(),
    companyLogoUrl = companyLogo?.takeIf { it.isNotBlank() },
    employmentType = jobType.firstOrNull(),
    seniority = jobLevel.split(',').map(String::trim).filter { it.isNotEmpty() && it != "Any" },
    locationRestrictions = jobGeo.takeUnless { it.isWorldwide() }
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        .orEmpty(),
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    salaryCurrency = salaryCurrency,
    // Himalayas says "annual", Jobicy says "yearly"; the app speaks Himalayas' words.
    salaryPeriod = if (salaryPeriod == "yearly") "annual" else salaryPeriod,
    excerpt = decodeHtmlEntities(jobExcerpt).trim(),
    descriptionHtml = jobDescription,
    publishedAt = OffsetDateTime.parse(pubDate).toInstant(),
    // The feed only holds the last 7 days; a job that drops out is removed on the next refresh.
    expiresAt = null,
    // The source's terms: Apply buttons must open the URL from the feed.
    applicationUrl = url,
)

private fun String.isWorldwide() = isBlank() || equals("Anywhere", ignoreCase = true) ||
    equals("Worldwide", ignoreCase = true)

private val namedEntities = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to " ",
    "hellip" to "…",
    "ndash" to "–",
    "mdash" to "—",
    "rsquo" to "’",
    "lsquo" to "‘",
    "rdquo" to "”",
    "ldquo" to "“",
)

private val entity = Regex("""&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);""")

/**
 * Jobicy's plain-text fields contain HTML entities ("&hellip;", "&#8217;"). Decoding them here
 * keeps the rest of the app free of source quirks. Unknown entities are left as they are.
 */
internal fun decodeHtmlEntities(text: String): String = entity.replace(text) { match ->
    val name = match.groupValues[1]
    when {
        name.startsWith("#x") -> name.drop(2).toIntOrNull(16)?.let(::codePointToString)
        name.startsWith("#") -> name.drop(1).toIntOrNull()?.let(::codePointToString)
        else -> namedEntities[name]
    } ?: match.value
}

private fun codePointToString(codePoint: Int): String? =
    if (Character.isValidCodePoint(codePoint)) String(Character.toChars(codePoint)) else null
