plugins {
    alias(libs.plugins.offerward.android.library)
    alias(libs.plugins.offerward.android.library.compose)
}

android {
    namespace = "io.github.tugakca.offerward.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.iconsCore)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
