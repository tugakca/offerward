package io.github.tugakca.offerward.feature.applications.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.tugakca.offerward.feature.applications.ApplicationsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ApplicationsRoute : NavKey

fun EntryProviderScope<NavKey>.applicationsEntries() {
    entry<ApplicationsRoute> {
        ApplicationsScreen()
    }
}
