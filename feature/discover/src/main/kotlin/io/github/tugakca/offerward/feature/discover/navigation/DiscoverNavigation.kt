package io.github.tugakca.offerward.feature.discover.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.tugakca.offerward.feature.discover.DiscoverScreen
import kotlinx.serialization.Serializable

@Serializable
data object DiscoverRoute : NavKey

fun EntryProviderScope<NavKey>.discoverEntries() {
    entry<DiscoverRoute> {
        DiscoverScreen()
    }
}
