package io.github.tugakca.offerward.feature.discover.ui

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.github.tugakca.offerward.core.model.ApplicationStatus
import io.github.tugakca.offerward.core.model.Job
import io.github.tugakca.offerward.core.model.Salary
import io.github.tugakca.offerward.feature.discover.R
import java.time.Instant
import java.util.Currency
import kotlin.math.roundToInt

/** "Worldwide", "Turkey, Germany" or "Open in 42 countries". */
@Composable
internal fun Job.locationLabel(): String = when {
    isWorldwide -> stringResource(R.string.feature_discover_worldwide)

    locationRestrictions.size <= 2 -> locationRestrictions.joinToString()

    else -> pluralStringResource(
        R.plurals.feature_discover_open_in_countries,
        locationRestrictions.size,
        locationRestrictions.size,
    )
}

/** The stage name shown on a tracked job's card. */
@get:StringRes
internal val ApplicationStatus.label: Int
    get() = when (this) {
        ApplicationStatus.SAVED -> R.string.feature_discover_status_saved
        ApplicationStatus.APPLIED -> R.string.feature_discover_status_applied
        ApplicationStatus.INTERVIEW -> R.string.feature_discover_status_interview
        ApplicationStatus.OFFER -> R.string.feature_discover_status_offer
        ApplicationStatus.REJECTED -> R.string.feature_discover_status_rejected
    }

/** "2 hours ago", relative to [now]. */
internal fun Instant.relativeTo(now: Instant): String = DateUtils.getRelativeTimeSpanString(
    toEpochMilli(),
    now.toEpochMilli(),
    DateUtils.MINUTE_IN_MILLIS,
).toString()

/** "$70k–90k / year", in the app's language. Null when the listing has no usable salary. */
@Composable
internal fun Salary.formatted(): String? {
    val symbol = currency?.let { runCatching { Currency.getInstance(it).symbol }.getOrNull() ?: "$it " } ?: ""
    val low = min
    val high = max
    val range = when {
        low != null && high != null && low != high -> "${low.compact()}–${high.compact()}"
        low != null -> low.compact()
        high != null -> high.compact()
        else -> return null
    }
    val amount = "$symbol$range"
    val perPeriod = when (period) {
        "annual" -> R.string.feature_discover_salary_per_year
        "monthly" -> R.string.feature_discover_salary_per_month
        "weekly" -> R.string.feature_discover_salary_per_week
        "hourly" -> R.string.feature_discover_salary_per_hour
        else -> return amount
    }
    return stringResource(perPeriod, amount)
}

private fun Double.compact(): String = if (this >= 1_000) "${(this / 1_000).roundToInt()}k" else roundToInt().toString()
