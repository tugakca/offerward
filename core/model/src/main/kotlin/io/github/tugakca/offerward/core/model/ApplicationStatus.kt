package io.github.tugakca.offerward.core.model

/**
 * Where a job application stands. The order of the entries is the order of the pipeline,
 * so the UI can group and sort applications by [ordinal].
 */
enum class ApplicationStatus {
    SAVED,
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED,
    ;

    val isClosed: Boolean get() = this == OFFER || this == REJECTED
}
