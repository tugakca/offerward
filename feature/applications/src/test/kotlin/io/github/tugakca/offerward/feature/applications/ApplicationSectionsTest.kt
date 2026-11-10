package io.github.tugakca.offerward.feature.applications

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.ApplicationStatus.APPLIED
import io.github.tugakca.offerward.core.model.ApplicationStatus.INTERVIEW
import io.github.tugakca.offerward.core.model.ApplicationStatus.OFFER
import io.github.tugakca.offerward.core.model.ApplicationStatus.REJECTED
import io.github.tugakca.offerward.core.model.ApplicationStatus.SAVED
import io.github.tugakca.offerward.feature.applications.list.sectionsOf
import org.junit.Test

class ApplicationSectionsTest {

    @Test
    fun sectionsGoFromMostUrgentToClosedAndSkipEmptyOnes() {
        val sections = sectionsOf(
            listOf(
                application("r", REJECTED),
                application("s", SAVED),
                application("i", INTERVIEW),
                application("o", OFFER),
            ),
        )

        assertThat(sections.map { it.status }).containsExactly(INTERVIEW, SAVED, OFFER, REJECTED).inOrder()
    }

    @Test
    fun interviewsAreSoonestFirstWithUndatedOnesLast() {
        val sections = sectionsOf(
            listOf(
                application("undated", INTERVIEW),
                application("later", INTERVIEW, interviewAt = "2026-11-20T10:00:00Z"),
                application("sooner", INTERVIEW, interviewAt = "2026-11-12T10:00:00Z"),
            ),
        )

        assertThat(sections.single().applications.map { it.jobId })
            .containsExactly("sooner", "later", "undated").inOrder()
    }

    @Test
    fun otherSectionsAreMostRecentlyUpdatedFirst() {
        val sections = sectionsOf(
            listOf(
                application("old", APPLIED, updatedAt = "2026-11-01T00:00:00Z"),
                application("new", APPLIED, updatedAt = "2026-11-09T00:00:00Z"),
            ),
        )

        assertThat(sections.single().applications.map { it.jobId }).containsExactly("new", "old").inOrder()
    }
}
