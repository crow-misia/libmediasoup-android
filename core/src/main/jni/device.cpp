#define MSC_CLASS "device"

#include "device.h"

#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>
#include <sdk/android/src/jni/pc/peer_connection.h>

#include <Device.hpp>
#include <Logger.hpp>

#include "recv_transport.h"
#include "send_transport.h"

using namespace webrtc;

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jlong, Device, nativeNewDevice)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto* result = new Device();
                               return NativeToJavaPointer(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(void, Device, nativeDispose, jlong j_device)
  {
    MSC_TRACE();

    delete reinterpret_cast<Device*>(j_device);
  }

  JNI_DEFINE_METHOD(jboolean, Device, nativeIsLoaded, jlong j_device)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = reinterpret_cast<Device*>(j_device)->IsLoaded();
                               return static_cast<jboolean>(result);
                             })
      .value_or(false);
  }

  JNI_DEFINE_METHOD(jstring, Device, nativeGetRtpCapabilities, jlong j_device)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = reinterpret_cast<Device*>(j_device)->GetRtpCapabilities();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, Device, nativeGetSctpCapabilities, jlong j_device)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = reinterpret_cast<Device*>(j_device)->GetSctpCapabilities();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Device, nativeLoad, jlong j_device, jstring j_routerRtpCapabilities)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() {
      auto capabilities = JavaToNativeString(env, JavaParamRef<jstring>(env, j_routerRtpCapabilities));
      reinterpret_cast<Device*>(j_device)->Load(json::parse(capabilities));
    });
  }

  JNI_DEFINE_METHOD(jboolean, Device, nativeCanProduce, jlong j_device, jstring j_kind)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto nativeKind = JavaToNativeString(env, JavaParamRef<jstring>(env, j_kind));
                               auto result = reinterpret_cast<Device*>(j_device)->CanProduce(nativeKind);
                               return static_cast<jboolean>(result);
                             })
      .value_or(false);
  }

  JNI_DEFINE_METHOD(jobject, Device, nativeCreateSendTransport, jlong j_device, jobject j_listener, jstring j_id, jstring j_iceParameters, jstring j_iceCandidates, jstring j_dtlsParameters,
                    jstring j_sctpParameters, jobject j_configuration, jlong j_peerConnectionFactory, jstring j_appData)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new SendTransportListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto id = JavaToNativeString(env, JavaParamRef<jstring>(env, j_id));
                               auto iceParameters = JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceParameters));
                               auto iceCandidates = JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceCandidates));
                               auto dtlsParameters = JavaToNativeString(env, JavaParamRef<jstring>(env, j_dtlsParameters));
                               json sctpParameters;
                               if (j_sctpParameters == nullptr)
                               {
                                 sctpParameters = nullptr;
                               }
                               else
                               {
                                 sctpParameters = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_sctpParameters)));
                               }
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               PeerConnection::Options options;
                               JavaToNativeOptions(env, JavaParamRef<jobject>(env, j_configuration), j_peerConnectionFactory, options);

                               auto transport = reinterpret_cast<Device*>(j_device)->CreateSendTransport(listener, id, json::parse(iceParameters), json::parse(iceCandidates),
                                                                                                         json::parse(dtlsParameters), sctpParameters, &options, appData);
                               return NativeToJavaSendTransport(env, transport, listener).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jobject, Device, nativeCreateRecvTransport, jlong j_device, jobject j_listener, jstring j_id, jstring j_iceParameters, jstring j_iceCandidates, jstring j_dtlsParameters,
                    jstring j_sctpParameters, jobject j_configuration, jlong j_peerConnectionFactory, jstring j_appData)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new RecvTransportListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto id = JavaToNativeString(env, JavaParamRef<jstring>(env, j_id));
                               auto iceParameters = JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceParameters));
                               auto iceCandidates = JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceCandidates));
                               auto dtlsParameters = JavaToNativeString(env, JavaParamRef<jstring>(env, j_dtlsParameters));
                               json sctpParameters;
                               if (j_sctpParameters == nullptr)
                               {
                                 sctpParameters = nullptr;
                               }
                               else
                               {
                                 sctpParameters = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_sctpParameters)));
                               }
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               PeerConnection::Options options;
                               JavaToNativeOptions(env, JavaParamRef<jobject>(env, j_configuration), j_peerConnectionFactory, options);

                               auto transport = reinterpret_cast<Device*>(j_device)->CreateRecvTransport(listener, id, json::parse(iceParameters), json::parse(iceCandidates),
                                                                                                         json::parse(dtlsParameters), sctpParameters, &options, appData);
                               return NativeToJavaRecvTransport(env, transport, listener).Release();
                             })
      .value_or(nullptr);
  }
}

void JavaToNativeOptions(JNIEnv* env, const JavaRef<jobject>& j_configuration, jlong j_factory, PeerConnection::Options& options)
{
  MSC_TRACE();

  if (!j_configuration.is_null())
  {
    PeerConnectionInterface::RTCConfiguration rtc_config(PeerConnectionInterface::RTCConfigurationType::kAggressive);
    jni::JavaToNativeRTCConfiguration(env, j_configuration, &rtc_config);
    options.config = rtc_config;
  }
  options.factory = reinterpret_cast<PeerConnectionFactoryInterface*>(j_factory);
}

} // namespace mediasoupclient