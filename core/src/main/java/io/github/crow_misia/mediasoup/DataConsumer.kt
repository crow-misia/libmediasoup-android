package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.DataChannel

/**
 * DataConsumer.
 */
class DataConsumer @CalledByNative private constructor(
    private var nativeDataConsumer: Long,
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

    /**
     * DataConsumer ID.
     */
    val id: String by lazy {
        checkDataConsumerExists()
        nativeGetId(nativeDataConsumer)
    }

    /**
     * Local ID.
     */
    val localId: String by lazy {
        checkDataConsumerExists()
        nativeGetLocalId(nativeDataConsumer)
    }

    /**
     * Associated DataProducer ID.
     */
    val dataProducerId: String by lazy {
        checkDataConsumerExists()
        nativeGetDataProducerId(nativeDataConsumer)
    }

    /**
     * Whether the DataConsumer is closed.
     */
    val closed: Boolean
        get() {
            checkDataConsumerExists()
            return nativeIsClosed(nativeDataConsumer)
        }

    /**
     * SCTP stream parameters.
     */
    val sctpStreamParameters: String by lazy {
        checkDataConsumerExists()
        nativeGetSctpStreamParameters(nativeDataConsumer)
    }

    /**
     * DataChannel readyState.
     */
    val readyState: DataChannel.State
        get() {
            checkDataConsumerExists()
            val state = nativeGetReadyState(nativeDataConsumer)
            return DataChannel.State.values()[state]
        }

    /**
     * DataChannel label.
     */
    val label: String by lazy {
        checkDataConsumerExists()
        nativeGetLabel(nativeDataConsumer)
    }

    /**
     * DataChannel protocol.
     */
    val protocol: String by lazy {
        checkDataConsumerExists()
        nativeGetProtocol(nativeDataConsumer)
    }

    /**
     * App custom data.
     */
    val appData: String by lazy {
        checkDataConsumerExists()
        nativeGetAppData(nativeDataConsumer)
    }

    /**
     * Closes the DataConsumer.
     */
    fun close() {
        checkDataConsumerExists()
        nativeClose(nativeDataConsumer)
    }

    /**
     * Dispose the Consumer.
     */
    fun dispose() {
        val ptr = nativeDataConsumer
        if (ptr == 0L) {
            return
        }
        nativeDataConsumer = 0L
        nativeDispose(ptr)
    }

    private fun checkDataConsumerExists() {
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
