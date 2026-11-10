package io.github.tugakca.offerward.feature.applications.undo

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.tugakca.offerward.feature.applications.R
import kotlinx.coroutines.flow.collectLatest

/**
 * Shows "Stopped tracking X" with an Undo action on [snackbarHostState] whenever an application is
 * removed, wherever the user lands afterwards. Place it once, next to the app's snackbar host.
 */
@Composable
fun StopTrackingUndoEffect(snackbarHostState: SnackbarHostState) {
    val viewModel = hiltViewModel<StopTrackingUndoViewModel>()
    val resources = LocalResources.current
    LaunchedEffect(viewModel, snackbarHostState) {
        // A newer removal replaces the offer for the previous one.
        viewModel.stopped.collectLatest { application ->
            val result = snackbarHostState.showSnackbar(
                message = resources.getString(R.string.feature_applications_stopped_tracking, application.jobTitle),
                actionLabel = resources.getString(R.string.feature_applications_undo),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(application)
        }
    }
}
