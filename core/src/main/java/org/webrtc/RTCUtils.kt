package org.webrtc

import io.github.zncmn.mediasoup.model.RtpEncodingParameters

fun RtpEncodingParameters.toRtpParametersEncoding(): RtpParameters.Encoding {
    return RtpParameters.Encoding(
        rid,
        active ?: true,
        bitratePriority ?: 1.0,
        networkPriority ?: 1,
        maxBitrate,
        minBitrate,
        maxFramerate,
        numTemporalLayers,
        scaleResolutionDownBy,
        ssrc,
        false,
    )
}
