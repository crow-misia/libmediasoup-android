#ifndef TRANSPORT_H_
#define TRANSPORT_H_

#include <jni.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Transport.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{
extern "C"
{

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetId, jlong j_transport);

  JNI_DEFINE_METHOD(jboolean, Transport, nativeIsClosed, jlong j_transport);

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetConnectionState, jlong j_transport);

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetAppData, jlong j_transport);

  JNI_DEFINE_METHOD(void, Transport, nativeClose, jlong j_transport);

  JNI_DEFINE_METHOD(jstring, Transport, nativeGetStats, jlong j_transport);

  JNI_DEFINE_METHOD(void, Transport, nativeRestartIce, jlong j_transport, jstring j_iceParameters);

  JNI_DEFINE_METHOD(void, Transport, nativeUpdateIceServers, jlong j_transport, jstring j_iceServers);
}

class OwnedTransport
{
public:
  virtual ~OwnedTransport() = default;
  virtual Transport* transport() const = 0;
};

inline Transport* getTransport(jlong j_transport);

} // namespace mediasoupclient

#endif // TRANSPORT_H_