package org.webrtc

object RTCUtils {
        fun createMediaStreamTrack(track: Long): MediaStreamTrack {
            return checkNotNull(MediaStreamTrack.createMediaStreamTrack(track)) { "nativeTrack may not be null" }
        }

        fun getNativeMediaStreamTrack(mediaStreamTrack: MediaStreamTrack): Long {
            return mediaStreamTrack.nativeMediaStreamTrack
        }

        fun genRtpEncodingParameters(
            rid: String?,
            active: Boolean,
            bitratePriority: Double?,
            networkPriority: Int?,
            maxBitrateBps: Int?,
            minBitrateBps: Int?,
            maxFramerate: Int?,
            numTemporalLayers: Int?,
            scaleResolutionDownBy: Double?,
            ssrc: Long?,
            adaptiveAudioPacketTime: Boolean?
        ): RtpParameters.Encoding? {
            return RtpParameters.Encoding(
                rid,
                active,
                bitratePriority!!,
                networkPriority!!,
                maxBitrateBps,
                minBitrateBps!!,
                maxFramerate,
                numTemporalLayers,
                scaleResolutionDownBy,
                ssrc,
                adaptiveAudioPacketTime!!
            )
        }
}
