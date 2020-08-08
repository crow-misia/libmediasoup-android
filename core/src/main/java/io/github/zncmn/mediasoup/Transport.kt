package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.DtlsParameters
import io.github.zncmn.mediasoup.model.ExtendedRtpCapabilities
import io.github.zncmn.mediasoup.model.IceParameters
import org.webrtc.PeerConnection
import org.webrtc.RTCStatsReport

abstract class Transport(
    protected open val listener: Listener,
    val id: String,
    protected val extendedRtpCapabilities: ExtendedRtpCapabilities,
    val appData: Any? = null
) : Handler.PrivateListener {
    internal abstract val handler: Handler

    var connectionState: PeerConnection.IceConnectionState = PeerConnection.IceConnectionState.NEW
        private set

    var closed: Boolean = false
        private set

    val stats: RTCStatsReport
        get() {
            check(!closed) { "Transport closed" }
            return handler.transportStats
        }

    open fun close() {
        if (closed) {
            return
        }
        closed = true
        handler.close()
    }

    fun dispose() {
        close()
        handler.dispose()
    }

    suspend fun restartIce(iceParameters: IceParameters) {
        check(!closed) { "Transport closed" }
        handler.restartIce(iceParameters)
    }

    fun updateIceServers(iceServers: List<String>) {
        check(!closed) { "Transport closed" }
        handler.updateIceServers(iceServers)
    }

    override fun onConnect(dtlsParameters: DtlsParameters) {
        check(!closed) { "Transport closed" }
        listener.onConnect(this, dtlsParameters)
    }

    override fun onConnectionStateChange(newState: PeerConnection.IceConnectionState) {
        // Update connection state.
        this.connectionState = connectionState

        listener.onConnectionStateChange(this, newState)
    }

    interface Listener {
        fun onConnect(transport: Transport, dtlsParameters: DtlsParameters)

        fun onConnectionStateChange(transport: Transport, newState: PeerConnection.IceConnectionState)
    }
}