import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

object Maven {
    const val groupId = "io.github.crow-misia.libmediasoup-android"
    const val artifactId = "libmediasoup-android"
    const val name = "libmediasoup-android"
    const val desc = "mediasoup client side library for Android"
    const val version = "0.4.1"
    const val siteUrl = "https://github.com/crow-misia/libmediasoup-android"
    const val gitUrl = "https://github.com/crow-misia/libmediasoup-android.git"
    const val githubRepo = "crow-misia/libmediasoup-android"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
}

group = Maven.groupId
version = Maven.version

android {
    buildToolsVersion = "31.0.0"
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")

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
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    lint {
        textReport = true
        textOutput("stdout")
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }

    buildTypes {
        getByName("debug") {
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
        getByName("release") {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-module-name", "libmediasoup-android")
            jvmTarget = "1.8"
            apiVersion = "1.5"
            languageVersion = "1.5"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("${projectDir}/CMakeLists.txt")
        }
    }
    ndkVersion = "22.1.7171670"
}

dependencies {
    api(Kotlin.stdlib)
    implementation(fileTree(mapOf("dir" to "${projectDir}/deps/webrtc/lib", "include" to arrayOf("*.jar"))))
    api("io.github.crow-misia.libwebrtc:libwebrtc-ktx:_")

    testImplementation(Testing.junit4)
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
    androidTestImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.ext.junitKtx)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
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
        plugins("org.jetbrains.dokka:javadoc-plugin:_")
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
                from(components["release"])

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
                        tag.set("HEAD")
                    }

                    developers {
                        developer {
                            id.set("crow-misia")
                            name.set("Zenichi Amano")
                            email.set("crow.misia@gmail.com")
                            roles.set(listOf("Project-Administrator", "Developer"))
                            timezone.set("+9")
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
                val releasesRepoUrl = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = URI("https://oss.sonatype.org/content/repositories/snapshots")
                url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                val sonatypeUsername: String? by project
                val sonatypePassword: String? by project
                credentials {
                    username = sonatypeUsername.orEmpty()
                    password = sonatypePassword.orEmpty()
                }
            }
        }
    }

    signing {
        sign(publishing.publications.getByName("maven"))
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
