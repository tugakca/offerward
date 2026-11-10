package io.github.tugakca.offerward.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.tugakca.offerward.core.model.JobSource
import java.time.Instant

/**
 * A cached job. `isWorldwide` and `isSenior` are derived when the job is stored so that the
 * filter chips become simple indexed WHERE clauses instead of string matching on every query.
 */
@Entity(
    tableName = "jobs",
    indices = [Index("published_at")],
)
data class JobEntity(
    @PrimaryKey val id: String,
    /** Rows cached before version 2 all came from Himalayas, the only source back then. */
    @ColumnInfo(defaultValue = "HIMALAYAS") val source: JobSource,
    val title: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    @ColumnInfo(name = "company_logo_url") val companyLogoUrl: String?,
    @ColumnInfo(name = "employment_type") val employmentType: String?,
    val seniority: List<String>,
    @ColumnInfo(name = "location_restrictions") val locationRestrictions: List<String>,
    @ColumnInfo(name = "is_worldwide") val isWorldwide: Boolean,
    @ColumnInfo(name = "is_senior") val isSenior: Boolean,
    @ColumnInfo(name = "salary_min") val salaryMin: Double?,
    @ColumnInfo(name = "salary_max") val salaryMax: Double?,
    @ColumnInfo(name = "salary_currency") val salaryCurrency: String?,
    @ColumnInfo(name = "salary_period") val salaryPeriod: String?,
    val excerpt: String,
    @ColumnInfo(name = "description_html") val descriptionHtml: String,
    @ColumnInfo(name = "published_at") val publishedAt: Instant,
    @ColumnInfo(name = "expires_at") val expiresAt: Instant?,
    @ColumnInfo(name = "application_url") val applicationUrl: String,
    @ColumnInfo(name = "source_url") val sourceUrl: String,
)
