// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.library") apply false
    kotlin("android") apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Android.tools.build.gradlePlugin)
        classpath(libs.dokka.gradle.plugin)
    }
}

val clean by tasks.creating(Delete::class) {
    group = "build"
    delete(rootProject.layout.buildDirectory)
}
