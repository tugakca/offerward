package io.github.tugakca.offerward.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.tugakca.offerward.core.database.model.ApplicationEntity
import io.github.tugakca.offerward.core.model.ApplicationStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM applications ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE job_id = :jobId")
    fun observe(jobId: String): Flow<ApplicationEntity?>

    @Query("SELECT * FROM applications WHERE job_id = :jobId")
    suspend fun get(jobId: String): ApplicationEntity?

    @Upsert
    suspend fun upsert(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status, updated_at = :updatedAt WHERE job_id = :jobId")
    suspend fun updateStatus(jobId: String, status: ApplicationStatus, updatedAt: Instant)

    @Query("UPDATE applications SET notes = :notes, updated_at = :updatedAt WHERE job_id = :jobId")
    suspend fun updateNotes(jobId: String, notes: String, updatedAt: Instant)

    @Query("UPDATE applications SET interview_at = :interviewAt, updated_at = :updatedAt WHERE job_id = :jobId")
    suspend fun updateInterviewAt(jobId: String, interviewAt: Instant?, updatedAt: Instant)

    @Query("DELETE FROM applications WHERE job_id = :jobId")
    suspend fun delete(jobId: String)
}
