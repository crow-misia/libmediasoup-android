package io.github.zncmn.mediasoup

import org.webrtc.CalledByNative
import org.webrtc.MediaStreamTrack
import org.webrtc.RTCUtils
import org.webrtc.RtpSender

class Producer @CalledByNative internal constructor(
    private var nativeProducer: Long
) {
    interface Listener {
        @CalledByNative("Listener")
        fun onTransportClose(producer: Producer)
    }

    private var cachedTrack: MediaStreamTrack?

    val id: String by lazy {
        checkDeviceExists()
        nativeGetId(nativeProducer)
    }

    val localId: String by lazy {
        checkDeviceExists()
        nativeGetLocalId(nativeProducer)
    }

    val closed: Boolean
        get() {
            checkDeviceExists()
            return nativeIsClosed(nativeProducer)
        }

    val paused: Boolean
        get() {
            checkDeviceExists()
            return nativeIsPaused(nativeProducer)
        }

    val kind: String by lazy {
        checkDeviceExists()
        nativeGetKind(nativeProducer)
    }

    val rtpSender: RtpSender by lazy {
        checkDeviceExists()
        val nativeRtpSender = nativeGetRtpSender(nativeProducer)
        RtpSender(nativeRtpSender)
    }

    var track: MediaStreamTrack?
        get() = cachedTrack
        set(value) {
            checkDeviceExists()
            cachedTrack = value?.also {
                val nativeTrack = RTCUtils.getNativeMediaStreamTrack(it)
                nativeReplaceTrack(nativeProducer, nativeTrack)
            }
        }

    var maxSpatialLayer: Int
        get() {
            checkDeviceExists()
            return nativeGetMaxSpatialLayer(nativeProducer)
        }
        set(value) {
            checkDeviceExists()
            nativeSetMaxSpatialLayer(nativeProducer, value)
        }

    val rtpParameters: String by lazy {
        checkDeviceExists()
        nativeGetRtpParameters(nativeProducer)
    }

    val appData: String by lazy {
        checkDeviceExists()
        nativeGetAppData(nativeProducer)
    }

    val stats: String
        get() {
            checkDeviceExists()
            return nativeGetStats(nativeProducer)
        }

    init {
        val nativeTrack = nativeGetTrack(nativeProducer)
        cachedTrack = RTCUtils.createMediaStreamTrack(nativeTrack)
    }

    fun resume() {
        checkDeviceExists()
        nativeResume(nativeProducer)
    }

    fun pause() {
        checkDeviceExists()
        nativePause(nativeProducer)
    }

    fun close() {
        checkDeviceExists()
        cachedTrack?.dispose()
        cachedTrack = null
        nativeClose(nativeProducer)
    }

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

    private fun checkDeviceExists() {
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