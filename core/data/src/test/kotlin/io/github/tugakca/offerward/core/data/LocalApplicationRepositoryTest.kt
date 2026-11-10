package io.github.tugakca.offerward.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.data.repository.LocalApplicationRepository
import io.github.tugakca.offerward.core.database.OfferwardDatabase
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobApplication
import io.github.tugakca.offerward.core.model.JobSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalApplicationRepositoryTest {

    private lateinit var db: OfferwardDatabase
    private lateinit var repository: LocalApplicationRepository
    private val now = Instant.parse("2026-11-10T12:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OfferwardDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = LocalApplicationRepository(db.applicationDao(), Clock.fixed(now, ZoneOffset.UTC))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun trackingAJobSnapshotsItAsSaved() = runTest {
        repository.track(job("1"))

        val application = repository.observeApplication("1").first()
        assertThat(application?.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(application?.jobTitle).isEqualTo("Senior Android Engineer")
        assertThat(application?.createdAt).isEqualTo(now)
    }

    @Test
    fun trackingTheSameJobAgainKeepsItsProgress() = runTest {
        repository.track(job("1"))
        repository.updateStatus("1", ApplicationStatus.INTERVIEW)
        repository.updateNotes("1", "Second round on Friday")

        repository.track(job("1"))

        val application = repository.observeApplication("1").first()
        assertThat(application?.status).isEqualTo(ApplicationStatus.INTERVIEW)
        assertThat(application?.notes).isEqualTo("Second round on Friday")
    }

    @Test
    fun untrackRemovesTheApplication() = runTest {
        repository.track(job("1"))

        repository.untrack("1")

        assertThat(repository.observeApplications().first()).isEmpty()
    }

    @Test
    fun restoreBringsBackAnUntrackedApplicationExactlyAsItWas() = runTest {
        repository.track(job("1"))
        repository.updateStatus("1", ApplicationStatus.INTERVIEW)
        repository.updateNotes("1", "Second round on Friday")
        repository.setInterviewAt("1", Instant.parse("2026-11-14T13:00:00Z"))
        val before = repository.observeApplication("1").first()!!

        repository.untrack("1")
        assertThat(repository.observeApplication("1").first()).isNull()
        repository.restore(before)

        assertThat(repository.observeApplication("1").first()).isEqualTo(before)
    }

    @Test
    fun restoreKeepsTheOriginalDatesEvenThoughTheClockMovedOn() = runTest {
        val old = Instant.parse("2026-10-01T08:00:00Z")
        val snapshot = JobApplication(
            jobId = "1",
            jobTitle = "Senior Android Engineer",
            companyName = "Acme",
            companyLogoUrl = null,
            applicationUrl = "https://example.com/1/apply",
            status = ApplicationStatus.OFFER,
            notes = "Offer in hand",
            interviewAt = null,
            createdAt = old,
            updatedAt = old,
        )

        repository.restore(snapshot)

        assertThat(repository.observeApplications().first()).containsExactly(snapshot)
    }

    private fun job(id: String) = Job(
        id = id,
        source = JobSource.HIMALAYAS,
        title = "Senior Android Engineer",
        companyName = "Acme",
        companyLogoUrl = null,
        employmentType = null,
        seniority = emptyList(),
        locationRestrictions = emptyList(),
        salary = null,
        excerpt = "",
        descriptionHtml = "",
        publishedAt = now,
        expiresAt = null,
        applicationUrl = "https://example.com/$id/apply",
        sourceUrl = "https://example.com/$id",
    )
}
