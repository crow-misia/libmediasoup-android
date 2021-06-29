@file:Suppress("unused")

package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import io.github.zncmn.mediasoup.model.IceCandidate
import io.github.zncmn.mediasoup.model.RtpParameters
import io.github.zncmn.mediasoup.sdp.toNormalizeInt
import io.github.zncmn.mediasoup.sdp.toNormalizeString
import org.webrtc.*
import org.webrtc.generateProfileLevelIdForAnswer
import org.webrtc.isSameH264Profile
import java.util.*
import java.util.regex.Pattern

private const val RTP_PROBATOR_MID = "probator"
private const val RTP_PROBATOR_SSRC = 1234L
private const val RTP_PROBATOR_CODEC_PAYLOAD_TYPE = 127

private val MIME_TYPE_PATTERN = "^(audio|video)/(.+)".toPattern(Pattern.CASE_INSENSITIVE)
private val PROTOCOL_PATTERN = "(udp|tcp)".toPattern(Pattern.CASE_INSENSITIVE)
private val TYPE_PATTERN = "(host|srflx|prflx|relay)".toPattern(Pattern.CASE_INSENSITIVE)

/**
 * Validates RtpCapabilities.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
@Throws(MediasoupException::class)
fun RtpCapabilities.validate() {
    // codecs is optional. If unset, fill with an empty array.

    codecs.forEach { it.validate() }

    // headerExtensions is optional. If unset, fill with an empty array.

    headerExtensions.forEach { it.validate() }
}

/**
 * Validates RtpCodecCapability.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
@Throws(MediasoupException::class)
fun RtpCodecCapability.validate() {
    normalize()

    // mimeType is mandatory.
    val mimeTypeMatch = MIME_TYPE_PATTERN.matcher(mimeType)
    if (!mimeTypeMatch.matches()) {
        throw MediasoupException("invalid codec.mimeType")
    }
    // Just override kind with media component of mimeType.
    kind = mimeTypeMatch.group(1) ?: ""

    // preferredPayloadType is optional.

    // clockRate is mandatory.

    // channels is optional. If unset, set it to 1 (just if audio).
    when (kind) {
        "audio" -> {
            if (channels == null) {
                channels = 1
            }
        }
        else -> {
            channels = null
        }
    }

    // parameters is optional. If unset, set it to an empty object.
    parameters.forEach { (key, value) ->
        if (value !is String && value !is Number && value != null) {
            throw MediasoupException("invalid codec parameter")
        }

        // Specific parameters validation.
        if (key == "apt" && value !is Int) {
            throw MediasoupException("invalid codec apt parameter")
        }
    }

    // rtcpFeedback is optional. If unset, set it to an empty array.

    rtcpFeedback.forEach { it.validate() }
}

/**
 * Validates RtcpFeedback.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtcpFeedback.validate() {
    // type is mandatory.

    // parameter is optional. If unset set it to an empty string.
}

/**
 * Validates RtpHeaderExtension.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtpHeaderExtension.validate() {
    // kind is optional. If unset set it to an empty string.

    if (kind.isNotEmpty() && kind != "audio" && kind != "video") {
        throw MediasoupException("invalid ext.kind")
    }

    // uri is mandatory.
    if (uri.isEmpty()) {
        throw MediasoupException("missing ext.uri")
    }

    // preferredId is mandatory.

    // preferredEncrypt is optional. If unset set it to false.

    // direction is optional. If unset set it to sendrecv.
}

/**
 * Validates RtpParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtpParameters.validate() {
    // mid is optional.
    if (mid?.isEmpty() == true) {
        throw MediasoupException("params.mid is not a string")
    }

    // codecs is mandatory.

    codecs.forEach { it.validate() }

    // headerExtensions is optional. If unset, fill with an empty array.

    headerExtensions.forEach { it.validate() }

    // encodings is optional. If unset, fill with an empty array.

    encodings.forEach { it.validate() }

    // rtcp is optional. If unset, fill with an empty object.

    rtcp.validate()
}

/**
 * Validates RtpCodecParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtpCodecParameters.validate() {
    normalize()

    // mimeType is mandatory.

    val mimeTypeMatch = MIME_TYPE_PATTERN.matcher(mimeType)
    if (!mimeTypeMatch.matches()) {
        throw MediasoupException("invalid codec.mimeType")
    }

    // payloadType is mandatory.

    // clockRate is mandatory.

    // Retrieve media kind from mimeType.

    // channels is optional. If unset, set it to 1 (just for audio).
    when (mimeTypeMatch.group(1)) {
        "audio" -> {
            if (channels == null) {
                channels = 1
            }
        }
        else -> {
            channels = null
        }
    }

    // parameters is optional. If unset, set it to an empty object.
    parameters.forEach { (key, value) ->
        if (value != null && value !is String && value !is Number) {
            throw MediasoupException("invalid codec parameter")
        }

        // Specific parameters validation.
        if (key == "apt" && value !is Int) {
            throw MediasoupException("invalid codec apt parameter")
        }
    }

    // rtcpFeedback is optional. If unset, set it to an empty array.

    rtcpFeedback.forEach { it.validate() }
}

/**
 * Validates RtpHeaderExtensionParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtpHeaderExtensionParameters.validate() {
    normalize()

    // uri is mandatory.
    if (uri.isEmpty()) {
        throw MediasoupException("missing ext.uri")
    }

    // id is mandatory.

    // encrypt is optional. If unset set it to false.

    // parameters is optional. If unset, set it to an empty object.
    parameters.values.forEach {
        if (it !is String && it !is Number) {
            throw MediasoupException("invalid header extension parameter")
        }
    }
}

/**
 * Validates RtpEncodingParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtpEncodingParameters.validate() {
    // ssrc is optional.

    // rid is optional.
    if (rid?.isEmpty() == true) {
        throw MediasoupException("invalid header extension parameter")
    }

    // rtx is optional.
    val rtx = rtx
    // RTX ssrc is mandatory if rtx is present.
    if (rtx != null && rtx.ssrc == null) {
        throw MediasoupException("missing encoding.rtx.ssrc")
    }

    // dtx is optional. If unset set it to false.

    // scalabilityMode is optional.
    if (scalabilityMode?.isEmpty() == true) {
        throw MediasoupException("invalid encoding.scalabilityMode")
    }
}

/**
 * Validates RtcpParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun RtcpParameters.validate() {
    // cname is optional.

    // reducedSize is optional. If unset set it to true.
}

/**
 * Validates SctpCapabilities.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun SctpCapabilities.validate() {
    // numStreams is mandatory.

    numStreams.forEach { it.validate() }
}

/**
 * Validates NumSctpStreams. It may modify given data by adding missing
 * fields with default values.
 * It throws if invalid.
 */
