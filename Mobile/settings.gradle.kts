pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Use PREFER_SETTINGS so Gradle relies on these repositories instead of throwing an error
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        mavenCentral()

        // Optional fallback repositories
        /*
        maven {
            url = uri("https://jitpack.io")
        }
         */
        maven {
            url = uri("https://maven.google.com")
        }
    }
}

rootProject.name = "ForkIt"
include(":app")
