package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.PeerConnection

abstract class Transport {
    interface Listener {
        @CalledByNative("Listener")
        fun onConnect(transport: Transport, dtlsParameters: String)

        @CalledByNative("Listener")
        fun onConnectionStateChange(transport: Transport, newState: String)
    }

    protected abstract var nativeTransport: Long

    val id: String by lazy {
        checkTransportExists()
        nativeGetId(nativeTransport)
    }

    val connectionState: PeerConnection.IceConnectionState
        get() {
            checkTransportExists()
            val state = nativeGetConnectionState(nativeTransport)
            return PeerConnection.IceConnectionState.valueOf(state)
        }

    val appData: String by lazy {
        checkTransportExists()
        nativeGetAppData(nativeTransport)
    }

    val stats: String
        get() {
            checkTransportExists()
            return nativeGetStats(nativeTransport)
        }

    val closed: Boolean
        get() {
            checkTransportExists()
            return nativeIsClosed(nativeTransport)
        }

    fun restartIce(iceParameters: String) {
        checkTransportExists()
        nativeRestartIce(nativeTransport, iceParameters)
    }

    fun updateIceServers(iceServers: List<String>) {
        checkTransportExists()
        nativeUpdateIceServers(nativeTransport, iceServers.joinToString(","))
    }

    fun close() {
        checkTransportExists()
        nativeClose(nativeTransport)
    }

    fun dispose() {
        val transport = nativeTransport
        if (transport == 0L) {
            return
        }
        nativeTransport = 0L
        nativeDispose(transport)
    }

    protected abstract fun checkTransportExists()

    private external fun nativeGetId(transport: Long): String
    private external fun nativeIsClosed(transport: Long): Boolean
    private external fun nativeGetConnectionState(transport: Long): String
    private external fun nativeGetAppData(transport: Long): String
    private external fun nativeClose(transport: Long)
    private external fun nativeGetStats(transport: Long): String
    private external fun nativeRestartIce(transport: Long, iceParameters: String)
    private external fun nativeUpdateIceServers(transport: Long, iceServers: String)
    private external fun nativeDispose(transport: Long)
}