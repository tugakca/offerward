package io.github.tugakca.offerward.feature.applications.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.tugakca.offerward.feature.applications.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Picks a date, then a time, in the device's time zone. Material's DatePicker works in UTC
 * midnights, so the conversion happens here and nowhere else.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InterviewPicker(
    initial: Instant?,
    onPicked: (Instant) -> Unit,
    onDismiss: () -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val start = initial?.atZone(zone)?.toLocalDateTime()
        ?: LocalDate.now(zone).plusDays(1).atTime(DEFAULT_TIME)
    var pickedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    val date = pickedDate
    if (date == null) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = start.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            pickedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                    },
                    enabled = dateState.selectedDateMillis != null,
                ) { Text(stringResource(R.string.feature_applications_next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_applications_cancel)) }
            },
        ) {
            DatePicker(state = dateState)
        }
    } else {
        val timeState = rememberTimePickerState(
            initialHour = start.hour,
            initialMinute = start.minute,
            is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current),
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timeState.hour, timeState.minute)
                    onPicked(LocalDateTime.of(date, time).atZone(zone).toInstant())
                }) { Text(stringResource(R.string.feature_applications_ok)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_applications_cancel)) }
            },
        )
    }
}

private val DEFAULT_TIME: LocalTime = LocalTime.of(10, 0)
