package io.github.tugakca.offerward.feature.discover.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.feature.discover.R
import io.github.tugakca.offerward.feature.discover.list.JobTag
import io.github.tugakca.offerward.feature.discover.list.previewJob
import io.github.tugakca.offerward.feature.discover.ui.formatted
import io.github.tugakca.offerward.feature.discover.ui.isHeadingBlock
import io.github.tugakca.offerward.feature.discover.ui.locationLabel
import io.github.tugakca.offerward.feature.discover.ui.openUrl
import io.github.tugakca.offerward.feature.discover.ui.relativeTo
import io.github.tugakca.offerward.feature.discover.ui.splitHtmlBlocks
import java.time.Instant

@Composable
internal fun JobDetailRoute(
    jobId: String,
    onBack: () -> Unit,
    onOpenApplication: (jobId: String) -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel<JobDetailViewModel, JobDetailViewModel.Factory>(
        key = jobId,
        creationCallback = { factory -> factory.create(jobId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    JobDetailScreen(
        state = state,
        onBack = onBack,
        onTrack = { viewModel.onAction(JobDetailAction.Track) },
        onOpenApplication = { onOpenApplication(jobId) },
        onOpenUrl = context::openUrl,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun JobDetailScreen(
    state: JobDetailUiState,
    onBack: () -> Unit,
    onTrack: () -> Unit,
    onOpenApplication: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    now: Instant = Instant.now(),
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberScrollState()
    val titleThreshold = with(LocalDensity.current) { TITLE_SCROLL_THRESHOLD.toPx() }
    val showTitle by remember { derivedStateOf { scrollState.value > titleThreshold } }
    val loaded = state as? JobDetailUiState.Loaded

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    // Only once the header with the job title has scrolled away.
                    AnimatedVisibility(visible = showTitle && loaded != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(text = loaded?.job?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_discover_back),
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (loaded != null) {
                ActionBar(
                    job = loaded.job,
                    applicationStatus = loaded.applicationStatus,
                    onTrack = onTrack,
                    onOpenApplication = onOpenApplication,
                    onOpenUrl = onOpenUrl,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                JobDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                JobDetailUiState.NotFound -> Text(
                    text = stringResource(R.string.feature_discover_job_not_found),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                )

                is JobDetailUiState.Loaded -> JobDetailContent(
                    job = state.job,
                    now = now,
                    scrollState = scrollState,
                    onOpenUrl = onOpenUrl,
                )
            }
        }
    }
}

private val TITLE_SCROLL_THRESHOLD = 72.dp

/** Apply and track stay in reach while the description scrolls. */
@Composable
private fun ActionBar(
    job: Job,
    applicationStatus: ApplicationStatus?,
    onTrack: () -> Unit,
    onOpenApplication: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = { onOpenUrl(job.applicationUrl) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.feature_discover_apply))
            }
            OutlinedButton(
                // Once tracked, the same button leads to the application instead.
                onClick = if (applicationStatus == null) onTrack else onOpenApplication,
                modifier = Modifier.weight(1f),
            ) {
                if (applicationStatus != null) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.feature_discover_tracked), modifier = Modifier.padding(start = 6.dp))
                } else {
                    Text(stringResource(R.string.feature_discover_track))
                }
            }
        }
    }
}

@Composable
private fun JobDetailContent(job: Job, now: Instant, scrollState: ScrollState, onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CompanyLogo(logoUrl = job.companyLogoUrl, companyName = job.companyName, size = 56.dp)
            Column {
                Text(job.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = job.companyName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            JobTag(job.locationLabel())
            job.employmentType?.let { JobTag(it) }
            job.seniority.forEach { JobTag(it) }
            job.salary?.formatted()?.let { JobTag(it, highlighted = true) }
            JobTag(stringResource(R.string.feature_discover_posted, job.publishedAt.relativeTo(now)))
        }

        if (!job.isWorldwide && job.locationRestrictions.size > 2) {
            LocationList(job.locationRestrictions)
        }

        HorizontalDivider()
        HtmlText(html = job.descriptionHtml)
        HorizontalDivider()

        TextButton(onClick = { onOpenUrl(job.sourceUrl) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.feature_discover_view_on_source, job.source.displayName))
        }
    }
}

@Composable
private fun LocationList(countries: List<String>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Text(stringResource(R.string.feature_discover_open_to), style = MaterialTheme.typography.titleSmall)
        Text(
            text = countries.joinToString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
        )
        if (!expanded) {
            TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.feature_discover_show_all)) }
        }
    }
}

@Composable
private fun HtmlText(html: String) {
    val linkColor = MaterialTheme.colorScheme.primary
    val blocks = remember(html, linkColor) {
        splitHtmlBlocks(html).map { block ->
            val text = AnnotatedString.fromHtml(
                htmlString = block,
                linkStyles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
            )
            DescriptionBlock(text, isHeading = isHeadingBlock(block))
        }.filter { it.text.isNotBlank() }
    }
    val body = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
    // Real space between blocks: the platform HTML parser only separates them by a line break.
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            Text(
                text = block.text,
                style = if (block.isHeading) MaterialTheme.typography.titleMedium else body,
                modifier = if (block.isHeading) Modifier.padding(top = 8.dp).semantics { heading() } else Modifier,
            )
        }
    }
}

private data class DescriptionBlock(val text: AnnotatedString, val isHeading: Boolean)

@Preview(showBackground = true)
@Composable
private fun JobDetailScreenPreview() {
    OfferwardTheme {
        JobDetailScreen(
            state = JobDetailUiState.Loaded(previewJob, applicationStatus = null),
            onBack = {},
            onTrack = {},
            onOpenApplication = {},
            onOpenUrl = {},
            now = Instant.parse("2026-11-10T12:00:00Z"),
        )
    }
}