fun NumSctpStreams.validate() {
    // OS is mandatory.

    // MIS is mandatory.
}

/**
 * Validates SctpParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun SctpParameters.validate() {
    // port is mandatory.

    // OS is mandatory.

    // MIS is mandatory.

    // maxMessageSize is mandatory.
}

/**
 * Validates SctpStreamParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun SctpStreamParameters.validate() {
    // streamId is mandatory.

    // ordered is optional.

    // maxPacketLifeTime is optional. If unset set it to 0.

    // maxRetransmits is optional. If unset set it to 0.

    // priority is optional. If unset set it to empty string.

    // label is optional. If unset set it to empty string.

    // protocol is optional. If unset set it to empty string.
}

/**
 * Validates IceParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun IceParameters.validate() {
    // usernameFragment is mandatory.
    if (usernameFragment.isEmpty()) {
        throw MediasoupException("missing params.usernameFragment")
    }

    // password is mandatory.
    if (password.isEmpty()) {
        throw MediasoupException("missing params.password")
    }

    // iceLIte is optional. If unset set it to false.
}

/**
 * Validates IceCandidate.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun IceCandidate.validate() {
    // foundation is mandatory.
    if (foundation.isEmpty()) {
        throw MediasoupException("missing params.foundation")
    }

    // priority is mandatory.
    if (priority < 0L) {
        throw MediasoupException("missing params.priority")
    }

    // ip is mandatory.
    if (ip.isEmpty()) {
        throw MediasoupException("missing params.ip")
    }

    // protocol is mandatory.
    if (protocol.isEmpty()) {
        throw MediasoupException("missing params.protocol")
    }
    if (!PROTOCOL_PATTERN.matcher(protocol).matches()) {
        throw MediasoupException("invalid params.protocol")
    }

    // port is mandatory.
    if (port < 0) {
        throw MediasoupException("missing params.port")
    }

    // type is mandatory.
    if (type.isEmpty()) {
        throw MediasoupException("missing params.type")
    }
    if (!TYPE_PATTERN.matcher(type).matches()) {
        throw MediasoupException("invalid params.type")
    }
}

/**
 * Validates IceCandidates.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun List<IceCandidate>.validate() {
    forEach { it.validate() }
}

/**
 * Validates DtlsFingerprint.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun DtlsFingerprint.validate() {
    // foundation is mandatory.
    if (algorithm.isEmpty()) {
        throw MediasoupException("missing params.algorithm")
    }

    // value is mandatory.
    if (value.isEmpty()) {
        throw MediasoupException("missing params.value")
    }
}

/**
 * Validates DtlsParameters.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun DtlsParameters.validate() {
    // role is mandatory.

    // fingerprints is mandatory.
    if (fingerprints.isEmpty()) {
        throw MediasoupException("missing params.fingerprints")
    }
    fingerprints.forEach { it.validate() }
}

/**
 * Validates Producer codec options.
 * It may modify given data by adding missing fields with default values.
 * It throws if invalid.
 */
