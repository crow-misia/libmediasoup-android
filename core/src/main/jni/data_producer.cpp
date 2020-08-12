#define MSC_CLASS "data_producer"

#include "data_producer.h"

#include <api/data_channel_interface.h>
#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <DataProducer.hpp>
#include <Logger.hpp>

using namespace webrtc;

namespace mediasoupclient
{

extern jclass dataProducerClass;
extern jmethodID dataProducerConstructorMethod;

extern jmethodID dataProducerListenerOnOpenMethod;
extern jmethodID dataProducerListenerOnCloseMethod;
extern jmethodID dataProducerListenerOnBufferedAmountChangeMethod;
extern jmethodID dataProducerListenerOnTransportCloseMethod;

extern "C"
{

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetId, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetLocalId, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetLocalId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetSctpStreamParameters, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetSctpStreamParameters();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jint, DataProducer, nativeGetReadyState, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetReadyState();
                               return static_cast<jint>(result);
                             })
      .value_or(0);
  }

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetLabel, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetLabel();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetProtocol, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetProtocol();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jlong, DataProducer, nativeGetBufferedAmount, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetBufferedAmount();
                               return static_cast<jlong>(result);
                             })
      .value_or(0L);
  }

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetAppData, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->GetAppData();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, DataProducer, nativeIsClosed, jlong j_dataProducer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataProducer(j_dataProducer)->IsClosed();
                               return static_cast<jboolean>(result);
                             })
      .value_or(true);
  }

  JNI_DEFINE_METHOD(void, DataProducer, nativeClose, jlong j_dataProducer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getDataProducer(j_dataProducer)->Close(); });
  }

  JNI_DEFINE_METHOD(void, DataProducer, nativeSend, jlong j_dataProducer, jbyteArray j_buffer, jboolean j_binary)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() {
      auto buffer = JavaToNativeByteArray(env, JavaParamRef<jbyteArray>(env, j_buffer));
      getDataProducer(j_dataProducer)->Send(DataBuffer(rtc::CopyOnWriteBuffer(buffer.data(), buffer.size()), j_binary));
    });
  }

  JNI_DEFINE_METHOD(void, DataProducer, nativeDispose, jlong j_dataProducer)
  {
    MSC_TRACE();

    delete reinterpret_cast<OwnedDataProducer*>(j_dataProducer);
  }
}

void DataProducerListenerJni::OnOpen(DataProducer*)
{
  MSC_TRACE();

  JNIEnv* env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataProducerListenerOnOpenMethod, j_dataProducer_.obj());
}

void DataProducerListenerJni::OnClose(DataProducer*)
{
  MSC_TRACE();

  JNIEnv* env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataProducerListenerOnCloseMethod, j_dataProducer_.obj());
}

void DataProducerListenerJni::OnBufferedAmountChange(DataProducer*, uint64_t sentDataSize)
{
  MSC_TRACE();

  JNIEnv* env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataProducerListenerOnBufferedAmountChangeMethod, j_dataProducer_.obj(), sentDataSize);
}

void DataProducerListenerJni::OnTransportClose(DataProducer*)
{
  MSC_TRACE();

  JNIEnv* env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataProducerListenerOnTransportCloseMethod, j_dataProducer_.obj());
}

inline DataProducer* getDataProducer(jlong j_dataProducer)
{
  return reinterpret_cast<OwnedDataProducer*>(j_dataProducer)->dataProducer();
}

ScopedJavaLocalRef<jobject> NativeToJavaDataProducer(JNIEnv* env, DataProducer* dataProducer, DataProducerListenerJni* listener)
{
  MSC_TRACE();

  auto ownedDataProducer = new OwnedDataProducer(dataProducer, listener);
  auto j_dataProducer = ScopedJavaLocalRef<jobject>(env, env->NewObject(dataProducerClass, dataProducerConstructorMethod, NativeToJavaPointer(ownedDataProducer)));
  listener->SetJDataProducer(env, j_dataProducer);
  return j_dataProducer;
}

} // namespace mediasoupclient