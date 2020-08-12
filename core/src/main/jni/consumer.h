#ifndef CONSUMER_H_
#define CONSUMER_H_

#include <jni.h>

#include <Consumer.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetId, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetLocalId, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetProducerId, jlong j_consumer);

  JNI_DEFINE_METHOD(jboolean, Consumer, nativeIsClosed, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetKind, jlong j_consumer);

  JNI_DEFINE_METHOD(jlong, Consumer, nativeGetRtpReceiver, jlong j_consumer);

  JNI_DEFINE_METHOD(jlong, Consumer, nativeGetTrack, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetRtpParameters, jlong j_consumer);

  JNI_DEFINE_METHOD(jboolean, Consumer, nativeIsPaused, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetAppData, jlong j_consumer);

  JNI_DEFINE_METHOD(void, Consumer, nativeClose, jlong j_consumer);

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetStats, jlong j_consumer);

  JNI_DEFINE_METHOD(void, Consumer, nativePause, jlong j_consumer);

  JNI_DEFINE_METHOD(void, Consumer, nativeResume, jlong j_consumer);

  JNI_DEFINE_METHOD(void, Consumer, nativeDispose, jlong j_consumer);
}

class ConsumerListenerJni final : public Consumer::Listener
{
public:
  ConsumerListenerJni(JNIEnv *env, const JavaRef<jobject> &j_listener) : j_listener_(env, j_listener){};

  ~ConsumerListenerJni() {}

  void OnTransportClose(Consumer *) override;

public:
  void SetJConsumer(JNIEnv *env, const JavaRef<jobject> &j_consumer) { j_consumer_ = j_consumer; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_consumer_;
};

class OwnedConsumer
{
public:
  OwnedConsumer(Consumer *consumer, ConsumerListenerJni *listener) : consumer_(consumer), listener_(listener) {}

  ~OwnedConsumer()
  {
    delete listener_;
    delete consumer_;
  }

  Consumer *consumer() const { return consumer_; }

private:
  Consumer *consumer_;
  ConsumerListenerJni *listener_;
};

inline Consumer *getConsumer(jlong j_consumer);

ScopedJavaLocalRef<jobject> NativeToJavaConsumer(JNIEnv *env, Consumer *consumer, ConsumerListenerJni *listener);

} // namespace mediasoupclient

#endif // CONSUMER_H_