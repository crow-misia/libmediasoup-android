#define MSC_CLASS "send_transport"

#include "send_transport.h"

#include <jni.h>
#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>
#include <sdk/android/src/jni/pc/rtp_parameters.h>

#include <Logger.hpp>
#include <Transport.hpp>
#include <future>
#include <thread>

#include "data_producer.h"
#include "jni_util.h"
#include "producer.h"

using namespace webrtc;

namespace mediasoupclient
{

extern jclass sendTransportClass;
extern jmethodID sendTransportConstructorMethod;

extern jmethodID transportListenerOnConnectMethod;
extern jmethodID transportListenerOnConnectionStateChangeMethod;
extern jmethodID sendTransportListenerOnProduceMethod;
extern jmethodID sendTransportListenerOnProduceDataMethod;

extern "C"
{
  JNI_DEFINE_METHOD(jobject, SendTransport, nativeProduce, jlong j_transport, jobject j_listener, jlong j_track, jobjectArray j_encodings, jstring j_codecOptions, jstring j_appData)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new ProducerListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto track = reinterpret_cast<MediaStreamTrackInterface*>(j_track);
                               std::vector<RtpEncodingParameters> encodings;
                               if (j_encodings != nullptr)
                               {
                                 encodings = JavaToNativeVector<RtpEncodingParameters>(env, JavaParamRef<jobjectArray>(env, j_encodings), &jni::JavaToNativeRtpEncodingParameters);
                               }
                               auto codecOptions = json::object();
                               if (j_codecOptions != nullptr)
                               {
                                 codecOptions = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_codecOptions)));
                               }
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               auto producer = getSendTransport(j_transport)->Produce(listener, track, &encodings, &codecOptions, appData);
                               return NativeToJavaProducer(env, producer, listener).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jobject, SendTransport, nativeProduceData, jlong j_transport, jobject j_listener, jstring j_label, jstring j_protocol, jboolean j_ordered, jint j_maxRetransmits,
                    jint j_maxPacketLifeTime, jstring j_appData)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new DataProducerListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto label = JavaToNativeString(env, JavaParamRef<jstring>(env, j_label));
                               auto protocol = JavaToNativeString(env, JavaParamRef<jstring>(env, j_protocol));
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               auto dataProducer = getSendTransport(j_transport)->ProduceData(listener, label, protocol, j_ordered, j_maxRetransmits, j_maxPacketLifeTime, appData);
                               return NativeToJavaDataProducer(env, dataProducer, listener).Release();
                             })
      .value_or(nullptr);
  }
}

std::future<void> SendTransportListenerJni::OnConnect(Transport*, const json& dtlsParameters)
{
  MSC_TRACE();

  return std::async(
    std::launch::async,
    [](const jobject& j_listener, const jobject& j_transport, const json& dtlsParameters) {
      JNIEnv* env = webrtc::AttachCurrentThreadIfNeeded();
      env->CallVoidMethod(j_listener, transportListenerOnConnectMethod, j_transport, NativeToJavaString(env, dtlsParameters.dump()).obj());
    },
    j_listener_.obj(), j_transport_.obj(), dtlsParameters);
}

void SendTransportListenerJni::OnConnectionStateChange(Transport*, const std::string& connectionState)
{
  MSC_TRACE();

  JNIEnv* env = webrtc::AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), transportListenerOnConnectionStateChangeMethod, j_transport_.obj(), NativeToJavaString(env, connectionState).obj());
}

std::future<std::string> SendTransportListenerJni::OnProduce(SendTransport*, const std::string& kind, json rtpParameters, const json& appData)
{
  MSC_TRACE();

  return std::async(
    std::launch::async,
    [](const jobject& j_listener, const jobject& j_transport, const std::string& kind, const json& rtpParameters, const json& appData) {
      JNIEnv* env = webrtc::AttachCurrentThreadIfNeeded();
      auto result = env->CallObjectMethod(j_listener, sendTransportListenerOnProduceMethod, j_transport, NativeToJavaString(env, kind).obj(), NativeToJavaString(env, rtpParameters.dump()).obj(),
                                          NativeToJavaString(env, appData.dump()).obj());
      return JavaToNativeString(env, ScopedJavaLocalRef<jstring>(env, static_cast<jstring>(result)));
    },
    j_listener_.obj(), j_transport_.obj(), kind, rtpParameters, appData);
}

std::future<std::string> SendTransportListenerJni::OnProduceData(SendTransport*, const json& sctpStreamParameters, const std::string& label, const std::string& protocol, const json& appData)
{
  MSC_TRACE();

  return std::async(
    std::launch::async,
    [](const jobject& j_listener, const jobject& j_transport, const json& sctpStreamParameters, const std::string& label, const std::string& protocol, const json& appData) {
      JNIEnv* env = webrtc::AttachCurrentThreadIfNeeded();
      auto result = env->CallObjectMethod(j_listener, sendTransportListenerOnProduceDataMethod, j_transport, NativeToJavaString(env, sctpStreamParameters.dump()).obj(),
                                          NativeToJavaString(env, label).obj(), NativeToJavaString(env, protocol).obj(), NativeToJavaString(env, appData.dump()).obj());
      return JavaToNativeString(env, ScopedJavaLocalRef<jstring>(env, static_cast<jstring>(result)));
    },
    j_listener_.obj(), j_transport_.obj(), sctpStreamParameters, label, protocol, appData);
}

inline SendTransport* getSendTransport(jlong j_transport)
{
  return reinterpret_cast<OwnedSendTransport*>(j_transport)->sendTransport();
}

ScopedJavaLocalRef<jobject> NativeToJavaSendTransport(JNIEnv* env, SendTransport* transport, SendTransportListenerJni* listener)
{
  MSC_TRACE();

  auto ownedTransport = new OwnedSendTransport(transport, listener);
  auto j_transport = ScopedJavaLocalRef<jobject>(env, env->NewObject(sendTransportClass, sendTransportConstructorMethod, NativeToJavaPointer(ownedTransport)));
  listener->SetJTransport(env, j_transport);
  return j_transport;
}

} // namespace mediasoupclient
