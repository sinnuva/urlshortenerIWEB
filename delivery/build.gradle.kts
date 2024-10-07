plugins {
    // Apply the common conventions plugin for the project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Kotlin Spring plugin
    alias(libs.plugins.kotlin.spring)

    // Apply the Spring Boot plugin but do not apply it immediately
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Include the core project as an implementation dependency
    implementation(project(":core"))

    // Include Spring Boot Starter Web as an implementation dependency
    implementation(libs.spring.boot.starter.web)

    // Include Spring Boot Starter HATEOAS as an implementation dependency
    implementation(libs.spring.boot.starter.hateoas)

    // Include Apache Commons Validator as an implementation dependency
    implementation(libs.commons.validator)

    // Include Google Guava as an implementation dependency
    implementation(libs.guava)

    implementation("io.github.cdimascio:java-dotenv:5.2.2")


    // Include Kotlin Test as a test implementation dependency
    testImplementation(libs.kotlin.test)

    // Include Mockito Kotlin as a test implementation dependency
    testImplementation(libs.mockito.kotlin)

    // Include JUnit Jupiter as a test implementation dependency
    testImplementation(libs.junit.jupiter)

    // Include JUnit Platform Launcher as a test runtime-only dependency
    testRuntimeOnly(libs.junit.platform.launcher)

    // Include Spring Boot Starter Test as a test implementation dependency
    testImplementation(libs.spring.boot.starter.test)
}

dependencyManagement {
    imports {
        // Import the Spring Boot BOM (Bill of Materials) for dependency management
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Force the use of Kotlin version 1.9.23 for all dependencies in the detekt configuration
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}
