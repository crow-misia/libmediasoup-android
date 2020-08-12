package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.DataChannel

class DataProducer @CalledByNative private constructor(
    private var nativeDataProducer: Long
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

    val id: String by lazy {
        checkConsumerExists()
        nativeGetId(nativeDataProducer)
    }

    val localId: String by lazy {
        checkConsumerExists()
        nativeGetLocalId(nativeDataProducer)
    }

    val closed: Boolean
        get() {
            checkConsumerExists()
            return nativeIsClosed(nativeDataProducer)
        }

    val sctpStreamParameters: String? by lazy {
        checkConsumerExists()
        nativeGetSctpStreamParameters(nativeDataProducer)
    }

    val readyState: DataChannel.State? by lazy {
        checkConsumerExists()
        val state = nativeGetReadyState(nativeDataProducer)
        DataChannel.State.values()[state]
    }

    val label: String by lazy {
        checkConsumerExists()
        nativeGetLabel(nativeDataProducer)
    }

    val protocol: String by lazy {
        checkConsumerExists()
        nativeGetProtocol(nativeDataProducer)
    }

    val appData: String by lazy {
        checkConsumerExists()
        nativeGetAppData(nativeDataProducer)
    }

    fun close() {
        checkConsumerExists()
        nativeClose(nativeDataProducer)
    }

    fun send(buffer: DataChannel.Buffer) {
        checkConsumerExists()
        val data = ByteArray(buffer.data.remaining())
        buffer.data.get(data)
        return nativeSend(nativeDataProducer, data, buffer.binary)
    }

    fun dispose() {
        val ptr = nativeDataProducer
        if (ptr == 0L) {
            return
        }
        nativeDataProducer = 0L
        nativeDispose(ptr)
    }

    private fun checkConsumerExists() {
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