fun ProducerCodecOptions.validate() {
    if ((opusMaxPlaybackRate ?: 0) < 0) {
        throw MediasoupException("invalid params.opusMaxPlaybackRate")
    }
}

/**
 * Generate extended RTP capabilities for sending and receiving.
 */
internal fun RtpCapabilities.extended(remoteCaps: RtpCapabilities): ExtendedRtpCapabilities {
    // This may throw.
    validate()
    remoteCaps.validate()

    // Match media codecs and keep the order preferred by remoteCaps.
    val extendedCodecs = remoteCaps.codecs.asSequence()
        .filterNot { it.isRtxCodec() }
        .mapNotNull { remoteCodec ->
            val matchingLocalCodec = codecs.find {
                it.matchCodec(remoteCodec, strict = true, modify = true)
            } ?: return@mapNotNull null

            ExtendedRtpCodecCapability(
                mimeType = matchingLocalCodec.mimeType,
                kind = matchingLocalCodec.kind,
                clockRate = matchingLocalCodec.clockRate,
                localPayloadType = matchingLocalCodec.preferredPayloadType,
                localRtxPayloadType = null,
                remotePayloadType = remoteCodec.preferredPayloadType,
                remoteRtxPayloadType = null,
                localParameters = matchingLocalCodec.parameters,
                remoteParameters = remoteCodec.parameters,
                rtcpFeedback = matchingLocalCodec.reduceRtcpFeedback(remoteCodec),
                channels = matchingLocalCodec.channels
            )
        }.toList()

    // Match RTX codecs
    extendedCodecs.forEach { extendedCodec ->
        val matchingLocalRtxCodec = codecs.find { it.isRtxCodec() && it.parameters["apt"] == extendedCodec.localPayloadType }
            ?: return@forEach
        val matchingRemoteRtxCodec = remoteCaps.codecs.find { it.isRtxCodec() && it.parameters["apt"] == extendedCodec.remotePayloadType }
            ?: return@forEach
        extendedCodec.localRtxPayloadType = matchingLocalRtxCodec.preferredPayloadType
        extendedCodec.remoteRtxPayloadType = matchingRemoteRtxCodec.preferredPayloadType
    }

    // Match header extensions.
    val extendedHeaderExtensions = remoteCaps.headerExtensions.mapNotNull { remoteExt ->
        val matchingLocalExt = headerExtensions.find { it.match(remoteExt) } ?: return@mapNotNull null

        // TODO: Must do stuff for encrypted extensions.

        ExtendedRtpHeaderExtension(
            kind = remoteExt.kind,
            uri = remoteExt.uri,
            sendId = matchingLocalExt.preferredId,
            recvId = remoteExt.preferredId,
            encrypt = matchingLocalExt.preferredEncrypt,
            direction = when(remoteExt.direction) {
                RtpHeaderExtensionDirection.SENDRECV -> RtpHeaderExtensionDirection.SENDRECV
                RtpHeaderExtensionDirection.RECVONLY -> RtpHeaderExtensionDirection.SENDONLY
                RtpHeaderExtensionDirection.SENDONLY -> RtpHeaderExtensionDirection.RECVONLY
                RtpHeaderExtensionDirection.INACTIVE -> RtpHeaderExtensionDirection.INACTIVE
            }
        )
    }

    return ExtendedRtpCapabilities(
        codecs = extendedCodecs,
        headerExtensions = extendedHeaderExtensions
    )
}

