package scale

import org.gradle.api.Project

val Project.minSdk: Int
    get() = project.properties["minSdk"].toString().toInt()

val Project.targetSdk: Int
    get() = project.properties["targetSdk"].toString().toInt()

val Project.compileSdk: Int
    get() = project.properties["compileSdk"].toString().toInt()