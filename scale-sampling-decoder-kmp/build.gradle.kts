import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import scale.compileSdk
import scale.minSdk

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {

    androidLibrary {
        namespace = "com.jvziyaoyao.scale.decoder.kmp"
        compileSdk = project.compileSdk
        minSdk = project.minSdk
    }

    val xcfName = "scale-sampling-decoder-kmpKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":scale-image-viewer"))
                implementation(project(":scale-zoomable-view"))

                implementation("com.ionspin.kotlin:bignum:0.3.10")

                implementation(libs.kotlin.stdlib)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.components.resources)
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
                implementation(libs.androidx.exif)
            }
        }

//        named("nonAndroidMain") {
//            dependencies {
//                implementation("org.jetbrains.skiko:skiko:0.9.4")
//            }
//        }

        iosMain {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:0.9.4")
            }
        }
    }

}