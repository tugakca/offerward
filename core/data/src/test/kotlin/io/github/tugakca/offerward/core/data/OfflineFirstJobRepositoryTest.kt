package io.github.tugakca.offerward.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.data.repository.OfflineFirstJobRepository
import io.github.tugakca.offerward.core.data.settings.SyncSettings
import io.github.tugakca.offerward.core.database.OfferwardDatabase
import io.github.tugakca.offerward.core.model.JobFilter
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.model.RefreshResult
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.model.NetworkJob
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OfflineFirstJobRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var db: OfferwardDatabase
    private lateinit var himalayas: FakeJobNetworkDataSource
    private lateinit var jobicy: FakeJobNetworkDataSource
    private lateinit var clock: MutableClock
    private lateinit var repository: OfflineFirstJobRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OfferwardDatabase::class.java,
        ).allowMainThreadQueries().build()
        val dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            tmp.newFile("settings.preferences_pb")
        }
        himalayas = FakeJobNetworkDataSource(JobSource.HIMALAYAS)
        jobicy = FakeJobNetworkDataSource(JobSource.JOBICY, jobs = emptyList())
        clock = MutableClock(Instant.parse("2026-11-10T12:00:00Z"))
        repository = OfflineFirstJobRepository(
            jobDao = db.jobDao(),
            sources = setOf(himalayas, jobicy),
            settings = SyncSettings(dataStore),
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        db.close()
        dataStoreScope.cancel()
    }

    @Test
    fun refreshAsksEveryBoardAndCachesTheirJobsTogether() = runTest {
        himalayas.jobs = listOf(networkJob("h1", "Senior Android Engineer"))
        jobicy.jobs = listOf(networkJob("j1", "Backend Developer", source = JobSource.JOBICY))

        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Updated(jobCount = 2))
        assertThat(himalayas.calls).isEqualTo(1)
        assertThat(jobicy.calls).isEqualTo(1)
        val cached = repository.observeJobs(JobFilter()).first()
        assertThat(cached.associate { it.id to it.source })
            .containsExactly("h1", JobSource.HIMALAYAS, "j1", JobSource.JOBICY)
    }

    @Test
    fun theSameJobOnTwoBoardsIsShownOnceFromTheHigherPrioritySource() = runTest {
        himalayas.jobs = listOf(networkJob("h1", "Senior Android Engineer", company = "Acme, Inc."))
        jobicy.jobs = listOf(
            networkJob("j1", "Senior Android Engineer", company = "ACME", source = JobSource.JOBICY),
            networkJob("j2", "Android Engineer", company = "Acme", source = JobSource.JOBICY),
        )

        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Updated(jobCount = 2))
        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("h1", "j2")
    }

    @Test
    fun aBoardThatIsDownKeepsItsCachedJobsWhileTheOthersUpdate() = runTest {
        himalayas.jobs = listOf(networkJob("h-old", "Android Developer"))
        jobicy.jobs = listOf(networkJob("j-old", "Kotlin Developer", source = JobSource.JOBICY))
        repository.refresh(userInitiated = false)
        clock.advanceBy(Duration.ofHours(7))
        himalayas.jobs = listOf(networkJob("h-new", "Android Developer"))
        jobicy.failWith = IOException("jobicy is down")

        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Updated(jobCount = 2, unavailableSources = setOf(JobSource.JOBICY)))
        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("h-new", "j-old")
        // Partial success still counts as a refresh, so the cooldown starts.
        assertThat(repository.refresh(userInitiated = false)).isInstanceOf(RefreshResult.Skipped::class.java)
    }

    @Test
    fun automaticRefreshIsSkippedWhileTheCacheIsFresh() = runTest {
        repository.refresh(userInitiated = false)

        clock.advanceBy(Duration.ofHours(5))
        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Skipped(Instant.parse("2026-11-10T18:00:00Z")))
        assertThat(himalayas.calls).isEqualTo(1)

        clock.advanceBy(Duration.ofHours(1))
        assertThat(repository.refresh(userInitiated = false)).isInstanceOf(RefreshResult.Updated::class.java)
        assertThat(himalayas.calls).isEqualTo(2)
    }

    @Test
    fun pullToRefreshHasAShorterMinimumInterval() = runTest {
        repository.refresh(userInitiated = false)

        clock.advanceBy(Duration.ofMinutes(10))
        assertThat(repository.refresh(userInitiated = true)).isInstanceOf(RefreshResult.Skipped::class.java)

        clock.advanceBy(Duration.ofMinutes(5))
        assertThat(repository.refresh(userInitiated = true)).isInstanceOf(RefreshResult.Updated::class.java)
    }

    @Test
    fun whenEveryBoardFailsTheOldCacheStaysAndTheCooldownDoesNotStart() = runTest {
        himalayas.jobs = listOf(networkJob("1", "Android Developer"))
        repository.refresh(userInitiated = false)
        clock.advanceBy(Duration.ofHours(7))
        himalayas.failWith = IOException("offline")
        jobicy.failWith = IOException("offline")

        val failed = repository.refresh(userInitiated = false)

        assertThat(failed).isInstanceOf(RefreshResult.Failed::class.java)
        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("1")
        himalayas.failWith = null
        jobicy.failWith = null
        assertThat(repository.refresh(userInitiated = false)).isInstanceOf(RefreshResult.Updated::class.java)
    }

    @Test
    fun expiredJobsAreNeverStored() = runTest {
        himalayas.jobs = listOf(
            networkJob("open", "Android Developer", expiresAt = Instant.parse("2026-12-01T00:00:00Z")),
            networkJob("expired", "Android Developer", expiresAt = Instant.parse("2026-11-01T00:00:00Z")),
        )

        repository.refresh(userInitiated = false)

        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("open")
    }

    @Test
    fun seniorityComesFromTheTagOrTheTitle() = runTest {
        himalayas.jobs = listOf(
            networkJob("tagged", "Android Developer", seniority = listOf("Senior")),
            networkJob("titled", "Lead Android Engineer"),
            networkJob("mid", "Android Developer"),
            networkJob("trap", "Android Developer for Seniors App"),
        )
        repository.refresh(userInitiated = false)

        val senior = repository.observeJobs(JobFilter(seniorOnly = true)).first().map { it.id }

        assertThat(senior).containsExactly("tagged", "titled")
    }

    private class FakeJobNetworkDataSource(
        override val source: JobSource,
        var jobs: List<NetworkJob> = listOf(networkJob("default", "Android Developer", source = source)),
    ) : JobNetworkDataSource {
        var failWith: Exception? = null
        var calls = 0

        override suspend fun fetchLatestJobs(): List<NetworkJob> {
            calls++
            failWith?.let { throw it }
            return jobs
        }
    }

    private class MutableClock(private var now: Instant) : Clock() {
        fun advanceBy(duration: Duration) {
            now = now.plus(duration)
        }

        override fun instant(): Instant = now

        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this
    }
}

private fun networkJob(
    id: String,
    title: String,
    company: String = "Acme",
    source: JobSource = JobSource.HIMALAYAS,
    seniority: List<String> = emptyList(),
    expiresAt: Instant? = null,
) = NetworkJob(
    source = source,
    id = id,
    title = title,
    companyName = company,
    companyLogoUrl = null,
    employmentType = null,
    seniority = seniority,
    locationRestrictions = emptyList(),
    salaryMin = null,
    salaryMax = null,
    salaryCurrency = null,
    salaryPeriod = null,
    excerpt = "",
    descriptionHtml = "",
    publishedAt = Instant.parse("2026-11-09T00:00:00Z"),
    expiresAt = expiresAt,
    applicationUrl = "https://example.com/$id/apply",
)
