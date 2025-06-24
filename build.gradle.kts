import scale.versionName

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin) apply false
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}

tasks.dokkaHtmlMultiModule {
    moduleName.set("Scale")
    moduleVersion.set(project.versionName)
    outputDirectory.set(file("$rootDir/doc/docs/reference"))
}