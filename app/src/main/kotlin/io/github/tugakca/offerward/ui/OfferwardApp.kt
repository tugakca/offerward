package io.github.tugakca.offerward.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import io.github.tugakca.offerward.core.designsystem.motion.Motion
import io.github.tugakca.offerward.feature.applications.navigation.ApplicationDetailKey
import io.github.tugakca.offerward.feature.applications.navigation.applicationsEntries
import io.github.tugakca.offerward.feature.discover.navigation.JobDetailKey
import io.github.tugakca.offerward.feature.discover.navigation.discoverEntries

@Composable
fun OfferwardApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(TopLevelDestination.entries.first().key)
    val currentTab = backStack.currentTopLevelDestination()
    val showBottomBar = backStack.lastOrNull() in TopLevelDestination.entries.map { it.key }

    Scaffold(
        modifier = modifier,
        // Without the bar, detail screens handle the navigation bar inset themselves, so their
        // own bottom bars can draw behind it.
        contentWindowInsets = if (showBottomBar) {
            ScaffoldDefaults.contentWindowInsets
        } else {
            ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
        },
        bottomBar = {
            // Detail screens use the full height; the bar only belongs to the top-level tabs.
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(Motion.SharedAxisEnterMillis, easing = Motion.EmphasizedDecelerate)) {
                    it
                },
                exit = slideOutVertically(tween(Motion.SharedAxisExitMillis, easing = Motion.EmphasizedAccelerate)) {
                    it
                },
            ) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == currentTab,
                            onClick = { backStack.navigateToTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val provider = entryProvider {
            discoverEntries(
                onJobClick = { jobId -> backStack.add(JobDetailKey(jobId)) },
                onOpenApplication = { jobId -> backStack.add(ApplicationDetailKey(jobId)) },
                onBack = { backStack.removeLastOrNull() },
            )
            applicationsEntries(
                onApplicationClick = { jobId -> backStack.add(ApplicationDetailKey(jobId)) },
                onBrowseJobs = { backStack.navigateToTopLevel(TopLevelDestination.DISCOVER) },
                onBack = { backStack.removeLastOrNull() },
            )
        }
        val tabContentKeys = remember(provider) {
            TopLevelDestination.entries.map { provider(it.key).contentKey }.toSet()
        }
        val forward: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
            if (isTabSwitch(tabContentKeys)) fadeThrough() else sharedAxisForward()
        }
        val backward: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
            if (isTabSwitch(tabContentKeys)) fadeThrough() else sharedAxisBackward()
        }
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = forward,
            popTransitionSpec = backward,
            predictivePopTransitionSpec = { backward() },
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = provider,
        )
    }
}

/** Switching between two tabs, as opposed to going into or out of a detail screen. */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.isTabSwitch(tabContentKeys: Set<Any>) =
    initialState.entries.last().contentKey in tabContentKeys && targetState.entries.last().contentKey in tabContentKeys

private fun fadeThrough(): ContentTransform = (
    fadeIn(
        tween(
            Motion.FadeThroughInMillis,
            delayMillis = Motion.FadeThroughOutMillis,
            easing = Motion.EmphasizedDecelerate,
        ),
    ) + scaleIn(
        initialScale = Motion.FadeThroughInitialScale,
        animationSpec = tween(
            Motion.FadeThroughInMillis,
            delayMillis = Motion.FadeThroughOutMillis,
            easing = Motion.EmphasizedDecelerate,
        ),
    )
    ) togetherWith fadeOut(tween(Motion.FadeThroughOutMillis, easing = Motion.EmphasizedAccelerate))

/** Shared axis on X, drilling in: the new screen slides in from the end while the old one drifts away. */
private fun sharedAxisForward(): ContentTransform = (
    slideInHorizontally(tween(Motion.SharedAxisEnterMillis, easing = Motion.EmphasizedDecelerate)) { it / 8 } +
        fadeIn(tween(Motion.FadeInMillis, delayMillis = Motion.FadeInDelayMillis))
    ) togetherWith (
    slideOutHorizontally(tween(Motion.SharedAxisExitMillis, easing = Motion.EmphasizedAccelerate)) { -it / 8 } +
        fadeOut(tween(Motion.FadeOutMillis))
    )

/** The mirror of [sharedAxisForward], used for back and predictive back. */
private fun sharedAxisBackward(): ContentTransform = (
    slideInHorizontally(tween(Motion.SharedAxisEnterMillis, easing = Motion.EmphasizedDecelerate)) { -it / 8 } +
        fadeIn(tween(Motion.FadeInMillis, delayMillis = Motion.FadeInDelayMillis))
    ) togetherWith (
    slideOutHorizontally(tween(Motion.SharedAxisExitMillis, easing = Motion.EmphasizedAccelerate)) { it / 8 } +
        fadeOut(tween(Motion.FadeOutMillis))
    )

private fun NavBackStack<NavKey>.currentTopLevelDestination(): TopLevelDestination {
    val topLevelKeys = TopLevelDestination.entries.associateBy { it.key }
    return lastOrNull { it in topLevelKeys }?.let(topLevelKeys::getValue)
        ?: TopLevelDestination.entries.first()
}

/**
 * The start tab is always the root of the back stack, so system back from any other tab
 * returns to it before leaving the app. Switching tabs drops the previous tab's screens.
 */
private fun NavBackStack<NavKey>.navigateToTopLevel(destination: TopLevelDestination) {
    while (size > 1) removeAt(lastIndex)
    if (destination != TopLevelDestination.entries.first()) add(destination.key)
}
