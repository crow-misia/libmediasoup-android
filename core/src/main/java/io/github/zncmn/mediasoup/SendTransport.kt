package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpParameters

class SendTransport @CalledByNative private constructor(
    override var nativeTransport: Long
) : Transport() {
    interface Listener : Transport.Listener {
        /**
         * @return producer Id
         */
        @CalledByNative("Listener")
        fun onProduce(
            transport: Transport,
            kind: String,
            rtpParameters: String,
            appData: String?
        ): String

        /**
         * @return producer Id
         */
        @CalledByNative("Listener")
        fun onProduceData(
            transport: Transport,
            sctpStreamParameters: String,
            label: String,
            protocol: String,
            appData: String?
        ): String
    }

    /**
     * Create a Producer.
     */
    @JvmOverloads
    fun produce(
        listener: Producer.Listener,
        track: MediaStreamTrack,
        encodings: Array<RtpParameters.Encoding>,
        codecOptions: String? = null,
        appData: String? = null
    ): Producer {
        checkTransportExists()
        val nativeTrack: Long = RTCUtils.getNativeMediaStreamTrack(track)
        return nativeProduce(
            transport = nativeTransport,
            listener = listener,
            track = nativeTrack,
            encodings = encodings,
            codecOptions = codecOptions,
            appData = appData
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
        appData: String? = null
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
            appData = appData
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
        appData: String?
    ): Producer

    private external fun nativeProduceData(
        transport: Long,
        listener: DataProducer.Listener,
        label: String,
        protocol: String,
        ordered: Boolean,
        maxRetransmits: Int,
        maxPacketLifeTime: Int,
        appData: String?
    ): DataProducer
}