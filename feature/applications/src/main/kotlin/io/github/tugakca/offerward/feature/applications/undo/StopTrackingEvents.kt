package io.github.tugakca.offerward.feature.applications.undo

import io.github.tugakca.offerward.core.model.JobApplication
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Tells the rest of the app that an application was just removed, together with its full state so
 * it can be put back. The detail screen closes right after stopping, so the "Undo" offer is shown
 * by whoever is on screen next; the channel holds the event until that collector is there.
 */
@Singleton
class StopTrackingEvents @Inject constructor() {
    private val channel = Channel<JobApplication>(Channel.BUFFERED)

    val stopped: Flow<JobApplication> = channel.receiveAsFlow()

    suspend fun publish(application: JobApplication) = channel.send(application)
}
