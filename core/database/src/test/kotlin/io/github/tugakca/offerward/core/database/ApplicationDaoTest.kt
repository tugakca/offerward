package io.github.tugakca.offerward.core.database

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.ApplicationStatus
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ApplicationDaoTest : DatabaseTest() {

    private val dao get() = db.applicationDao()

    @Test
    fun recentlyUpdatedApplicationsComeFirst() = runTest {
        dao.upsert(testApplication("a", updatedAt = Instant.parse("2026-11-01T00:00:00Z")))
        dao.upsert(testApplication("b", updatedAt = Instant.parse("2026-11-03T00:00:00Z")))

        assertThat(dao.observeAll().first().map { it.jobId }).containsExactly("b", "a").inOrder()
    }

    @Test
    fun updatingStatusIsObservedAndBumpsUpdatedAt() = runTest {
        dao.upsert(testApplication("a"))
        val later = Instant.parse("2026-11-09T09:00:00Z")

        dao.observe("a").test {
            assertThat(awaitItem()?.status).isEqualTo(ApplicationStatus.SAVED)

            dao.updateStatus("a", ApplicationStatus.INTERVIEW, later)

            val updated = awaitItem()
            assertThat(updated?.status).isEqualTo(ApplicationStatus.INTERVIEW)
            assertThat(updated?.updatedAt).isEqualTo(later)
        }
    }

    @Test
    fun notesAndInterviewDateCanBeSetAndCleared() = runTest {
        dao.upsert(testApplication("a"))
        val interview = Instant.parse("2026-11-20T13:30:00Z")
        val now = Instant.parse("2026-11-10T00:00:00Z")

        dao.updateNotes("a", "Recruiter: Ayşe", now)
        dao.updateInterviewAt("a", interview, now)
        assertThat(dao.get("a")?.notes).isEqualTo("Recruiter: Ayşe")
        assertThat(dao.get("a")?.interviewAt).isEqualTo(interview)

        dao.updateInterviewAt("a", null, now)
        assertThat(dao.get("a")?.interviewAt).isNull()
    }

    @Test
    fun deleteRemovesTheApplication() = runTest {
        dao.upsert(testApplication("a"))

        dao.delete("a")

        assertThat(dao.get("a")).isNull()
    }
}
