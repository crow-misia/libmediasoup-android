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
        loggableSeverity: Logging.Severity = Logging.Severity.LS_NONE,
    ) {
        WebRtcLogger.setHandler(logHandler)

        context.initializePeerConnectionFactory(
            loggableSeverity = loggableSeverity,
        )
    }
}