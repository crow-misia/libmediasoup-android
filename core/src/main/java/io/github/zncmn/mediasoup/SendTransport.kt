package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import kotlinx.coroutines.runBlocking
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStatsReport
import java.util.concurrent.ConcurrentHashMap

class SendTransport internal constructor(
    override val listener: Listener,
    id: String,
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    rtcConfig: PeerConnection.RTCConfiguration,
    peerConnectionFactory: PeerConnectionFactory,
    extendedRtpCapabilities: ExtendedRtpCapabilities,
    private val canProduceByKind: Map<MediaKind, Boolean>,
    appData: Any?
) : Transport(listener, id, extendedRtpCapabilities, appData), Producer.PrivateListener {
    val maxSctpMessageSize = sctpParameters?.maxMessageSize

    private val sendingRtpParametersByKind: Map<MediaKind, RtpParameters> = mapOf(
        "audio" to extendedRtpCapabilities.getSendingRtpParameters("audio"),
        "video" to extendedRtpCapabilities.getSendingRtpParameters("video")
    )
    private val sendingRemoteRtpParametersByKind: Map<MediaKind, RtpParameters> = mapOf(
        "audio" to extendedRtpCapabilities.getSendingRemoteRtpParameters("audio"),
        "video" to extendedRtpCapabilities.getSendingRemoteRtpParameters("video")
    )

    override val handler = SendHandler(
        listener = this,
        iceParameters = iceParameters,
        iceCandidates = iceCandidates,
        dtlsParameters = dtlsParameters,
        sctpParameters = sctpParameters,
        rtcConfig = rtcConfig,
        peerConnectionFactory = peerConnectionFactory,
        sendingRtpParametersByKind = sendingRtpParametersByKind,
        sendingRemoteRtpParametersByKind = sendingRemoteRtpParametersByKind
    )

    private val producers = ConcurrentHashMap<String, Producer>()

    interface Listener : Transport.Listener {
        /**
         * @return producer Id
         */
        fun onProduce(transport: Transport, kind: MediaKind, rtpParameters: RtpParameters, appData: Any?): String
    }

    /**
     * Create a Producer.
     */
    @JvmOverloads
    @Throws(MediasoupException::class)
    suspend fun produce(
        listener: Producer.Listener,
        track: MediaStreamTrack,
        encodings: List<RtpEncodingParameters>,
        codecOptions: ProducerCodecOptions?,
        appData: Any? = null
    ): Producer {
        check(!closed) { "SendTransport closed" }
        check(track.state() != MediaStreamTrack.State.ENDED) { "track ended" }
        checkNotNull(canProduceByKind[track.kind()]) { "cannot produce track kind" }

        codecOptions?.also { it.validate() }

        val producerId: String

        // May throw.
        val (localId, rtpParameters, rtpSender) = handler.send(track, encodings, codecOptions)

        try {
            // This will fill rtpParameters's missing fields with default values.
            rtpParameters.validate()

            // May throw.
            producerId = this.listener.onProduce(this, track.kind(), rtpParameters, appData)
        } catch (e: MediasoupException) {
            handler.stopSending(localId)

            throw e
        }

        val producer = Producer(
            privateListener = this,
            listener = listener,
            id = producerId,
            localId = localId,
            rtpSender = rtpSender,
            track = track,
            rtpParameters = rtpParameters,
            appData = appData
        )
        producers[producerId] = producer

        return producer
    }

    override fun close() {
        if (closed) {
            return
        }

        super.close()

        // Close all Producers.
        producers.values.forEach { it.transportClosed() }
    }

    override fun onClose(producer: Producer) {
        producers.remove(producer.id)
        if (closed) {
            return
        }

        // May throw.
        suspend {
            handler.stopSending(producer.localId)
        }
    }

    override fun onReplaceTrack(producer: Producer, track: MediaStreamTrack) {
        handler.replaceTrack(producer.localId, track)
    }

    override fun onSetMaxSpatialLayer(producer: Producer, maxSpatialLayer: Int) {
        handler.setMaxSpatialLayer(producer.localId, maxSpatialLayer)
    }

    override fun onGetStats(producer: Producer): RTCStatsReport {
        check(!closed) { "SendTransport closed" }

        return runBlocking {
            handler.getSenderStats(producer.localId)
        }
    }
}