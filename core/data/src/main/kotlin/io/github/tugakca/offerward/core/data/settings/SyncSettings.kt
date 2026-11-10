package io.github.tugakca.offerward.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Small key-value settings the data layer needs to survive process death. */
internal class SyncSettings @Inject constructor(private val dataStore: DataStore<Preferences>) {
    suspend fun lastJobsFetchAt(): Instant? = dataStore.data.first()[LAST_JOBS_FETCH_AT]?.let(Instant::ofEpochMilli)

    suspend fun setLastJobsFetchAt(instant: Instant) {
        dataStore.edit { it[LAST_JOBS_FETCH_AT] = instant.toEpochMilli() }
    }

    private companion object {
        val LAST_JOBS_FETCH_AT = longPreferencesKey("last_jobs_fetch_at")
    }
}
