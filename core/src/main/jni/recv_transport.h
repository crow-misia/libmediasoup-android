#ifndef RECV_TRANSPORT_JNI_H_
#define RECV_TRANSPORT_JNI_H_

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
  JNI_DEFINE_METHOD(jobject, RecvTransport, nativeConsume, jlong j_transport, jobject j_listener, jstring j_id, jstring j_producerId, jstring j_kind, jstring j_rtpParameters, jstring j_appData);
  JNI_DEFINE_METHOD(jobject, RecvTransport, nativeConsumeData, jlong j_transport, jobject j_listener, jstring j_id, jstring j_producerId, jstring j_label, jstring j_protocol, jstring j_appData);
}

extern jclass transportListenerClass;

class RecvTransportListenerJni final : public RecvTransport::Listener
{
public:
  RecvTransportListenerJni(JNIEnv* env, const JavaRef<jobject>& j_listener) : j_listener_(env, j_listener) {}

  ~RecvTransportListenerJni() {}

  std::future<void> OnConnect(Transport* transport, const json& dtlsParameters) override;

  void OnConnectionStateChange(Transport* transport, const std::string& connectionState) override;

public:
  void SetJTransport(JNIEnv* env, const JavaRef<jobject>& j_transport) { j_transport_ = j_transport; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_transport_;
};

class OwnedRecvTransport final : public OwnedTransport
{
public:
  OwnedRecvTransport(RecvTransport* transport, RecvTransportListenerJni* listener) : transport_(transport), listener_(listener) {}

  ~OwnedRecvTransport() override
  {
    delete listener_;
    delete transport_;
  }

  Transport* transport() const override { return transport_; }
  RecvTransport* recvTransport() const { return transport_; }

private:
  RecvTransport* transport_;
  RecvTransportListenerJni* listener_;
};

inline RecvTransport* getRecvTransport(jlong j_transport);

ScopedJavaLocalRef<jobject> NativeToJavaRecvTransport(JNIEnv* env, RecvTransport* transport, RecvTransportListenerJni* listener);

} // namespace mediasoupclient

#endif // RECV_TRANSPORT_JNI_H_