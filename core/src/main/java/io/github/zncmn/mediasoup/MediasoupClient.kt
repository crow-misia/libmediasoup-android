package io.github.zncmn.mediasoup

import android.app.Application
import io.github.crow_misia.webrtc.initializePeerConnectionFactory
import io.github.crow_misia.webrtc.log.LogHandler
import io.github.crow_misia.webrtc.log.WebRtcLogger
import org.webrtc.Logging

object MediasoupClient {
    @JvmStatic
    @JvmOverloads
    fun initialize(
        context: Application,
        logHandler: LogHandler,
        useTracer: Boolean = false,
        fieldTrials: String? = null,
        loggableSeverity: Logging.Severity = Logging.Severity.LS_NONE,
        nativeLibraryName: String = "mediasoupclient_so",
    ) {
        WebRtcLogger.setHandler(logHandler)

        context.initializePeerConnectionFactory(
            useTracer = useTracer,
            fieldTrials = fieldTrials,
            loggableSeverity = loggableSeverity,
            nativeLibraryName = nativeLibraryName,
        )
    }
}