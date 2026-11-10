package io.github.tugakca.offerward.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.data.repository.OfflineFirstJobRepository
import io.github.tugakca.offerward.core.data.settings.SyncSettings
import io.github.tugakca.offerward.core.database.OfferwardDatabase
import io.github.tugakca.offerward.core.model.JobFilter
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
    private lateinit var network: FakeJobNetworkDataSource
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
        network = FakeJobNetworkDataSource()
        clock = MutableClock(Instant.parse("2026-11-10T12:00:00Z"))
        repository = OfflineFirstJobRepository(
            jobDao = db.jobDao(),
            network = network,
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
    fun firstRefreshFetchesNewestDeveloperJobsFromEveryCountryAndCachesThem() = runTest {
        network.jobs = listOf(networkJob("1", "Senior Android Engineer"), networkJob("2", "Android Developer"))

        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Updated(jobCount = 2))
        assertThat(network.calls.single()).isEqualTo(FakeJobNetworkDataSource.Call("developer", null, 5))
        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun automaticRefreshIsSkippedWhileTheCacheIsFresh() = runTest {
        repository.refresh(userInitiated = false)

        clock.advanceBy(Duration.ofHours(5))
        val result = repository.refresh(userInitiated = false)

        assertThat(result).isEqualTo(RefreshResult.Skipped(Instant.parse("2026-11-10T18:00:00Z")))
        assertThat(network.calls).hasSize(1)

        clock.advanceBy(Duration.ofHours(1))
        assertThat(repository.refresh(userInitiated = false)).isInstanceOf(RefreshResult.Updated::class.java)
        assertThat(network.calls).hasSize(2)
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
    fun failedRefreshKeepsTheOldCacheAndDoesNotStartTheCooldown() = runTest {
        network.jobs = listOf(networkJob("1", "Android Developer"))
        repository.refresh(userInitiated = false)
        clock.advanceBy(Duration.ofHours(7))
        network.failWith = IOException("offline")

        val failed = repository.refresh(userInitiated = false)

        assertThat(failed).isInstanceOf(RefreshResult.Failed::class.java)
        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("1")
        network.failWith = null
        assertThat(repository.refresh(userInitiated = false)).isInstanceOf(RefreshResult.Updated::class.java)
    }

    @Test
    fun expiredJobsAreNeverStored() = runTest {
        network.jobs = listOf(
            networkJob("open", "Android Developer", expiresAt = Instant.parse("2026-12-01T00:00:00Z")),
            networkJob("expired", "Android Developer", expiresAt = Instant.parse("2026-11-01T00:00:00Z")),
        )

        repository.refresh(userInitiated = false)

        assertThat(repository.observeJobs(JobFilter()).first().map { it.id }).containsExactly("open")
    }

    @Test
    fun seniorityComesFromTheTagOrTheTitle() = runTest {
        network.jobs = listOf(
            networkJob("tagged", "Android Developer", seniority = listOf("Senior")),
            networkJob("titled", "Lead Android Engineer"),
            networkJob("mid", "Android Developer"),
            networkJob("trap", "Android Developer for Seniors App"),
        )
        repository.refresh(userInitiated = false)

        val senior = repository.observeJobs(JobFilter(seniorOnly = true)).first().map { it.id }

        assertThat(senior).containsExactly("tagged", "titled")
    }

    private class FakeJobNetworkDataSource : JobNetworkDataSource {
        data class Call(val query: String, val countryCode: String?, val maxPages: Int)

        var jobs: List<NetworkJob> = listOf(networkJob("default", "Android Developer"))
        var failWith: Exception? = null
        val calls = mutableListOf<Call>()

        override suspend fun searchJobs(query: String, countryCode: String?, maxPages: Int): List<NetworkJob> {
            calls += Call(query, countryCode, maxPages)
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

private fun networkJob(id: String, title: String, seniority: List<String> = emptyList(), expiresAt: Instant? = null) =
    NetworkJob(
        guid = id,
        title = title,
        companyName = "Acme",
        seniority = seniority,
        pubDate = Instant.parse("2026-11-09T00:00:00Z").epochSecond,
        expiryDate = expiresAt?.epochSecond,
        applicationLink = "https://example.com/$id/apply",
    )
