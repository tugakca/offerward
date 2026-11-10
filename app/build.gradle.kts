plugins {
    alias(libs.plugins.offerward.android.application)
    alias(libs.plugins.offerward.android.application.compose)
    alias(libs.plugins.offerward.hilt)
}

android {
    namespace = "io.github.tugakca.offerward"

    defaultConfig {
        applicationId = "io.github.tugakca.offerward"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.feature.applications)
    implementation(projects.feature.discover)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.testManifest)
}
