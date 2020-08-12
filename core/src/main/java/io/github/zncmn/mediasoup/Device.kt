package io.github.zncmn.mediasoup

import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

class Device(
    private val peerConnectionFactory: PeerConnectionFactory
) {
    private var nativeDevice: Long = nativeNewDevice()

    val loaded: Boolean
        get() {
            checkDeviceExists()
            return nativeIsLoaded(nativeDevice)
        }

    val rtpCapabilities: String by lazy {
        checkDeviceExists()
        nativeGetRtpCapabilities(nativeDevice)
    }

    val sctpCapabilities: String by lazy {
        checkDeviceExists()
        nativeGetSctpCapabilities(nativeDevice)
    }

    /**
     * Initialize the Device.
     */
    fun load(routerRtpCapabilities: String) {
        checkDeviceExists()
        nativeLoad(nativeDevice, routerRtpCapabilities)
    }

    /**
     * Whether we can produce audio/video.
     */
    fun canProduce(kind: String): Boolean {
        checkDeviceExists()
        return nativeCanProduce(nativeDevice, kind)
    }

    @JvmOverloads
    fun createSendTransport(
        listener: SendTransport.Listener,
        id: String,
        iceParameters: String,
        iceCandidates: String,
        dtlsParameters: String,
        sctpParameters: String? = null,
        rtcConfig: PeerConnection.RTCConfiguration,
        appData: String? = null
    ): SendTransport {
        checkDeviceExists()
        // Create a new Transport.
        return nativeCreateSendTransport(
            nativeDevice = nativeDevice,
            listener = listener,
            id = id,
            iceParameters = iceParameters,
            iceCandidates = iceCandidates,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            configuration = rtcConfig,
            peerConnectionFactory = peerConnectionFactory.nativePeerConnectionFactory,
            appData = appData
        )
    }

    @JvmOverloads
    fun createRecvTransport(
        listener: RecvTransport.Listener,
        id: String,
        iceParameters: String,
        iceCandidates: String,
        dtlsParameters: String,
        sctpParameters: String? = null,
        rtcConfig: PeerConnection.RTCConfiguration,
        appData: String? = null
    ): RecvTransport {
        checkDeviceExists()
        // Create a new Transport.
        return nativeCreateRecvTransport(
            nativeDevice = nativeDevice,
            listener = listener,
            id = id,
            iceParameters = iceParameters,
            iceCandidates = iceCandidates,
            dtlsParameters = dtlsParameters,
            sctpParameters = sctpParameters,
            configuration = rtcConfig,
            peerConnectionFactory = peerConnectionFactory.nativePeerConnectionFactory,
            appData = appData
        )
    }

    fun dispose() {
        val ptr = nativeDevice
        if (ptr == 0L) {
            return
        }
        nativeDevice = 0L
        nativeDispose(ptr)
    }

    private fun checkDeviceExists() {
        check(nativeDevice != 0L) { "Device has been disposed." }
    }

    private external fun nativeNewDevice(): Long
    private external fun nativeDispose(nativeDevice: Long)
    private external fun nativeIsLoaded(nativeDevice: Long): Boolean
    private external fun nativeGetRtpCapabilities(nativeDevice: Long): String
    private external fun nativeGetSctpCapabilities(nativeDevice: Long): String
    private external fun nativeLoad(nativeDevice: Long, routerRtpCapabilities: String)
    private external fun nativeCanProduce(nativeDevice: Long, kind: String): Boolean
    private external fun nativeCreateSendTransport(
        nativeDevice: Long,
        listener: SendTransport.Listener,
        id: String,
        iceParameters: String,
        iceCandidates: String,
        dtlsParameters: String,
        sctpParameters: String?,
        configuration: PeerConnection.RTCConfiguration,
        peerConnectionFactory: Long,
        appData: String?
    ): SendTransport

    private external fun nativeCreateRecvTransport(
        nativeDevice: Long,
        listener: RecvTransport.Listener,
        id: String,
        iceParameters: String,
        iceCandidates: String,
        dtlsParameters: String,
        sctpParameters: String?,
        configuration: PeerConnection.RTCConfiguration,
        peerConnectionFactory: Long,
        appData: String?
    ): RecvTransport
}