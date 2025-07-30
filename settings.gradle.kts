pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")}
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven {url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")}
    }
}

rootProject.name = "libmediasoup-android"
include("core")
