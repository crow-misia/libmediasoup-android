#ifndef SEND_TRANSPORT_JNI_H_
#define SEND_TRANSPORT_JNI_H_

#include <jni.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Transport.hpp>

#include "jni_common.h"
#include "jni_util.h"
#include "transport.h"

using namespace webrtc;

namespace mediasoupclient
{

extern "C"
{
  JNI_DEFINE_METHOD(jobject, SendTransport, nativeProduce, jlong j_transport, jobject j_listener, jlong j_track, jobjectArray j_encodings, jstring j_codecOptions, jstring j_appData);
  JNI_DEFINE_METHOD(jobject, SendTransport, nativeProduceData, jlong j_transport, jobject j_listener, jstring j_label, jstring j_protocol, jboolean j_ordered, jint j_maxRetransmits,
                    jint j_maxPacketLifeTime, jstring j_appData);
}

extern jclass transportListenerClass;

class SendTransportListenerJni final : public SendTransport::Listener
{
public:
  SendTransportListenerJni(JNIEnv* env, const JavaRef<jobject>& j_listener) : j_listener_(env, j_listener) {}

  ~SendTransportListenerJni() {}

  std::future<void> OnConnect(Transport* transport, const json& dtlsParameters) override;

  void OnConnectionStateChange(Transport* transport, const std::string& connectionState) override;

  std::future<std::string> OnProduce(SendTransport* transport, const std::string& kind, json rtpParameters, const json& appData) override;

  std::future<std::string> OnProduceData(SendTransport* transport, const json& sctpStreamParameters, const std::string& label, const std::string& protocol, const json& appData) override;

public:
  void SetJTransport(JNIEnv* env, const JavaRef<jobject>& j_transport) { j_transport_ = j_transport; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_transport_;
};

class OwnedSendTransport final : public OwnedTransport
{
public:
  OwnedSendTransport(SendTransport* transport, SendTransportListenerJni* listener) : transport_(transport), listener_(listener) {}

  ~OwnedSendTransport()
  {
    delete listener_;
    delete transport_;
  }

  Transport* transport() const override { return transport_; }
  SendTransport* sendTransport() const { return transport_; }

private:
  SendTransport* transport_;
  SendTransportListenerJni* listener_;
};

inline SendTransport* getSendTransport(jlong j_transport);

ScopedJavaLocalRef<jobject> NativeToJavaSendTransport(JNIEnv* env, SendTransport* transport, SendTransportListenerJni* listener);

} // namespace mediasoupclient

#endif // SEND_TRANSPORT_JNI_H_