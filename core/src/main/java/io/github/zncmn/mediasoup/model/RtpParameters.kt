package io.github.zncmn.mediasoup.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.zncmn.mediasoup.sdp.normalize

/**
 * cf. https://mediasoup.org/documentation/v3/mediasoup/rtp-parameters-and-capabilities/
 */

/**
 * The RTP capabilities define what mediasoup or an endpoint can receive at media level.
 */
@JsonClass(generateAdapter = true)
data class RtpCapabilities @JvmOverloads constructor(
    var codecs: List<RtpCodecCapability> = emptyList(),
    var headerExtensions: List<RtpHeaderExtension> = emptyList(),
    var fecMechanisms: List<String> = emptyList()
)

/**
 * Media kind ('audio' or 'video').
 */
typealias MediaKind = String

@JsonClass(generateAdapter = true)
data class RtpCodecCapability @JvmOverloads constructor(
    /**
     * Media kind.
     */
    var kind: MediaKind,

    /**
     * The codec MIME media type/subtype (e.g. 'audio/opus', 'video/VP8').
     */
    var mimeType: String,

    /**
     * The preferred RTP payload type.
     */
    var preferredPayloadType: Int,

    /**
     * Codec clock rate expressed in Hertz.
     */
    var clockRate: Int,

    /**
     * The number of channels supported (e.g. two for stereo). Just for audio Default 1.
     */
    var channels: Int? = null,

    /**
     * Codec specific parameters. Some parameters (such as 'packetization-mode'
     * and 'profile-level-id' in H264 or 'profile-id' in VP9) are critical for
     * codec matching.
     */
    var parameters: Map<String, Any?> = emptyMap(),

    /**
     * Transport layer and codec-specific feedback messages for this codec.
     */
    var rtcpFeedback: List<RtcpFeedback> = emptyList()
) {
    fun normalize() {
        parameters = parameters.mapValues { it.value.normalize() }
    }

    fun toRtpCodecParameters() = RtpCodecParameters(
        mimeType = mimeType,
        payloadType = preferredPayloadType,
        clockRate = clockRate,
        channels = channels,
        parameters = parameters,
        rtcpFeedback = rtcpFeedback
    )
}

