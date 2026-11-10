package io.github.tugakca.offerward.core.network

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.network.di.networkJson
import io.github.tugakca.offerward.core.network.retrofit.HimalayasApi
import io.github.tugakca.offerward.core.network.retrofit.RetrofitJobNetworkDataSource
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitJobNetworkDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: RetrofitJobNetworkDataSource

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HimalayasApi::class.java)
        dataSource = RetrofitJobNetworkDataSource(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun parsesJobsAndToleratesNullsAndUnknownFields() = runTest {
        server.enqueue(jsonResponse(fixture("search_page.json")))

        val jobs = dataSource.searchJobs(query = "android", countryCode = "TR", maxPages = 3)

        assertThat(jobs.map { it.title }).containsExactly("Senior Android Engineer", "Mobile Developer").inOrder()
        val senior = jobs.first()
        assertThat(senior.seniority).containsExactly("Senior")
        assertThat(senior.locationRestrictions).isEmpty()
        assertThat(senior.minSalary).isEqualTo(70000.0)
        val mobile = jobs.last()
        assertThat(mobile.companyLogo).isNull()
        assertThat(mobile.seniority).isEmpty()
        assertThat(mobile.expiryDate).isNull()
    }

    @Test
    fun sendsQueryCountryAndPageAndStopsAfterAShortPage() = runTest {
        server.enqueue(jsonResponse(fixture("search_page.json")))

        dataSource.searchJobs(query = "android", countryCode = "TR", maxPages = 3)

        // Two jobs is less than a full page of 20, so there is no second request.
        assertThat(server.requestCount).isEqualTo(1)
        val url = server.takeRequest().url
        assertThat(url.encodedPath).isEqualTo("/jobs/api/search")
        assertThat(url.queryParameter("q")).isEqualTo("android")
        assertThat(url.queryParameter("country")).isEqualTo("TR")
        assertThat(url.queryParameter("page")).isEqualTo("1")
    }

    @Test
    fun readsFullPagesUntilAnEmptyPageAndDeduplicatesByGuid() = runTest {
        server.enqueue(jsonResponse(pageOf((1..20).map(::job))))
        // Page 2 repeats job 20, as can happen when new jobs push results down.
        server.enqueue(jsonResponse(pageOf((20..39).map(::job))))
        server.enqueue(jsonResponse(pageOf(emptyList())))

        val jobs = dataSource.searchJobs(query = "android", countryCode = null, maxPages = 5)

        assertThat(server.requestCount).isEqualTo(3)
        assertThat(jobs).hasSize(39)
        assertThat(jobs.map { it.guid }.toSet()).hasSize(39)
    }

    @Test
    fun neverReadsMoreThanMaxPages() = runTest {
        repeat(3) { page -> server.enqueue(jsonResponse(pageOf((1..20).map { job(page * 20 + it) }))) }

        dataSource.searchJobs(query = "android", countryCode = null, maxPages = 2)

        assertThat(server.requestCount).isEqualTo(2)
    }

    private fun fixture(name: String): String = checkNotNull(javaClass.classLoader?.getResource(name)).readText()

    private fun jsonResponse(body: String) = MockResponse.Builder()
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()

    private fun job(index: Int) =
        """{"guid":"https://example.com/jobs/$index","title":"Job $index","companyName":"Co","pubDate":1794317072,""" +
            """"applicationLink":"https://example.com/apply/$index"}"""

    private fun pageOf(jobs: List<String>) = """{"jobs":[${jobs.joinToString(",")}]}"""
}
