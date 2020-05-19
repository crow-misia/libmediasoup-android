@file:Suppress("unused")

package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import java.util.*

fun generateRouterRtpCapabilities(): RtpCapabilities {
    val codecs = listOf(
        RtpCodecCapability(
            mimeType = "audio/opus",
            kind = "audio",
            clockRate = 48000,
            preferredPayloadType = 100,
            channels = 2,
            rtcpFeedback = emptyList(),
            parameters = mapOf("useinbandfec" to 1)
        ),
        RtpCodecCapability(
            mimeType = "video/VP8",
            kind = "video",
            clockRate = 90000,
            preferredPayloadType = 101,
            rtcpFeedback = listOf(
                RtcpFeedback("nack"),
                RtcpFeedback("nack", "pli"),
                RtcpFeedback("nack", "sli"),
                RtcpFeedback("nack", "rpsi"),
                RtcpFeedback("nack", "app"),
                RtcpFeedback("nack", "fir"),
                RtcpFeedback("goog-remb")
            ),
            parameters = mapOf("x-google-start-bitrate" to "1500")
        ),
        RtpCodecCapability(
            mimeType = "video/rtx",
            kind = "video",
            clockRate = 90000,
            preferredPayloadType = 102,
            rtcpFeedback = emptyList(),
            parameters = mapOf("apt" to 101)
        ),
		RtpCodecCapability(
			mimeType = "video/H264",
			kind = "video",
			clockRate = 90000,
			preferredPayloadType = 103,
			rtcpFeedback = listOf(
				RtcpFeedback("nack"),
				RtcpFeedback("nack", "pli"),
				RtcpFeedback("nack", "sli"),
				RtcpFeedback("nack", "rpsi"),
				RtcpFeedback("nack", "app"),
				RtcpFeedback("nack", "fir"),
				RtcpFeedback("goog-remb")
			),
			parameters = mapOf(
				"level-asymmetry-allowed" to 1,
				"packetization-mode" to 1,
				"profile-level-id" to "42e01f"
			)
		),
        RtpCodecCapability(
            mimeType = "video/rtx",
            kind = "video",
            clockRate = 90000,
            preferredPayloadType = 104,
			rtcpFeedback = emptyList(),
			parameters = mapOf("apt" to 103)
        )
	)

    val headerExtensions = listOf(
		RtpHeaderExtension(
			kind = "audio",
			uri = "urn:ietf:params:rtp-hdrext:ssrc-audio-level",
			preferredId = 1,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "video",
			uri = "urn:ietf:params:rtp-hdrext:ssrc-audio-level",
			preferredId = 2,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "video",
			uri = "http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time",
			preferredId = 3,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "video",
			uri = "urn:3gpp:video-orientation",
			preferredId = 4,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "audio",
			uri = "urn:ietf:params:rtp-hdrext:sdes:mid",
			preferredId = 5,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "video",
			uri = "urn:ietf:params:rtp-hdrext:sdes:mid",
			preferredId = 5,
			preferredEncrypt = false
		),
		RtpHeaderExtension(
			kind = "video",
			uri = "urn:ietf:params:rtp-hdrext:sdes:rtp-stream-id",
			preferredId = 6,
			preferredEncrypt = false
		)
	)

    return RtpCapabilities(
        codecs = codecs,
        headerExtensions = headerExtensions,
        fecMechanisms = emptyList()
    )
}

fun generateRtpParametersByKind(): Map<MediaKind, RtpParameters> {
	val routerRtpCapabilities = generateRouterRtpCapabilities()

	val audioCodecs = arrayListOf<RtpCodecParameters>()
	val videoCodecs = arrayListOf<RtpCodecParameters>()
	val audioHeaderExtensions = arrayListOf<RtpHeaderExtensionParameters>()
	val videoHeaderExtensions = arrayListOf<RtpHeaderExtensionParameters>()

	routerRtpCapabilities.codecs.forEach { codec ->
		val rtpCodec = codec.toRtpCodecParameters()
		when (codec.kind.toLowerCase(Locale.ENGLISH)) {
			"audio" -> audioCodecs.add(rtpCodec)
			"video" -> videoCodecs.add(rtpCodec)
		}
	}

	routerRtpCapabilities.headerExtensions.forEach { ext ->
		val rtpExt = ext.toRtpHeaderExtensionParameters()
		when (ext.kind.toLowerCase(Locale.ENGLISH)) {
			"audio" -> audioHeaderExtensions.add(rtpExt)
			"video" -> videoHeaderExtensions.add(rtpExt)
		}
	}

	return mapOf(
		"audio" to RtpParameters(codecs = audioCodecs, headerExtensions = audioHeaderExtensions),
		"video" to RtpParameters(codecs = videoCodecs, headerExtensions = videoHeaderExtensions)
	)
}

fun generateLocalDtlsParameters(): DtlsParameters {
	return DtlsParameters(
		role = DtlsRole.AUTO,
		fingerprints = listOf(
			DtlsFingerprint("sha-256", "82:5A:68:3D:36:C3:0A:DE:AF:E7:32:43:D2:88:83:57:AC:2D:65:E5:80:C4:B6:FB:AF:1A:A0:21:9F:6D:0C:AD")
		)
	)
}

