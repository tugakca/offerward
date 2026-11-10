import io.github.tugakca.offerward.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module is a Compose UI library with Hilt and Navigation 3.
 * Feature modules never depend on each other; the app module wires them together.
 */
abstract class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "offerward.android.library")
            apply(plugin = "offerward.android.library.compose")
            apply(plugin = "offerward.hilt")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:model"))
                "implementation"(libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
                "implementation"(libs.findLibrary("androidx.hilt.lifecycle.viewModelCompose").get())
                "implementation"(libs.findLibrary("androidx.navigation3.runtime").get())
                "implementation"(libs.findLibrary("kotlinx.serialization.json").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
                "testImplementation"(libs.findLibrary("truth").get())
            }
        }
    }
}
