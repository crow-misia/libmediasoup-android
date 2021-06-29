package io.github.zncmn.mediasoup.sdp

import io.github.zncmn.mediasoup.model.*
import io.github.crow_misia.sdp.*
import io.github.crow_misia.sdp.attribute.FingerprintAttribute
import io.github.crow_misia.sdp.attribute.GroupAttribute
import io.github.crow_misia.sdp.attribute.IceLiteAttribute
import io.github.crow_misia.sdp.attribute.MsidSemanticAttribute
import io.github.crow_misia.webrtc.log.WebRtcLogger
import java.math.BigInteger

class RemoteSdp(
    private var iceParameters: IceParameters,
    private var iceCandidates: List<IceCandidate>,
    private var dtlsParameters: DtlsParameters,
    private var sctpParameters: SctpParameters?
) {
    companion object {
        private val TAG = RemoteSdp::class.simpleName
    }

    val sdp: String
        get() {
            // Increase SDP version.
            sessionDescription.origin.sessVersion++
            return sessionDescription.toString()
        }

    private val medisSections = hashMapOf<String, MediaSection>()
    private var firstMid = ""

    private val sessionDescription = SdpSessionDescription.of(
        version = SdpVersion.of(0),
        origin = SdpOrigin.of(
            unicastAddress = "0.0.0.0",
            addrtype = "IP4",
            nettype = "IN",
            sessId = BigInteger("10000"),
            sessVersion = BigInteger.ZERO,
            username = "libmediasoupclient"
        ),
        sessionName = SdpSessionName.of("-"),
        timings = listOf(
            SdpTiming.of(0, 0)
        ),
        mediaDescriptions = emptyList(),
        attributes = listOf(
            // add ICE-Lite indicator.
            IceLiteAttribute,
            MsidSemanticAttribute.of("WMS", "*")
        )
    )

    init {
        // NOTE: We take the latest fingerprint.
        dtlsParameters.fingerprints.lastOrNull()?.also {
            sessionDescription.addAttribute(FingerprintAttribute.of(
                type = it.algorithm,
                hash = it.value
            ))
        }
        sessionDescription.addAttribute(GroupAttribute.of("BUNDLE"))
    }

    fun updateIceParameters(iceParameters: IceParameters) {
        this.iceParameters = iceParameters

        sessionDescription.setAttribute(IceLiteAttribute)

        sessionDescription.getMediaDescriptions().forEach {
            it.setIceParameters(iceParameters)
        }
    }

    fun updateDtlsRole(role: DtlsRole) {
        dtlsParameters.role = role

        sessionDescription.setAttribute(IceLiteAttribute)

        medisSections.values.forEach {
            it.setDtlsRole(role)
        }
    }

    fun getNextMediaSectionMid(): Pair<Int, String?> {
        var idx = 0
        sessionDescription.getMediaDescriptions().forEach {
            if (it.isClosed()) {
                return idx to it.mid
            }
            idx++
        }
        return idx to null
    }

    fun send(offerMediaDescription: SdpMediaDescription, reuseMid: String?, offerRtpParameters: RtpParameters, answerRtpParameters: RtpParameters, codecOptions: ProducerCodecOptions?) {
        WebRtcLogger.v(TAG, "send [offerMediaDescription:%s, reuseMid:%s, offerRtpParameters:%s, answerRtpParameters:%s, codecOptions:%s]",
            offerMediaDescription, reuseMid, offerRtpParameters, answerRtpParameters, codecOptions)

        val mediaSection = AnswerMediaSection(
            iceCandidates = iceCandidates,
            iceParameters = iceParameters,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            offerMediaDescription = offerMediaDescription,
            offerRtpParameters = offerRtpParameters,
            answerRtpParameters = answerRtpParameters,
            codecOptions = codecOptions
        )

        if (reuseMid.isNullOrEmpty()) {
            addMediaSection(mediaSection)
        } else {
            replaceMediaSection(mediaSection, reuseMid)
        }
    }

    fun receive(mid: String, kind: MediaKind, offerRtpParameters: RtpParameters, streamId: String, trackId: String) {
        WebRtcLogger.d(TAG, "receive [mid:%s, kind:%s, offerRtpParameters:%s, streamId:%s, trackId:%s]",
            mid, kind, offerRtpParameters, streamId, trackId)

        val mediaSection = OfferMediaSection(
            iceCandidates = iceCandidates,
            iceParameters = iceParameters,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            mid = mid,
            kind = kind,
            offerRtpParameters = offerRtpParameters,
            streamId = streamId,
            trackId = trackId
        )

        addMediaSection(mediaSection)
    }

    fun disableMediaSection(mid: String) {
        sessionDescription.getMediaDescription(mid)?.disable()
    }

    fun closeMediaSection(mid: String) {
        sessionDescription.getMediaDescription(mid)?.also {
            if (mid == firstMid) {
                it.disable()
            } else {
                it.close()
            }

            // Regenerate BUNDLE mids.
            regenerateBundleMids()
        }
    }

    private fun addMediaSection(newMediaSection: MediaSection) {
        val mid = newMediaSection.mid
        if (firstMid.isEmpty()) {
            firstMid = mid
        }

        medisSections[mid] = newMediaSection
        sessionDescription.addMediaDescription(newMediaSection.mediaDescription)

        // Regenerate BUNDLE mids.
        regenerateBundleMids()
    }

    private fun replaceMediaSection(newMediaSection: MediaSection, reuseMid: String) {
        medisSections.remove(reuseMid)
        medisSections[newMediaSection.mid] = newMediaSection
        sessionDescription.setMediaDescription(newMediaSection.mediaDescription, reuseMid)

        // Regenerate BUNDLE mids.
        regenerateBundleMids()
    }

    private fun regenerateBundleMids() {
        val mids = sessionDescription.getMediaDescriptions().filterNot { it.isClosed() }.map { it.mid }.toMutableList()
        sessionDescription.getAttribute<GroupAttribute>()?.mids = mids
    }
}