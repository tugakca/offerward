package io.github.tugakca.offerward.core.model

import java.time.Instant

/**
 * The user's application to a job. It keeps its own copy of the job's key fields, so the
 * application stays intact after the job expires and drops out of the cache.
 */
data class JobApplication(
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val companyLogoUrl: String?,
    val applicationUrl: String,
    val status: ApplicationStatus,
    val notes: String,
    val interviewAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
