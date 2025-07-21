import scale.applyScaleHierarchyTemplate
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
    applyScaleHierarchyTemplate()

    androidLibrary {
        namespace = "com.jvziyaoyao.scale.image.sampling"
        compileSdk = project.compileSdk
        minSdk = project.minSdk
    }

    val xcfName = "samplingDecoderKit"

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

                implementation(libs.bignum)

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

        named("nonAndroidMain") {
            dependencies {
                implementation(libs.skiko)
            }
        }

        iosMain {
            dependencies {}
        }
    }

}