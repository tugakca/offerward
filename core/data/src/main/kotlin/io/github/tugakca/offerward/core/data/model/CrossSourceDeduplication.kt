package io.github.tugakca.offerward.core.data.model

import io.github.tugakca.offerward.core.database.model.JobEntity
import java.text.Normalizer

/**
 * Companies often post the same job on several boards. Two listings are treated as the same job
 * when the company and title match after normalising case, accents, punctuation and spacing.
 * "Acme, Inc." and "ACME Inc" match; "Android Engineer" and "Senior Android Engineer" do not.
 *
 * When they collide, the copy from the higher-priority source (earlier in
 * [io.github.tugakca.offerward.core.model.JobSource]) wins; within a source, the newest wins.
 * Jobs keep their original order otherwise.
 */
internal fun List<JobEntity>.withoutCrossSourceDuplicates(): List<JobEntity> {
    val winners = HashMap<String, JobEntity>()
    for (job in this) {
        val key = job.dedupKey()
        val current = winners[key]
        if (current == null || job.beats(current)) winners[key] = job
    }
    val kept = winners.values.mapTo(HashSet()) { it.id }
    return filter { it.id in kept }
}

private fun JobEntity.beats(other: JobEntity): Boolean = when {
    source != other.source -> source.ordinal < other.source.ordinal
    else -> publishedAt.isAfter(other.publishedAt)
}

internal fun JobEntity.dedupKey(): String {
    val company = companyName.words().dropLastWhile { it in companySuffixes }
    return company.joinToString(" ") + "|" + title.words().joinToString(" ")
}

private val companySuffixes = setOf("inc", "llc", "ltd", "gmbh", "corp", "co", "sa", "bv", "ag")
private val nonAlphanumeric = Regex("""[^a-z0-9]+""")
private val diacritics = Regex("""\p{M}+""")

/** Lower-case ASCII words: accents dropped, punctuation and spacing ignored. */
private fun String.words(): List<String> = Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
    .replace(diacritics, "")
    .split(nonAlphanumeric)
    .filter(String::isNotEmpty)
