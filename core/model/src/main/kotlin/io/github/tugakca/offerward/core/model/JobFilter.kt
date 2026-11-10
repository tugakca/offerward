package io.github.tugakca.offerward.core.model

/** Filters applied on the device to the cached jobs; changing them never hits the network. */
data class JobFilter(
    val query: String = "",
    val androidOnly: Boolean = true,
    val seniorOnly: Boolean = false,
    val worldwideOnly: Boolean = false,
)
