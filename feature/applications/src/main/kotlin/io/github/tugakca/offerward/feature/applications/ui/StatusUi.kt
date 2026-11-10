package io.github.tugakca.offerward.feature.applications.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.feature.applications.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@get:StringRes
internal val ApplicationStatus.label: Int
    get() = when (this) {
        ApplicationStatus.SAVED -> R.string.feature_applications_status_saved
        ApplicationStatus.APPLIED -> R.string.feature_applications_status_applied
        ApplicationStatus.INTERVIEW -> R.string.feature_applications_status_interview
        ApplicationStatus.OFFER -> R.string.feature_applications_status_offer
        ApplicationStatus.REJECTED -> R.string.feature_applications_status_rejected
    }

internal data class StatusColors(val container: Color, val content: Color)

@Composable
internal fun ApplicationStatus.colors(): StatusColors {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        ApplicationStatus.SAVED -> StatusColors(scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        ApplicationStatus.APPLIED -> StatusColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
        ApplicationStatus.INTERVIEW -> StatusColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        ApplicationStatus.OFFER -> StatusColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        ApplicationStatus.REJECTED -> StatusColors(scheme.errorContainer, scheme.onErrorContainer)
    }
}

/** A small tonal label with the stage name, in the stage's own colors. */
@Composable
internal fun StatusLabel(status: ApplicationStatus, modifier: Modifier = Modifier) {
    val colors = status.colors()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.content,
    ) {
        Text(
            text = stringResource(status.label),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            maxLines = 1,
        )
    }
}

internal fun Instant.formatDateTime(zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(zone).format(this)
