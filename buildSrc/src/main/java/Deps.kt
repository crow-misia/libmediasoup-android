object Deps {
    const val androidPlugin = "com.android.tools.build:gradle:${Versions.gradlePlugin}"
    const val dokkaPlugin = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokkaPlugin}"
    const val dokkaJavadocPlugin = "org.jetbrains.dokka:javadoc-plugin:${Versions.dokkaPlugin}"

    const val coroutinsCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinsAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    const val webrtc = "com.github.crow-misia:libwebrtc-bin:${Versions.webrtc}"
    const val webrtcKtx = "io.github.crow-misia.libwebrtc:libwebrtc-ktx:${Versions.webrtcKtx}"
    const val sdp = "io.github.crow-misia.sdp:sdp:${Versions.sdp}"

    const val junit = "junit:junit:${Versions.junit}"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"
    const val extJunit = "androidx.test.ext:junit:${Versions.extJunit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
}
