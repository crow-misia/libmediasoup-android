import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI
import java.io.File
import java.io.FileInputStream
import java.util.*

val prop = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

object Maven {
    const val groupId = "live.videosdk"
    const val artifactId = "libmediasoup-android"
    const val name = "libmediasoup-android"
    const val desc = "mediasoup client side library for Android"
    const val version = "1.0"
    const val siteUrl = "https://github.com/zujonow/libmediasoup-android"
    const val gitUrl = "https://github.com/zujonow/libmediasoup-android.git"
    const val githubRepo = "zujonow/libmediasoup-android"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
}

group = Maven.groupId
version = Maven.version

android {
    buildToolsVersion = "33.0.1"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")
        namespace = "live.videosdk.mediasoup"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DLIBWEBRTC_INCLUDE_PATH=${projectDir}/deps/webrtc/include",
                    "-DLIBWEBRTC_BINARY_ANDROID_PATH=${projectDir}/deps/webrtc/lib",
                    "-DLIBMEDIASOUPCLIENT_ROOT_PATH=${projectDir}/deps/libmediasoupclient",
                    "-DMEDIASOUPCLIENT_BUILD_TESTS=OFF"
                )
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    lint {
        textReport = true
        checkDependencies = true
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }

    buildTypes {
        debug {
            isJniDebuggable = true
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DMEDIASOUPCLIENT_LOG_TRACE=ON",
                        "-DMEDIASOUPCLIENT_LOG_DEV=ON"
                    )
                }
            }
        }
        release {
            isJniDebuggable = false
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DMEDIASOUPCLIENT_LOG_TRACE=OFF",
                        "-DMEDIASOUPCLIENT_LOG_DEV=OFF"
                    )
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-module-name", "libmediasoup-android")
            jvmTarget = "11"
            apiVersion = "1.7"
            languageVersion = "1.7"
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("${projectDir}/CMakeLists.txt")
        }
    }
    ndkVersion = "25.1.8937393"
}

dependencies {
    api(Kotlin.stdlib)
    implementation(fileTree(mapOf("dir" to "${projectDir}/deps/webrtc/lib", "include" to arrayOf("*.jar"))))
    api(libs.libwebrtc.ktx)

    testImplementation(Testing.junit4)
    testImplementation(libs.assertk.jvm)
    androidTestImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.ext.junit.ktx)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(libs.assertk.jvm)
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.create("main").allSource)
}

val customDokkaTask by tasks.creating(DokkaTask::class) {
    dokkaSourceSets.getByName("main") {
        noAndroidSdkLink.set(false)
    }
    dependencies {
        plugins(libs.javadoc.plugin)
    }
    inputs.dir("src/main/java")
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(customDokkaTask)
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles JavaDoc JAR"
    archiveClassifier.set("javadoc")
    from(customDokkaTask.outputDirectory)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("release"))

                groupId = Maven.groupId
                artifactId = Maven.artifactId

                println("""
                    |Creating maven publication
                    |    Group: $groupId
                    |    Artifact: $artifactId
                    |    Version: $version
                """.trimMargin())

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(Maven.name)
                    description.set(Maven.desc)
                    url.set(Maven.siteUrl)

                    scm {
                        val scmUrl = "scm:git:${Maven.gitUrl}"
                        connection.set(scmUrl)
                        developerConnection.set(scmUrl)
                        url.set(Maven.gitUrl)
                    }

                    developers {
                        developer {
                            id.set("videosdk")
                            name.set("VideoSDK")
                            email.set("admin@videosdk.live")
                        }
                    }

                    licenses {
                        license {
                            name.set(Maven.licenseName)
                            url.set(Maven.licenseUrl)
                            distribution.set(Maven.licenseDist)
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = URI("https://s01.oss.sonatype.org/content/repositories/releases/")
                val snapshotsRepoUrl = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                val ossrhUsername: String = prop.getProperty("ossrhUsername")
                val ossrhPassword: String = prop.getProperty("ossrhPassword")
                credentials {
                    username = ossrhUsername.orEmpty()
                    password = ossrhPassword.orEmpty()
                }
            }
        }
    }

    signing {
        val keyId: String = prop.getProperty("keyId")
        val key: String = prop.getProperty("key")
        val password: String = prop.getProperty("password")
        useInMemoryPgpKeys(
            keyId.orEmpty(),
            key.orEmpty(),
            password.orEmpty(),
        )
        sign(publishing.publications)
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}