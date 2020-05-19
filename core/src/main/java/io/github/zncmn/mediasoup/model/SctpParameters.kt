@file:Suppress("unused")

package io.github.zncmn.mediasoup.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * cf. https://mediasoup.org/documentation/v3/mediasoup/sctp-parameters/
 */
@JsonClass(generateAdapter = true)
data class SctpCapabilities @JvmOverloads constructor(
    var numStreams: List<NumSctpStreams> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NumSctpStreams(
    /**
     * Initially requested number of outgoing SCTP streams.
     */
    @Json(name = "OS") var os: Int,

    /**
     * Maximum number of incoming SCTP streams.
     */
    @Json(name = "MIS") var mis: Int
)

@JsonClass(generateAdapter = true)
data class SctpParameters @JvmOverloads constructor(
    /**
     * Must always equal 5000.
     */
    var port: Int = 5000,

    /**
     * Initially requested number of outgoing SCTP streams.
     */
    @Json(name = "OS") var os: Int,

    /**
     * Maximum number of incoming SCTP streams.
     */
    @Json(name = "MIS") var mis: Int,

    /**
     * Maximum allowed size for SCTP messages.
     */
    var maxMessageSize: Int
)

@JsonClass(generateAdapter = true)
data class SctpStreamParameters @JvmOverloads constructor(
    /**
     * SCTP stream id.
     */
    var streamId: Int,

    /**
     * Whether data messages must be received in order. if true the messages will
     * be sent reliably. Default true.
     */
    var ordered: Boolean = true,

    /**
     * When ordered is false indicates the time (in milliseconds) after which a
     * SCTP packet will stop being retransmitted.
     */
    var maxPacketLifeTime: Int = 0,

    /**
     * When ordered is false indicates the maximum number of times a packet will
     * be retransmitted.
     */
    var maxRetransmits: Int = 0,

    /**
     * DataChannel priority.
     */
    var priority: String = "",

    /**
     * A label which can be used to distinguish this DataChannel from others.
     */
    var label: String = "",

    /**
     * Name of the sub-protocol used by this DataChannel.
     */
    var protocol: String = ""
)

@JsonClass(generateAdapter = false)
enum class TransportTraceEventType {
    /**
     * RTP probation packet.
     */
    @Json(name = "probation") PROBATION,

    /**
     * Transport bandwidth estimation changed.
     */
    @Json(name = "bwe") BWE
}

@JsonClass(generateAdapter = false)
enum class TransportSctpState {
    /**
     * SCTP procedures not yet initiated.
     */
    @Json(name = "new") NEW,

    /**
     * SCTP connecting.
     */
    @Json(name = "connecting") CONNECTING,

    /**
     * SCTP successfully connected.
     */
    @Json(name = "connected") CONNECTED,

    /**
     * SCTP connection failed.
     */
    @Json(name = "failed") FAILED,

    /**
     * SCTP state when the transport has been closed.
     */
    @Json(name = "closed") CLOSED
}
