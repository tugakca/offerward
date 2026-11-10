import com.android.build.api.dsl.LibraryExtension
import io.github.tugakca.offerward.buildlogic.TARGET_SDK
import io.github.tugakca.offerward.buildlogic.configureKotlinAndroid
import io.github.tugakca.offerward.buildlogic.configureSpotless
import io.github.tugakca.offerward.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = TARGET_SDK
                lint.targetSdk = TARGET_SDK
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
                // Resources in ":core:designsystem" must be prefixed with "core_designsystem_",
                // so two modules can never silently override each other's resources.
                resourcePrefix = path.split("""\W""".toRegex()).drop(1).distinct()
                    .joinToString(separator = "_").lowercase() + "_"
            }
            configureSpotless()
            dependencies {
                "testImplementation"(libs.findLibrary("kotlin.test").get())
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
