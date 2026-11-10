package io.github.tugakca.offerward.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.tugakca.offerward.feature.applications.navigation.applicationsEntries
import io.github.tugakca.offerward.feature.discover.navigation.JobDetailKey
import io.github.tugakca.offerward.feature.discover.navigation.discoverEntries

@Composable
fun OfferwardApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(TopLevelDestination.entries.first().key)
    val currentTab = backStack.currentTopLevelDestination()

    Scaffold(
        modifier = modifier,
        bottomBar = {
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
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                discoverEntries(
                    onJobClick = { jobId -> backStack.add(JobDetailKey(jobId)) },
                    onBack = { backStack.removeLastOrNull() },
                )
                applicationsEntries()
            },
        )
    }
}

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
