package io.github.tugakca.offerward.core.designsystem.motion

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset

/** Material 3 motion tokens used across the app: explicit easing curves and durations. */
@Suppress("ktlint:standard:property-naming")
object Motion {
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val SharedAxisEnterMillis = 350
    const val SharedAxisExitMillis = 250
    const val FadeInMillis = 250
    const val FadeInDelayMillis = 100
    const val FadeOutMillis = 100

    const val FadeThroughOutMillis = 90
    const val FadeThroughInMillis = 210
    const val FadeThroughInitialScale = 0.92f

    const val PulseMillis = 900

    /** Selected chips and other small state changes: container color and the check mark. */
    const val StateChangeMillis = 200

    /** Rows entering and leaving a list, and the swap between a list's content states. */
    const val ItemFadeInMillis = 200
    const val ItemFadeOutMillis = 150
    const val ContentFadeInMillis = 220
    const val ContentFadeInDelayMillis = 90
    const val ContentFadeOutMillis = 90

    /** Swapping one button for another in place. */
    const val ButtonSwapMillis = 150

    /** Spatial movement (placement, bounds) settles without overshoot; fades use tweens instead. */
    fun <T> spatialSpring(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow, visibilityThreshold)
}

/**
 * False when the user turned system animations off (animator duration scale 0), so decorative
 * looping animations can fall back to a static state.
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

/** A lazy list row that fades in, fades out and glides to its new place when the list changes. */
fun LazyItemScope.animateListItem(): Modifier = Modifier.animateItem(
    fadeInSpec = tween(Motion.ItemFadeInMillis, easing = Motion.EmphasizedDecelerate),
    placementSpec = Motion.spatialSpring(IntOffset.VisibilityThreshold),
    fadeOutSpec = tween(Motion.ItemFadeOutMillis, easing = Motion.EmphasizedAccelerate),
)
