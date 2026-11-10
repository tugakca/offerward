package io.github.tugakca.offerward.core.model

/**
 * A job board Offerward reads from. The order is the priority when the same job is listed on
 * more than one board: the first source's copy is kept.
 */
enum class JobSource(val displayName: String) {
    /** First because its Apply links usually go straight to the employer's applicant tracker. */
    HIMALAYAS("Himalayas"),
    JOBICY("Jobicy"),
}
