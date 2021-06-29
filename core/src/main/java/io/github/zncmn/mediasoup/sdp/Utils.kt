package io.github.zncmn.mediasoup.sdp

import io.github.crow_misia.sdp.*
import io.github.zncmn.mediasoup.MediasoupException
import io.github.zncmn.mediasoup.model.*
import io.github.crow_misia.sdp.attribute.*
import java.util.*

internal fun SdpSessionDescription.extractRtpCapabilities(): RtpCapabilities {
    val codecsMap = mutableMapOf<Int, RtpCodecCapability>()

    val headerExtensions = arrayListOf<RtpHeaderExtension>()

    var gotAudio = false
    var gotVideo = false

    getMediaDescriptions().forEach { m ->
        val kind = m.type

        when (kind) {
            "audio" -> {
                if (gotAudio) return@forEach
                gotAudio = true
            }
            "video" -> {
                if (gotVideo) return@forEach
                gotVideo = true
            }
            else -> return@forEach
        }

        // Get codecs
        m.getAttributes<RTPMapAttribute>().forEach { rtp ->
            val mimeType = "$kind/${rtp.encodingName}"

            val codec = RtpCodecCapability(
                mimeType = mimeType,
                kind = kind,
                clockRate = rtp.clockRate ?: 0,
                preferredPayloadType = rtp.payloadType,
                rtcpFeedback = emptyList(),
                parameters = emptyMap()
            )
            if (kind == "audio") {
                codec.channels = rtp.encodingParameters?.toIntOrNull() ?: 1
            }
            codecsMap[codec.preferredPayloadType] = codec
        }

        // Get codec parameters
        m.getAttributes<FormatAttribute>().forEach { fmtp ->
            val parameters = fmtp.parameters
            codecsMap[fmtp.format]?.also { codec ->
                codec.parameters = parameters.mapValues { entry -> entry.value }
            }
        }

        m.getAttributes(RTCPFbAttribute::class.java).forEach { fb ->
            codecsMap[fb.payloadType.toInt()]?.also { codec ->
                codec.rtcpFeedback += RtcpFeedback(
                    type = fb.type,
                    parameter = fb.subtype.orEmpty()
                )
            }
        }


        // Get RTP header extensions
        m.getAttributes<ExtMapAttribute>().forEach { ext ->
            headerExtensions.add(RtpHeaderExtension(
                kind = kind,
                uri = ext.uri,
                preferredId = ext.id
            ))
        }
    }
    return RtpCapabilities(
        headerExtensions = headerExtensions,
        codecs = codecsMap.values.toList(),
        fecMechanisms = emptyList() // TODO
    )
}


internal fun SdpSessionDescription.extractDtlsParameters(): DtlsParameters {
    val media = getMediaDescriptions().find { it.hasAttribute<IceUfragAttribute>() && it.port != 0}
    checkNotNull(media) { "mediaDescription(contain ice-ufrag and port=0) not found" }

    val fingerprint = media.getAttribute() ?: getAttribute<FingerprintAttribute>()
    checkNotNull(fingerprint) { "fingerprint not found" }

    val role = media.getAttribute<SetupAttribute>()?.let { setup ->
        when (setup) {
            SetupAttribute.of(SetupAttribute.Type.ACTIVE) -> DtlsRole.CLIENT
            SetupAttribute.of(SetupAttribute.Type.PASSIVE) -> DtlsRole.SERVER
            SetupAttribute.of(SetupAttribute.Type.ACTPASS) -> DtlsRole.AUTO
            else -> { throw MediasoupException("invalid setup type: $setup")
            }
        }
    }

    return DtlsParameters(
        role = role ?: DtlsRole.AUTO,
        fingerprints = listOf(
            DtlsFingerprint(
                algorithm = fingerprint.type,
                value = fingerprint.hash
            )
        )
    )
}

internal fun RtpParameters.applyCodecParameters(answerMediaDescription: SdpMediaDescription) {
    codecs.asSequence().filter { codec ->
        // Avoid parsing codec parameters for unhandled codecs.
        val mimeType = codec.mimeType.lowercase(Locale.ENGLISH)
        "audio/opus" == mimeType
    }.forEach { codec ->
        val payloadType = codec.payloadType
        answerMediaDescription.getAttributes<RTPMapAttribute>()
            .find { it.payloadType == payloadType } ?: return@forEach

        val formatAttribute = answerMediaDescription.getAttributes<FormatAttribute>()
            .find { it.format == payloadType } ?: run {
            FormatAttribute.of(payloadType).also {
                answerMediaDescription.addAttribute(it)
            }
        }
        codec.parameters["sprop-stereo"]?.also {
            formatAttribute.addParameter("stereo", it.toNormalizeInt())
        }
    }
}

internal fun SdpMediaDescription.setIceParameters(iceParameters: IceParameters) {
    setAttribute(IceUfragAttribute.of( iceParameters.usernameFragment))
    setAttribute(IcePwdAttribute.of(iceParameters.password))
}

internal fun SdpMediaDescription.isClosed() = port == 0

internal fun SdpMediaDescription.disable() {
    setAttribute(InactiveAttribute, DirectionAttribute::class)
    removeAttribute<ExtMapAttribute>()
    removeAttribute<SsrcAttribute>()
    removeAttribute<SsrcGroupAttribute>()
    removeAttribute<SimulcastAttribute>()
    removeAttribute<RidAttribute>()
}

internal fun SdpMediaDescription.close() {
    disable()
    port = 0
    removeAttribute<ExtmapAllowMixedAttribute>()
}

internal fun SdpMediaDescription.getCname(): String? {
    return getAttribute<CNameAttribute>()?.value
}

internal fun SdpMediaDescription.getRtpEncodings(): List<RtpEncodingParameters> {
    val ssrcs = getAttributes<SsrcAttribute>().map { it.id }.toHashSet()
    check(ssrcs.isNotEmpty()) { "no a=ssrc line found" }

    val ssrcToRtxSsrc = linkedMapOf<Long, Long>()
    getAttributes<SsrcGroupAttribute>().filter { it.semantics == "FID" }.forEach {
        val (ssrc, rtxSsrc) = it.ssrcs.toLongArray()
        if (ssrcs.contains(ssrc)) {
            ssrcs.remove(ssrc)
            ssrcs.remove(rtxSsrc)
        }
        ssrcToRtxSsrc[ssrc] = rtxSsrc
    }
    ssrcs.forEach { ssrc -> ssrcToRtxSsrc[ssrc] = 0L }

    // Fill RTP parameters.
    return ssrcToRtxSsrc.map { entry ->
        RtpEncodingParameters(ssrc = entry.key).also {
            if (entry.value != 0L) {
                it.rtx = RtxParameter(ssrc = entry.value)
            }
        }
    }
}


internal fun Any?.normalize(): Any? {
    return when (this) {
        is Double -> {
            val intValue = toInt()
            if (intValue.toDouble() == this) {
                intValue
            } else {
                this
            }
        }
        is Float -> {
            val intValue = toInt()
            if (intValue.toFloat() == this) {
                intValue
            } else {
                this
            }
        }
        else -> this
    }
}

internal fun Any?.toNormalizeString(): String? {
    return normalize()?.toString()
}

internal fun Any?.toNormalizeInt(): Int? {
    return when (this) {
        is String -> toIntOrNull()
        is Int -> this
        is Double -> toInt()
        is Float -> toInt()
        null -> null
        else -> toString().toIntOrNull()
    }
}
