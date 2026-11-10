package io.github.tugakca.offerward.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.tugakca.offerward.core.data.repository.ApplicationRepository
import io.github.tugakca.offerward.core.data.repository.JobRepository
import io.github.tugakca.offerward.core.data.repository.LocalApplicationRepository
import io.github.tugakca.offerward.core.data.repository.OfflineFirstJobRepository
import io.github.tugakca.offerward.core.data.settings.SyncSettings
import java.time.Clock
import java.util.Locale
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindJobRepository(impl: OfflineFirstJobRepository): JobRepository

    @Binds
    abstract fun bindApplicationRepository(impl: LocalApplicationRepository): ApplicationRepository

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemUTC()

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

        @Provides
        @Named(SyncSettings.DEFAULT_COUNTRY)
        fun provideDefaultCountryCode(): String = Locale.getDefault().country
    }
}
