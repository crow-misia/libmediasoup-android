package io.github.zncmn.mediasoup

import io.github.zncmn.mediasoup.model.*

data class TransportRemoteParameters(
    val id: String,
    val iceParameters: IceParameters,
    val iceCandidates: List<IceCandidate>,
    val dtlsParameters: DtlsParameters,
    val sctpParameters: SctpParameters
)

data class ConsumerRemoteParameters(
    val producerId: String,
    val id: String,
    val kind: MediaKind,
    val rtpParameters: RtpParameters
)

