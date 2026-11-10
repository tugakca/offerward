package io.github.tugakca.offerward.feature.applications

import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class FakeApplicationRepository(initial: List<JobApplication> = emptyList()) : ApplicationRepository {
    val applications = MutableStateFlow(initial)
    val notesWrites = mutableListOf<String>()

    override fun observeApplications(): Flow<List<JobApplication>> = applications

    override fun observeApplication(jobId: String): Flow<JobApplication?> =
        applications.map { list -> list.find { it.jobId == jobId } }

    override suspend fun track(job: Job) = error("not used")

    override suspend fun updateStatus(jobId: String, status: ApplicationStatus) = edit(jobId) {
        it.copy(status = status)
    }

    override suspend fun updateNotes(jobId: String, notes: String) {
        notesWrites += notes
        edit(jobId) { it.copy(notes = notes) }
    }

    override suspend fun setInterviewAt(jobId: String, interviewAt: Instant?) =
        edit(jobId) { it.copy(interviewAt = interviewAt) }

    override suspend fun untrack(jobId: String) = applications.update { list -> list.filterNot { it.jobId == jobId } }

    override suspend fun restore(application: JobApplication) =
        applications.update { list -> list.filterNot { it.jobId == application.jobId } + application }

    private fun edit(jobId: String, transform: (JobApplication) -> JobApplication) =
        applications.update { list -> list.map { if (it.jobId == jobId) transform(it) else it } }
}

internal fun application(
    jobId: String,
    status: ApplicationStatus = ApplicationStatus.SAVED,
    updatedAt: String = "2026-11-01T00:00:00Z",
    interviewAt: String? = null,
    notes: String = "",
) = JobApplication(
    jobId = jobId,
    jobTitle = "Android Engineer $jobId",
    companyName = "Acme",
    companyLogoUrl = null,
    applicationUrl = "https://example.com/$jobId",
    status = status,
    notes = notes,
    interviewAt = interviewAt?.let(Instant::parse),
    createdAt = Instant.parse("2026-11-01T00:00:00Z"),
    updatedAt = Instant.parse(updatedAt),
)
