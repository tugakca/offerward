package io.github.tugakca.offerward.core.data.repository

import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** The user's application pipeline. Local only; there is no account or cloud sync. */
interface ApplicationRepository {
    /** All applications, most recently changed first. */
    fun observeApplications(): Flow<List<JobApplication>>

    fun observeApplication(jobId: String): Flow<JobApplication?>

    /** Starts tracking [job] as [ApplicationStatus.SAVED]. Does nothing if it is already tracked. */
    suspend fun track(job: Job)

    suspend fun updateStatus(jobId: String, status: ApplicationStatus)

    suspend fun updateNotes(jobId: String, notes: String)

    suspend fun setInterviewAt(jobId: String, interviewAt: Instant?)

    suspend fun untrack(jobId: String)

    /**
     * Puts [application] back exactly as it was, including stage, notes and dates; used to undo
     * [untrack]. Replaces whatever is stored for the same job.
     */
    suspend fun restore(application: JobApplication)
}
