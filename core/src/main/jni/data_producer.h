#ifndef DATA_PRODUCER_H_
#define DATA_PRODUCER_H_

#include <jni.h>

#include <DataProducer.hpp>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetId, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetLocalId, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetSctpStreamParameters, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jint, DataProducer, nativeGetReadyState, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetLabel, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetProtocol, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jlong, DataProducer, nativeGetBufferedAmount, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jstring, DataProducer, nativeGetAppData, jlong j_dataProducer);

  JNI_DEFINE_METHOD(jboolean, DataProducer, nativeIsClosed, jlong j_dataProducer);

  JNI_DEFINE_METHOD(void, DataProducer, nativeClose, jlong j_dataProducer);

  JNI_DEFINE_METHOD(void, DataProducer, nativeSend, jlong j_dataProducer, jbyteArray j_buffer, jboolean j_binary);

  JNI_DEFINE_METHOD(void, DataProducer, nativeDispose, jlong j_dataProducer);
}

class DataProducerListenerJni final : public DataProducer::Listener
{
public:
  DataProducerListenerJni(JNIEnv* env, const JavaRef<jobject>& j_listener) : j_listener_(env, j_listener){};

  ~DataProducerListenerJni() {}

  void OnOpen(DataProducer*) override;
  void OnClose(DataProducer*) override;
  void OnBufferedAmountChange(DataProducer*, uint64_t) override;
  void OnTransportClose(DataProducer*) override;

public:
  void SetJDataProducer(JNIEnv* env, const JavaRef<jobject>& j_dataProducer) { j_dataProducer_ = j_dataProducer; }

private:
  const ScopedJavaGlobalRef<jobject> j_listener_;
  ScopedJavaGlobalRef<jobject> j_dataProducer_;
};

class OwnedDataProducer
{
public:
  OwnedDataProducer(DataProducer* dataProducer, DataProducerListenerJni* listener) : dataProducer_(dataProducer), listener_(listener) {}

  ~OwnedDataProducer()
  {
    delete listener_;
    delete dataProducer_;
  }

  DataProducer* dataProducer() const { return dataProducer_; }

private:
  DataProducer* dataProducer_;
  DataProducerListenerJni* listener_;
};

inline DataProducer* getDataProducer(jlong j_dataProducer);

ScopedJavaLocalRef<jobject> NativeToJavaDataProducer(JNIEnv* env, DataProducer* dataProducer, DataProducerListenerJni* listener);

} // namespace mediasoupclient

#endif // DATA_PRODUCER_H_