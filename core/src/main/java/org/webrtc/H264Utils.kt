package org.webrtc

const val H264_FMTP_PROFILE_LEVEL_ID = H264Utils.H264_FMTP_PROFILE_LEVEL_ID
const val H264_FMTP_LEVEL_ASYMMETRY_ALLOWED = H264Utils.H264_FMTP_LEVEL_ASYMMETRY_ALLOWED
const val H264_FMTP_PACKETIZATION_MODE = H264Utils.H264_FMTP_PACKETIZATION_MODE
const val H264_PROFILE_CONSTRAINED_BASELINE = H264Utils.H264_PROFILE_CONSTRAINED_BASELINE
const val H264_PROFILE_CONSTRAINED_HIGH = H264Utils.H264_PROFILE_CONSTRAINED_HIGH
const val H264_LEVEL_3_1 = H264Utils.H264_LEVEL_3_1
const val H264_CONSTRAINED_HIGH_3_1 = H264Utils.H264_CONSTRAINED_HIGH_3_1
const val H264_CONSTRAINED_BASELINE_3_1 = H264Utils.H264_CONSTRAINED_BASELINE_3_1

internal val DEFAULT_H264_BASELINE_PROFILE_CODEC = H264Utils.DEFAULT_H264_BASELINE_PROFILE_CODEC
internal val DEFAULT_H264_HIGH_PROFILE_CODEC = H264Utils.DEFAULT_H264_HIGH_PROFILE_CODEC


internal fun isSameH264Profile(param1: Map<String, String>,
                               param2: Map<String, String>): Boolean {
    return H264Utils.isSameH264Profile(param1, param2)
}

internal fun generateH264Profile(profileLevelId: String,
                                 levelAsymmetryAllowed: String,
                                 packetizationMode: String): Map<String, String> {
    return mapOf(
        H264_FMTP_PROFILE_LEVEL_ID to profileLevelId,
        H264_FMTP_LEVEL_ASYMMETRY_ALLOWED to levelAsymmetryAllowed,
        H264_FMTP_PACKETIZATION_MODE to packetizationMode
    )
}

/**
 * cf. https://tools.ietf.org/html/rfc6184#section-8.2.2.
 */
internal fun generateProfileLevelIdForAnswer(param1: Map<String, String>,
                                             param2: Map<String, String>,
                                             answerParam: MutableMap<String, String>): Boolean {

    return true
}
