rootProject.name = "scale"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.android") version "2.1.0"
        id("com.android.library") version "8.4.0"
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}