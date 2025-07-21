import scale.compileSdk
import scale.minSdk

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka")
}

kotlin {
    androidLibrary {
        namespace = "com.jvziyaoyao.scale.zoomable"
        compileSdk = project.compileSdk
        minSdk = project.minSdk
    }

    val xcfName = "zoomableViewKit"

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
            dependencies {}
        }

        iosMain {
            dependencies {}
        }
    }

}