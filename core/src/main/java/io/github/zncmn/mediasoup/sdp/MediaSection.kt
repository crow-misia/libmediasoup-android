package io.github.zncmn.mediasoup.sdp

import io.github.zncmn.mediasoup.MediasoupException
import io.github.zncmn.mediasoup.model.*
import io.github.zncmn.sdp.SdpConnection
import io.github.zncmn.sdp.SdpMediaDescription
import io.github.zncmn.sdp.attribute.*
import java.util.*

sealed class MediaSection(
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>
) {
    val mediaDescription: SdpMediaDescription = SdpMediaDescription.of(type = "", port = 0)
    val mid: String
        get() = mediaDescription.mid

    init {
        mediaDescription.setIceParameters(iceParameters)
        iceCandidates.forEach { candidate ->
            val attributes = CandidateAttribute.of(
                component = 1,
                foundation = candidate.foundation,
                address = candidate.ip,
                port = candidate.port,
                priority = candidate.priority,
                transport = candidate.protocol,
                type = candidate.type
            )
            candidate.tcpType?.takeIf { it.isNotEmpty() }?.also {
                attributes.addExtension("tcptype", it)
            }
            mediaDescription.addAttribute(attributes)
        }
        mediaDescription.addAttribute(EndOfCandidatesAttribute)
        mediaDescription.addAttribute(IceOptionsAttribute.of("renomination"))
    }

    abstract fun setDtlsRole(role: DtlsRole)
}

class AnswerMediaSection(
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    offerMediaDescription: SdpMediaDescription,
    offerRtpParameters: RtpParameters,
    answerRtpParameters: RtpParameters,
    codecOptions: ProducerCodecOptions?
) : MediaSection(iceParameters, iceCandidates) {
    init {
        val type = offerMediaDescription.type
        mediaDescription.mid = offerMediaDescription.mid
        mediaDescription.type = type
        mediaDescription.protos = offerMediaDescription.protos
        mediaDescription.connections.also {
            it.clear()
            it.add(SdpConnection.of("IN", "IP4", "127.0.0.1"))
        }
        mediaDescription.port = 7

        when (dtlsParameters.role) {
            DtlsRole.CLIENT -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.ACTIVE))
            DtlsRole.SERVER -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.PASSIVE))
            DtlsRole.AUTO -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.ACTPASS))
        }
        when (type) {
            "audio", "video" -> {
                mediaDescription.addAttribute(RecvOnlyAttribute)
                answerRtpParameters.codecs.forEach { codec ->
                    val payloadType = codec.payloadType
                    mediaDescription.addAttribute(RTPMapAttribute.of(
                        payloadType = payloadType,
                        encodingName = codec.getCodecName(),
                        clockRate = codec.clockRate,
                        encodingParameters = codec.channels?.takeIf { it > 1 }?.toString()
                    ))

                    val codecParameters = codec.parameters.toMutableMap()
                    codec.parameters = codecParameters
                    val offerCodec = offerRtpParameters.codecs.find { it.payloadType == codec.payloadType }
                    if (codecOptions != null && offerCodec != null) {
                        val offerCodecParameters = offerCodec.parameters.toMutableMap()
                        offerCodec.parameters = offerCodecParameters
                        when (offerCodec.mimeType.toLowerCase(Locale.ENGLISH)) {
                            "audio/opus" -> {
                                setParameterBool(codecOptions.opusStereo, "sprop-stereo", "stereo", offerCodecParameters, codecParameters)

                                setParameterBool(codecOptions.opusFec, "useinbandfec", "useinbandfec", offerCodecParameters, codecParameters)

                                setParameterBool(codecOptions.opusDtx, "usedtx", "usedtx", offerCodecParameters, codecParameters)

                                codecOptions.opusMaxPlaybackRate?.also { codecParameters["maxplaybackrate"] = it }
                            }
                            "audio/vp8", "audio/vp9", "video/h264", "video/h265" -> {
                                codecOptions.videoGoogleStartBitrate?.also { codecParameters["x-google-start-bitrate"] = it }
                                codecOptions.videoGoogleMaxBitrate?.also { codecParameters["x-google-max-bitrate"] = it }
                                codecOptions.videoGoogleMinBitrate?.also { codecParameters["x-google-min-bitrate"] = it }
                            }
                        }
                    }

                    val fmtp = FormatAttribute.of(format = codec.payloadType)
                    codecParameters.forEach { entry ->
                        fmtp.addParameter(entry.key, entry.value.toString())
                    }
                    if (fmtp.isNotEmptyParameters) {
                        mediaDescription.addAttribute(fmtp)
                    }

                    codec.rtcpFeedback.forEach { fb ->
                        mediaDescription.addAttribute(RTCPFbAttribute.of(
                            payloadType = payloadType.toString(),
                            type = fb.type,
                            subtype = fb.parameter
                        ))
                    }
                }

                answerRtpParameters.codecs.forEach { mediaDescription.formats.add(it.payloadType) }

                // Don't add a header extension if not present in the offer.
                answerRtpParameters.headerExtensions.forEach { ext ->
                    offerMediaDescription.getAttributes(ExtMapAttribute::class).firstOrNull { it.uri == ext.uri } ?: return@forEach

                    mediaDescription.addAttribute(ExtMapAttribute.of(
                        uri = ext.uri,
                        value = ext.id
                    ))
                }

                // Allow both 1 byte and 2 bytes length header extensions.
                if (offerMediaDescription.hasAttribute(ExtmapAllowMixedAttribute::class)) {
                    mediaDescription.addAttribute(ExtmapAllowMixedAttribute.of())
                }

                // Simulcast.
                val rids = mediaDescription.getAttributes(RidAttribute::class).toList()
                offerMediaDescription.getAttribute(SimulcastAttribute::class)?.also { simulcast ->
                    mediaDescription.addAttribute(SimulcastAttribute.of(
                        dir1 = "recv",
                        list1 = simulcast.list1
                    ))
                    rids.filter { it.direction == "send" }.forEach {
                        mediaDescription.addAttribute(RidAttribute.of(it.id, "recv"))
                    }
                }

                mediaDescription.addAttribute(RTCPMuxAttribute)
                mediaDescription.addAttribute(RTCPRsizeAttribute)
            }
            "application" -> {
                sctpParameters?.also {
                    mediaDescription.addAttribute(SctpMapAttribute.of(
                        app = "webrtc-datachannel",
                        sctpmapNumber = it.port,
                        maxMessageSize = it.maxMessageSize
                    ))
                }
            }
        }
    }

    override fun setDtlsRole(role: DtlsRole) {
        when (role) {
            DtlsRole.CLIENT -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.ACTIVE))
            DtlsRole.SERVER -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.PASSIVE))
            DtlsRole.AUTO -> mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.ACTPASS))
        }
    }

    private fun setParameterBool(value: Boolean?, offerParamName: String, codecParamName: String, offerParams: MutableMap<String, Any?>, codecParams: MutableMap<String, Any?>) {
        value ?: return
        if (value) {
            offerParams[offerParamName] = 1
            codecParams[codecParamName] = 1
        } else {
            offerParams[offerParamName] = 0
            codecParams[codecParamName] = 0
        }
    }
}

