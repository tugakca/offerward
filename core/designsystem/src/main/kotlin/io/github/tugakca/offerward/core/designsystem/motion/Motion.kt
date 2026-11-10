package io.github.tugakca.offerward.core.designsystem.motion

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
