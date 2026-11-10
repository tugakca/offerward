package io.github.tugakca.offerward.core.data.model

import io.github.tugakca.offerward.core.database.model.ApplicationEntity
import io.github.tugakca.offerward.core.database.model.JobEntity
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import io.github.tugakca.offerward.core.model.Salary
import io.github.tugakca.offerward.core.network.model.NetworkJob
import java.time.Instant

private val seniorTitle = Regex("""\b(senior|sr\.?|lead|staff|principal)\b""", RegexOption.IGNORE_CASE)

internal fun NetworkJob.isExpiredAt(now: Instant): Boolean = expiresAt?.isBefore(now) == true

internal fun JobEntity.isExpiredAt(now: Instant): Boolean = expiresAt?.isBefore(now) == true

internal fun NetworkJob.asEntity() = JobEntity(
    id = id,
    source = source,
    title = title.trim(),
    companyName = companyName.trim(),
    companyLogoUrl = companyLogoUrl,
    employmentType = employmentType,
    seniority = seniority,
    locationRestrictions = locationRestrictions,
    isWorldwide = locationRestrictions.isEmpty(),
    // The seniority tag is missing on many listings, so the title is a second signal.
    isSenior = seniority.any { it.equals("Senior", ignoreCase = true) } || seniorTitle.containsMatchIn(title),
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    salaryCurrency = salaryCurrency,
    salaryPeriod = salaryPeriod,
    excerpt = excerpt.trim(),
    descriptionHtml = descriptionHtml,
    publishedAt = publishedAt,
    expiresAt = expiresAt,
    applicationUrl = applicationUrl,
    sourceUrl = id,
)

internal fun JobEntity.asModel() = Job(
    id = id,
    source = source,
    title = title,
    companyName = companyName,
    companyLogoUrl = companyLogoUrl,
    employmentType = employmentType,
    seniority = seniority,
    locationRestrictions = locationRestrictions,
    salary = if (salaryMin == null && salaryMax == null) {
        null
    } else {
        Salary(min = salaryMin, max = salaryMax, currency = salaryCurrency, period = salaryPeriod)
    },
    excerpt = excerpt,
    descriptionHtml = descriptionHtml,
    publishedAt = publishedAt,
    expiresAt = expiresAt,
    applicationUrl = applicationUrl,
    sourceUrl = sourceUrl,
)

internal fun ApplicationEntity.asModel() = JobApplication(
    jobId = jobId,
    jobTitle = jobTitle,
    companyName = companyName,
    companyLogoUrl = companyLogoUrl,
    applicationUrl = applicationUrl,
    status = status,
    notes = notes,
    interviewAt = interviewAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
