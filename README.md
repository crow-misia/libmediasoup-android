# mediasoup client for android

[![Android CI](https://github.com/crow-misia/libmediasoup-android/workflows/Android%20CI/badge.svg)](https://github.com/crow-misia/libmediasoup-android/actions)
[![License](https://img.shields.io/github/license/crow-misia/libmediasoup-android)](LICENSE)

mediasoup android client side library https://mediasoup.org

## Build

- Download dependencies files.

```
$ cd core/scripts
$ ./get-deps.sh
```

## Publish

Step 1 : Change the version in `build.gradle.kts(:core)` file.

```
const val version = "2.0"
```

- If you want to publish SNAPSHOT version, add prefix `SNAPSHOT`

```
const val version = "2.0-SNAPSHOT"
```


Step 2 : Publish it on MavenCenter using the below command.

```
./gradlew publish 
```

Step 3 : Add dependencies in other project.

```
allprojects {
    repositories {
        //...
        maven {url "https://s01.oss.sonatype.org/content/repositories/releases/"}

        // for snapshot
        maven {url "https://s01.oss.sonatype.org/content/repositories/snapshots/"}
    }
}
```

```
implementation("live.videosdk:libmediasoup-android:${latest_version}")
```


## Get Started

### Gradle

Add dependencies (you can also add other modules that you need):

`${latest.version}` is [![Maven Central](https://img.shields.io/maven-central/v/io.github.crow-misia.libmediasoup-android/libmediasoup-android.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.crow-misia.libmediasoup-android%22%20AND%20a:%22libmediasoup-android%22)

```groovy
dependencies {
    implementation "io.github.crow-misia.libmediasoup-android:libmediasoup-android:${latest.version}"
}
```

Make sure that you have either `mavenCentral()` in the list of repositories:

```
repository {
    mavenCentral()
}
```

## Dependencies

* [libwebrtc-ktx](https://github.com/crow-misia/libwebrtc-ktx)

## Demo project

[mediasoup-demo-android](https://github.com/crow-misia/mediasoup-demo-android)

## License

```
Copyright 2020, Zenichi Amano.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
