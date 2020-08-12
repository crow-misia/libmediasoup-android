package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative

class RecvTransport @CalledByNative private constructor(
    override var nativeTransport: Long
) : Transport() {
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
        appData: String? = null
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
        label: String,
        protocol: String = "",
        appData: String? = null
    ): DataConsumer {
        checkTransportExists()
        return nativeConsumeData(
            nativeTransport = nativeTransport,
            listener = listener,
            id = id,
            producerId = producerId,
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
        appData: String?
    ): Consumer

    private external fun nativeConsumeData(
        nativeTransport: Long,
        listener: DataConsumer.Listener,
        id: String,
        producerId: String,
        label: String,
        protocol: String,
        appData: String?
    ): DataConsumer
}
