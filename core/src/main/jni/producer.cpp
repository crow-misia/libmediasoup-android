#define MSC_CLASS "producer"

#include "producer.h"

#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Logger.hpp>
#include <Producer.hpp>

using namespace webrtc;

namespace mediasoupclient
{

extern jclass producerClass;
extern jmethodID producerConstructorMethod;

extern jmethodID producerListenerOnTransportCloseMethod;

extern "C"
{

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetId, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetLocalId, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetLocalId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, Producer, nativeIsClosed, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->IsClosed();
                               return static_cast<jboolean>(result);
                             })
      .value_or(true);
  }

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetKind, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetKind();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jlong, Producer, nativeGetRtpSender, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetRtpSender();
                               return NativeToJavaPointer(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(jlong, Producer, nativeGetTrack, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetTrack();
                               return NativeToJavaPointer(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetRtpParameters, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetRtpParameters();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, Producer, nativeIsPaused, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->IsPaused();
                               return static_cast<jboolean>(result);
                             })
      .value_or(false);
  }

  JNI_DEFINE_METHOD(jint, Producer, nativeGetMaxSpatialLayer, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetMaxSpatialLayer();
                               return static_cast<jint>(result);
                             })
      .value_or(0);
  }

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetAppData, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetAppData();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Producer, nativeClose, jlong j_producer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getProducer(j_producer)->Close(); });
  }

  JNI_DEFINE_METHOD(jstring, Producer, nativeGetStats, jlong j_producer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getProducer(j_producer)->GetStats();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Producer, nativePause, jlong j_producer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getProducer(j_producer)->Pause(); });
  }

  JNI_DEFINE_METHOD(void, Producer, nativeResume, jlong j_producer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getProducer(j_producer)->Resume(); });
  }

  JNI_DEFINE_METHOD(void, Producer, nativeReplaceTrack, jlong j_producer, jlong j_track)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() {
      auto track = reinterpret_cast<webrtc::MediaStreamTrackInterface *>(j_track);
      getProducer(j_producer)->ReplaceTrack(track);
    });
  }

  JNI_DEFINE_METHOD(void, Producer, nativeSetMaxSpatialLayer, jlong j_producer, jint j_spatialLayer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getProducer(j_producer)->SetMaxSpatialLayer(j_spatialLayer); });
  }

  JNI_DEFINE_METHOD(void, Producer, nativeDispose, jlong j_producer)
  {
    MSC_TRACE();

    delete reinterpret_cast<OwnedProducer *>(j_producer);
  }
}

void ProducerListenerJni::OnTransportClose(Producer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), producerListenerOnTransportCloseMethod, j_producer_.obj());
}

inline Producer *getProducer(jlong j_producer)
{
  return reinterpret_cast<OwnedProducer *>(j_producer)->producer();
}

ScopedJavaLocalRef<jobject> NativeToJavaProducer(JNIEnv *env, Producer *producer, ProducerListenerJni *listener)
{
  MSC_TRACE();

  auto ownedProducer = new OwnedProducer(producer, listener);
  auto j_producer = ScopedJavaLocalRef<jobject>(env, env->NewObject(producerClass, producerConstructorMethod, NativeToJavaPointer(ownedProducer)));
  listener->SetJProducer(env, j_producer);
  return j_producer;
}

} // namespace mediasoupclient