package io.github.tugakca.offerward.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Upgrades real databases written with the exported schemas. A user who installed version 1 must
 * keep their cached jobs and, above all, their applications.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), OfferwardDatabase::class.java)

    @Test
    fun migrate1To2KeepsDataAndMarksOldJobsAsHimalayas() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO jobs (id, title, company_name, company_logo_url, employment_type, seniority,
                    location_restrictions, is_worldwide, is_senior, salary_min, salary_max, salary_currency,
                    salary_period, excerpt, description_html, published_at, expires_at, application_url, source_url)
                VALUES ('https://himalayas.app/j/1', 'Android Engineer', 'Acme', NULL, NULL, '[]', '[]', 1, 0,
                    NULL, NULL, NULL, NULL, '', '<p>Job</p>', 1, NULL, 'https://acme.com/apply', 'https://himalayas.app/j/1')
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO applications (job_id, job_title, company_name, company_logo_url, application_url, status,
                    notes, interview_at, created_at, updated_at)
                VALUES ('https://himalayas.app/j/1', 'Android Engineer', 'Acme', NULL, 'https://acme.com/apply',
                    'INTERVIEW', 'Bring portfolio', 5, 1, 2)
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true).use { db ->
            db.query("SELECT source FROM jobs").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("HIMALAYAS")
            }
            db.query("SELECT status, notes FROM applications").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("INTERVIEW")
                assertThat(cursor.getString(1)).isEqualTo("Bring portfolio")
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
