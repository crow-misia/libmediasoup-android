package io.github.zncmn.mediasoup.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * cf. https://mediasoup.org/documentation/v3/mediasoup/api/#WebRtcTransport
 */
@JsonClass(generateAdapter = true)
data class WebRtcTransportOptions(
    /**
     * Listening IP address or addresses in order of preference (first one is the preferred one).
     */
    var listenIps: List<String>,

    /**
     * Listen in UDP.
     */
    var enableUdp: Boolean = true,

    /**
     * Listen in TCP.
     */
    var enableTcp: Boolean = false,

    /**
     * Listen in UDP.
     */
    var preferUdp: Boolean = false,

    /**
     * Listen in TCP.
     */
    var preferTcp: Boolean = false,

    /**
     * Initial available outgoing bitrate (in bps).
     */
    var initialAvailableOutgoingBitrate: Long = 600000L,

    /**
     * Create a SCTP association.
     */
    var numSctpStreams: NumSctpStreams? = null,

    /**
     * Maximum size of data that can be passed to DataProducer's send() method.
     */
    var maxSctpMessageSize: Int = 262144,

    /**
     * Custom application data.
     */
    var appData: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class IceParameters(
    /**
     * ICE username fragment.
     */
    var usernameFragment: String,

    /**
     * ICE password.
     */
    var password: String,

    /**
     * ICE Lite.
     */
    var iceLite: Boolean = false
)

@JsonClass(generateAdapter = true)
data class IceCandidate(
    /**
     * The address family of the candidate.
     */
    var family: String? = null,

    /**
     * Unique identifier that allows ICE to correlate candidates that appear on
     * multiple transports.
     */
    var foundation: String,

    /**
     * The assigned priority of the candidate.
     */
    var priority: Long,

    /**
     * The IP address of the candidate.
     */
    var ip: String,

    /**
     * The protocol of the candidate.
     */
    var protocol: String,

    /**
     * The port for the candidate.
     */
    var port: Int,

    /**
     * The type of candidate..
     */
    var type: String,

    /**
     * The type of TCP candidate.
     */
    var tcpType: String? = null
)

@JsonClass(generateAdapter = true)
data class DtlsParameters(
    /**
     * DTLS role. Default 'auto'.
     */
    var role: DtlsRole = DtlsRole.AUTO,

    /**
     * DTLS fingerprints.
     */
    var fingerprints: List<DtlsFingerprint>
)

@JsonClass(generateAdapter = true)
data class DtlsFingerprint(
    /**
     * Hash function algorithm.
     */
    var algorithm: String,

    /**
     * Certificate fingerprint value.
     */
    var value: String
)

@JsonClass(generateAdapter = false)
enum class DtlsRole {
    /**
     * The DTLS role is determined based on the resolved ICE role (the “controlled” role acts as DTLS client,
     * the “controlling” role acts as DTLS server”). Since mediasoup is a ICE Lite implementation it always behaves as ICE “controlled”.
     */
    @Json(name = "auto") AUTO,

    /**
     * DTLS client role.
     */
    @Json(name = "client") CLIENT,

    /**
     * DTLS server role.
     */
    @Json(name = "server") SERVER
}

@JsonClass(generateAdapter = false)
enum class IceState {
    /**
     * No ICE Binding Requests have been received yet.
     */
    @Json(name = "new") NEW,

    /**
     * Valid ICE Binding Request have been received, but none with USE-CANDIDATE attribute. Outgoing media is allowed.
     */
    @Json(name = "connected") CONNECTED,

    /**
     * ICE Binding Request with USE_CANDIDATE attribute has been received. Media in both directions is now allowed.
     */
    @Json(name = "completed") COMPLETED,

    /**
     * ICE was “connected” or “completed” but it has suddenly failed (this can just happen if the selected tuple has “tcp” protocol).
     */
    @Json(name = "disconnected") DISCONNECTED,

    /**
     * ICE state when the transport has been closed.
     */
    @Json(name = "closed") CLOSED
}


@JsonClass(generateAdapter = false)
enum class DtlsState {
    /**
     * DTLS procedures not yet initiated.
     */
    @Json(name = "new") NEW,

    /**
     * DTLS connecting.
     */
    @Json(name = "connecting") CONNECTING,

    /**
     * DTLS successfully connected (SRTP keys already extracted).
     */
    @Json(name = "connected") CONNECTED,

    /**
     * DTLS connection failed.
     */
    @Json(name = "failed") FAILED,

    /**
     * DTLS state when the transport has been closed.
     */
    @Json(name = "closed") CLOSED
}

@JsonClass(generateAdapter = false)
enum class ConnectionState {
    @Json(name = "new") NEW,
    @Json(name = "connecting") CONNECTING,
    @Json(name = "connected") CONNECTED,
    @Json(name = "failed") FAILED,
    @Json(name = "disconnected") DISCONNECTED,
    @Json(name = "closed") CLOSED
}