/**
 * Generate RTP capabilities for receiving media based on the given extended
 * RTP capabilities.
 */
internal fun ExtendedRtpCapabilities.getRecvRtpCapabilities(): RtpCapabilities {
    val rtpCodecs = codecs.flatMap { codec ->
        val kind = codec.kind
        arrayListOf(
            RtpCodecCapability(
                mimeType = codec.mimeType,
                kind = codec.kind,
                preferredPayloadType = codec.remotePayloadType,
                clockRate = codec.clockRate,
                parameters = codec.localParameters,
                rtcpFeedback = codec.rtcpFeedback,
                channels = codec.channels
            )
        ).also {
            // Add RTX codec.
            val remoteRtxPayloadType = codec.remoteRtxPayloadType ?: return@also

            it.add(RtpCodecCapability(
                mimeType = "$kind/rtx",
                kind = kind,
                preferredPayloadType = remoteRtxPayloadType,
                clockRate = codec.clockRate,
                parameters = mapOf(
                    "apt" to codec.remotePayloadType
                ),
                rtcpFeedback = emptyList()
            ))

            // TODO: In the future, we need to add FEC, CN, etc, codecs.
        }
    }

    val headerExtensions = headerExtensions
        // Ignore RTP extensions not valid for receiving.
        .filter { it.direction == RtpHeaderExtensionDirection.SENDRECV || it.direction == RtpHeaderExtensionDirection.RECVONLY }
        .map { ext -> RtpHeaderExtension(
            kind = ext.kind,
            uri = ext.uri,
            preferredId = ext.recvId,
            preferredEncrypt = ext.encrypt,
            direction = ext.direction
        )}

    return RtpCapabilities(
        codecs = rtpCodecs,
        headerExtensions = headerExtensions
    )
}

/**
 * Generate RTP parameters of the given kind for sending media.
 * Just the first media codec per kind is considered.
 * NOTE: mid, encodings and rtcp fields are left empty.
 */
