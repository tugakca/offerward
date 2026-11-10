package io.github.tugakca.offerward.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.tugakca.offerward.core.network.BuildConfig
import io.github.tugakca.offerward.core.network.JobNetworkDataSource
import io.github.tugakca.offerward.core.network.himalayas.HimalayasApi
import io.github.tugakca.offerward.core.network.himalayas.HimalayasJobDataSource
import io.github.tugakca.offerward.core.network.jobicy.JobicyApi
import io.github.tugakca.offerward.core.network.jobicy.JobicyJobDataSource
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Every job board is bound into one `Set<JobNetworkDataSource>`. Adding a board is a new data
 * source plus one `@IntoSet` line here; the repository does not change.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {

    @Binds
    @IntoSet
    abstract fun bindHimalayas(impl: HimalayasJobDataSource): JobNetworkDataSource

    @Binds
    @IntoSet
    abstract fun bindJobicy(impl: JobicyJobDataSource): JobNetworkDataSource

    companion object {
        @Provides
        @Singleton
        fun provideJson(): Json = networkJson

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "Offerward-Android")
                        .build(),
                )
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

        @Provides
        @Singleton
        fun provideHimalayasApi(okHttpClient: OkHttpClient, json: Json): HimalayasApi =
            retrofit("https://himalayas.app/", okHttpClient, json).create(HimalayasApi::class.java)

        @Provides
        @Singleton
        fun provideJobicyApi(okHttpClient: OkHttpClient, json: Json): JobicyApi =
            retrofit("https://jobicy.com/", okHttpClient, json).create(JobicyApi::class.java)

        private fun retrofit(baseUrl: String, okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}

/** Lenient on purpose: the APIs add fields over time and sometimes send nulls. */
internal val networkJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
