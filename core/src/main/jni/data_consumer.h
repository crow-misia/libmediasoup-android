#ifndef DATA_CONSUMER_H_
#define DATA_CONSUMER_H_

#include <jni.h>

#include <DataConsumer.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetId, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetLocalId, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetDataProducerId, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetSctpStreamParameters, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jint, DataConsumer, nativeGetReadyState, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetLabel, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetProtocol, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jstring, DataConsumer, nativeGetAppData, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(jboolean, DataConsumer, nativeIsClosed, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(void, DataConsumer, nativeClose, jlong j_dataConsumer);

  JNI_DEFINE_METHOD(void, DataConsumer, nativeDispose, jlong j_dataConsumer);
}

class DataConsumerListenerJni final : public DataConsumer::Listener
{
public:
  DataConsumerListenerJni(JNIEnv* env, const JavaRef<jobject>& j_listener) : j_listener_(env, j_listener){};

  ~DataConsumerListenerJni() {}

  void OnConnecting(DataConsumer*) override;
  void OnOpen(DataConsumer*) override;
  void OnClosing(DataConsumer*) override;
  void OnClose(DataConsumer*) override;
  void OnMessage(DataConsumer*, const webrtc::DataBuffer&) override;
  void OnTransportClose(DataConsumer*) override;

public:
  void SetJDataConsumer(JNIEnv* env, const JavaRef<jobject>& j_data_consumer) { j_dataConsumer_ = j_data_consumer; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_dataConsumer_;
};

class OwnedDataConsumer
{
public:
  OwnedDataConsumer(DataConsumer* dataConsumer, DataConsumerListenerJni* listener) : dataConsumer_(dataConsumer), listener_(listener) {}

  ~OwnedDataConsumer()
  {
    delete listener_;
    delete dataConsumer_;
  }

  DataConsumer* dataConsumer() const { return dataConsumer_; }

private:
  DataConsumer* dataConsumer_;
  DataConsumerListenerJni* listener_;
};

inline DataConsumer* getDataConsumer(jlong j_dataConsumer_);

ScopedJavaLocalRef<jobject> NativeToJavaDataConsumer(JNIEnv* env, DataConsumer* dataConsumer, DataConsumerListenerJni* listener);

} // namespace mediasoupclient

#endif // DATA_CONSUMER_H_