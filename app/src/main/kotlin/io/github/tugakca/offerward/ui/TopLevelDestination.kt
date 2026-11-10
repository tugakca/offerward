package io.github.tugakca.offerward.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import io.github.tugakca.offerward.R
import io.github.tugakca.offerward.feature.applications.navigation.ApplicationsRoute
import io.github.tugakca.offerward.feature.discover.navigation.DiscoverRoute

/** The tabs in the bottom bar. The first entry is the start destination. */
enum class TopLevelDestination(val key: NavKey, val icon: ImageVector, @StringRes val label: Int) {
    DISCOVER(DiscoverRoute, Icons.Filled.Search, R.string.tab_discover),
    APPLICATIONS(ApplicationsRoute, Icons.AutoMirrored.Filled.List, R.string.tab_applications),
}
