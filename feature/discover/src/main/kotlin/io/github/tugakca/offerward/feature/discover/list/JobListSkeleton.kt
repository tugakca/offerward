package io.github.tugakca.offerward.feature.discover.list

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tugakca.offerward.core.designsystem.motion.Motion
import io.github.tugakca.offerward.core.designsystem.motion.rememberAnimationsEnabled
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.feature.discover.R

private const val PLACEHOLDER_COUNT = 5
private const val STATIC_ALPHA = 0.7f

/** Placeholder cards shown while the first jobs load; they pulse gently unless animations are off. */
@Composable
internal fun JobListSkeleton(modifier: Modifier = Modifier) {
    val alpha = if (rememberAnimationsEnabled()) {
        val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(Motion.PulseMillis), RepeatMode.Reverse),
            label = "skeleton alpha",
        )
        pulse
    } else {
        STATIC_ALPHA
    }
    val loading = stringResource(R.string.feature_discover_loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(PLACEHOLDER_COUNT) { SkeletonCard(modifier = Modifier.alpha(alpha)) }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier = Modifier) {
    val block = MaterialTheme.colorScheme.surfaceContainerHighest
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Placeholder(Modifier.size(48.dp), block, CircleShape)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Placeholder(Modifier.fillMaxWidth(0.8f).height(16.dp), block, RoundedCornerShape(4.dp))
                Placeholder(Modifier.fillMaxWidth(0.5f).height(12.dp), block, RoundedCornerShape(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Placeholder(Modifier.width(72.dp).height(20.dp), block, RoundedCornerShape(8.dp))
                    Placeholder(Modifier.width(56.dp).height(20.dp), block, RoundedCornerShape(8.dp))
                }
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier, color: Color, shape: Shape) {
    Spacer(modifier.background(color, shape))
}

@Preview(showBackground = true)
@Composable
private fun JobListSkeletonPreview() {
    OfferwardTheme { JobListSkeleton() }
}
