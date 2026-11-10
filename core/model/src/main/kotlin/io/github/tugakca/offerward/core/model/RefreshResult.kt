package io.github.tugakca.offerward.core.model

import java.time.Instant

/** Outcome of asking the repository to refresh its cache from the network. */
sealed interface RefreshResult {
    /**
     * At least one source answered. Jobs from [unavailableSources] could not be fetched this time;
     * their previously cached jobs are kept.
     */
    data class Updated(val jobCount: Int, val unavailableSources: Set<JobSource> = emptySet()) : RefreshResult

    /** Skipped because the cache is fresh enough; the next refresh is allowed at [nextAllowedAt]. */
    data class Skipped(val nextAllowedAt: Instant) : RefreshResult

    /** No source answered. The cache is unchanged. */
    data class Failed(val cause: Throwable) : RefreshResult
}
