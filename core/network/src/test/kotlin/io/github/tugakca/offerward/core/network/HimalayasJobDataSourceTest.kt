package io.github.tugakca.offerward.core.network

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.di.networkJson
import io.github.tugakca.offerward.core.network.himalayas.HimalayasApi
import io.github.tugakca.offerward.core.network.himalayas.HimalayasJobDataSource
import java.time.Instant
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class HimalayasJobDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: HimalayasJobDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = HimalayasJobDataSource(testApi<HimalayasApi>(server, networkJson))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun translatesJobsAndToleratesNullsAndUnknownFields() = runTest {
        server.enqueue(jsonResponse(fixture("himalayas_page.json")))

        val jobs = dataSource.fetchLatestJobs()

        assertThat(jobs.map { it.title }).containsExactly("Senior Android Engineer", "Mobile Developer").inOrder()
        val senior = jobs.first()
        assertThat(senior.source).isEqualTo(JobSource.HIMALAYAS)
        assertThat(senior.id).isEqualTo("https://example.com/acme/jobs/senior-android-engineer")
        assertThat(senior.seniority).containsExactly("Senior")
        assertThat(senior.locationRestrictions).isEmpty()
        assertThat(senior.salaryMin).isEqualTo(70000.0)
        assertThat(senior.publishedAt).isEqualTo(Instant.ofEpochSecond(1794317072))
        // The source's terms: the application link is used exactly as given.
        assertThat(senior.applicationUrl).isEqualTo("https://example.com/acme/apply?utm_source=api")
        val mobile = jobs.last()
        assertThat(mobile.companyLogoUrl).isNull()
        assertThat(mobile.seniority).isEmpty()
        assertThat(mobile.expiresAt).isNull()
    }

    @Test
    fun asksForTheNewestDeveloperJobsAndStopsAfterAShortPage() = runTest {
        server.enqueue(jsonResponse(fixture("himalayas_page.json")))

        dataSource.fetchLatestJobs()

        // Two jobs is less than a full page of 20, so there is no second request.
        assertThat(server.requestCount).isEqualTo(1)
        val url = server.takeRequest().url
        assertThat(url.encodedPath).isEqualTo("/jobs/api/search")
        assertThat(url.queryParameter("q")).isEqualTo("developer")
        assertThat(url.queryParameter("sort")).isEqualTo("recent")
        assertThat(url.queryParameter("page")).isEqualTo("1")
    }

    @Test
    fun readsFullPagesUntilAnEmptyPageAndDeduplicatesByGuid() = runTest {
        server.enqueue(jsonResponse(pageOf((1..20).map(::job))))
        // Page 2 repeats job 20, as can happen when new jobs push results down.
        server.enqueue(jsonResponse(pageOf((20..39).map(::job))))
        server.enqueue(jsonResponse(pageOf(emptyList())))

        val jobs = dataSource.fetchLatestJobs()

        assertThat(server.requestCount).isEqualTo(3)
        assertThat(jobs).hasSize(39)
        assertThat(jobs.map { it.id }.toSet()).hasSize(39)
    }

    @Test
    fun neverReadsMoreThanFivePages() = runTest {
        repeat(6) { page -> server.enqueue(jsonResponse(pageOf((1..20).map { job(page * 20 + it) }))) }

        dataSource.fetchLatestJobs()

        assertThat(server.requestCount).isEqualTo(5)
    }

    private fun job(index: Int) =
        """{"guid":"https://example.com/jobs/$index","title":"Job $index","companyName":"Co","pubDate":1794317072,""" +
            """"applicationLink":"https://example.com/apply/$index"}"""

    private fun pageOf(jobs: List<String>) = """{"jobs":[${jobs.joinToString(",")}]}"""
}
