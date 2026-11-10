package io.github.tugakca.offerward.core.data.repository

import io.github.tugakca.offerward.core.data.model.asEntity
import io.github.tugakca.offerward.core.data.model.asModel
import io.github.tugakca.offerward.core.database.dao.ApplicationDao
import io.github.tugakca.offerward.core.database.model.ApplicationEntity
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LocalApplicationRepository @Inject constructor(
    private val dao: ApplicationDao,
    private val clock: Clock,
) : ApplicationRepository {

    override fun observeApplications(): Flow<List<JobApplication>> =
        dao.observeAll().map { list -> list.map { it.asModel() } }

    override fun observeApplication(jobId: String): Flow<JobApplication?> = dao.observe(jobId).map { it?.asModel() }

    override suspend fun track(job: Job) {
        if (dao.get(job.id) != null) return
        val now = clock.instant()
        dao.upsert(
            ApplicationEntity(
                jobId = job.id,
                jobTitle = job.title,
                companyName = job.companyName,
                companyLogoUrl = job.companyLogoUrl,
                applicationUrl = job.applicationUrl,
                status = ApplicationStatus.SAVED,
                notes = "",
                interviewAt = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateStatus(jobId: String, status: ApplicationStatus) =
        dao.updateStatus(jobId, status, clock.instant())

    override suspend fun updateNotes(jobId: String, notes: String) = dao.updateNotes(jobId, notes, clock.instant())

    override suspend fun setInterviewAt(jobId: String, interviewAt: Instant?) =
        dao.updateInterviewAt(jobId, interviewAt, clock.instant())

    override suspend fun untrack(jobId: String) = dao.delete(jobId)

    override suspend fun restore(application: JobApplication) = dao.upsert(application.asEntity())
}
