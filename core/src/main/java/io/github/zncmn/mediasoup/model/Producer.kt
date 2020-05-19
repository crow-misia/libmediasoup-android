package io.github.zncmn.mediasoup.model

import com.squareup.moshi.JsonClass

/**
 * cf. https://mediasoup.org/documentation/v3/mediasoup-client/api/#Producer
 */
@JsonClass(generateAdapter = true)
data class ProducerCodecOptions(
    /**
     * Enable OPUS stereo (if the audio source is stereo).
     */
    var opusStereo: Boolean? = null,

    /**
     * Enable OPUS in band FEC.
     */
    var opusFec: Boolean? = null,

    /**
     * Enable OPUS discontinuous transmission.
     */
    var opusDtx: Boolean? = null,

    /**
     * Set OPUS maximum playback rate.
     */
    var opusMaxPlaybackRate: Int? = null,

    /**
     * Set OPUS preferred duration of media represented by a packet.
     */
    var opusPtime: Long? = null,

    /**
     * Just for libwebrtc based browsers. Set video initial bitrate.
     */
    var videoGoogleStartBitrate: Int? = null,

    /**
     * Just for libwebrtc based browsers. Set video maximum bitrate.
     */
    var videoGoogleMaxBitrate: Int? = null,

    /**
     * Just for libwebrtc based browsers. Set video minimum bitrate.
     */
    var videoGoogleMinBitrate: Int? = null
)
