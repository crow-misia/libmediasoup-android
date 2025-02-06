# mediasoup client for android

[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/crow-misia/libmediasoup-android/build.yml)](https://github.com/crow-misia/libmediasoup-android/actions/workflows/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.crow-misia.libmediasoup-android/libmediasoup-android)](https://central.sonatype.com/artifact/io.github.crow-misia.libmediasoup-android/libmediasoup-android)
[![GitHub License](https://img.shields.io/github/license/crow-misia/libmediasoup-android)](LICENSE)

mediasoup android client side library https://mediasoup.org

## Get Started

### Gradle

Add dependencies (you can also add other modules that you need):

`${latest.version}` is [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.crow-misia.libmediasoup-android/libmediasoup-android)](https://central.sonatype.com/artifact/io.github.crow-misia.libmediasoup-android/libmediasoup-android)

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

* [libmediasoup-android](https://github.com/crow-misia/libmediasoup-android)

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
