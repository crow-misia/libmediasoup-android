package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.RtpParameters
import io.github.zncmn.webrtc.log.WebRtcLogger
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCStatsReport
import org.webrtc.RtpSender

class Producer internal constructor(
    private val privateListener: PrivateListener,
    private val listener: Listener,
    val id: String,
    val localId: String,
    val rtpSender: RtpSender,
    track: MediaStreamTrack,
    val rtpParameters: RtpParameters,
    val appData: Any? = null
) {
    companion object {
        private val TAG = Producer::class.simpleName
    }

    interface Listener {
        fun onTransportClose(producer: Producer)
    }

    internal interface PrivateListener {
        fun onClose(producer: Producer)
        fun onReplaceTrack(producer: Producer, track: MediaStreamTrack)
        fun onSetMaxSpatialLayer(producer: Producer, maxSpatialLayer: Int)
        fun onGetStats(producer: Producer): RTCStatsReport
    }

    var track: MediaStreamTrack = track
        internal set(value) {
            check(!closed) { "Producer closed" }
            check(value.state() != MediaStreamTrack.State.ENDED) { "track ended" }

            if (field == value) {
                WebRtcLogger.d(TAG, "same track, ignored")
                return
            }

            privateListener.onReplaceTrack(this, track)

            // If this Producer was paused/resumed and the state of the new
            // track does not match, fix it.
            value.setEnabled(!paused)

            field = value
        }

    val kind: String = track.kind()

    var closed: Boolean = false
        private set

    val paused: Boolean
        get() = !track.enabled()

    var maxSpatialLayer: Int = 0
        set(layer) {
            check(!closed) { "Producer closed" }
            if (kind == "video") {
                throw MediasoupException("not a video Producer")
            }
            if (layer == field) {
                return
            }

            // May throw.
            privateListener.onSetMaxSpatialLayer(this, layer)

            field = layer
        }

    fun resume() {
        if (closed) {
            WebRtcLogger.e(TAG, "Producer closed")
            return
        }
        track.setEnabled(true)
    }

    fun pause() {
        if (closed) {
            WebRtcLogger.e(TAG, "Producer closed")
            return
        }
        track.setEnabled(false)
    }

    val stats: RTCStatsReport
        get() {
            check(!closed) { "Producer closed" }
            return privateListener.onGetStats(this)
        }

    fun close() {
        if (closed) {
            return
        }
        closed = true
        privateListener.onClose(this)
    }

    internal fun transportClosed() {
        if (closed) {
            return
        }
        closed = true
        listener.onTransportClose(this)
    }
}