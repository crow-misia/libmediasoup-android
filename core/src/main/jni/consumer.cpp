#define MSC_CLASS "consumer"

#include "consumer.h"

#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Consumer.hpp>
#include <Logger.hpp>

using namespace webrtc;

namespace mediasoupclient
{

extern jclass consumerClass;
extern jmethodID consumerConstructorMethod;
extern jmethodID consumerListenerOnTransportCloseMethod;

extern "C"
{

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetId, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetLocalId, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetLocalId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetProducerId, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetProducerId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, Consumer, nativeIsClosed, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->IsClosed();
                               return static_cast<jboolean>(result);
                             })
      .value_or(true);
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetKind, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetKind();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jlong, Consumer, nativeGetRtpReceiver, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetRtpReceiver();
                               return NativeToJavaPointer(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(jlong, Consumer, nativeGetTrack, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetTrack();
                               return NativeToJavaPointer(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetRtpParameters, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetRtpParameters();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, Consumer, nativeIsPaused, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->IsPaused();
                               return static_cast<jboolean>(result);
                             })
      .value_or(false);
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetAppData, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetAppData();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Consumer, nativeClose, jlong j_consumer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getConsumer(j_consumer)->Close(); });
  }

  JNI_DEFINE_METHOD(jstring, Consumer, nativeGetStats, jlong j_consumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getConsumer(j_consumer)->GetStats();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(void, Consumer, nativePause, jlong j_consumer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getConsumer(j_consumer)->Pause(); });
  }

  JNI_DEFINE_METHOD(void, Consumer, nativeResume, jlong j_consumer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getConsumer(j_consumer)->Resume(); });
  }

  JNI_DEFINE_METHOD(void, Consumer, nativeDispose, jlong j_consumer)
  {
    MSC_TRACE();

    delete reinterpret_cast<OwnedConsumer *>(j_consumer);
  }
}

void ConsumerListenerJni::OnTransportClose(Consumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), consumerListenerOnTransportCloseMethod, j_consumer_.obj());
}

inline Consumer *getConsumer(jlong j_consumer)
{
  return reinterpret_cast<OwnedConsumer *>(j_consumer)->consumer();
}

ScopedJavaLocalRef<jobject> NativeToJavaConsumer(JNIEnv *env, Consumer *consumer, ConsumerListenerJni *listener)
{
  MSC_TRACE();

  auto ownedConsumer = new OwnedConsumer(consumer, listener);
  auto j_consumer = ScopedJavaLocalRef<jobject>(env, env->NewObject(consumerClass, consumerConstructorMethod, NativeToJavaPointer(ownedConsumer)));
  listener->SetJConsumer(env, j_consumer);
  return j_consumer;
}

} // namespace mediasoupclient