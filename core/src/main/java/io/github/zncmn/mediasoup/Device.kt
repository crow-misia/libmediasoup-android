package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import io.github.zncmn.webrtc.log.WebRtcLogger
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

class Device(
    private val peerConnectionFactory: PeerConnectionFactory
) {
    companion object {
        private val TAG = Device::class.simpleName
    }

    /**
     * Whether the Device is loaded.
     */
    var loaded: Boolean = false
        private set

    /** Extended RTP capabilities. */
    lateinit var extendedRtpCapabilities: ExtendedRtpCapabilities
        private set

    /** Local RTP capabilities for receiving media. */
    lateinit var rtpCapabilities: RtpCapabilities
        private set

    /** Whether we can produce audio/video based on computed extended RTP capabilities. */
    private val canProduceByKind: MutableMap<MediaKind, Boolean> = mutableMapOf(
        "audio" to false,
        "video" to false
    )

    /** Local SCTP capabilities. */
    lateinit var sctpCapabilities: SctpCapabilities
        private set

    /**
     * Initialize the Device.
     */
    @Throws(MediasoupException::class)
    suspend fun load(routerRtpCapabilities: RtpCapabilities) {
        if (loaded) {
            WebRtcLogger.w(TAG, "already loaded")
            return
        }

        // This may thrw.
        routerRtpCapabilities.validate()

        // get Native RTP capabilities.
        val nativeRtpCapabilities = Handler.getNativeRtpCapabilities(peerConnectionFactory)
        WebRtcLogger.d(TAG, "got native RTP capabilities:\n%s", rtpCapabilitiesAdapter.indent("  ").toJson(nativeRtpCapabilities))

        // This may throw.
        nativeRtpCapabilities.validate()

        // Get extended RTP capability.
        extendedRtpCapabilities = nativeRtpCapabilities.extended(routerRtpCapabilities)
        WebRtcLogger.d(TAG, "got extended RTP capabilities:\n%s", extendedRtpCapabilitiesAdapter.indent("  ").toJson(extendedRtpCapabilities))

        // Check whether we can produce audio/video.
        canProduceByKind["audio"] = extendedRtpCapabilities.canSend("audio")
        canProduceByKind["video"] = extendedRtpCapabilities.canSend("video")

        // Generate our receiving RTP capabilities for receiving media.
        rtpCapabilities = extendedRtpCapabilities.getRecvRtpCapabilities()

        WebRtcLogger.d(TAG, "got receiving RTP capabilities:\n%s", rtpCapabilitiesAdapter.indent(" ").toJson(rtpCapabilities))

        // This may throw.
        rtpCapabilities.validate()

        // Generate our SCTP capabilities.
        sctpCapabilities = Handler.getNativeSctpCapabilities()

        WebRtcLogger.d(TAG, "got receiving SCTP capabilities:\n%s", sctpCapabilitiesAdapter.indent(" ").toJson(sctpCapabilities))

        // This may throw.
        sctpCapabilities.validate()

        WebRtcLogger.d(TAG, "succeeded")

        loaded = true
    }

    /**
     * Whether we can produce audio/video.
     */
    @Throws(MediasoupException::class)
    fun canProduce(kind: MediaKind): Boolean {
        check(loaded) { "not loaded" }
        if (kind != "audio" && kind != "video") {
            throw MediasoupException("invalid kind: $kind")
        }
        return canProduceByKind[kind] ?: false
    }

    @JvmOverloads
    @Throws(MediasoupException::class)
    fun createSendTransport(
        listener: SendTransport.Listener,
        id: String,
        iceParameters: IceParameters,
        iceCandidates: List<IceCandidate>,
        dtlsParameters: DtlsParameters,
        sctpParameters: SctpParameters?,
        rtcConfig: PeerConnection.RTCConfiguration,
        appData: Any? = null
    ): SendTransport {
        check(loaded) { "not loaded" }

        // Validate arguments.
        iceParameters.validate()
        iceCandidates.validate()
        dtlsParameters.validate()
        sctpParameters?.validate()

        // Create a new Transport.
        return SendTransport(
            listener = listener,
            id = id,
            iceParameters = iceParameters,
            iceCandidates = iceCandidates,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            rtcConfig = rtcConfig,
            peerConnectionFactory = peerConnectionFactory,
            extendedRtpCapabilities = extendedRtpCapabilities,
            canProduceByKind = canProduceByKind,
            appData = appData
        )
    }

    @JvmOverloads
    @Throws(MediasoupException::class)
    fun createRecvTransport(
        listener: RecvTransport.Listener,
        id: String,
        iceParameters: IceParameters,
        iceCandidates: List<IceCandidate>,
        dtlsParameters: DtlsParameters,
        sctpParameters: SctpParameters?,
        rtcConfig: PeerConnection.RTCConfiguration,
        appData: Any? = null
    ): RecvTransport {
        check(loaded) { "not loaded" }

        // Validate arguments.
        iceParameters.validate()
        iceCandidates.validate()
        dtlsParameters.validate()
        sctpParameters?.validate()

        // Create a new Transport.
        return RecvTransport(
            listener = listener,
            id = id,
            iceParameters = iceParameters,
            iceCandidates = iceCandidates,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            rtcConfig = rtcConfig,
            peerConnectionFactory = peerConnectionFactory,
            extendedRtpCapabilities = extendedRtpCapabilities,
            appData = appData
        )
    }
}