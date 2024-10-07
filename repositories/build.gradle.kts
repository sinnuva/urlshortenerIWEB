plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Spring Boot plugin without automatically applying it
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Add the core project as an implementation dependency
    implementation(project(":core"))

    // Add the Spring Boot Starter Data JPA library as an implementation dependency
    implementation(libs.spring.boot.starter.data.jpa)

    // Add the HTTP request library OkHttp as an implementation dependency
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}

dependencyManagement {
    imports {
        // Import the Spring Boot BOM (Bill of Materials) for dependency management
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Ensure that all dependencies from the org.jetbrains.kotlin group use version 1.9.23
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}
