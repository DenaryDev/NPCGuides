pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://the-planet.fun/repo/snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

rootProject.name = "NPCGuides"
