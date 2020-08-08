package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import io.github.zncmn.mediasoup.model.IceCandidate
import io.github.zncmn.mediasoup.model.RtpParameters
import io.github.zncmn.mediasoup.sdp.*
import io.github.zncmn.mediasoup.sdp.applyCodecParameters
import io.github.zncmn.mediasoup.sdp.extractDtlsParameters
import io.github.zncmn.mediasoup.sdp.extractRtpCapabilities
import io.github.zncmn.sdp.SdpMediaDescription
import io.github.zncmn.sdp.SdpSessionDescription
import io.github.zncmn.sdp.attribute.CNameAttribute
import io.github.zncmn.sdp.attribute.SsrcAttribute
import io.github.zncmn.sdp.attribute.SsrcGroupAttribute
import io.github.zncmn.webrtc.createAnswer
import io.github.zncmn.webrtc.createOffer
import io.github.zncmn.webrtc.log.WebRtcLogger
import io.github.zncmn.webrtc.observer.PeerConnectionDefaultObserver
import io.github.zncmn.webrtc.setLocalDescription
import io.github.zncmn.webrtc.setRemoteDescription
import kotlinx.coroutines.runBlocking
import org.webrtc.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class Handler(
    private val listener: PrivateListener,
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    private val rtcConfig: PeerConnection.RTCConfiguration,
    peerConnectionFactory: PeerConnectionFactory
) : PeerConnectionDefaultObserver {
    companion object {
        private val SCTP_NUM_STREAMS = listOf(NumSctpStreams(os = 1024, mis = 1024))

        internal val sdpConstraints = MediaConstraints()

        fun createConfiguration(): PeerConnection.RTCConfiguration {
            return createConfiguration(PeerConnection.RTCConfiguration(emptyList()))
        }

        fun createConfiguration(config: PeerConnection.RTCConfiguration): PeerConnection.RTCConfiguration {
            config.enableDtlsSrtp = true
            config.keyType = PeerConnection.KeyType.ECDSA
            config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

            config.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            config.enableCpuOveruseDetection = true
            return config
        }

        suspend fun getNativeRtpCapabilities(peerConnectionFactory: PeerConnectionFactory): RtpCapabilities {
            val pc = checkNotNull(peerConnectionFactory.createPeerConnection(createConfiguration(), object : PeerConnectionDefaultObserver { }))
            pc.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO)
            pc.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO)

            // May throw.
            val offer = pc.createOffer(sdpConstraints)
            val sdp = SdpSessionDescription.parse(offer.description)
            return sdp.extractRtpCapabilities()
        }

        fun getNativeSctpCapabilities(): SctpCapabilities {
            return SctpCapabilities(numStreams = SCTP_NUM_STREAMS)
        }
    }

    internal interface PrivateListener {
        fun onConnect(dtlsParameters: DtlsParameters)
        fun onConnectionStateChange(newState: PeerConnection.IceConnectionState)
    }

    protected val peerConnection: PeerConnection by lazy {
        val dependencies = PeerConnectionDependencies.builder(this).createPeerConnectionDependencies()
        peerConnectionFactory.createPeerConnection(createConfiguration(rtcConfig), dependencies) ?: run {
            throw MediasoupException("failed create peerConnection")
        }
    }
    protected val remoteSdp = RemoteSdp(iceParameters, iceCandidates, dtlsParameters, sctpParameters)
    protected var transportReady = false
    protected val mapMidTransceiver = ConcurrentHashMap<String, RtpTransceiver>()

    // TODO change API
    val transportStats: RTCStatsReport
        get() = runBlocking {
            suspendCoroutine<RTCStatsReport> { continuation ->
                peerConnection.getStats { continuation.resume(it) }
            }
        }

    fun close() {
        peerConnection.close()
    }

    fun dispose() {
        mapMidTransceiver.values.forEach { it.dispose() }
        peerConnection.dispose()
    }

    abstract suspend fun restartIce(iceParameters: IceParameters)

    fun updateIceServers(iceServers: List<String>) {
        rtcConfig.iceServers.also {
            it.clear()
            it.addAll(iceServers.map { server -> PeerConnection.IceServer.builder(server).createIceServer() })
        }
        if (peerConnection.setConfiguration(rtcConfig)) {
            return
        }
        throw MediasoupException("failed to update ICE servers")
    }

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
        listener.onConnectionStateChange(newState)
    }

    protected fun setupTransport(localDtlsRole: DtlsRole, localSdp: SdpSessionDescription) {
        // Get our local DTLS parameters.
        val dtlsParameters = localSdp.extractDtlsParameters()

        // Set our DTLS role.
        dtlsParameters.role = localDtlsRole

        // Update the remote DTLS role in the SDP.
        val remoteDtlsRole = if (localDtlsRole == DtlsRole.CLIENT) DtlsRole.SERVER else DtlsRole.CLIENT
        remoteSdp.updateDtlsRole(remoteDtlsRole)

        // May throw.
        listener.onConnect(dtlsParameters)
        transportReady = true
    }
}

