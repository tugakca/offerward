package io.github.tugakca.offerward.feature.applications.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.tugakca.offerward.feature.applications.detail.ApplicationDetailRoute
import io.github.tugakca.offerward.feature.applications.list.ApplicationsRoute
import kotlinx.serialization.Serializable

@Serializable
data object ApplicationsKey : NavKey

@Serializable
data class ApplicationDetailKey(val jobId: String) : NavKey

fun EntryProviderScope<NavKey>.applicationsEntries(
    onApplicationClick: (jobId: String) -> Unit,
    onBrowseJobs: () -> Unit,
    onBack: () -> Unit,
) {
    entry<ApplicationsKey> {
        ApplicationsRoute(onApplicationClick = onApplicationClick, onBrowseJobs = onBrowseJobs)
    }
    entry<ApplicationDetailKey> { key ->
        ApplicationDetailRoute(jobId = key.jobId, onBack = onBack)
    }
}
