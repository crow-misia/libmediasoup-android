package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpParameters

/**
 * SendTransport.
 */
class SendTransport @CalledByNative private constructor(
    override var nativeTransport: Long,
) : Transport() {
    /**
     * SendTransport Listener.
     */
    interface Listener : Transport.Listener {
        /**
         * @return Producer ID
         */
        @CalledByNative("Listener")
        fun onProduce(
            transport: Transport,
            kind: String,
            rtpParameters: String,
            appData: String?,
        ): String

        /**
         * @return Producer ID
         */
        @CalledByNative("Listener")
        fun onProduceData(
            transport: Transport,
            sctpStreamParameters: String,
            label: String,
            protocol: String,
            appData: String?,
        ): String
    }

    /**
     * Create a Producer.
     */
    @JvmOverloads
    fun produce(
        listener: Producer.Listener,
        track: MediaStreamTrack,
        encodings: List<RtpParameters.Encoding> = emptyList(),
        codecOptions: String? = null,
        codec: String? = null,
        appData: String? = null,
    ): Producer {
        checkTransportExists()
        val nativeTrack: Long = RTCUtils.getNativeMediaStreamTrack(track)
        return nativeProduce(
            transport = nativeTransport,
            listener = listener,
            track = nativeTrack,
            encodings = encodings.toTypedArray(),
            codecOptions = codecOptions,
            codec = codec,
            appData = appData,
        )
    }

    /**
     * Create a DataProducer.
     */
    @JvmOverloads
    fun produceData(
        listener: DataProducer.Listener,
        label: String = "",
        protocol: String = "",
        ordered: Boolean = true,
        maxRetransmits: Int = 0,
        maxPacketLifeTime: Int = 0,
        appData: String? = null,
    ): DataProducer {
        checkTransportExists()
        return nativeProduceData(
            transport = nativeTransport,
            listener = listener,
            label = label,
            protocol = protocol,
            ordered = ordered,
            maxRetransmits = maxRetransmits,
            maxPacketLifeTime = maxPacketLifeTime,
            appData = appData,
        )
    }

    override fun checkTransportExists() {
        check(nativeTransport != 0L) { "SendTransport has been disposed." }
    }

    private external fun nativeProduce(
        transport: Long,
        listener: Producer.Listener,
        track: Long,
        encodings: Array<RtpParameters.Encoding>,
        codecOptions: String?,
        codec: String?,
        appData: String?,
    ): Producer

    private external fun nativeProduceData(
        transport: Long,
        listener: DataProducer.Listener,
        label: String,
        protocol: String,
        ordered: Boolean,
        maxRetransmits: Int,
        maxPacketLifeTime: Int,
        appData: String?,
    ): DataProducer
}