internal fun ExtendedRtpCapabilities.getSendingRtpParameters(kind: MediaKind): RtpParameters {
    val rtpCodecs = codecs
        .filter { it.kind == kind }
        // NOTE: We assume a single media codec plus an optional RTX codec.
        .take(1)
        .flatMap { codec -> arrayListOf(
            RtpCodecParameters(
                mimeType = codec.mimeType,
                payloadType = codec.localPayloadType,
                clockRate = codec.clockRate,
                parameters = codec.localParameters,
                rtcpFeedback = codec.rtcpFeedback,
                channels = codec.channels
            )
        ).also {
            // Add RTX codec.
            val payloadType = codec.localRtxPayloadType ?: return@also

            it.add(RtpCodecParameters(
                mimeType = "${codec.kind}/rtx",
                payloadType = payloadType,
                clockRate = codec.clockRate,
                parameters = mapOf(
                    "apt" to codec.localPayloadType
                )
            ))
        } }

    val rtpHeaderExtensions = headerExtensions
        .filter {it.kind == kind && (it.direction == RtpHeaderExtensionDirection.SENDRECV || it.direction == RtpHeaderExtensionDirection.SENDONLY) }
        .map { extension ->
            RtpHeaderExtensionParameters(
                uri = extension.uri,
                id = extension.sendId,
                encrypt = extension.encrypt,
                parameters = emptyMap()
            )
        }


    return RtpParameters(
        mid = null,
        codecs = rtpCodecs,
        headerExtensions = rtpHeaderExtensions
    )
}

/**
 * Generate RTP parameters of the given kind for sending media.
 */
internal fun ExtendedRtpCapabilities.getSendingRemoteRtpParameters(kind: MediaKind): RtpParameters {
    val rtpCodecs = codecs
        .filter { it.kind == kind }
        // NOTE: We assume a single media codec plus an optional RTX codec.
        .take(1)
        .flatMap { codec -> arrayListOf(
            RtpCodecParameters(
                mimeType = codec.mimeType,
                payloadType = codec.localPayloadType,
                clockRate = codec.clockRate,
                parameters = codec.remoteParameters,
                rtcpFeedback = codec.rtcpFeedback,
                channels = codec.channels
            )
        ).also {
            // Add RTX codec.
            val payloadType = codec.localRtxPayloadType ?: return@also

            it.add(RtpCodecParameters(
                mimeType = "${codec.kind}/rtx",
                payloadType = payloadType,
                clockRate = codec.clockRate,
                parameters = mapOf(
                    "apt" to codec.localPayloadType
                ),
                rtcpFeedback = emptyList()
            ))
        } }

    val rtpHeaderExtensions = headerExtensions
        // Ignore RTP extensions not valid for sending.
        .filter { it.kind == kind && (it.direction == RtpHeaderExtensionDirection.SENDRECV || it.direction == RtpHeaderExtensionDirection.SENDONLY) }
        .map { ext -> RtpHeaderExtensionParameters(
            uri = ext.uri,
            id = ext.sendId,
            encrypt = ext.encrypt,
            parameters = emptyMap()
        )}

    // Reduce codecs' RTCP feedback. Use Transport-CC if available, REMB otherwise.
    var isRemoveGoogRemb = rtpHeaderExtensions.none { it.uri == "http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01" }
    var isRemoveTransportCC = rtpHeaderExtensions.none { it.uri == "http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time" }

    if (!isRemoveGoogRemb && !isRemoveTransportCC) {
        isRemoveGoogRemb = true
        isRemoveTransportCC = true
    }

    codecs.forEach { codec ->
        codec.rtcpFeedback = codec.rtcpFeedback.filterNot {
            (isRemoveGoogRemb && it.type == "goog-remb") ||
            (isRemoveTransportCC && it.type == "transport-cc")
        }
    }

    return RtpParameters(
        mid = null,
        codecs = rtpCodecs,
        headerExtensions = rtpHeaderExtensions
    )
}

/**
 * Create RTP parameters for a Consumer for the RTP probator.
 */
fun RtpParameters.generateProbatorRtpParameters(): RtpParameters {
    // This may throw.
    validate()

    val rtpCodecs = codecs.firstOrNull()?.let { listOf(it.copy(payloadType = RTP_PROBATOR_CODEC_PAYLOAD_TYPE)) } ?: emptyList()

    val rtpHeaderExtensions = headerExtensions.filter {
        it.uri == "http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time" ||
        it.uri == "http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01"
    }

    return RtpParameters(
        mid = RTP_PROBATOR_MID,
        codecs = rtpCodecs,
        headerExtensions = rtpHeaderExtensions,
        encodings = listOf(RtpEncodingParameters(ssrc = RTP_PROBATOR_SSRC)),
        rtcp = RtcpParameters(cname = "probator")
    )
}

