package io.github.tugakca.offerward.feature.discover.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.motion.sharedElementOrNone
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.JobSource
import io.github.tugakca.offerward.core.model.Salary
import io.github.tugakca.offerward.feature.discover.ui.formatted
import io.github.tugakca.offerward.feature.discover.ui.label
import io.github.tugakca.offerward.feature.discover.ui.locationLabel
import io.github.tugakca.offerward.feature.discover.ui.relativeTo
import java.time.Instant

/** Shared-element key of the logo that travels between a job's card and its detail screen. */
internal fun jobLogoKey(jobId: String) = "job-logo-$jobId"

@Composable
internal fun JobCard(
    job: Job,
    now: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trackedStatus: ApplicationStatus? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompanyLogo(
                logoUrl = job.companyLogoUrl,
                companyName = job.companyName,
                modifier = Modifier.sharedElementOrNone(jobLogoKey(job.id)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = job.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Its own line, so the board name is never the part that gets cut off.
                Text(
                    text = "${job.publishedAt.relativeTo(now)} · ${job.source.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    trackedStatus?.let { TrackedTag(it) }
                    JobTag(job.locationLabel())
                    job.seniority.firstOrNull()?.let { JobTag(it) }
                    job.salary?.formatted()?.let { JobTag(it, highlighted = true) }
                }
            }
        }
    }
}

@Composable
private fun TrackedTag(status: ApplicationStatus) {
    JobTag(
        text = stringResource(status.label),
        leadingIcon = Icons.Filled.Check,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
internal fun JobTag(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    leadingIcon: ImageVector? = null,
    containerColor: Color = if (highlighted) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    },
    contentColor: Color = if (highlighted) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    },
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            leadingIcon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(14.dp)) }
            Text(text = text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
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
        JobCard(
            job = previewJob,
            now = Instant.parse("2026-11-10T12:00:00Z"),
            onClick = {},
            trackedStatus = ApplicationStatus.INTERVIEW,
        )
    }
}
