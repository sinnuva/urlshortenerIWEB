plugins {
    // Applies the Kotlin DSL plugin to the project.
    `kotlin-dsl`
}

repositories {
    // Adds the Maven Central repository to the list of repositories.
    mavenCentral()
}

dependencies {
    // Adds the Kotlin Gradle plugin as a dependency.
    implementation(libs.kotlin.gradle.plugin)
    // Adds the Detekt Gradle plugin as a dependency.
    implementation(libs.detekt.gradle.plugin)
}
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
