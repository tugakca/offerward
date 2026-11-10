import com.android.build.api.dsl.ApplicationExtension
import io.github.tugakca.offerward.buildlogic.TARGET_SDK
import io.github.tugakca.offerward.buildlogic.configureKotlinAndroid
import io.github.tugakca.offerward.buildlogic.configureSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

abstract class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = TARGET_SDK
                testOptions.animationsDisabled = true
            }
            configureSpotless()
        }
    }
}
