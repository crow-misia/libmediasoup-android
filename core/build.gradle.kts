import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.kotlin.android)
    id("signing")
    id("maven-publish")
}

object Maven {
    const val groupId = "io.github.crow-misia.libmediasoup-android"
    const val artifactId = "libmediasoup-android"
    const val name = "libmediasoup-android"
    const val desc = "mediasoup client side library for Android"
    const val version = "0.16.0"
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
    namespace = "io.github.crow_misia.mediasoup"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
        javaParameters = true
        jvmTarget = JvmTarget.JVM_11
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

val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            afterEvaluate {
                from(components.named("release").get())
            }

            groupId = Maven.groupId
            artifactId = Maven.artifactId

            println("""
                |Creating maven publication
                |    Group: $groupId
                |    Artifact: $artifactId
                |    Version: $version
            """.trimMargin())

            artifact(dokkaJavadocJar)

            pom {
                name = Maven.name
                description = Maven.desc
                url = Maven.siteUrl

                scm {
                    val scmUrl = "scm:git:${Maven.gitUrl}"
                    connection = scmUrl
                    developerConnection = scmUrl
                    url = Maven.gitUrl
                    tag = "HEAD"
                }

                developers {
                    developer {
                        id = "crow-misia"
                        name = "Zenichi Amano"
                        email = "crow.misia@gmail.com"
                        roles = listOf("Project-Administrator", "Developer")
                        timezone = "+9"
                    }
                }

                licenses {
                    license {
                        name = Maven.licenseName
                        url = Maven.licenseUrl
                        distribution = Maven.licenseDist
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = project.findProperty("sona.user") as String? ?: providers.environmentVariable("SONA_USER").orNull
                password = project.findProperty("sona.password") as String? ?: providers.environmentVariable("SONA_PASSWORD").orNull
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
    config.from(rootDir.resolve("config/detekt.yml"))
}
