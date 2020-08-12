#include <jni.h>
#include <sdk/android/native_api/base/init.h>
#include <sdk/android/src/jni/jvm.h>

#include <mediasoupclient.hpp>

#include "jni_common.h"
#include "jni_util.h"

extern "C"
{

  JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *)
  {
    webrtc::InitAndroid(jvm);

    JNIEnv *env = webrtc::jni::GetEnv();

    mediasoupclient::init(env);

    mediasoupclient::Initialize();

    return JNI_VERSION;
  }

  extern "C" void JNIEXPORT JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) { mediasoupclient::Cleanup(); }
}