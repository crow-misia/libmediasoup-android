package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpReceiver

class Consumer @CalledByNative private constructor(
    private var nativeConsumer: Long
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onTransportClose(consumer: Consumer)
    }

    val id: String by lazy {
        checkConsumerExists()
        nativeGetId(nativeConsumer)
    }

    val localId: String by lazy {
        checkConsumerExists()
        nativeGetLocalId(nativeConsumer)
    }

    val producerId: String by lazy {
        checkConsumerExists()
        nativeGetProducerId(nativeConsumer)
    }

    val closed: Boolean
        get() {
            checkConsumerExists()
            return nativeIsClosed(nativeConsumer)
        }

    val paused: Boolean
        get() {
            checkConsumerExists()
            return nativeIsPaused(nativeConsumer)
        }

    val kind: String by lazy {
        checkConsumerExists()
        nativeGetKind(nativeConsumer)
    }

    val rtpReceiver: RtpReceiver by lazy {
        checkConsumerExists()
        val nativeRtpReceiver = nativeGetRtpReceiver(nativeConsumer)
        RtpReceiver(nativeRtpReceiver)
    }

    val track: MediaStreamTrack by lazy {
        checkConsumerExists()
        val nativeTrack = nativeGetTrack(nativeConsumer)
        RTCUtils.createMediaStreamTrack(nativeTrack)
    }

    val rtpParameters: String by lazy {
        checkConsumerExists()
        nativeGetRtpParameters(nativeConsumer)
    }

    val appData: String by lazy {
        checkConsumerExists()
        nativeGetAppData(nativeConsumer)
    }

    val stats: String
        get() {
            checkConsumerExists()
            return nativeGetStats(nativeConsumer)
        }

    fun resume() {
        checkConsumerExists()
        nativeResume(nativeConsumer)
    }

    fun pause() {
        checkConsumerExists()
        nativePause(nativeConsumer)
    }

    fun close() {
        checkConsumerExists()
        nativeClose(nativeConsumer)
    }

    fun dispose() {
        val ptr = nativeConsumer
        if (ptr == 0L) {
            return
        }
        nativeConsumer = 0L
        nativeDispose(ptr)
    }

    private fun checkConsumerExists() {
        check(nativeConsumer != 0L) { "Consumer has been disposed." }
    }

    private external fun nativeGetId(nativeConsumer: Long): String
    private external fun nativeGetLocalId(nativeConsumer: Long): String
    private external fun nativeGetProducerId(nativeConsumer: Long): String
    private external fun nativeIsClosed(nativeConsumer: Long): Boolean
    private external fun nativeGetKind(nativeConsumer: Long): String
    private external fun nativeGetRtpReceiver(nativeConsumer: Long): Long
    private external fun nativeGetTrack(nativeConsumer: Long): Long
    private external fun nativeGetRtpParameters(nativeConsumer: Long): String
    private external fun nativeIsPaused(nativeConsumer: Long): Boolean
    private external fun nativeGetAppData(nativeConsumer: Long): String
    private external fun nativeClose(nativeConsumer: Long)
    private external fun nativeGetStats(nativeConsumer: Long): String
    private external fun nativePause(nativeConsumer: Long)
    private external fun nativeResume(nativeConsumer: Long)
    private external fun nativeDispose(nativeConsumer: Long)
}