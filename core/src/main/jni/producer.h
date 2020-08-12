#ifndef PRODUCER_H_
#define PRODUCER_H_

#include <jni.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Producer.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetId, jlong j_producer);

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetLocalId, jlong j_producer);

  JNI_DEFINE_METHOD(jboolean, Producer, nativeIsClosed, jlong j_producer);

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetKind, jlong j_producer);

  JNI_DEFINE_METHOD(jlong, Producer, nativeGetRtpSender, jlong j_producer);

  JNI_DEFINE_METHOD(jlong, Producer, nativeGetTrack, jlong j_producer);

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetRtpParameters, jlong j_producer);

  JNI_DEFINE_METHOD(jboolean, Producer, nativeIsPaused, jlong j_producer);

  JNI_DEFINE_METHOD(jint, Producer, nativeGetMaxSpatialLayer, jlong j_producer);

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetAppData, jlong j_producer);

  JNI_DEFINE_METHOD(void, Producer, nativeClose, jlong j_producer);

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetStats, jlong j_producer);

  JNI_DEFINE_METHOD(void, Producer, nativePause, jlong j_producer);

  JNI_DEFINE_METHOD(void, Producer, nativeResume, jlong j_producer);

  JNI_DEFINE_METHOD(void, Producer, nativeReplaceTrack, jlong j_producer, jlong j_track);

  JNI_DEFINE_METHOD(void, Producer, nativeSetMaxSpatialLayer, jlong j_producer, jint spatialLayer);

  JNI_DEFINE_METHOD(void, Producer, nativeDispose, jlong j_producer);
}

class ProducerListenerJni final : public Producer::Listener
{
public:
  ProducerListenerJni(JNIEnv *env, const JavaRef<jobject> &j_listener) : j_listener_(env, j_listener){};

  ~ProducerListenerJni() {}

  void OnTransportClose(Producer *native_producer) override;

public:
  void SetJProducer(JNIEnv *env, const JavaRef<jobject> &j_producer) { j_producer_ = j_producer; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_producer_;
};

class OwnedProducer
{
public:
  OwnedProducer(Producer *producer, ProducerListenerJni *listener) : producer_(producer), listener_(listener) {}

  ~OwnedProducer()
  {
    delete listener_;
    delete producer_;
  }

  Producer *producer() const { return producer_; }

private:
  Producer *producer_;
  ProducerListenerJni *listener_;
};

inline Producer *getProducer(jlong j_producer);

ScopedJavaLocalRef<jobject> NativeToJavaProducer(JNIEnv *env, Producer *producer, ProducerListenerJni *listener);

} // namespace mediasoupclient

#endif // PRODUCER_H_