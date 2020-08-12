#define MSC_CLASS "data_consumer"

#include "data_consumer.h"

#include <api/data_channel_interface.h>
#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>
#include <sdk/android/src/jni/jni_helpers.h>

#include <DataConsumer.hpp>
#include <Logger.hpp>

using namespace webrtc;

namespace mediasoupclient
{

extern jclass dataConsumerClass;
extern jclass bufferClass;
extern jmethodID bufferConstructorMethod;
extern jmethodID dataConsumerConstructorMethod;

extern jmethodID dataConsumerListenerOnConnectingMethod;
extern jmethodID dataConsumerListenerOnOpenMethod;
extern jmethodID dataConsumerListenerOnClosingMethod;
extern jmethodID dataConsumerListenerOnCloseMethod;
extern jmethodID dataConsumerListenerOnMessageMethod;
extern jmethodID dataConsumerListenerOnTransportCloseMethod;

extern "C"
{

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetId, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetLocalId, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetLocalId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetDataProducerId, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetDataProducerId();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetSctpStreamParameters, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetSctpStreamParameters();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jint, DataConsumer, nativeGetReadyState, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetReadyState();
                               return static_cast<jint>(result);
                             })
      .value_or(0);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetLabel, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetLabel();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetProtocol, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetProtocol();
                               return NativeToJavaString(env, result).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetAppData, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->GetAppData();
                               return NativeToJavaString(env, result.dump()).Release();
                             })
      .value_or(nullptr);
  }

  JNI_DEFINE_METHOD(jboolean, DataConsumer, nativeIsClosed, jlong j_dataConsumer)
  {
    MSC_TRACE();

    return handleNativeCrash(env,
                             [&]() {
                               auto result = getDataConsumer(j_dataConsumer)->IsClosed();
                               return static_cast<jboolean>(result);
                             })
      .value_or(true);
  }

  JNI_DEFINE_METHOD(void, DataConsumer, nativeClose, jlong j_dataConsumer)
  {
    MSC_TRACE();

    handleNativeCrashNoReturn(env, [&]() { getDataConsumer(j_dataConsumer)->Close(); });
  }

  JNI_DEFINE_METHOD(void, DataConsumer, nativeDispose, jlong j_dataConsumer)
  {
    MSC_TRACE();

    delete reinterpret_cast<OwnedDataConsumer *>(j_dataConsumer);
  }
}

void DataConsumerListenerJni::OnConnecting(DataConsumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnConnectingMethod, j_dataConsumer_.obj());
}

void DataConsumerListenerJni::OnOpen(DataConsumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnOpenMethod, j_dataConsumer_.obj());
}

void DataConsumerListenerJni::OnClosing(DataConsumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnClosingMethod, j_dataConsumer_.obj());
}

void DataConsumerListenerJni::OnClose(DataConsumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnCloseMethod, j_dataConsumer_.obj());
}

void DataConsumerListenerJni::OnMessage(DataConsumer *, const DataBuffer &buffer)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  auto byte_buffer = jni::NewDirectByteBuffer(env, const_cast<char *>(buffer.data.data<char>()), buffer.data.size());
  auto j_buffer = ScopedJavaLocalRef<jobject>(env, env->NewObject(bufferClass, bufferConstructorMethod, byte_buffer.obj(), buffer.binary));

  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnMessageMethod, j_dataConsumer_.obj(), j_buffer.obj());
}

void DataConsumerListenerJni::OnTransportClose(DataConsumer *)
{
  MSC_TRACE();

  JNIEnv *env = AttachCurrentThreadIfNeeded();
  env->CallVoidMethod(j_listener_.obj(), dataConsumerListenerOnTransportCloseMethod, j_dataConsumer_.obj());
}

inline DataConsumer *getDataConsumer(jlong j_dataConsumer)
{
  return reinterpret_cast<OwnedDataConsumer *>(j_dataConsumer)->dataConsumer();
}

ScopedJavaLocalRef<jobject> NativeToJavaDataConsumer(JNIEnv *env, DataConsumer *dataConsumer, DataConsumerListenerJni *listener)
{
  MSC_TRACE();

  auto ownedDataConsumer = new OwnedDataConsumer(dataConsumer, listener);
  auto j_dataConsumer = ScopedJavaLocalRef<jobject>(env, env->NewObject(dataConsumerClass, dataConsumerConstructorMethod, NativeToJavaPointer(ownedDataConsumer)));
  listener->SetJDataConsumer(env, j_dataConsumer);
  return j_dataConsumer;
}

} // namespace mediasoupclient