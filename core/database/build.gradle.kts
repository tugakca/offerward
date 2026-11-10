plugins {
    alias(libs.plugins.offerward.android.library)
    alias(libs.plugins.offerward.android.room)
    alias(libs.plugins.offerward.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.tugakca.offerward.core.database"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
