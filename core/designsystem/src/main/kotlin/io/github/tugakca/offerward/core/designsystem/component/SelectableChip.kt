package io.github.tugakca.offerward.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.tugakca.offerward.core.designsystem.motion.Motion

/**
 * A filter chip whose selected state does not rely on color alone: a leading check mark and a
 * visible border come with the tinted container. The container fades between states and the
 * check mark grows in, so a tap reads as a change rather than a jump.
 */
@Composable
fun SelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    val colorSpec = tween<Color>(Motion.StateChangeMillis, easing = Motion.EmphasizedDecelerate)
    // Fades to a transparent version of the same color: fading to plain transparent would pass through gray.
    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedContainerColor else selectedContainerColor.copy(alpha = 0f),
        animationSpec = colorSpec,
        label = "chip container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = colorSpec,
        label = "chip content",
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        // Tell accessibility services which chip is the current one, not only the eye.
        modifier = modifier.semantics { this.selected = selected },
        leadingIcon = {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(Motion.StateChangeMillis)) +
                    expandHorizontally(tween(Motion.StateChangeMillis, easing = Motion.EmphasizedDecelerate)),
                exit = fadeOut(tween(Motion.StateChangeMillis)) +
                    shrinkHorizontally(tween(Motion.StateChangeMillis, easing = Motion.EmphasizedAccelerate)),
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
            labelColor = contentColor,
            selectedLabelColor = contentColor,
            iconColor = contentColor,
            selectedLeadingIconColor = contentColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = selectedContentColor,
            selectedBorderWidth = SelectedBorderWidth,
        ),
    )
}

private val SelectedBorderWidth = 1.dp
