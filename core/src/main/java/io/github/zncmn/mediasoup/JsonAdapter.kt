package io.github.zncmn.mediasoup

import com.squareup.moshi.Moshi
import io.github.zncmn.mediasoup.model.ExtendedRtpCapabilities
import io.github.zncmn.mediasoup.model.RtpCapabilities
import io.github.zncmn.mediasoup.model.SctpCapabilities

internal val moshi = Moshi.Builder().build()

internal val rtpCapabilitiesAdapter = moshi.adapter(RtpCapabilities::class.java)

internal val extendedRtpCapabilitiesAdapter = moshi.adapter(ExtendedRtpCapabilities::class.java)

internal val sctpCapabilitiesAdapter = moshi.adapter(SctpCapabilities::class.java)
