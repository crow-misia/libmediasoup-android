package io.github.zncmn.mediasoup

import android.util.Log
import io.github.zncmn.webrtc.log.WebRtcLogger
import org.webrtc.CalledByNative


class Logger {
    enum class LogLevel(val priority: Int, val level: Int) {
        LOG_NONE(Log.ASSERT, 0),
        LOG_ERROR(Log.ERROR, 1),
        LOG_WARN(Log.WARN, 2),
        LOG_DEBUG(Log.INFO, 3),
        LOG_TRACE(Log.DEBUG, 4);

        companion object {
            private val map = values().associateBy({ it.level }, { it.priority })

            fun convertToPriority(logLevel: Int): Int {
                return map[logLevel] ?: Log.ASSERT
            }
        }
    }

    interface LogHandlerInterface {
        @CalledByNative("LogHandlerInterface")
        fun onLog(logLevel: Int, tag: String, message: String?)
    }

    companion object {
        private var logHandler: LogHandlerInterface? = null
        private var nativeHandler: Long = 0

        fun setDefaultHandler() {
            setHandler(DefaultLogHandler())
        }

        fun setLogLevel(logLevel: LogLevel) {
            nativeSetLogLevel(logLevel.level)
        }

        fun setHandler(handler: LogHandlerInterface) {
            logHandler = handler
            nativeHandler = nativeSetHandler(handler)
        }

        fun dispose() {
            val handler = nativeHandler
            if (handler == 0L) {
                return
            }
            nativeHandler = 0L
            nativeDispose(handler)
        }

        @JvmStatic
        private external fun nativeSetHandler(handler: LogHandlerInterface): Long

        @JvmStatic
        private external fun nativeSetLogLevel(level: Int)

        @JvmStatic
        private external fun nativeDispose(nativeHandler: Long)
    }

    private class DefaultLogHandler : LogHandlerInterface {
        override fun onLog(logLevel: Int, tag: String, message: String?) {
            WebRtcLogger.println(LogLevel.convertToPriority(logLevel), tag, message)
        }
    }
}