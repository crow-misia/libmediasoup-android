package io.github.crow_misia.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpSender

class Producer @CalledByNative internal constructor(
    private var nativeProducer: Long,
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onTransportClose(producer: Producer)
    }

    private var cachedTrack: MediaStreamTrack?

    /**
     * Producer ID.
     */
    val id: String by lazy {
        checkProducerExists()
        nativeGetId(nativeProducer)
    }

    /**
     * Local ID.
     */
    val localId: String by lazy {
        checkProducerExists()
        nativeGetLocalId(nativeProducer)
    }

    /**
     * Whether the Producer is closed.
     */
    val closed: Boolean
        get() {
            checkProducerExists()
            return nativeIsClosed(nativeProducer)
        }

    /**
     *  Whether the Producer is paused.
     */
    val paused: Boolean
        get() {
            checkProducerExists()
            return nativeIsPaused(nativeProducer)
        }

    /**
     * Media kind.
     */
    val kind: String by lazy {
        checkProducerExists()
        nativeGetKind(nativeProducer)
    }

    /**
     * Associated RtpSender.
     */
    val rtpSender: RtpSender by lazy {
        checkProducerExists()
        val nativeRtpSender = nativeGetRtpSender(nativeProducer)
        RtpSender(nativeRtpSender)
    }

    /**
     * The associated track.
     */
    var track: MediaStreamTrack?
        get() = cachedTrack
        set(value) {
            checkProducerExists()
            cachedTrack = value?.also {
                val nativeTrack = RTCUtils.getNativeMediaStreamTrack(it)
                nativeReplaceTrack(nativeProducer, nativeTrack)
            }
        }

    /**
     * Max SpatialLayer.
     */
    var maxSpatialLayer: Int
        get() {
            checkProducerExists()
            return nativeGetMaxSpatialLayer(nativeProducer)
        }
        set(value) {
            checkProducerExists()
            nativeSetMaxSpatialLayer(nativeProducer, value)
        }

    /**
     * RTP parameters.
     */
    val rtpParameters: String by lazy {
        checkProducerExists()
        nativeGetRtpParameters(nativeProducer)
    }

    /**
     * App custom data.
     */
    val appData: String by lazy {
        checkProducerExists()
        nativeGetAppData(nativeProducer)
    }

    /**
     * Get associated RTCRtpReceiver stats.
     */
    val stats: String
        get() {
            checkProducerExists()
            return nativeGetStats(nativeProducer)
        }

    init {
        val nativeTrack = nativeGetTrack(nativeProducer)
        cachedTrack = RTCUtils.createMediaStreamTrack(nativeTrack)
    }

    /**
     * Resumes sending media.
     */
    fun resume() {
        checkProducerExists()
        nativeResume(nativeProducer)
    }

    /**
     *  Pauses sending media.
     */
    fun pause() {
        checkProducerExists()
        nativePause(nativeProducer)
    }

    /**
     * Closes the Producer.
     */
    fun close() {
        checkProducerExists()
        cachedTrack?.dispose()
        cachedTrack = null
        nativeClose(nativeProducer)
    }

    /**
     * Dispose the Producer.
     */
    fun dispose() {
        cachedTrack?.dispose()
        cachedTrack = null

        val ptr = nativeProducer
        if (ptr == 0L) {
            return
        }
        nativeProducer = 0L
        nativeDispose(ptr)
    }

    private fun checkProducerExists() {
        check(nativeProducer != 0L) { "Producer has been disposed." }
    }

    private external fun nativeGetId(nativeProducer: Long): String
    private external fun nativeGetLocalId(nativeProducer: Long): String
    private external fun nativeIsClosed(nativeProducer: Long): Boolean
    private external fun nativeGetKind(nativeProducer: Long): String
    private external fun nativeGetRtpSender(nativeProducer: Long): Long
    private external fun nativeGetTrack(nativeProducer: Long): Long
    private external fun nativeGetRtpParameters(nativeProducer: Long): String
    private external fun nativeIsPaused(nativeProducer: Long): Boolean
    private external fun nativeGetMaxSpatialLayer(nativeProducer: Long): Int
    private external fun nativeGetAppData(nativeProducer: Long): String
    private external fun nativeClose(nativeProducer: Long)
    private external fun nativeGetStats(nativeProducer: Long): String
    private external fun nativePause(nativeProducer: Long)
    private external fun nativeResume(nativeProducer: Long)
    private external fun nativeReplaceTrack(nativeProducer: Long, track: Long)
    private external fun nativeSetMaxSpatialLayer(nativeProducer: Long, spatialLayer: Int)
    private external fun nativeDispose(nativeProducer: Long)
}