internal class SendHandler(
    listener: PrivateListener,
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    rtcConfig: PeerConnection.RTCConfiguration,
    peerConnectionFactory: PeerConnectionFactory,
    private val sendingRtpParametersByKind: Map<MediaKind, RtpParameters>,
    private val sendingRemoteRtpParametersByKind: Map<MediaKind, RtpParameters>
) : Handler(listener, iceParameters, iceCandidates, dtlsParameters, sctpParameters, rtcConfig, peerConnectionFactory) {
    companion object {
        private val TAG = SendHandler::class.simpleName
    }

    suspend fun send(
        track: MediaStreamTrack,
        encodings: List<RtpEncodingParameters>,
        codecOptions: ProducerCodecOptions?
    ): Triple<String, RtpParameters, RtpSender> {
        val kind = track.kind()
        WebRtcLogger.d(TAG, "send [id:%s, kind:%s]", track.id(), kind)

        encodings.forEachIndexed { idx, encoding ->
            encoding.rid = "r$idx"
        }

        val (mediaSectionIdx, reuseMid) = remoteSdp.getNextMediaSectionMid()

        val sendOnlyDirection = RtpTransceiver.RtpTransceiverInit(
            RtpTransceiver.RtpTransceiverDirection.SEND_ONLY, emptyList(), encodings.map {
                it.toRtpParametersEncoding()
            })
        val transceiver = peerConnection.addTransceiver(track, sendOnlyDirection)
        transceiver.direction = RtpTransceiver.RtpTransceiverDirection.SEND_ONLY

        var offer: SessionDescription
        val localId: String
        val sendingRtpParameters = sendingRtpParametersByKind.getOrElse(kind) {
            throw MediasoupException("$kind not found in sendingRtpParametersByKind")
        }

        try {
            offer = peerConnection.createOffer(sdpConstraints)
            val localSdp = SdpSessionDescription.parse(offer.description)

            // Transport is not ready.
            if (!transportReady) {
                setupTransport(DtlsRole.SERVER, localSdp)
                offer = SessionDescription(SessionDescription.Type.OFFER, localSdp.toString())
            }

            WebRtcLogger.d(TAG, "(Send) calling pc->SetLocalDescription():\n%s", offer.description)

            peerConnection.setLocalDescription(offer)

            // We can now get the transceiver.mid.
            localId = transceiver.mid

            // Set MID
            sendingRtpParameters.mid = localId
        } catch (e: Exception) {
            // Panic here. Try to undo things.
            transceiver.direction = RtpTransceiver.RtpTransceiverDirection.INACTIVE
            transceiver.sender.setTrack(null, false)

            throw e
        }

        val localSdp = SdpSessionDescription.parse(offer.description)

        val offerMedia = localSdp.getMediaDescriptionAt(mediaSectionIdx)

        // Set RTCP CNAME.
        sendingRtpParameters.rtcp = RtcpParameters(cname = offerMedia.getCname().orEmpty())

        when {
            // Set RTP encodings by parsing the SDP offer if no encodings are given.
            encodings.isEmpty() -> sendingRtpParameters.encodings = offerMedia.getRtpEncodings()

            // Set RTP encodings by parsing the SDP offer and complete them with given
            // one if just a single encoding has been given.
            encodings.size == 1 -> {
                val newEncodings = offerMedia.getRtpEncodings()
                sendingRtpParameters.encodings = encodings.subList(0, 1) + newEncodings.subList(1, newEncodings.size)
            }

            // Otherwise if more than 1 encoding are given use them verbatim.
            else -> sendingRtpParameters.encodings = encodings
        }

        // If VP8 and there is effective simulcast, add scalabilityMode to each encoding.
        val mimeType = sendingRtpParameters.codecs[0].mimeType.toLowerCase(Locale.ENGLISH)
        if (sendingRtpParameters.encodings.size > 1 && (mimeType == "video/vp8" || mimeType == "video/h264")) {
            sendingRtpParameters.encodings.forEach { it.scalabilityMode = "S1T3" }
        }

        remoteSdp.send(
            offerMediaDescription = offerMedia,
            reuseMid = reuseMid,
            offerRtpParameters = sendingRtpParameters,
            answerRtpParameters = checkNotNull(sendingRemoteRtpParametersByKind[kind]),
            codecOptions = codecOptions)
        val answer = SessionDescription(SessionDescription.Type.ANSWER, remoteSdp.sdp)

        WebRtcLogger.d(TAG, "(Send) calling pc->SetRemoteDescription():\n%s", answer.description)
        peerConnection.setRemoteDescription(answer)

        // Store in the map
        mapMidTransceiver[localId] = transceiver

        return Triple(localId, sendingRtpParameters, transceiver.sender)
    }

    suspend fun stopSending(localId: String) {
        WebRtcLogger.d(TAG, "stopSending [localId:%s]", localId)

        val transceiver = mapMidTransceiver.getOrElse(localId) {
            throw MediasoupException("associated RtpTransceiver not found")
        }
        transceiver.sender.also {
            it.setTrack(null, false)
            peerConnection.removeTrack(it)
        }
        remoteSdp.closeMediaSection(transceiver.mid)

        // May throw.
        val offer = peerConnection.createOffer(MediaConstraints())
        WebRtcLogger.d(TAG, "(Send) calling pc->SetLocalDescription():\n%s", offer.description)

        // May throw.
        peerConnection.setLocalDescription(offer)

        val answer = SessionDescription(SessionDescription.Type.ANSWER, remoteSdp.sdp)

        WebRtcLogger.d(TAG, "(Send) calling pc->SetRemoteDescription():\n%s", answer.description)

        // May throw.
        peerConnection.setRemoteDescription(answer)
    }

    fun replaceTrack(localId: String, track: MediaStreamTrack?) {
        WebRtcLogger.d(TAG, "[localId:%s, track->id():%s", localId, track?.id())

        val transceiver = mapMidTransceiver.getOrElse(localId) {
            throw MediasoupException("associated RtpTransceiver not found")
        }

        transceiver.sender.setTrack(track, true)
    }

    fun setMaxSpatialLayer(localId: String, spatialLayer: Int) {
        WebRtcLogger.d(TAG, "[localId:%s, spatialLayer:%d]", localId, spatialLayer)

        val transceiver = mapMidTransceiver.getOrElse(localId) {
            throw MediasoupException("associated RtpTransceiver not found")
        }

        val parameters = transceiver.sender.parameters
        val encodings = parameters.encodings
        val lowEncoding = if (encodings.isNotEmpty()) encodings[0] else null
        val mediumEncoding = if (encodings.size > 1) encodings[1] else null
        val highEncoding = if (encodings.size > 2) encodings[2] else null

        // Edit encodings.
        when (spatialLayer) {
            1 -> {
                lowEncoding?.active = true
                mediumEncoding?.active = false
                highEncoding?.active = false
            }
            2 -> {
                lowEncoding?.active = true
                mediumEncoding?.active = true
                highEncoding?.active = false
            }
            3 -> {
                lowEncoding?.active = true
                mediumEncoding?.active = true
                highEncoding?.active = true
            }
            else -> Unit
        }

        if (!transceiver.sender.setParameters(parameters)) {
            throw MediasoupException("failed sender.setParameter")
        }
    }

    suspend fun getSenderStats(localId: String): RTCStatsReport {
        WebRtcLogger.d(TAG, "[localId:%s]", localId)

        return suspendCoroutine { continuation ->
            peerConnection.getStats { continuation.resume(it) }
        }
    }

    override suspend fun restartIce(iceParameters: IceParameters) {
        // Provide the remote SDP handler with new remote ICE parameters.
        remoteSdp.updateIceParameters(iceParameters)

        if (!transportReady) {
            return
        }

        // TODO implementation
    }
}

