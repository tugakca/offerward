plugins {
    alias(libs.plugins.offerward.android.feature)
}

android {
    namespace = "io.github.tugakca.offerward.feature.discover"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.androidx.browser)
}
