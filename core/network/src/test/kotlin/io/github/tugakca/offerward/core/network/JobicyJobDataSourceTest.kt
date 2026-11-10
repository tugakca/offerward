package io.github.tugakca.offerward.core.network

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.network.di.networkJson
import io.github.tugakca.offerward.core.network.jobicy.JobicyApi
import io.github.tugakca.offerward.core.network.jobicy.JobicyJobDataSource
import io.github.tugakca.offerward.core.network.jobicy.decodeHtmlEntities
import java.time.Instant
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class JobicyJobDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: JobicyJobDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = JobicyJobDataSource(testApi<JobicyApi>(server, networkJson))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun translatesJobicyFieldsIntoTheCommonShape() = runTest {
        server.enqueue(jsonResponse(fixture("jobicy_page.json")))

        val (android, backend, qa) = dataSource.fetchLatestJobs()

        assertThat(android.source).isEqualTo(JobSource.JOBICY)
        assertThat(android.id).isEqualTo("https://jobicy.com/jobs/101-senior-android-engineer")
        // The source's terms: Apply opens the URL from the feed.
        assertThat(android.applicationUrl).isEqualTo(android.id)
        assertThat(android.title).isEqualTo("Senior Android Engineer – Kotlin")
        assertThat(android.companyName).isEqualTo("Acme & Sons")
        assertThat(android.excerpt).isEqualTo("We’re building things…")
        assertThat(android.employmentType).isEqualTo("Full-Time")
        assertThat(android.seniority).containsExactly("Senior")
        assertThat(android.locationRestrictions).isEmpty()
        assertThat(android.salaryPeriod).isEqualTo("annual")
        assertThat(android.publishedAt).isEqualTo(Instant.parse("2026-11-10T14:03:49Z"))

        assertThat(backend.locationRestrictions).containsExactly("Canada", "USA").inOrder()
        assertThat(backend.seniority).isEmpty()
        assertThat(backend.employmentType).isNull()
        assertThat(backend.publishedAt).isEqualTo(Instant.parse("2026-11-10T10:00:00Z"))

        assertThat(qa.locationRestrictions).containsExactly("Europe")
        assertThat(qa.seniority).containsExactly("Entry-Level", "Junior").inOrder()
        assertThat(qa.companyLogoUrl).isNull()
    }

    @Test
    fun asksForEngineeringJobsAndFollowsTheCursor() = runTest {
        server.enqueue(jsonResponse(pageOf(1..100, nextCursor = "abc+/=")))
        // The second page repeats job 100, as can happen when new jobs push results down.
        server.enqueue(jsonResponse(pageOf(100..150, nextCursor = null)))

        val jobs = dataSource.fetchLatestJobs()

        assertThat(jobs).hasSize(150)
        assertThat(server.requestCount).isEqualTo(2)
        val first = server.takeRequest().url
        assertThat(first.encodedPath).isEqualTo("/api/jobs")
        assertThat(first.queryParameter("industry")).isEqualTo("engineering")
        assertThat(first.queryParameter("count")).isEqualTo("100")
        assertThat(first.queryParameter("cursor")).isNull()
        assertThat(server.takeRequest().url.queryParameter("cursor")).isEqualTo("abc+/=")
    }

    @Test
    fun neverReadsMoreThanThreePages() = runTest {
        repeat(4) { page -> server.enqueue(jsonResponse(pageOf(page * 10 + 1..page * 10 + 10, nextCursor = "p$page"))) }

        dataSource.fetchLatestJobs()

        assertThat(server.requestCount).isEqualTo(3)
    }

    @Test
    fun decodesNamedAndNumericEntitiesAndLeavesUnknownOnesAlone() {
        assertThat(decodeHtmlEntities("a &amp; b &#8217; &#x2014; &bogus; & c")).isEqualTo("a & b ’ — &bogus; & c")
    }

    private fun pageOf(ids: IntRange, nextCursor: String?): String {
        val jobs = ids.joinToString(",") { id ->
            """{"id":$id,"url":"https://jobicy.com/jobs/$id","jobTitle":"Job $id","companyName":"Co",""" +
                """"pubDate":"2026-11-10T10:00:00+00:00"}"""
        }
        val cursor = nextCursor?.let { "\"$it\"" } ?: "null"
        return """{"jobs":[$jobs],"nextCursor":$cursor}"""
    }
}
