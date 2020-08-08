package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.MediaKind
import io.github.zncmn.mediasoup.model.RtpParameters
import io.github.zncmn.webrtc.log.WebRtcLogger
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCStatsReport
import org.webrtc.RtpReceiver

class Consumer internal constructor(
    private val privateListener: PrivateListener,
    private val listener: Listener,
    val id: String,
    val localId: String,
    val producerId: String,
    val rtpReceiver: RtpReceiver,
    val track: MediaStreamTrack,
    val rtpParameters: RtpParameters,
    val appData: Any? = null
) {
    companion object {
        private val TAG = Consumer::class.simpleName
    }

    interface Listener {
        fun onTransportClose(consumer: Consumer)
    }

    internal interface PrivateListener {
        fun onClose(consumer: Consumer)
        fun onGetStats(consumer: Consumer): RTCStatsReport
    }

    var closed: Boolean = false
        private set

    val kind: MediaKind = track.kind()

    val paused: Boolean
        get() = !track.enabled()

    val stats: RTCStatsReport
        get() {
            check(!closed) { "Consumer closed" }
            return privateListener.onGetStats(this)
        }

    fun close() {
        if (closed) {
            return
        }
        closed = true
        privateListener.onClose(this)
    }

    fun pause() {
        if (closed) {
            WebRtcLogger.e(TAG, "Consumer closed")
            return
        }
        track.setEnabled(false)
    }

    fun resume() {
        if (closed) {
            WebRtcLogger.e(TAG, "Consumer closed")
            return
        }
        track.setEnabled(true)
    }

    internal fun transportClosed() {
        if (closed) {
            return
        }
        closed = true
        listener.onTransportClose(this)
    }
}