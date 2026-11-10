package io.github.tugakca.offerward.core.network

import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal inline fun <reified T> testApi(server: MockWebServer, json: Json): T = Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(T::class.java)

internal fun Any.fixture(name: String): String = checkNotNull(javaClass.classLoader?.getResource(name)).readText()

internal fun jsonResponse(body: String) = MockResponse.Builder()
    .addHeader("Content-Type", "application/json")
    .body(body)
    .build()
