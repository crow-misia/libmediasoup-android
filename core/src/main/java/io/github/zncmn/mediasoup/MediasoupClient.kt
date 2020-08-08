package io.github.zncmn.mediasoup

import android.app.Application
import io.github.zncmn.webrtc.initializePeerConnectionFactory
import io.github.zncmn.webrtc.log.LogHandler
import io.github.zncmn.webrtc.log.WebRtcLogger

object MediasoupClient {
    fun initialize(application: Application, logHandler: LogHandler) {
        WebRtcLogger.setHandler(logHandler)

        application.initializePeerConnectionFactory()
    }
}