package io.github.tugakca.offerward.core.database

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class JobDaoTest : DatabaseTest() {

    private val dao get() = db.jobDao()

    @Test
    fun jobsAreNewestFirst() = runTest {
        dao.upsertJobs(
            listOf(
                testJob("old", publishedAt = Instant.parse("2026-10-01T00:00:00Z")),
                testJob("new", publishedAt = Instant.parse("2026-11-05T00:00:00Z")),
            ),
        )

        assertThat(observeIds()).containsExactly("new", "old").inOrder()
    }

    @Test
    fun queryMatchesTitleOrCompanyIgnoringCase() = runTest {
        dao.upsertJobs(
            listOf(
                testJob("1", title = "Senior Android Engineer", companyName = "Acme"),
                testJob("2", title = "Android Developer", companyName = "Kotlin Labs"),
                testJob("3", title = "Android Lead", companyName = "Other"),
            ),
        )

        assertThat(observeIds(query = "kotlin")).containsExactly("2")
        assertThat(observeIds(query = "SENIOR")).containsExactly("1")
    }

    @Test
    fun filterChipsNarrowTheList() = runTest {
        dao.upsertJobs(
            listOf(
                testJob("android-senior-ww", title = "Senior Android Engineer", isSenior = true, isWorldwide = true),
                testJob("android-mid-tr", title = "Android Developer", isSenior = false, isWorldwide = false),
                testJob("ios-senior-ww", title = "Senior iOS Engineer", isSenior = true, isWorldwide = true),
            ),
        )

        assertThat(observeIds(androidOnly = false)).hasSize(3)
        assertThat(observeIds(androidOnly = true)).containsExactly("android-senior-ww", "android-mid-tr")
        assertThat(observeIds(androidOnly = true, seniorOnly = true)).containsExactly("android-senior-ww")
        assertThat(observeIds(androidOnly = false, worldwideOnly = true))
            .containsExactly("android-senior-ww", "ios-senior-ww")
    }

    @Test
    fun replaceAllKeepsOnlyTheLatestJobsAndUpdatesChangedOnes() = runTest {
        dao.upsertJobs(listOf(testJob("stays", title = "Android Old Title"), testJob("goes")))

        dao.replaceAll(listOf(testJob("stays", title = "Android New Title"), testJob("arrives")))

        val jobs = dao.observeJobs("", androidOnly = false, seniorOnly = false, worldwideOnly = false).first()
        assertThat(jobs.map { it.id }).containsExactly("stays", "arrives")
        assertThat(jobs.single { it.id == "stays" }.title).isEqualTo("Android New Title")
    }

    @Test
    fun replaceAllWithNothingClearsTheCache() = runTest {
        dao.upsertJobs(listOf(testJob("1"), testJob("2")))

        dao.replaceAll(emptyList())

        assertThat(observeIds(androidOnly = false)).isEmpty()
    }

    @Test
    fun listsRoundTripThroughTheConverter() = runTest {
        dao.upsertJobs(listOf(testJob("tr", isWorldwide = false, isSenior = true)))

        val job = dao.observeJob("tr").first()

        assertThat(job?.locationRestrictions).containsExactly("Turkey")
        assertThat(job?.seniority).containsExactly("Senior")
    }

    private suspend fun observeIds(
        query: String = "",
        androidOnly: Boolean = false,
        seniorOnly: Boolean = false,
        worldwideOnly: Boolean = false,
    ) = dao.observeJobs(query, androidOnly, seniorOnly, worldwideOnly).first().map { it.id }
}