internal class RecvHandler(
    listener: PrivateListener,
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    rtcConfig: PeerConnection.RTCConfiguration,
    peerConnectionFactory: PeerConnectionFactory
) : Handler(listener, iceParameters, iceCandidates, dtlsParameters, sctpParameters, rtcConfig, peerConnectionFactory) {
    companion object {
        private val TAG = RecvHandler::class.simpleName
    }

    suspend fun receive(id: String, kind: MediaKind, rtpParameters: RtpParameters): Pair<String, RtpReceiver> {
        WebRtcLogger.d(TAG, "receive [id:%s, kind:%s]", id, kind)

        // mid is optional, check whether it exists and is a non empty string.
        var localId = rtpParameters.mid
        if (localId.isNullOrEmpty()) {
            localId = mapMidTransceiver.size.toString()
        }
        val cname = rtpParameters.rtcp.cname.orEmpty()

        remoteSdp.receive(localId, kind, rtpParameters, cname, id)

        val offer = SessionDescription(SessionDescription.Type.OFFER, remoteSdp.sdp)

        WebRtcLogger.d(TAG, "(Recv) calling pc->setRemoteDescription():\n%s", offer.description)
        peerConnection.setRemoteDescription(offer)

        var answer = peerConnection.createAnswer(sdpConstraints)
        val localSdp = SdpSessionDescription.parse(answer.description)
        val answerMedia = checkNotNull(localSdp.getMediaDescription(localId))

        rtpParameters.applyCodecParameters(answerMedia)

        answer = SessionDescription(SessionDescription.Type.ANSWER, localSdp.toString())

        if (!transportReady) {
            setupTransport(DtlsRole.CLIENT, localSdp)
        }

        WebRtcLogger.d(TAG, "(Recv) calling pc->SetLocalDescription():\n%s", answer.description)
        peerConnection.setLocalDescription(answer)

        // TODO optimize
        while (true) {
            mapMidTransceiver[localId]?.also {
                return localId to it.receiver
            }
            Thread.sleep(100L)
        }
    }

    override fun onTrack(transceiver: RtpTransceiver) {
        mapMidTransceiver[transceiver.mid] = transceiver
    }

    suspend fun stopReceiving(localId: String) {
        WebRtcLogger.d(TAG, "stopReceiving [localId:%s]", localId)

        val transceiver = mapMidTransceiver.getOrElse(localId) {
            throw MediasoupException("associated RtpTransceiver not found")
        }
        val mid = transceiver.mid
        WebRtcLogger.d(TAG, "disabling mid:%s", mid)

        remoteSdp.closeMediaSection(mid)

        val offer = SessionDescription(SessionDescription.Type.OFFER, remoteSdp.sdp)

        WebRtcLogger.d(TAG, "(Recv) calling pc->setRemoteDescription():\n%s", offer.description)

        // May throw.
        peerConnection.setRemoteDescription(offer)

        // May throw.
        val answer = peerConnection.createAnswer(sdpConstraints)

        WebRtcLogger.d(TAG, "(Recv) calling pc->SetLocalDescription():\n%s", answer.description)

        // May throw.
        peerConnection.setLocalDescription(answer)
    }

    suspend fun getReceiverStats(localId: String): RTCStatsReport {
        WebRtcLogger.d(TAG, "getReceiverStats [localId:%s]", localId)

        return suspendCoroutine { continuation ->
            peerConnection.getStats { continuation.resume(it) }
        }
    }

    override suspend fun restartIce(iceParameters: IceParameters) {
        // Provide the remote SDP handler with new remote ICE parameters.
        remoteSdp.updateIceParameters(iceParameters)

        val offer = SessionDescription(SessionDescription.Type.OFFER, remoteSdp.sdp)

        WebRtcLogger.d(TAG, "(Recv) calling pc->SetRemoteDescription():\n%s", offer.description)

        // May throw.
        peerConnection.setRemoteDescription(offer)

        // May throw.
        val answer = peerConnection.createAnswer(sdpConstraints)

        WebRtcLogger.d(TAG, "(Recv) calling pc->SetLocalDescription():\n%s", answer.description)

        // May throw.
        peerConnection.setLocalDescription(answer)
    }
}