@JsonClass(generateAdapter = true)
data class ExtendedRtpCapabilities @JvmOverloads constructor(
    var codecs: List<ExtendedRtpCodecCapability> = emptyList(),
    var headerExtensions: List<ExtendedRtpHeaderExtension> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ExtendedRtpCodecCapability @JvmOverloads constructor(
    /**
     * Media kind.
     */
    var kind: MediaKind,

    /**
     * The codec MIME media type/subtype (e.g. 'audio/opus', 'video/VP8').
     */
    var mimeType: String,

    /**
     * Local RTP payload type.
     */
    var localPayloadType: Int,

    /**
     * Local RTX RTP payload type.
     */
    var localRtxPayloadType: Int? = null,

    /**
     * Remote RTP payload type.
     */
    var remotePayloadType: Int,

    /**
     * Remote RTX RTP payload type.
     */
    var remoteRtxPayloadType: Int? = null,

    /**
     * Codec clock rate expressed in Hertz.
     */
    var clockRate: Int,

    /**
     * The number of channels supported (e.g. two for stereo).
     */
    var channels: Int? = null,

    /**
     * Local Codec specific parameters.
     */
    var localParameters: Map<String, Any?> = emptyMap(),

    /**
     * Remote Codec specific parameters.
     */
    var remoteParameters: Map<String, Any?> = emptyMap(),

    /**
     * Transport layer and codec-specific feedback messages for this codec.
     */
    var rtcpFeedback: List<RtcpFeedback> = emptyList()
) {
    fun normalize() {
        localParameters = localParameters.mapValues { it.value.normalize() }
        remoteParameters = remoteParameters.mapValues { it.value.normalize() }
    }
}

@JsonClass(generateAdapter = false)
enum class RtpHeaderExtensionDirection {
    @Json(name = "sendrecv") SENDRECV,
    @Json(name = "sendonly") SENDONLY,
    @Json(name = "recvonly") RECVONLY,
    @Json(name = "inactive") INACTIVE
}

@JsonClass(generateAdapter = true)
data class RtpHeaderExtension @JvmOverloads constructor(
    /**
     * Media kind. If empty string, it's valid for all kinds.
     * Default any media kind.
     */
    var kind: MediaKind = "",

    /**
     * The URI of the RTP header extension, as defined in RFC 5285.
     */
    var uri: String,

    /**
     * The preferred numeric identifier that goes in the RTP packet. Must be unique.
     */
    var preferredId: Long,

    /**
     * If true, it is preferred that the value in the header be encrypted as per RFC 6904.
     * Default false.
     */
    var preferredEncrypt: Boolean = false,

    /**
     * If 'sendrecv', mediasoup supports sending and receiving this RTP extension.
     * 'sendonly' means that mediasoup can send (but not receive) it. 'recvonly'
     * means that mediasoup can receive (but not send) it.
     */
    var direction: RtpHeaderExtensionDirection = RtpHeaderExtensionDirection.SENDRECV
) {
    fun toRtpHeaderExtensionParameters() = RtpHeaderExtensionParameters(
        uri = uri,
        id = preferredId,
        encrypt = preferredEncrypt
    )
}

@JsonClass(generateAdapter = true)
data class ExtendedRtpHeaderExtension @JvmOverloads constructor(
    var kind: MediaKind = "",
    var uri: String,
    var sendId: Long,
    var recvId: Long,
    var encrypt: Boolean = false,
    var direction: RtpHeaderExtensionDirection = RtpHeaderExtensionDirection.SENDRECV
)

@JsonClass(generateAdapter = true)
data class RtpParameters @JvmOverloads constructor(
    /**
     * The MID RTP extension value as defined in the BUNDLE specification.
     */
    var mid: String? = null,

    /**
     * Media and RTX codecs in use.
     */
    var codecs: List<RtpCodecParameters> = emptyList(),

    /**
     * RTP header extensions in use.
     */
    var headerExtensions: List<RtpHeaderExtensionParameters> = emptyList(),

    /**
     * Transmitted RTP streams and their settings.
     */
    var encodings: List<RtpEncodingParameters> = emptyList(),

    /**
     * Parameters used for RTCP.
     */
    var rtcp: RtcpParameters = RtcpParameters()
)

@JsonClass(generateAdapter = true)
data class RtpCodecParameters @JvmOverloads constructor(
    /**
     * The codec MIME media type/subtype (e.g. 'audio/opus', 'video/VP8').
     */
    var mimeType: String,

    /**
     * The value that goes in the RTP Payload Type Field. Must be unique.
     */
    var payloadType: Int,

    /**
     * Codec clock rate expressed in Hertz.
     */
    var clockRate: Int,

    /**
     * The number of channels supported (e.g. two for stereo). Just for audio.
     * Default 1.
     */
    var channels: Int? = null,

    /**
     * Codec-specific parameters available for signaling. Some parameters (such
     * as 'packetization-mode' and 'profile-level-id' in H264 or 'profile-id' in
     * VP9) are critical for codec matching.
     */
    var parameters: Map<String, Any?> = emptyMap(),

    /**
     * Transport layer and codec-specific feedback messages for this codec.
     */
    var rtcpFeedback: List<RtcpFeedback> = emptyList()
) {
    fun normalize() {
        parameters = parameters.mapValues { it.value.normalize() }
    }
}

@JsonClass(generateAdapter = true)
data class RtcpFeedback(
    /**
     * RTCP feedback type.
     */
    var type: String,

    /**
     * RTCP feedback parameter.
     */
    var parameter: String = ""
)

@JsonClass(generateAdapter = true)
data class RtpEncodingParameters @JvmOverloads constructor(
    /**
     * The media SSRC.
     */
    var ssrc: Long,

    /**
     * The RID RTP extension value. Must be unique.
     */
    var rid: String? = null,

    /**
     * Codec payload type this encoding affects. If unset, first media codec is
     * chosen.
     */
    var codecPayloadType: Int? = null,

    /**
     * RTX stream information. It must contain a numeric ssrc field indicating
     * the RTX SSRC.
     */
    var rtx: RtxParameter? = null,

    /**
     * It indicates whether discontinuous RTP transmission will be used. Useful
     * for audio (if the codec supports it) and for video screen sharing (when
     * static content is being transmitted, this option disables the RTP
     * inactivity checks in mediasoup). Default false.
     */
    var dtx: Boolean = false,

    /**
     * Number of spatial and temporal layers in the RTP stream (e.g. 'L1T3').
     * See webrtc-svc.
     */
    var scalabilityMode: String? = null,

    /**
     * Others.
     */
    var active: Boolean? = null,
    var scaleFramerateDownBy: Double? = null,
    var scaleResolutionDownBy: Double? = null,
    var minBitrate: Int? = null,
    var maxBitrate: Int? = null,
    var maxFramerate: Int? = null,
    var numTemporalLayers: Int? = null,
    var bitratePriority: Double? = null,
    var networkPriority: Int? = null
)

@JsonClass(generateAdapter = true)
data class RtxParameter @JvmOverloads constructor(
    var ssrc: Long? = null
)

@JsonClass(generateAdapter = true)
data class RtpHeaderExtensionParameters @JvmOverloads constructor(
    /**
     * The URI of the RTP header extension, as defined in RFC 5285.
     */
    var uri: String,

    /**
     * The numeric identifier that goes in the RTP packet. Must be unique.
     */
    var id: Long,

    /**
     * If true, the value in the header is encrypted as per RFC 6904. Default false.
     */
    var encrypt: Boolean = false,

    /**
     * Configuration parameters for the header extension.
     */
    var parameters: Map<String, Any?> = emptyMap()
) {
    fun normalize() {
        parameters = parameters.mapValues { it.value.normalize() }
    }
}

@JsonClass(generateAdapter = true)
data class RtcpParameters @JvmOverloads constructor(
    /**
     * The Canonical Name (CNAME) used by RTCP (e.g. in SDES messages).
     */
    var cname: String? = null,

    /**
     * Whether reduced size RTCP RFC 5506 is configured (if true) or compound RTCP
     * as specified in RFC 3550 (if false). Default true.
     */
    var reducedSize: Boolean = true,

    /**
     * Whether RTCP-mux is used. Default true.
     */
    var mux: Boolean? = null
)

