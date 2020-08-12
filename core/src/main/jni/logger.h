#ifndef LOGGER_H_
#define LOGGER_H_

#include <jni.h>

#include "jni_common.h"
#include "jni_util.h"

namespace mediasoupclient
{

extern "C"
{

  JNI_DEFINE_METHOD(jlong, Logger, nativeSetHandler, jobject j_handler);

  JNI_DEFINE_METHOD(void, Logger, nativeSetLogLevel, jint j_level);

  JNI_DEFINE_METHOD(void, Consumer, nativeDispose, jlong j_handler);
}

} // namespace mediasoupclient

#endif // LOGGER_H_