package io.github.tugakca.offerward.core.database.util

import androidx.room.TypeConverter
import io.github.tugakca.offerward.core.model.ApplicationStatus
import java.time.Instant
import kotlinx.serialization.json.Json

internal class Converters {
    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun stringListToJson(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = Json.decodeFromString(value)

    // Stored by name, not ordinal, so reordering the enum can never corrupt saved data.
    @TypeConverter
    fun statusToString(value: ApplicationStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): ApplicationStatus = ApplicationStatus.valueOf(value)
}
