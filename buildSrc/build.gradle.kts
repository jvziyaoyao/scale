plugins {
    `kotlin-dsl-base`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.gradle.plugin.android)
    implementation(libs.gradle.plugin.jetbrains.compose)
    implementation(libs.gradle.plugin.compose.compiler)
    implementation(libs.gradle.plugin.kotlin)
    implementation(libs.gradle.plugin.maven.publish)
    implementation(libs.gradle.plugin.dokka)
}