package io.github.tugakca.offerward.core.database

import io.github.tugakca.offerward.core.database.model.ApplicationEntity
import io.github.tugakca.offerward.core.database.model.JobEntity
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.JobSource
import java.time.Instant

internal fun testJob(
    id: String,
    title: String = "Android Engineer",
    companyName: String = "Acme",
    publishedAt: Instant = Instant.parse("2026-11-01T10:00:00Z"),
    isWorldwide: Boolean = true,
    isSenior: Boolean = false,
    source: JobSource = JobSource.HIMALAYAS,
) = JobEntity(
    id = id,
    source = source,
    title = title,
    companyName = companyName,
    companyLogoUrl = null,
    employmentType = "Full Time",
    seniority = if (isSenior) listOf("Senior") else emptyList(),
    locationRestrictions = if (isWorldwide) emptyList() else listOf("Turkey"),
    isWorldwide = isWorldwide,
    isSenior = isSenior,
    salaryMin = null,
    salaryMax = null,
    salaryCurrency = null,
    salaryPeriod = null,
    excerpt = "",
    descriptionHtml = "<p>Job</p>",
    publishedAt = publishedAt,
    expiresAt = null,
    applicationUrl = "https://example.com/$id/apply",
    sourceUrl = "https://example.com/$id",
)

internal fun testApplication(
    jobId: String,
    status: ApplicationStatus = ApplicationStatus.SAVED,
    updatedAt: Instant = Instant.parse("2026-11-01T10:00:00Z"),
) = ApplicationEntity(
    jobId = jobId,
    jobTitle = "Android Engineer",
    companyName = "Acme",
    companyLogoUrl = null,
    applicationUrl = "https://example.com/$jobId/apply",
    status = status,
    notes = "",
    interviewAt = null,
    createdAt = updatedAt,
    updatedAt = updatedAt,
)
