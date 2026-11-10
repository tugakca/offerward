package io.github.tugakca.offerward.feature.applications

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.feature.applications.undo.StopTrackingEvents
import io.github.tugakca.offerward.feature.applications.undo.StopTrackingUndoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopTrackingUndoViewModelTest {

    private val repository = FakeApplicationRepository()
    private val events = StopTrackingEvents()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun anEventPublishedBeforeAnyoneListensIsStillDelivered() = runTest {
        val removed = application("1", status = ApplicationStatus.OFFER, notes = "Offer in hand")
        events.publish(removed)
        val viewModel = StopTrackingUndoViewModel(events, repository)

        viewModel.stopped.test { assertThat(awaitItem()).isEqualTo(removed) }
    }

    @Test
    fun undoRestoresTheApplicationWithItsStageAndNotes() {
        val removed = application("1", status = ApplicationStatus.OFFER, notes = "Offer in hand")
        val viewModel = StopTrackingUndoViewModel(events, repository)

        viewModel.undo(removed)

        assertThat(repository.applications.value).containsExactly(removed)
    }
}
