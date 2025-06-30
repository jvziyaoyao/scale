import scale.versionName

plugins {
    id("org.jetbrains.dokka")
}

tasks.dokkaHtmlMultiModule {
    moduleName.set("Scale")
    moduleVersion.set(project.versionName)
    outputDirectory.set(file("$rootDir/doc/docs/reference"))
}