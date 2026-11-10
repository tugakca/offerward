package io.github.tugakca.offerward.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.tugakca.offerward.core.database.dao.ApplicationDao
import io.github.tugakca.offerward.core.database.dao.JobDao
import io.github.tugakca.offerward.core.database.model.ApplicationEntity
import io.github.tugakca.offerward.core.database.model.JobEntity
import io.github.tugakca.offerward.core.database.util.Converters

@Database(
    entities = [JobEntity::class, ApplicationEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OfferwardDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao

    abstract fun applicationDao(): ApplicationDao
}
