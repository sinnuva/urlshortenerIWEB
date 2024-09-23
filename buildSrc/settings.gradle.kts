dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Specifies the location of the version catalog file.
            from(files("../gradle/libs.versions.toml"))
        }
    }
}