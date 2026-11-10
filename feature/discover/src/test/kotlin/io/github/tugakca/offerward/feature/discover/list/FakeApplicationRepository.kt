package io.github.tugakca.offerward.feature.discover.list

import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeApplicationRepository : ApplicationRepository {
    val applications = MutableStateFlow<List<JobApplication>>(emptyList())

    override fun observeApplications(): Flow<List<JobApplication>> = applications

    override fun observeApplication(jobId: String): Flow<JobApplication?> =
        applications.map { list -> list.find { it.jobId == jobId } }

    override suspend fun track(job: Job) = error("not used")

    override suspend fun updateStatus(jobId: String, status: ApplicationStatus) = error("not used")

    override suspend fun updateNotes(jobId: String, notes: String) = error("not used")

    override suspend fun setInterviewAt(jobId: String, interviewAt: Instant?) = error("not used")

    override suspend fun untrack(jobId: String) = error("not used")

    override suspend fun restore(application: JobApplication) = error("not used")
}

internal fun application(jobId: String, status: ApplicationStatus) = JobApplication(
    jobId = jobId,
    jobTitle = "Android Engineer $jobId",
    companyName = "Acme",
    companyLogoUrl = null,
    applicationUrl = "https://example.com/$jobId",
    status = status,
    notes = "",
    interviewAt = null,
    createdAt = Instant.parse("2026-11-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-11-01T00:00:00Z"),
)
