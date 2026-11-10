plugins {
    alias(libs.plugins.offerward.android.library)
    alias(libs.plugins.offerward.hilt)
}

android {
    namespace = "io.github.tugakca.offerward.core.data"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(libs.androidx.dataStore.preferences)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.room.runtime)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
