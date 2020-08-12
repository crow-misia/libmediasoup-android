package io.github.zncmn.mediasoup

import android.app.Application
import io.github.zncmn.webrtc.initializePeerConnectionFactory
import io.github.zncmn.webrtc.log.LogHandler
import io.github.zncmn.webrtc.log.WebRtcLogger
import org.webrtc.Logging

object MediasoupClient {
    fun initialize(
        context: Application,
        logHandler: LogHandler,
        useTracer: Boolean = false,
        fieldTrials: String? = null,
        libwebrtcLoggingSeverity: Logging.Severity = Logging.Severity.LS_NONE,
        nativeLibraryName: String = "mediasoupclient_so"
    ) {
        WebRtcLogger.setHandler(logHandler)

        context.initializePeerConnectionFactory(
            useTracer = useTracer,
            fieldTrials = fieldTrials,
            libwebrtcLoggingSeverity = libwebrtcLoggingSeverity,
            nativeLibraryName = nativeLibraryName
        )
    }
}