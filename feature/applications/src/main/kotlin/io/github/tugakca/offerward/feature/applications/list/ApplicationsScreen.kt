package io.github.tugakca.offerward.feature.applications.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.JobApplication
import io.github.tugakca.offerward.feature.applications.R
import io.github.tugakca.offerward.feature.applications.ui.colors
import io.github.tugakca.offerward.feature.applications.ui.formatDateTime
import io.github.tugakca.offerward.feature.applications.ui.label
import java.time.Instant

@Composable
internal fun ApplicationsRoute(
    onApplicationClick: (jobId: String) -> Unit,
    onBrowseJobs: () -> Unit,
    viewModel: ApplicationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationsScreen(state = state, onApplicationClick = onApplicationClick, onBrowseJobs = onBrowseJobs)
}

@Composable
internal fun ApplicationsScreen(
    state: ApplicationsUiState,
    onApplicationClick: (jobId: String) -> Unit,
    onBrowseJobs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ApplicationsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        ApplicationsUiState.Empty -> EmptyState(onBrowseJobs = onBrowseJobs, modifier = modifier)

        is ApplicationsUiState.Loaded -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.sections.forEach { section ->
                item(key = "header-${section.status}") {
                    SectionHeader(section, modifier = Modifier.animateItem())
                }
                items(section.applications, key = { it.jobId }) { application ->
                    ApplicationCard(
                        application = application,
                        onClick = { onApplicationClick(application.jobId) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(section: ApplicationSection, modifier: Modifier = Modifier) {
    Text(
        text = "${stringResource(section.status.label)} · ${section.applications.size}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun ApplicationCard(application: JobApplication, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompanyLogo(logoUrl = application.companyLogoUrl, companyName = application.companyName, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                application.interviewAt?.let {
                    Text(
                        text = stringResource(R.string.feature_applications_interview_at, it.formatDateTime()),
                        style = MaterialTheme.typography.labelLarge,
                        color = ApplicationStatus.INTERVIEW.colors().content,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onBrowseJobs: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.feature_applications_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_applications_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onBrowseJobs) { Text(stringResource(R.string.feature_applications_browse_jobs)) }
    }
}

internal val previewApplication = JobApplication(
    jobId = "1",
    jobTitle = "Senior Android Engineer",
    companyName = "Acme Mobile",
    companyLogoUrl = null,
    applicationUrl = "https://example.com/apply",
    status = ApplicationStatus.INTERVIEW,
    notes = "Recruiter: Ayşe. Ask about the Compose migration.",
    interviewAt = Instant.parse("2026-11-14T13:00:00Z"),
    createdAt = Instant.parse("2026-11-01T09:00:00Z"),
    updatedAt = Instant.parse("2026-11-10T09:00:00Z"),
)

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenPreview() {
    OfferwardTheme {
        ApplicationsScreen(
            state = ApplicationsUiState.Loaded(
                sectionsOf(
                    listOf(
                        previewApplication,
                        previewApplication.copy(jobId = "2", status = ApplicationStatus.APPLIED, interviewAt = null),
                        previewApplication.copy(jobId = "3", status = ApplicationStatus.SAVED, interviewAt = null),
                    ),
                ),
            ),
            onApplicationClick = {},
            onBrowseJobs = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsEmptyPreview() {
    OfferwardTheme { ApplicationsScreen(ApplicationsUiState.Empty, onApplicationClick = {}, onBrowseJobs = {}) }
}
