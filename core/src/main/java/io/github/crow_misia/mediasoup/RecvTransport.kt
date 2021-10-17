package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative

/**
 * RecvTransport.
 */
class RecvTransport @CalledByNative private constructor(
    override var nativeTransport: Long,
) : Transport() {
    /**
     * RecvTransport Listener.
     */
    interface Listener : Transport.Listener

    /**
     * Create a Consumer.
     */
    @JvmOverloads
    fun consume(
        listener: Consumer.Listener,
        id: String,
        producerId: String,
        kind: String,
        rtpParameters: String? = null,
        appData: String? = null,
    ): Consumer {
        checkTransportExists()
        return nativeConsume(
            nativeTransport = nativeTransport,
            listener = listener,
            id = id,
            producerId = producerId,
            kind = kind,
            rtpParameters = rtpParameters,
            appData = appData
        )
    }

    /**
     * Create a DataConsumer.
     */
    @JvmOverloads
    fun consumeData(
        listener: DataConsumer.Listener,
        id: String,
        producerId: String,
        streamId: Int,
        label: String,
        protocol: String = "",
        appData: String? = null,
    ): DataConsumer {
        checkTransportExists()
        return nativeConsumeData(
            nativeTransport = nativeTransport,
            listener = listener,
            id = id,
            producerId = producerId,
            streamId = streamId,
            label = label,
            protocol = protocol,
            appData = appData
        )
    }

    override fun checkTransportExists() {
        check(nativeTransport != 0L) { "RecvTransport has been disposed." }
    }

    private external fun nativeConsume(
        nativeTransport: Long,
        listener: Consumer.Listener,
        id: String,
        producerId: String,
        kind: String,
        rtpParameters: String?,
        appData: String?,
    ): Consumer

    private external fun nativeConsumeData(
        nativeTransport: Long,
        listener: DataConsumer.Listener,
        id: String,
        producerId: String,
        streamId: Int,
        label: String,
        protocol: String,
        appData: String?,
    ): DataConsumer
}
