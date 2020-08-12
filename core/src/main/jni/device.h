#ifndef DEVICE_H_
#define DEVICE_H_

#include <jni.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Device.hpp>
#include <PeerConnection.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jlong, Device, nativeNewDevice);

  JNI_DEFINE_METHOD(void, Device, nativeDispose, jlong j_device);

  JNI_DEFINE_METHOD(jboolean, Device, nativeIsLoaded, jlong j_device);

  JNI_DEFINE_METHOD(jstring, Device, nativeGetRtpCapabilities, jlong j_device);

  JNI_DEFINE_METHOD(jstring, Device, nativeGetSctpCapabilities, jlong j_device);

  JNI_DEFINE_METHOD(void, Device, nativeLoad, jlong j_device, jstring j_routerRtpCapabilities);

  JNI_DEFINE_METHOD(jboolean, Device, nativeCanProduce, jlong j_device, jstring j_kind);

  JNI_DEFINE_METHOD(jobject, Device, nativeCreateSendTransport, jlong j_device, jobject j_listener, jstring j_id, jstring j_iceParameters, jstring j_iceCandidates, jstring j_dtlsParameters,
                    jstring j_sctpParameters, jobject j_configuration, jlong j_peerConnectionFactory, jstring j_appData);

  JNI_DEFINE_METHOD(jobject, Device, nativeCreateRecvTransport, jlong j_device, jobject j_listener, jstring j_id, jstring j_iceParameters, jstring j_iceCandidates, jstring j_dtlsParameters,
                    jstring j_sctpParameters, jobject j_configuration, jlong j_peerConnectionFactory, jstring j_appData);
}

void JavaToNativeOptions(JNIEnv* env, const JavaRef<jobject>& configuration, jlong factory, PeerConnection::Options& options);

} // namespace mediasoupclient

#endif // DEVICE_H_