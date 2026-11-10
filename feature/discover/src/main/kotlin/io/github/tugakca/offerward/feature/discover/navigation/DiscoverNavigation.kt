package io.github.tugakca.offerward.feature.discover.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.tugakca.offerward.feature.discover.detail.JobDetailRoute
import io.github.tugakca.offerward.feature.discover.list.DiscoverRoute
import kotlinx.serialization.Serializable

@Serializable
data object DiscoverKey : NavKey

@Serializable
data class JobDetailKey(val jobId: String) : NavKey

fun EntryProviderScope<NavKey>.discoverEntries(onJobClick: (jobId: String) -> Unit, onBack: () -> Unit) {
    entry<DiscoverKey> {
        DiscoverRoute(onJobClick = onJobClick)
    }
    entry<JobDetailKey> { key ->
        JobDetailRoute(jobId = key.jobId, onBack = onBack)
    }
}
