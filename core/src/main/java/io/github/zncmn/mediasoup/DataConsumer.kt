package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.DataChannel

class DataConsumer @CalledByNative private constructor(
    private var nativeDataConsumer: Long
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onConnecting(dataConsumer: DataConsumer)

        @CalledByNative("Listener")
        fun onOpen(dataConsumer: DataConsumer)

        @CalledByNative("Listener")
        fun onClosing(dataConsumer: DataConsumer)

        @CalledByNative("Listener")
        fun onClose(dataConsumer: DataConsumer)

        @CalledByNative("Listener")
        fun onMessage(dataConsumer: DataConsumer, buffer: DataChannel.Buffer)

        @CalledByNative("Listener")
        fun onTransportClose(dataConsumer: DataConsumer)
    }

    val id: String by lazy {
        checkConsumerExists()
        nativeGetId(nativeDataConsumer)
    }

    val localId: String by lazy {
        checkConsumerExists()
        nativeGetLocalId(nativeDataConsumer)
    }

    val dataProducerId: String by lazy {
        checkConsumerExists()
        nativeGetDataProducerId(nativeDataConsumer)
    }

    val closed: Boolean
        get() {
            checkConsumerExists()
            return nativeIsClosed(nativeDataConsumer)
        }

    val sctpStreamParameters: String by lazy {
        checkConsumerExists()
        nativeGetSctpStreamParameters(nativeDataConsumer)
    }

    val readyState: DataChannel.State? by lazy {
        checkConsumerExists()
        val state = nativeGetReadyState(nativeDataConsumer)
        DataChannel.State.values()[state]
    }

    val label: String by lazy {
        checkConsumerExists()
        nativeGetLabel(nativeDataConsumer)
    }

    val protocol: String by lazy {
        checkConsumerExists()
        nativeGetProtocol(nativeDataConsumer)
    }

    val appData: String by lazy {
        checkConsumerExists()
        nativeGetAppData(nativeDataConsumer)
    }

    fun close() {
        checkConsumerExists()
        nativeClose(nativeDataConsumer)
    }

    fun dispose() {
        val ptr = nativeDataConsumer
        if (ptr == 0L) {
            return
        }
        nativeDataConsumer = 0L
        nativeDispose(ptr)
    }

    private fun checkConsumerExists() {
        check(nativeDataConsumer != 0L) { "DataConsumer has been disposed." }
    }

    private external fun nativeGetId(nativeDataConsumer: Long): String
    private external fun nativeGetLocalId(nativeDataConsumer: Long): String
    private external fun nativeGetDataProducerId(nativeDataConsumer: Long): String
    private external fun nativeGetSctpStreamParameters(nativeDataConsumer: Long): String
    private external fun nativeGetReadyState(nativeDataConsumer: Long): Int
    private external fun nativeGetLabel(nativeDataConsumer: Long): String
    private external fun nativeGetProtocol(nativeDataConsumer: Long): String
    private external fun nativeGetAppData(nativeDataConsumer: Long): String
    private external fun nativeIsClosed(nativeDataConsumer: Long): Boolean
    private external fun nativeClose(nativeDataConsumer: Long)
    private external fun nativeDispose(nativeDataConsumer: Long)
}