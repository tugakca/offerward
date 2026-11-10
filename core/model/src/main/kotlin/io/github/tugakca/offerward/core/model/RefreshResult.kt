package io.github.tugakca.offerward.core.model

import java.time.Instant

/** Outcome of asking the repository to refresh its cache from the network. */
sealed interface RefreshResult {
    data class Updated(val jobCount: Int) : RefreshResult

    /** Skipped because the cache is fresh enough; the next refresh is allowed at [nextAllowedAt]. */
    data class Skipped(val nextAllowedAt: Instant) : RefreshResult

    data class Failed(val cause: Throwable) : RefreshResult
}
