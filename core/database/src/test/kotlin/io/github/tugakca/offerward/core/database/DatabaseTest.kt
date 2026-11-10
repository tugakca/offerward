package io.github.tugakca.offerward.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Runs DAO tests against a real in-memory SQLite database on the JVM. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
abstract class DatabaseTest {
    protected lateinit var db: OfferwardDatabase

    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OfferwardDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }
}
