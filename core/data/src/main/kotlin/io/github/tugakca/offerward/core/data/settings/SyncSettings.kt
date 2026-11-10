package io.github.tugakca.offerward.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Small key-value settings the data layer needs to survive process death. */
internal class SyncSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:Named(DEFAULT_COUNTRY) private val defaultCountryCode: String,
) {
    /** ISO country the search is for; empty means "any country". Defaults to the device region. */
    val countryCode: Flow<String> = dataStore.data.map { it[COUNTRY_CODE] ?: defaultCountryCode }

    suspend fun lastJobsFetchAt(): Instant? = dataStore.data.first()[LAST_JOBS_FETCH_AT]?.let(Instant::ofEpochMilli)

    suspend fun setLastJobsFetchAt(instant: Instant?) {
        dataStore.edit { prefs ->
            if (instant ==
                null
            ) {
                prefs.remove(LAST_JOBS_FETCH_AT)
            } else {
                prefs[LAST_JOBS_FETCH_AT] = instant.toEpochMilli()
            }
        }
    }

    /** Changing the country makes the cached jobs stale, so the next refresh is never skipped. */
    suspend fun setCountryCode(code: String) {
        dataStore.edit { prefs ->
            prefs[COUNTRY_CODE] = code
            prefs.remove(LAST_JOBS_FETCH_AT)
        }
    }

    companion object {
        const val DEFAULT_COUNTRY = "defaultCountryCode"
        private val COUNTRY_CODE = stringPreferencesKey("country_code")
        private val LAST_JOBS_FETCH_AT = longPreferencesKey("last_jobs_fetch_at")
    }
}
