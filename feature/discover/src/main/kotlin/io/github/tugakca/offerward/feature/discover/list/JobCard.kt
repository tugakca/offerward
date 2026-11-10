package io.github.tugakca.offerward.feature.discover.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.model.Salary
import io.github.tugakca.offerward.feature.discover.ui.formatted
import io.github.tugakca.offerward.feature.discover.ui.locationLabel
import io.github.tugakca.offerward.feature.discover.ui.relativeTo
import java.time.Instant

@Composable
internal fun JobCard(job: Job, now: Instant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompanyLogo(logoUrl = job.companyLogoUrl, companyName = job.companyName)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${job.companyName} · ${job.publishedAt.relativeTo(now)} · ${job.source.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    JobTag(job.locationLabel())
                    job.seniority.firstOrNull()?.let { JobTag(it) }
                    job.salary?.formatted()?.let { JobTag(it, highlighted = true) }
                }
            }
        }
    }
}

@Composable
internal fun JobTag(text: String, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (highlighted) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (highlighted) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

internal val previewJob = Job(
    id = "preview",
    source = JobSource.HIMALAYAS,
    title = "Senior Android Engineer (Kotlin, Compose)",
    companyName = "Acme Mobile",
    companyLogoUrl = null,
    employmentType = "Full Time",
    seniority = listOf("Senior"),
    locationRestrictions = emptyList(),
    salary = Salary(min = 70_000.0, max = 95_000.0, currency = "USD", period = "annual"),
    excerpt = "Build our Compose app used by a million drivers.",
    descriptionHtml = "<p>Build our <b>Compose</b> app.</p><ul><li>Kotlin</li><li>Coroutines</li></ul>",
    publishedAt = Instant.parse("2026-11-10T09:00:00Z"),
    expiresAt = null,
    applicationUrl = "https://example.com/apply",
    sourceUrl = "https://example.com/job",
)

@Preview
@Composable
private fun JobCardPreview() {
    OfferwardTheme {
        JobCard(job = previewJob, now = Instant.parse("2026-11-10T12:00:00Z"), onClick = {})
    }
}
