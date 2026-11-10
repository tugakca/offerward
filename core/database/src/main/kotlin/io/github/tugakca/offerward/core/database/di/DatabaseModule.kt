package io.github.tugakca.offerward.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.tugakca.offerward.core.database.OfferwardDatabase
import io.github.tugakca.offerward.core.database.dao.ApplicationDao
import io.github.tugakca.offerward.core.database.dao.JobDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OfferwardDatabase =
        Room.databaseBuilder(context, OfferwardDatabase::class.java, "offerward.db").build()

    @Provides
    fun provideJobDao(database: OfferwardDatabase): JobDao = database.jobDao()

    @Provides
    fun provideApplicationDao(database: OfferwardDatabase): ApplicationDao = database.applicationDao()
}
