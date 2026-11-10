package io.github.tugakca.offerward.core.designsystem.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

/** The scope of the [androidx.compose.animation.SharedTransitionLayout] around the navigation. Null in previews. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** The visibility scope of the screen currently being shown or hidden by navigation. Null in previews. */
val LocalScreenVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Shares this element with the element carrying the same [key] on the screen being navigated to or
 * from. Does nothing outside a shared transition, so previews and tests render it unchanged.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementOrNone(key: Any): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalScreenVisibilityScope.current
    if (sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = SharedBoundsTransform,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private val SharedBoundsTransform = BoundsTransform { _, _ -> Motion.spatialSpring(Rect.VisibilityThreshold) }
