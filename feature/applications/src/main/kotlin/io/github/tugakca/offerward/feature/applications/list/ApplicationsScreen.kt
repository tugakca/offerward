package io.github.tugakca.offerward.feature.applications.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.component.StateMessage
import io.github.tugakca.offerward.core.designsystem.motion.animateListItem
import io.github.tugakca.offerward.core.designsystem.motion.sharedElementOrNone
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.JobApplication
import io.github.tugakca.offerward.feature.applications.R
import io.github.tugakca.offerward.feature.applications.ui.StatusLabel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ApplicationsScreen(
    state: ApplicationsUiState,
    onApplicationClick: (jobId: String) -> Unit,
    onBrowseJobs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // The app's scaffold already handles the system bars.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.feature_applications_title)) },
                windowInsets = WindowInsets(0),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                ApplicationsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                ApplicationsUiState.Empty -> StateMessage(
                    icon = Icons.AutoMirrored.Outlined.List,
                    title = stringResource(R.string.feature_applications_empty_title),
                    text = stringResource(R.string.feature_applications_empty_body),
                    actionLabel = stringResource(R.string.feature_applications_browse_jobs),
                    onAction = onBrowseJobs,
                    modifier = Modifier.fillMaxSize(),
                )

                is ApplicationsUiState.Loaded -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "summary") {
                        Text(
                            text = summaryOf(state),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = animateListItem(),
                        )
                    }
                    state.sections.forEach { section ->
                        item(key = "header-${section.status}") {
                            SectionHeader(section, modifier = animateListItem())
                        }
                        items(section.applications, key = { it.jobId }) { application ->
                            ApplicationCard(
                                application = application,
                                onClick = { onApplicationClick(application.jobId) },
                                modifier = animateListItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "1 interview · 3 saved": every stage that has applications, most urgent first. */
@Composable
private fun summaryOf(state: ApplicationsUiState.Loaded): String {
    val parts = buildList {
        state.count(ApplicationStatus.INTERVIEW).takeIf { it > 0 }
            ?.let { add(pluralStringResource(R.plurals.feature_applications_summary_interview, it, it)) }
        state.count(ApplicationStatus.APPLIED).takeIf { it > 0 }
            ?.let { add(pluralStringResource(R.plurals.feature_applications_summary_applied, it, it)) }
        state.count(ApplicationStatus.SAVED).takeIf { it > 0 }
            ?.let { add(pluralStringResource(R.plurals.feature_applications_summary_saved, it, it)) }
        val closed = state.count(ApplicationStatus.OFFER) + state.count(ApplicationStatus.REJECTED)
        if (closed > 0) add(pluralStringResource(R.plurals.feature_applications_summary_closed, closed, closed))
    }
    return parts.joinToString(" · ")
}

@Composable
private fun SectionHeader(section: ApplicationSection, modifier: Modifier = Modifier) {
    Text(
        text = "${stringResource(section.status.label)} · ${section.applications.size}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
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
            CompanyLogo(
                logoUrl = application.companyLogoUrl,
                companyName = application.companyName,
                modifier = Modifier.sharedElementOrNone(applicationLogoKey(application.jobId)),
                size = 40.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // The card carries its own stage, so it still reads correctly out of its section.
                FlowRow(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusLabel(application.status)
                    application.interviewAt?.let {
                        Text(
                            text = stringResource(R.string.feature_applications_interview_at, it.formatDateTime()),
                            style = MaterialTheme.typography.labelLarge,
                            color = ApplicationStatus.INTERVIEW.colors().content,
                        )
                    }
                }
            }
        }
    }
}

/** Shared-element key of the logo that travels between an application's card and its detail screen. */
internal fun applicationLogoKey(jobId: String) = "application-logo-$jobId"

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
