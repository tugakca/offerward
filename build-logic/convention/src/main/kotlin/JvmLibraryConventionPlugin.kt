import io.github.tugakca.offerward.buildlogic.configureKotlinJvm
import io.github.tugakca.offerward.buildlogic.configureSpotless
import io.github.tugakca.offerward.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

abstract class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
            configureSpotless()
            dependencies {
                "testImplementation"(libs.findLibrary("kotlin.test").get())
            }
        }
    }
}
