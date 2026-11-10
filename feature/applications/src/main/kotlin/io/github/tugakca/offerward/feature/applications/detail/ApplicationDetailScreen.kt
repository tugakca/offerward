package io.github.tugakca.offerward.feature.applications.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tugakca.offerward.core.designsystem.component.CompanyLogo
import io.github.tugakca.offerward.core.designsystem.component.SelectableChip
import io.github.tugakca.offerward.core.designsystem.motion.Motion
import io.github.tugakca.offerward.core.designsystem.motion.sharedElementOrNone
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.feature.applications.R
import io.github.tugakca.offerward.feature.applications.list.applicationLogoKey
import io.github.tugakca.offerward.feature.applications.list.previewApplication
import io.github.tugakca.offerward.feature.applications.ui.colors
import io.github.tugakca.offerward.feature.applications.ui.formatDateTime
import io.github.tugakca.offerward.feature.applications.ui.label

@Composable
internal fun ApplicationDetailRoute(
    jobId: String,
    onBack: () -> Unit,
    viewModel: ApplicationDetailViewModel =
        hiltViewModel<ApplicationDetailViewModel, ApplicationDetailViewModel.Factory>(
            key = jobId,
            creationCallback = { factory -> factory.create(jobId) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ApplicationDetailEffect.Closed -> onBack()
            }
        }
    }
    ApplicationDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ApplicationDetailScreen(
    state: ApplicationDetailUiState,
    onAction: (ApplicationDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_applications_back),
                        )
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                ApplicationDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                ApplicationDetailUiState.NotFound -> Text(
                    text = stringResource(R.string.feature_applications_not_found),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                )

                is ApplicationDetailUiState.Loaded -> ApplicationDetailContent(state, onAction)
            }
        }
    }
}

@Composable
private fun ApplicationDetailContent(
    state: ApplicationDetailUiState.Loaded,
    onAction: (ApplicationDetailAction) -> Unit,
) {
    val application = state.application
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var confirmStop by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CompanyLogo(
                logoUrl = application.companyLogoUrl,
                companyName = application.companyName,
                modifier = Modifier.sharedElementOrNone(applicationLogoKey(application.jobId)),
                size = 56.dp,
            )
            Column {
                Text(application.jobTitle, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.feature_applications_tracked_since,
                        application.createdAt.formatDateTime(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Section(title = stringResource(R.string.feature_applications_stage)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApplicationStatus.entries.forEach { status ->
                    val colors = status.colors()
                    SelectableChip(
                        selected = application.status == status,
                        onClick = {
                            if (application.status != status) {
                                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onAction(ApplicationDetailAction.ChangeStatus(status))
                            }
                        },
                        label = stringResource(status.label),
                        selectedContainerColor = colors.container,
                        selectedContentColor = colors.content,
                    )
                }
            }
        }

        Section(title = stringResource(R.string.feature_applications_interview)) {
            val interviewAt = application.interviewAt
            // Both rows live in one animated box, so choosing or clearing a date resizes the
            // section smoothly instead of moving everything below it in one frame.
            AnimatedContent(
                targetState = interviewAt != null,
                transitionSpec = {
                    (
                        fadeIn(tween(Motion.ContentFadeInMillis, Motion.ContentFadeInDelayMillis)) togetherWith
                            fadeOut(tween(Motion.ContentFadeOutMillis))
                        ).using(
                        SizeTransform(clip = false) { _, _ ->
                            Motion.spatialSpring(IntSize.VisibilityThreshold)
                        },
                    )
                },
                label = "interview row",
            ) { hasDate ->
                if (!hasDate || interviewAt == null) {
                    OutlinedButton(onClick = { showPicker = true }) {
                        Text(stringResource(R.string.feature_applications_set_interview))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = interviewAt.formatDateTime(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { showPicker = true }) {
                            Text(stringResource(R.string.feature_applications_change))
                        }
                        TextButton(onClick = { onAction(ApplicationDetailAction.SetInterview(null)) }) {
                            Text(stringResource(R.string.feature_applications_clear))
                        }
                    }
                }
            }
        }

        Section(title = stringResource(R.string.feature_applications_notes)) {
            OutlinedTextField(
                value = state.notesDraft,
                onValueChange = { onAction(ApplicationDetailAction.NotesChanged(it)) },
                placeholder = { Text(stringResource(R.string.feature_applications_notes_hint)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TextButton(onClick = { uriHandler.openUri(application.applicationUrl) }) {
            Text(stringResource(R.string.feature_applications_open_posting))
        }

        // The destructive action is set apart from the harmless one above it.
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        TextButton(
            onClick = { confirmStop = true },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(text = stringResource(R.string.feature_applications_stop_tracking))
        }
    }

    if (showPicker) {
        InterviewPicker(
            initial = application.interviewAt,
            onPicked = {
                showPicker = false
                onAction(ApplicationDetailAction.SetInterview(it))
                // Setting an interview date implies the interview stage.
                if (application.status < ApplicationStatus.INTERVIEW) {
                    onAction(ApplicationDetailAction.ChangeStatus(ApplicationStatus.INTERVIEW))
                }
            },
            onDismiss = { showPicker = false },
        )
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text(stringResource(R.string.feature_applications_stop_tracking_title)) },
            text = { Text(stringResource(R.string.feature_applications_stop_tracking_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmStop = false
                    onAction(ApplicationDetailAction.StopTracking)
                }) {
                    Text(
                        text = stringResource(R.string.feature_applications_stop_tracking),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmStop = false }) {
                    Text(stringResource(R.string.feature_applications_cancel))
                }
            },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationDetailPreview() {
    OfferwardTheme {
        ApplicationDetailScreen(
            state = ApplicationDetailUiState.Loaded(previewApplication, previewApplication.notes),
            onAction = {},
            onBack = {},
        )
    }
}