fun generateTransportRemoteParameters(): TransportRemoteParameters {
	return TransportRemoteParameters(
		id = Utils.getRandomString(12),
		iceParameters = IceParameters(
			iceLite = true,
			password = "yku5ej8nvfaor28lvtrabcx0wkrpkztz",
			usernameFragment = "h3hk1iz6qqlnqlne"
		),
		iceCandidates = listOf(
			IceCandidate(
				family = "ipv4",
				foundation = "udpcandidate",
				ip = "9.9.9.9",
				port = 40533,
				priority = 1078862079L,
				protocol = "udp",
				type = "host"
			),
			IceCandidate(
				family = "ipv6",
				foundation = "udpcandidate",
				ip = "9:9:9:9:9:9",
				port = 41333,
				priority = 1078862089L,
				protocol = "udp",
				type = "host"
			)
		),
		dtlsParameters = DtlsParameters(
			fingerprints = listOf(
				DtlsFingerprint("sha-256", "A9:F4:E0:D2:74:D3:0F:D9:CA:A5:2F:9F:7F:47:FA:F0:C4:72:DD:73:49:D0:3B:14:90:20:51:30:1B:90:8E:71"),
				DtlsFingerprint("sha-384", "03:D9:0B:87:13:98:F6:6D:BC:FC:92:2E:39:D4:E1:97:32:61:30:56:84:70:81:6E:D1:82:97:EA:D9:C1:21:0F:6B:C5:E7:7F:E1:97:0C:17:97:6E:CF:B3:EF:2E:74:B0"),
				DtlsFingerprint("sha-512", "84:27:A4:28:A4:73:AF:43:02:2A:44:68:FF:2F:29:5C:3B:11:9A:60:F4:A8:F0:F5:AC:A0:E3:49:3E:B1:34:53:A9:85:CE:51:9B:ED:87:5E:B8:F4:8E:3D:FA:20:51:B8:96:EE:DA:56:DC:2F:5C:62:79:15:23:E0:21:82:2B:2C")
			),
			role = DtlsRole.AUTO
		),
		sctpParameters = SctpParameters(
			port = 5000,
			os = 2048,
			mis = 2048,
			maxMessageSize = 2000000
		)
	)
}

fun generateProducerRemoteId(): String {
	return Utils.getRandomString(12)
}

fun generateConsumerRemoteParameters(mimeType: String): ConsumerRemoteParameters {
	return when (mimeType) {
		"audio/opus" -> {
			ConsumerRemoteParameters(
				producerId = Utils.getRandomString(12),
				id = Utils.getRandomString(12),
				kind = "audio",
				rtpParameters = RtpParameters(
					codecs = listOf(
						RtpCodecParameters(
							mimeType =  "audio/opus",
							clockRate = 48000,
							payloadType = 100,
							channels = 2,
							rtcpFeedback = emptyList(),
							parameters = mapOf("useinbandfec" to "1")
						)
					),
					encodings = listOf(
						RtpEncodingParameters(
							ssrc = (1000000L..1999999L).random()
						)
					),
					headerExtensions = listOf(
						RtpHeaderExtensionParameters(
							uri = "urn:ietf:params:rtp-hdrext:ssrc-audio-level",
							id = 1L
						)
					),
					rtcp = RtcpParameters(
						cname = Utils.getRandomString(16),
						reducedSize = true,
						mux = true
					)
				)
			)
		}
		"audio/ISAC" -> {
			ConsumerRemoteParameters(
				producerId = Utils.getRandomString(12),
				id = Utils.getRandomString(12),
				kind = "audio",
				rtpParameters = RtpParameters(
					codecs = listOf(
						RtpCodecParameters(
							mimeType =  "audio/ISAC",
							clockRate = 16000,
							payloadType = 111,
							channels = 2,
							rtcpFeedback = emptyList(),
							parameters = emptyMap()
						)
					),
					encodings = listOf(
						RtpEncodingParameters(
							ssrc = (1000000L..1999999L).random()
						)
					),
					headerExtensions = listOf(
						RtpHeaderExtensionParameters(
							uri = "urn:ietf:params:rtp-hdrext:ssrc-audio-level",
							id = 1L
						)
					),
					rtcp = RtcpParameters(
						cname = Utils.getRandomString(16),
						reducedSize = true,
						mux = true
					)
				)
			)
		}
		"video/VP8" -> {
			ConsumerRemoteParameters(
				producerId = Utils.getRandomString(12),
				id = Utils.getRandomString(12),
				kind = "audio",
				rtpParameters = RtpParameters(
					codecs = listOf(
						RtpCodecParameters(
							mimeType =  "video/VP8",
							clockRate = 90000,
							payloadType = 101,
							rtcpFeedback = listOf(
								RtcpFeedback("nack"),
								RtcpFeedback("nack", "pli"),
								RtcpFeedback("nack", "sli"),
								RtcpFeedback("nack", "rpsi"),
								RtcpFeedback("nack", "app"),
								RtcpFeedback("nack", "fir"),
								RtcpFeedback("goog-remb")
							),
							parameters = mapOf("x-google-start-bitrate" to "1500")
						),
						RtpCodecParameters(
							mimeType =  "video/rtx",
							clockRate = 90000,
							payloadType = 102,
							rtcpFeedback = emptyList(),
							parameters = mapOf("apt" to "101")
						)
					),
					encodings = listOf(
						RtpEncodingParameters(
							ssrc = (2000000..2999999L).random(),
							rtx = RtxParameter(ssrc = (3000000..3999999L).random())
						)
					),
					headerExtensions = listOf(
						RtpHeaderExtensionParameters(
							uri = "urn:ietf:params:rtp-hdrext:toffset",
							id = 2L
						),
						RtpHeaderExtensionParameters(
							uri = "http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time",
							id = 3L
						)
					),
					rtcp = RtcpParameters(
						cname = Utils.getRandomString(16),
						reducedSize = true,
						mux = true
					)
				)
			)
		}
		else -> throw IllegalAccessException("unsupported mimeType: $mimeType")
	}
}