pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }
}
rootProject.name = "scale"
include(":sample")
include(":scale-image-viewer")
include(":scale-image-viewer-classic")
include(":scale-zoomable-view")
include(":scale-sampling-decoder")
include(":shared")
include(":img")
include(":scale-sampling-decoder-kmp")