/**
 * Whether media can be sent based on the given RTP capabilities.
 */
fun ExtendedRtpCapabilities.canSend(kind: MediaKind): Boolean {
    return codecs.any { it.kind == kind }
}

/**
 * Whether the given RTP parameters can be received with the given RTP
 * capabilities.
 */
fun ExtendedRtpCapabilities.canReceive(rtpParameters: RtpParameters): Boolean {
    val firstMediaCodec = rtpParameters.codecs.firstOrNull() ?: return false

    return codecs.any { it.remotePayloadType == firstMediaCodec.payloadType }
}

private fun RtpCodecCapability.isRtxCodec(): Boolean {
    return mimeType.endsWith("/rtx")
}

private fun RtpCodecCapability.matchCodec(target: RtpCodecCapability, strict: Boolean, modify: Boolean): Boolean {
    val aMimeType = mimeType
    val bMimeType = target.mimeType

    if (aMimeType != bMimeType) {
        return false
    }
    if (clockRate != target.clockRate) {
        return false
    }
    if (channels != target.channels) {
        return false
    }

    when (aMimeType.lowercase(Locale.ENGLISH)) {
        // Match H264 parameters.
        "video/h264" -> {
            val aPacketizationMode = getH264PacketizationMode()
            val bPacketizationMode = target.getH264PacketizationMode()
            if (aPacketizationMode != bPacketizationMode) {
                return false
            }

            // If strict matching check profile-level-id.
            if (strict) {
                val aParameters = generateH264Profile(
                    levelAsymmetryAllowed = getH264LevelAssimetryAllowed().toString(),
                    packetizationMode = aPacketizationMode.toString(),
                    profileLevelId = getH264ProfileLevelId()
                )
                val bParameters = generateH264Profile(
                    levelAsymmetryAllowed = target.getH264LevelAssimetryAllowed().toString(),
                    packetizationMode = bPacketizationMode.toString(),
                    profileLevelId = target.getH264ProfileLevelId()
                )

                if (!isSameH264Profile(aParameters, bParameters)) {
                    return false
                }

                val answerParameters = hashMapOf<String, String>()
                if (!generateProfileLevelIdForAnswer(aParameters, bParameters, answerParameters)) {
                    return false
                }

                if (modify) {
                    answerParameters[H264_FMTP_PROFILE_LEVEL_ID]?.also {
                        parameters += H264_FMTP_PROFILE_LEVEL_ID to it
                    } ?: run {
                        parameters -= H264_FMTP_PROFILE_LEVEL_ID
                    }
                }
            }
        }
        // Match VP9 parameters.
        "video/vp9" -> {
            // If strict matching check profile-id.
            if (strict) {
                if (getVP9ProfileId() != target.getVP9ProfileId()) {
                    return false
                }
            }
        }
    }

    return true
}

private fun RtpHeaderExtension.match(target: RtpHeaderExtension): Boolean {
    return kind == target.kind && uri == target.uri
}

private fun RtpCodecCapability.reduceRtcpFeedback(target: RtpCodecCapability): List<RtcpFeedback> {
    return target.rtcpFeedback.filter { fb ->
        rtcpFeedback.any { it.type == fb.type && it.parameter == fb.parameter }
    }
}

private fun RtpCodecCapability.getH264PacketizationMode(): Int {
    return parameters["packetization-mode"]?.toNormalizeInt() ?: 0
}

private fun RtpCodecCapability.getH264LevelAssimetryAllowed(): Int {
    return parameters["level-asymmetry-allowed"]?.toNormalizeInt() ?: 0
}

private fun RtpCodecCapability.getH264ProfileLevelId(): String {
    return parameters["profile-level-id"]?.toNormalizeString() ?: ""
}

private fun RtpCodecCapability.getVP9ProfileId(): String {
    return parameters["profile-id"]?.toNormalizeString() ?: "0"
}
