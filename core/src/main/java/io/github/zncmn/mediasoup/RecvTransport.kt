package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*
import io.github.zncmn.webrtc.log.WebRtcLogger
import kotlinx.coroutines.runBlocking
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStatsReport
import java.util.concurrent.ConcurrentHashMap

class RecvTransport internal constructor(
    listener: Listener,
    id: String,
    iceParameters: IceParameters,
    iceCandidates: List<IceCandidate>,
    dtlsParameters: DtlsParameters,
    sctpParameters: SctpParameters?,
    rtcConfig: PeerConnection.RTCConfiguration,
    peerConnectionFactory: PeerConnectionFactory,
    extendedRtpCapabilities: ExtendedRtpCapabilities,
    appData: Map<String, Any>?
) : Transport(listener, id, extendedRtpCapabilities, appData), Consumer.PrivateListener {
    companion object {
        private val TAG = RecvTransport::class.simpleName
    }

    override val handler = RecvHandler(
        listener = this,
        iceParameters = iceParameters,
        iceCandidates = iceCandidates,
        dtlsParameters = dtlsParameters,
        sctpParameters = sctpParameters,
        rtcConfig = rtcConfig,
        peerConnectionFactory = peerConnectionFactory
    )

    private val consumers = ConcurrentHashMap<String, Consumer>()
    private var probatorConsumerCreated = false

    interface Listener : Transport.Listener

    /**
     * Create a Consumer.
     */
    @JvmOverloads
    @Throws(MediasoupException::class)
    suspend fun consume(
        consumerListener: Consumer.Listener,
        id: String,
        producerId: String,
        kind: MediaKind,
        rtpParameters: RtpParameters,
        appData: Any? = null
    ): Consumer {
        check(!closed) { "RecvTransport closed" }
        check(id.isNotEmpty()) { "missing id" }
        check(producerId.isNotEmpty()) { "missing producerId" }
        check(kind == "audio" || kind == "video") { "invalid kind" }
        check(extendedRtpCapabilities.canReceive(rtpParameters)) { "cannot consume this Producer" }

        // May throw.
        val (localId, rtpReceiver) = handler.receive(id, kind, rtpParameters)
        val track = rtpReceiver.track() ?: run {
            throw MediasoupException("failed create mediaStreamTrack")
        }

        val consumer = Consumer(
            this,
            consumerListener,
            id,
            localId,
            producerId,
            rtpReceiver,
            track,
            rtpParameters,
            appData
        )
        consumers[id] = consumer

        // If this is the first video Consumer and the Consumer for RTP probation
        // has not yet been created, create it now.
        if (!probatorConsumerCreated && kind == "video") {
            try {
                val probatorRtpParameters = rtpParameters.generateProbatorRtpParameters()

                // May throw.
                handler.receive("probator", kind, probatorRtpParameters)

                WebRtcLogger.d(TAG, "Consumer for RTP probation created")

                probatorConsumerCreated = true
            } catch (e: Exception) {
                WebRtcLogger.e(TAG, e, "failed to create Consumer for RTP probation:")
            }
        }

        return consumer
    }

    override fun close() {
        if (closed) {
            return
        }

        super.close()

        // Close all Cosumers.
        consumers.values.forEach { it.transportClosed() }
    }

    override fun onClose(consumer: Consumer) {
        consumers.remove(consumer.id)
        if (closed) {
            return
        }

        suspend {
            // May throw.
            handler.stopReceiving(consumer.localId)
        }
    }

    override fun onGetStats(consumer: Consumer): RTCStatsReport {
        check(!closed) { "RecvTransport closed" }

        return runBlocking {
            handler.getReceiverStats(consumer.localId)
        }
    }
}
