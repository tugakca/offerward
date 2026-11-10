package io.github.tugakca.offerward.core.data

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.data.model.dedupKey
import io.github.tugakca.offerward.core.data.model.withoutCrossSourceDuplicates
import io.github.tugakca.offerward.core.database.model.JobEntity
import io.github.tugakca.offerward.core.model.JobSource
import java.time.Instant
import org.junit.Test

class CrossSourceDeduplicationTest {

    @Test
    fun keyIgnoresCaseAccentsPunctuationAndCompanySuffixes() {
        assertThat(job("a", "Senior Android Engineer", "Acme, Inc.").dedupKey())
            .isEqualTo(job("b", "senior  android-engineer", "ACME").dedupKey())
        assertThat(job("a", "Développeur Android", "Société Générale").dedupKey())
            .isEqualTo(job("b", "Developpeur Android", "Societe Generale").dedupKey())
    }

    @Test
    fun differentTitlesOrCompaniesAreDifferentJobs() {
        assertThat(job("a", "Android Engineer", "Acme").dedupKey())
            .isNotEqualTo(job("b", "Senior Android Engineer", "Acme").dedupKey())
        assertThat(job("a", "Android Engineer", "Acme").dedupKey())
            .isNotEqualTo(job("b", "Android Engineer", "Acme Labs").dedupKey())
        // A suffix only counts at the end of the name.
        assertThat(job("a", "Android Engineer", "Co Studio").dedupKey())
            .isNotEqualTo(job("b", "Android Engineer", "Studio").dedupKey())
    }

    @Test
    fun higherPrioritySourceWinsAndOrderIsKept() {
        val jobs = listOf(
            job("j1", "Android Engineer", "Acme", JobSource.JOBICY),
            job("other", "iOS Engineer", "Acme", JobSource.JOBICY),
            job("h1", "Android Engineer", "Acme", JobSource.HIMALAYAS),
        )

        assertThat(jobs.withoutCrossSourceDuplicates().map { it.id }).containsExactly("other", "h1").inOrder()
    }

    @Test
    fun withinOneSourceTheNewestRepostWins() {
        val jobs = listOf(
            job("old", "Android Engineer", "Acme", publishedAt = Instant.parse("2026-11-01T00:00:00Z")),
            job("new", "Android Engineer", "Acme", publishedAt = Instant.parse("2026-11-08T00:00:00Z")),
        )

        assertThat(jobs.withoutCrossSourceDuplicates().map { it.id }).containsExactly("new")
    }

    private fun job(
        id: String,
        title: String,
        company: String,
        source: JobSource = JobSource.HIMALAYAS,
        publishedAt: Instant = Instant.parse("2026-11-09T00:00:00Z"),
    ) = JobEntity(
        id = id,
        source = source,
        title = title,
        companyName = company,
        companyLogoUrl = null,
        employmentType = null,
        seniority = emptyList(),
        locationRestrictions = emptyList(),
        isWorldwide = true,
        isSenior = false,
        salaryMin = null,
        salaryMax = null,
        salaryCurrency = null,
        salaryPeriod = null,
        excerpt = "",
        descriptionHtml = "",
        publishedAt = publishedAt,
        expiresAt = null,
        applicationUrl = "https://example.com/$id",
        sourceUrl = "https://example.com/$id",
    )
}
