package io.github.tugakca.offerward.feature.discover.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.feature.discover.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun DiscoverRoute(onJobClick: (jobId: String) -> Unit, viewModel: DiscoverViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            val message = when (effect) {
                DiscoverEffect.RefreshFailed -> resources.getString(R.string.feature_discover_refresh_failed)

                is DiscoverEffect.SourcesUnavailable -> resources.getString(
                    R.string.feature_discover_sources_unavailable,
                    effect.sources.sorted().joinToString { it.displayName },
                )

                is DiscoverEffect.AlreadyUpToDate -> resources.getString(
                    R.string.feature_discover_up_to_date,
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                        .withZone(ZoneId.systemDefault())
                        .format(effect.nextRefreshAt),
                )
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    DiscoverScreen(
        state = state,
        onAction = viewModel::onAction,
        onJobClick = onJobClick,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    state: DiscoverUiState,
    onAction: (DiscoverAction) -> Unit,
    onJobClick: (jobId: String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    now: Instant = Instant.now(),
) {
    Scaffold(
        modifier = modifier,
        // The app's scaffold already handles the system bars.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchField(
                query = state.filter.query,
                onQueryChange = { onAction(DiscoverAction.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            FilterChips(state = state, onAction = onAction)
            PullToRefreshBox(
                isRefreshing = state.isRefreshing && state.content == DiscoverContent.Jobs,
                onRefresh = { onAction(DiscoverAction.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (state.content) {
                    DiscoverContent.Loading -> CenteredBox { CircularProgressIndicator() }

                    DiscoverContent.Jobs -> JobList(state = state, now = now, onJobClick = onJobClick)

                    DiscoverContent.NoMatches -> Message(
                        text = stringResource(R.string.feature_discover_no_matches),
                        actionLabel = stringResource(R.string.feature_discover_clear_filters),
                        onAction = { onAction(DiscoverAction.ClearFilters) },
                    )

                    DiscoverContent.Empty -> Message(
                        text = stringResource(R.string.feature_discover_empty),
                        actionLabel = stringResource(R.string.feature_discover_retry),
                        onAction = { onAction(DiscoverAction.Refresh) },
                    )

                    DiscoverContent.Error -> Message(
                        text = stringResource(R.string.feature_discover_error),
                        actionLabel = stringResource(R.string.feature_discover_retry),
                        onAction = { onAction(DiscoverAction.Refresh) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        // Some translations are longer than the field; the hint must not grow it to two lines.
        placeholder = {
            Text(
                text = stringResource(R.string.feature_discover_search_hint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.feature_discover_clear_search),
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Composable
private fun FilterChips(state: DiscoverUiState, onAction: (DiscoverAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = state.filter.androidOnly,
            onClick = { onAction(DiscoverAction.ToggleAndroidOnly) },
            label = { Text(stringResource(R.string.feature_discover_filter_android)) },
        )
        FilterChip(
            selected = state.filter.seniorOnly,
            onClick = { onAction(DiscoverAction.ToggleSeniorOnly) },
            label = { Text(stringResource(R.string.feature_discover_filter_senior)) },
        )
        FilterChip(
            selected = state.filter.worldwideOnly,
            onClick = { onAction(DiscoverAction.ToggleWorldwideOnly) },
            label = { Text(stringResource(R.string.feature_discover_worldwide)) },
        )
    }
}

@Composable
private fun JobList(state: DiscoverUiState, now: Instant, onJobClick: (String) -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.jobs, key = { it.id }) { job ->
            JobCard(job = job, now = now, onClick = { onJobClick(job.id) }, modifier = Modifier.animateItem())
        }
        item(key = "attribution") {
            Text(
                text = stringResource(R.string.feature_discover_attribution),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Message(text: String, actionLabel: String, onAction: () -> Unit) {
    // Scrollable so pull-to-refresh also works on the empty and error states.
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(32.dp)) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Preview(showBackground = true)
@Composable
private fun DiscoverScreenPreview() {
    OfferwardTheme {
        DiscoverScreen(
            state = DiscoverUiState(
                jobs = List(3) { previewJob.copy(id = "$it") },
                hasLoadedJobs = true,
            ),
            onAction = {},
            onJobClick = {},
            now = Instant.parse("2026-11-10T12:00:00Z"),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoverScreenNoMatchesPreview() {
    OfferwardTheme {
        DiscoverScreen(
            state = DiscoverUiState(hasLoadedJobs = true, filter = DiscoverUiState().filter.copy(androidOnly = true)),
            onAction = {},
            onJobClick = {},
        )
    }
}
