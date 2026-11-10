package io.github.tugakca.offerward.feature.applications

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.feature.applications.detail.ApplicationDetailAction
import io.github.tugakca.offerward.feature.applications.detail.ApplicationDetailEffect
import io.github.tugakca.offerward.feature.applications.detail.ApplicationDetailUiState
import io.github.tugakca.offerward.feature.applications.detail.ApplicationDetailViewModel
import java.time.Instant
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
class ApplicationDetailViewModelTest {

    private val repository = FakeApplicationRepository(listOf(application("1", notes = "first")))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsTheApplicationAndSeedsTheNotesDraft() {
        val viewModel = ApplicationDetailViewModel("1", repository)

        val state = viewModel.uiState.value as ApplicationDetailUiState.Loaded
        assertThat(state.application.jobId).isEqualTo("1")
        assertThat(state.notesDraft).isEqualTo("first")
    }

    @Test
    fun anUnknownJobIsNotFound() {
        val viewModel = ApplicationDetailViewModel("missing", repository)

        assertThat(viewModel.uiState.value).isEqualTo(ApplicationDetailUiState.NotFound)
    }

    @Test
    fun changingTheStageIsSavedAndShown() {
        val viewModel = ApplicationDetailViewModel("1", repository)

        viewModel.onAction(ApplicationDetailAction.ChangeStatus(ApplicationStatus.OFFER))

        assertThat((viewModel.uiState.value as ApplicationDetailUiState.Loaded).application.status)
            .isEqualTo(ApplicationStatus.OFFER)
    }

    @Test
    fun typingIsSavedButNeverOverwrittenByTheDatabaseEcho() {
        val viewModel = ApplicationDetailViewModel("1", repository)

        viewModel.onAction(ApplicationDetailAction.NotesChanged("first n"))
        viewModel.onAction(ApplicationDetailAction.NotesChanged("first no"))
        // Something else changes the row (and re-emits it) while the user is typing.
        viewModel.onAction(ApplicationDetailAction.ChangeStatus(ApplicationStatus.APPLIED))

        assertThat((viewModel.uiState.value as ApplicationDetailUiState.Loaded).notesDraft).isEqualTo("first no")
        assertThat(repository.notesWrites.last()).isEqualTo("first no")
    }

    @Test
    fun theInterviewDateCanBeSetAndCleared() {
        val viewModel = ApplicationDetailViewModel("1", repository)
        val at = Instant.parse("2026-11-12T10:00:00Z")

        viewModel.onAction(ApplicationDetailAction.SetInterview(at))
        assertThat((viewModel.uiState.value as ApplicationDetailUiState.Loaded).application.interviewAt).isEqualTo(at)

        viewModel.onAction(ApplicationDetailAction.SetInterview(null))
        assertThat((viewModel.uiState.value as ApplicationDetailUiState.Loaded).application.interviewAt).isNull()
    }

    @Test
    fun stopTrackingDeletesClosesTheScreenAndNeverFlashesNotFound() = runTest {
        val viewModel = ApplicationDetailViewModel("1", repository)

        viewModel.effects.test {
            viewModel.onAction(ApplicationDetailAction.StopTracking)

            assertThat(awaitItem()).isEqualTo(ApplicationDetailEffect.Closed)
        }
        assertThat(repository.applications.value).isEmpty()
        assertThat(viewModel.uiState.value).isInstanceOf(ApplicationDetailUiState.Loaded::class.java)
    }
}
