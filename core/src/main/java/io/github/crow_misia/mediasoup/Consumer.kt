package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpReceiver

/**
 * Consumer.
 */
class Consumer @CalledByNative private constructor(
    private var nativeConsumer: Long,
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onTransportClose(consumer: Consumer)
    }

    /**
     * Consumer ID.
     */
    val id: String by lazy {
        checkConsumerExists()
        nativeGetId(nativeConsumer)
    }

    /**
     * Local ID.
     */
    val localId: String by lazy {
        checkConsumerExists()
        nativeGetLocalId(nativeConsumer)
    }

    /**
     * Producer ID.
     */
    val producerId: String by lazy {
        checkConsumerExists()
        nativeGetProducerId(nativeConsumer)
    }

    /**
     * Whether the Consumer is closed.
     */
    val closed: Boolean
        get() {
            checkConsumerExists()
            return nativeIsClosed(nativeConsumer)
        }

    /**
     *  Whether the Consumer is paused.
     */
    val paused: Boolean
        get() {
            checkConsumerExists()
            return nativeIsPaused(nativeConsumer)
        }

    /**
     * Media kind.
     */
    val kind: String by lazy {
        checkConsumerExists()
        nativeGetKind(nativeConsumer)
    }

    /**
     * Associated RtpReceiver.
     */
    val rtpReceiver: RtpReceiver by lazy {
        checkConsumerExists()
        val nativeRtpReceiver = nativeGetRtpReceiver(nativeConsumer)
        RtpReceiver(nativeRtpReceiver)
    }

    /**
     * The associated track.
     */
    val track: MediaStreamTrack by lazy {
        checkConsumerExists()
        val nativeTrack = nativeGetTrack(nativeConsumer)
        RTCUtils.createMediaStreamTrack(nativeTrack)
    }

    /**
     * RTP parameters.
     */
    val rtpParameters: String by lazy {
        checkConsumerExists()
        nativeGetRtpParameters(nativeConsumer)
    }

    /**
     * App custom data.
     */
    val appData: String by lazy {
        checkConsumerExists()
        nativeGetAppData(nativeConsumer)
    }

    /**
     * Get associated RTCRtpReceiver stats.
     */
    val stats: String
        get() {
            checkConsumerExists()
            return nativeGetStats(nativeConsumer)
        }

    /**
     *  Resumes receiving media.
     */
    fun resume() {
        checkConsumerExists()
        nativeResume(nativeConsumer)
    }

    /**
     *  Pauses receiving media.
     */
    fun pause() {
        checkConsumerExists()
        nativePause(nativeConsumer)
    }

    /**
     * Closes the Consumer.
     */
    fun close() {
        checkConsumerExists()
        nativeClose(nativeConsumer)
    }

    /**
     * Dispose the Consumer.
     */
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