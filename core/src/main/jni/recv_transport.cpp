#define MSC_CLASS "recv_transport"

#include "recv_transport.h"

#include <jni.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Logger.hpp>
#include <Transport.hpp>

#include "consumer.h"
#include "data_consumer.h"
#include "jni_util.h"

using namespace webrtc;

namespace mediasoupclient
{

extern jclass recvTransportClass;
extern jmethodID recvTransportConstructorMethod;

extern jmethodID transportListenerOnConnectMethod;
extern jmethodID transportListenerOnConnectionStateChangeMethod;

extern "C"
{
  JNI_DEFINE_METHOD(jobject, RecvTransport, nativeConsume, jlong j_transport, jobject j_listener, jstring j_id, jstring j_producerId, jstring j_kind, jstring j_rtpParameters, jstring j_appData)
  {
    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new ConsumerListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto id = JavaToNativeString(env, JavaParamRef<jstring>(env, j_id));
                               auto producerId = JavaToNativeString(env, JavaParamRef<jstring>(env, j_producerId));
                               auto kind = JavaToNativeString(env, JavaParamRef<jstring>(env, j_kind));
                               auto rtpParameters = json::object();
                               if (j_rtpParameters != nullptr)
                               {
                                 rtpParameters = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_rtpParameters)));
                               }
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               auto consumer = getRecvTransport(j_transport)->Consume(listener, id, producerId, kind, &rtpParameters, appData);
                               return NativeToJavaConsumer(env, consumer, listener).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jobject, RecvTransport, nativeConsumeData, jlong j_transport, jobject j_listener, jstring j_id, jstring j_producerId, jstring j_label, jstring j_protocol, jstring j_appData)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto listener = new DataConsumerListenerJni(env, JavaParamRef<jobject>(env, j_listener));
                               auto id = JavaToNativeString(env, JavaParamRef<jstring>(env, j_id));
                               auto producerId = JavaToNativeString(env, JavaParamRef<jstring>(env, j_producerId));
                               auto label = JavaToNativeString(env, JavaParamRef<jstring>(env, j_label));
                               auto protocol = JavaToNativeString(env, JavaParamRef<jstring>(env, j_protocol));
                               auto appData = json::object();
                               if (j_appData != nullptr)
                               {
                                 appData = json::parse(JavaToNativeString(env, JavaParamRef<jstring>(env, j_appData)));
                               }

                               auto dataConsumer = getRecvTransport(j_transport)->ConsumeData(listener, id, producerId, label, protocol, appData);
                               return NativeToJavaDataConsumer(env, dataConsumer, listener).Release();
                             })
      .value_or(nullptr);
  }
}

std::future<void> RecvTransportListenerJni::OnConnect(Transport*, const json& dtlsParameters)
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

void RecvTransportListenerJni::OnConnectionStateChange(Transport*, const std::string& connectionState)
{
  MSC_TRACE();

  JNIEnv* env = webrtc::AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), transportListenerOnConnectionStateChangeMethod, j_transport_.obj(), NativeToJavaString(env, connectionState).obj());
}

inline RecvTransport* getRecvTransport(jlong j_transport)
{
  return reinterpret_cast<OwnedRecvTransport*>(j_transport)->recvTransport();
}

ScopedJavaLocalRef<jobject> NativeToJavaRecvTransport(JNIEnv* env, RecvTransport* transport, RecvTransportListenerJni* listener)
{
  MSC_TRACE();

  auto ownedTransport = new OwnedRecvTransport(transport, listener);
  auto j_transport = ScopedJavaLocalRef<jobject>(env, env->NewObject(recvTransportClass, recvTransportConstructorMethod, NativeToJavaPointer(ownedTransport)));
  listener->SetJTransport(env, j_transport);
  return j_transport;
}

} // namespace mediasoupclient
