package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.DataChannel

/**
 * DataProducer.
 */
class DataProducer @CalledByNative private constructor(
    private var nativeDataProducer: Long,
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onOpen(dataProducer: DataProducer)

        @CalledByNative("Listener")
        fun onClose(dataProducer: DataProducer)

        @CalledByNative("Listener")
        fun onBufferedAmountChange(dataProducer: DataProducer, sentDataSize: Long)

        @CalledByNative("Listener")
        fun onTransportClose(dataProducer: DataProducer)
    }

    /**
     * DataProducer ID.
     */
    val id: String by lazy {
        checkDataProducerExists()
        nativeGetId(nativeDataProducer)
    }

    /**
     * Local ID.
     */
    val localId: String by lazy {
        checkDataProducerExists()
        nativeGetLocalId(nativeDataProducer)
    }

    /**
     * Whether the DataProducer is closed.
     */
    val closed: Boolean
        get() {
            checkDataProducerExists()
            return nativeIsClosed(nativeDataProducer)
        }

    /**
     * SCTP stream parameters.
     */
    val sctpStreamParameters: String? by lazy {
        checkDataProducerExists()
        nativeGetSctpStreamParameters(nativeDataProducer)
    }

    /**
     * DataChannel readyState.
     */
    val readyState: DataChannel.State
        get() {
            checkDataProducerExists()
            val state = nativeGetReadyState(nativeDataProducer)
            return DataChannel.State.values()[state]
        }

    /**
     * DataChannel label.
     */
    val label: String by lazy {
        checkDataProducerExists()
        nativeGetLabel(nativeDataProducer)
    }

    /**
     * DataChannel protocol.
     */
    val protocol: String by lazy {
        checkDataProducerExists()
        nativeGetProtocol(nativeDataProducer)
    }

    /**
     * App custom data.
     */
    val appData: String by lazy {
        checkDataProducerExists()
        nativeGetAppData(nativeDataProducer)
    }

    /**
     * Closes the DataConsumer.
     */
    fun close() {
        checkDataProducerExists()
        nativeClose(nativeDataProducer)
    }

    /**
     * Send data.
     */
    fun send(buffer: DataChannel.Buffer) {
        checkDataProducerExists()
        val data = ByteArray(buffer.data.remaining())
        buffer.data.get(data)
        return nativeSend(nativeDataProducer, data, buffer.binary)
    }

    /**
     * Dispose the Consumer.
     */
    fun dispose() {
        val ptr = nativeDataProducer
        if (ptr == 0L) {
            return
        }
        nativeDataProducer = 0L
        nativeDispose(ptr)
    }

    private fun checkDataProducerExists() {
        check(nativeDataProducer != 0L) { "DataProducer has been disposed." }
    }

    private external fun nativeGetId(nativeDataProducer: Long): String
    private external fun nativeGetLocalId(nativeDataProducer: Long): String
    private external fun nativeGetSctpStreamParameters(nativeDataProducer: Long): String
    private external fun nativeGetReadyState(nativeDataProducer: Long): Int
    private external fun nativeGetLabel(nativeDataProducer: Long): String
    private external fun nativeGetProtocol(nativeDataProducer: Long): String
    private external fun nativeGetBufferedAmount(nativeDataProducer: Long): Long
    private external fun nativeGetAppData(nativeDataProducer: Long): String
    private external fun nativeIsClosed(nativeDataProducer: Long): Boolean
    private external fun nativeClose(nativeDataProducer: Long)
    private external fun nativeSend(nativeDataProducer: Long, buffer: ByteArray, binary: Boolean)
    private external fun nativeDispose(nativeDataProducer: Long)
}
