import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI
import java.io.File
import java.io.FileInputStream
import java.util.*

val prop = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "gradle.properties")))
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.android)
    id("signing")
    id("maven-publish")
}

object Maven {
    const val groupId = "live.videosdk"
    const val artifactId = "libmediasoup-android"
    const val name = "libmediasoup-android"
    const val desc = "mediasoup client side library for Android"
    const val version = "2.0"
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
    namespace = "live.videosdk.mediasoup"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DLIBWEBRTC_INCLUDE_PATH=${projectDir.resolve("deps/webrtc/include")}",
                    "-DLIBWEBRTC_BINARY_ANDROID_PATH=${projectDir.resolve("deps/webrtc/lib")}",
                    "-DLIBMEDIASOUPCLIENT_ROOT_PATH=${projectDir.resolve("deps/libmediasoupclient")}",
                    "-DMEDIASOUPCLIENT_BUILD_TESTS=OFF"
                )
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
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

    lint {
        textReport = true
        checkDependencies = true
        baseline = file("lint-baseline.xml")
        disable.add("ChromeOsAbiSupport")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed")
            }
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("/META-INF/LICENSE*")
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = projectDir.resolve("CMakeLists.txt")
        }
    }
    ndkVersion = "26.1.10909125"

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        javaParameters.set(true)
        jvmTarget.set(JvmTarget.JVM_11)
        apiVersion.set(KotlinVersion.KOTLIN_1_7)
        languageVersion.set(KotlinVersion.KOTLIN_1_7)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to projectDir.resolve("deps/webrtc/lib"), "include" to arrayOf("*.jar"))))

    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.libwebrtc.ktx)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.ext.truth)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.truth)
}

val customDokkaTask by tasks.creating(DokkaTask::class) {
    dokkaSourceSets.getByName("main") {
        noAndroidSdkLink.set(false)
    }
    dependencies {
        plugins(libs.dokka.javadoc.plugin)
    }
    inputs.dir("src/main/java")
    outputDirectory.set(layout.buildDirectory.dir("javadoc"))
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(customDokkaTask)
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles JavaDoc JAR"
    archiveClassifier.set("javadoc")
    from(customDokkaTask.outputDirectory)
}

//val sourcesJar by tasks.creating(Jar::class) {
//    group = JavaBasePlugin.DOCUMENTATION_GROUP
//    description = "Assembles sources JAR"
//    archiveClassifier.set("sources")
//    from(sourceSets.create("main").allSource)
//}

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

//                artifact(sourcesJar)
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
        val keyId: String = prop.getProperty("signing.keyId")
        val key: String = prop.getProperty("signing.key")
        val password: String = prop.getProperty("signing.password")
        useInMemoryPgpKeys(
            keyId.orEmpty(),
            key.orEmpty(),
            password.orEmpty(),
        )
        sign(publishing.publications)
    }
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
    config.setFrom(files("$rootDir/config/detekt.yml"))
}