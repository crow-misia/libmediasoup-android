# mediasoup client for android

[![Android CI](https://github.com/crow-misia/libmediasoup-android/workflows/Android%20CI/badge.svg)](https://github.com/crow-misia/libmediasoup-android/actions)
[![License](https://img.shields.io/github/license/crow-misia/libmediasoup-android)](LICENSE)

mediasoup android client side library https://mediasoup.org


## Get Started

### Gradle

Add dependencies (you can also add other modules that you need):

`${latest.version}` is [![Download](https://api.bintray.com/packages/zncmn/maven/libmediasoup-android/images/download.svg)](https://bintray.com/zncmn/maven/libmediasoup-android/_latestVersion)

```groovy
dependencies {
    implementation "io.github.zncmn.mediasoup:libmediasoup-android:${latest.version}"
}
```

Make sure that you have either `jcenter()` in the list of repositories:

```
repository {
    jcenter()
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
