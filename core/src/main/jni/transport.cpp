#define MSC_CLASS "transport"

#include "transport.h"

#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Logger.hpp>
#include <Transport.hpp>
#include <json.hpp>

using namespace webrtc;

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetId, jlong j_transport)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getTransport(j_transport)->GetId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, Transport, nativeIsClosed, jlong j_transport)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getTransport(j_transport)->IsClosed();
                               return static_cast<jboolean>(result);
                             })
      .value_or(true);
  }

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetConnectionState, jlong j_transport)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getTransport(j_transport)->GetConnectionState();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetAppData, jlong j_transport)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getTransport(j_transport)->GetAppData();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Transport, nativeClose, jlong j_transport)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { reinterpret_cast<Transport*>(j_transport)->Close(); });
  }

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetStats, jlong j_transport)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getTransport(j_transport)->GetStats();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Transport, nativeRestartIce, jlong j_transport, jstring j_iceParameters)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() {
      auto iceParameters = json::object();
      if (j_iceParameters != nullptr)
      {
        iceParameters = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceParameters)));
      }
      getTransport(j_transport)->RestartIce(iceParameters);
    });
  }

  JNI_DEFINE_METHOD(void, Transport, nativeUpdateIceServers, jlong j_transport, jstring j_iceServers)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() {
      auto iceServers = json::object();
      if (j_iceServers != nullptr)
      {
        iceServers = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_iceServers)));
      }
      getTransport(j_transport)->UpdateIceServers(iceServers);
    });
  }

  JNI_DEFINE_METHOD(void, Transport, nativeDispose, jlong j_transport)
  {
    MSC_TRACE();

    delete reinterpret_cast<OwnedTransport*>(j_transport);
  }
}

inline Transport* getTransport(jlong j_transport)
{
  return reinterpret_cast<OwnedTransport*>(j_transport)->transport();
}

} // namespace mediasoupclient