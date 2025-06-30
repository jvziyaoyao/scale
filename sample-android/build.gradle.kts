import scale.compileSdk
import scale.minSdk
import scale.targetSdk

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("kotlin-android")
}

val kmpResDir = "composeResources/scale.sample_kmp.generated.resources"
tasks.register<Copy>("copySharedResources") {
    from(project(":sample-kmp").file("src/commonMain/composeResources"))
    into(layout.buildDirectory.dir("generated/kmpCopy/$kmpResDir"))
}

android {
    namespace = "com.jvziyaoyao.scale.sample"
    compileSdk = project.compileSdk

    defaultConfig {
        applicationId = "com.jvziyaoyao.scale.sample"
        minSdk = project.minSdk
        targetSdk = project.targetSdk
        versionCode = 1
        versionName = "1.1.0-alpha.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
//    packagingOptions {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }

    sourceSets.getByName("main") {
        assets.srcDirs(
            "src/main/assets",
            layout.buildDirectory.dir("generated/kmpCopy"),
        )
    }

    // composeResources/scale.sample_kmp.generated.resources/files/a350.jpg
    applicationVariants.all {
        this.mergeAssetsProvider.configure {
            dependsOn("copySharedResources")
        }
    }
}

dependencies {
    implementation(project(":scale-image-viewer-classic"))
    implementation(project(":scale-image-viewer"))
    implementation(project(":scale-sampling-decoder"))
//    implementation(project(":scale-sampling-decoder-kmp"))
    implementation(project(":scale-zoomable-view"))

    implementation(project(":sample-kmp"))
//    implementation(libs.scale.image.viewer)
//    implementation(libs.scale.image.viewer.classic)
//    implementation(libs.scale.sampling.decoder)

    implementation(libs.androidx.exif)

    implementation(compose.components.resources)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.jvziyaoyao.origeek.ui)

//    implementation(libs.coil)
    implementation(libs.coil.svg)
//    implementation(libs.coil.gif)
    implementation(libs.coil.compose)

    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.accompanist.systemuicontroller)

    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.util)
    androidTestImplementation(libs.androidx.compose.ui.test)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}