package io.github.tugakca.offerward.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.tugakca.offerward.core.model.ApplicationStatus
import java.time.Instant

/**
 * One application per job. Deliberately no foreign key to [JobEntity]: jobs leave the cache
 * when they expire, and the application (with its snapshot of the job) must outlive that.
 */
@Entity(
    tableName = "applications",
    indices = [Index("status"), Index("updated_at")],
)
data class ApplicationEntity(
    @PrimaryKey @ColumnInfo(name = "job_id") val jobId: String,
    @ColumnInfo(name = "job_title") val jobTitle: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    @ColumnInfo(name = "company_logo_url") val companyLogoUrl: String?,
    @ColumnInfo(name = "application_url") val applicationUrl: String,
    val status: ApplicationStatus,
    val notes: String,
    @ColumnInfo(name = "interview_at") val interviewAt: Instant?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