class OfferMediaSection(
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    mid: String,
    kind: MediaKind,
    offerRtpParameters: RtpParameters,
    streamId: String,
    trackId: String
) : MediaSection(iceParameters, iceCandidates) {
    init {
        mediaDescription.mid = mid
        mediaDescription.type = kind.toLowerCase(Locale.ENGLISH)

        if (sctpParameters == null) {
            mediaDescription.setProto("UDP/TLS/RTP/SAVPF")
        } else {
            mediaDescription.setProto("UDP/DTLS/SCTP")
        }
        mediaDescription.connections.add(SdpConnection.of("IN", "IP4", "127.0.0.1"))
        mediaDescription.port = 7

        // set DTLS role.
        mediaDescription.addAttribute(SetupAttribute.of(SetupAttribute.Type.ACTPASS))

        when (kind) {
            "audio", "video" -> {
                mediaDescription.addAttribute(SendOnlyAttribute)
                offerRtpParameters.codecs.forEach { codec ->
                    val channels = codec.channels
                    val payloadType = codec.payloadType
                    mediaDescription.addAttribute(RTPMapAttribute.of(
                        payloadType = payloadType,
                        encodingName = codec.getCodecName(),
                        clockRate = codec.clockRate,
                        encodingParameters = channels?.takeIf { it > 1 }?.toString()
                    ))

                    val formatParameter = FormatAttribute.of(payloadType)
                    codec.parameters.forEach {
                        formatParameter.addParameter(it.key, it.value.toNormalizeString())
                    }
                    if (formatParameter.isNotEmptyParameters) {
                        mediaDescription.addAttribute(formatParameter)
                    }

                    codec.rtcpFeedback.forEach { fb ->
                        mediaDescription.addAttribute(RTCPFbAttribute.of(
                            payloadType = payloadType.toString(),
                            type = fb.type,
                            subtype = fb.parameter
                        ))
                    }

                    mediaDescription.formats.add(payloadType)
                }

                offerRtpParameters.headerExtensions.forEach { ext ->
                    mediaDescription.addAttribute(ExtMapAttribute.of(
                        value = ext.id,
                        uri = ext.uri
                    ))
                }

                mediaDescription.addAttribute(RTCPMuxAttribute)
                mediaDescription.addAttribute(RTCPRsizeAttribute)

                val encoding = offerRtpParameters.encodings.firstOrNull() ?: run {
                    throw MediasoupException("not found encoding")
                }
                val ssrc = encoding.ssrc
                val rtxSsrc = encoding.rtx?.ssrc ?: 0L

                val rtcp = offerRtpParameters.rtcp
                val cname = rtcp?.cname ?: ""
                val msid = "$streamId $trackId"
                if (cname.isNotEmpty()) {
                    mediaDescription.addAttribute(SsrcAttribute.of(ssrc, "cname", cname))
                    mediaDescription.addAttribute(SsrcAttribute.of(ssrc, "msid", msid))
                }
                if (rtxSsrc != 0L) {
                    mediaDescription.addAttribute(SsrcAttribute.of(rtxSsrc, "cname", cname))
                    mediaDescription.addAttribute(SsrcAttribute.of(rtxSsrc, "msid", msid))
                    mediaDescription.addAttribute(SsrcGroupAttribute.of("FID", ssrc, rtxSsrc))
                }
            }
            "application" -> {
                sctpParameters?.also {
                    mediaDescription.addAttribute(SctpMapAttribute.of(
                        app = "webrtc-datachannel",
                        sctpmapNumber = it.port,
                        maxMessageSize = it.maxMessageSize
                    ))
                }
            }
        }
    }

    override fun setDtlsRole(role: DtlsRole) {
        mediaDescription.setAttribute(SetupAttribute.of(SetupAttribute.Type.ACTPASS))
    }
}

internal fun RtpCodecParameters.getCodecName(): String {
    return when (mimeType.substring(0, 6).toLowerCase(Locale.ENGLISH)) {
        "audio/", "video/" -> mimeType.substring(6)
        else -> mimeType
    }
}
