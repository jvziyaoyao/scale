import scale.compileSdk
import scale.minSdk

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

kotlin {

    androidLibrary {
        namespace = "com.jvziyaoyao.scale.sample"
        compileSdk = project.compileSdk
        minSdk = project.minSdk
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SampleKmpKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":scale-image-viewer"))
                implementation(project(":scale-zoomable-view"))
                implementation(project(":scale-sampling-decoder"))

                implementation(compose.materialIconsExtended)

                implementation(compose.material)
                implementation(compose.material3)

                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)

                implementation(libs.kotlin.stdlib)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                api(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.org.jetbrains.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                // Ktor
                implementation(libs.ktor.client.okhttp)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}