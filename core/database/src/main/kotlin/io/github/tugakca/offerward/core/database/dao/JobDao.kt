package io.github.tugakca.offerward.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.tugakca.offerward.core.database.model.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    /**
     * Cached jobs, newest first. [query] matches title or company (case-insensitive for ASCII);
     * an empty query matches everything.
     */
    @Query(
        """
        SELECT * FROM jobs
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR company_name LIKE '%' || :query || '%')
          AND (:androidOnly = 0 OR title LIKE '%android%')
          AND (:seniorOnly = 0 OR is_senior = 1)
          AND (:worldwideOnly = 0 OR is_worldwide = 1)
        ORDER BY published_at DESC
        """,
    )
    fun observeJobs(
        query: String,
        androidOnly: Boolean,
        seniorOnly: Boolean,
        worldwideOnly: Boolean,
    ): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun observeJob(id: String): Flow<JobEntity?>

    @Upsert
    suspend fun upsertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id NOT IN (:keepIds)")
    suspend fun deleteJobsNotIn(keepIds: Set<String>)

    @Query("DELETE FROM jobs")
    suspend fun deleteAll()

    /**
     * Makes the cache mirror the latest search: jobs that are no longer listed are removed,
     * the rest are inserted or updated. One transaction, so the UI never sees a half-written list.
     */
    @Transaction
    suspend fun replaceAll(jobs: List<JobEntity>) {
        if (jobs.isEmpty()) {
            deleteAll()
        } else {
            deleteJobsNotIn(jobs.mapTo(HashSet()) { it.id })
            upsertJobs(jobs)
        }
    }
}
