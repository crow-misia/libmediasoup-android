package org.webrtc

internal object RTCUtils {
    fun createMediaStreamTrack(track: Long): MediaStreamTrack {
        return checkNotNull(MediaStreamTrack.createMediaStreamTrack(track)) { "nativeTrack may not be null" }
    }

    fun getNativeMediaStreamTrack(mediaStreamTrack: MediaStreamTrack): Long {
        return mediaStreamTrack.nativeMediaStreamTrack
    }
}