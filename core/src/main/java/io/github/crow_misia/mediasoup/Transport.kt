package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.PeerConnection

/**
 * Transport.
 */
abstract class Transport {
    /**
     * Transport Listener.
     */
    interface Listener {
        @CalledByNative("Listener")
        fun onConnect(transport: Transport, dtlsParameters: String)

        @CalledByNative("Listener")
        fun onConnectionStateChange(transport: Transport, newState: String)
    }

    protected abstract var nativeTransport: Long

    /**
     * ID.
     */
    val id: String by lazy {
        checkTransportExists()
        nativeGetId(nativeTransport)
    }

    /**
     * Transport (IceConnection) connection state.
     */
    val connectionState: PeerConnection.IceConnectionState
        get() {
            checkTransportExists()
            val state = nativeGetConnectionState(nativeTransport)
            return PeerConnection.IceConnectionState.valueOf(state)
        }

    /**
     * App custom data.
     */
    val appData: String by lazy {
        checkTransportExists()
        nativeGetAppData(nativeTransport)
    }

    /**
     * Transport stats.
     */
    val stats: String
        get() {
            checkTransportExists()
            return nativeGetStats(nativeTransport)
        }

    /**
     * Whether the Transport is closed.
     */
    val closed: Boolean
        get() {
            checkTransportExists()
            return nativeIsClosed(nativeTransport)
        }

    /**
     * Restart ICE.
     */
    fun restartIce(iceParameters: String) {
        checkTransportExists()
        nativeRestartIce(nativeTransport, iceParameters)
    }

    /**
     * Update ICE Servers.
     */
    fun updateIceServers(iceServers: List<String>) {
        checkTransportExists()
        nativeUpdateIceServers(nativeTransport, iceServers.joinToString(","))
    }

    /**
     * Closes the Transport.
     */
    fun close() {
        checkTransportExists()
        nativeClose(nativeTransport)
    }

    /**
     * Dispose the Transport.
     */
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