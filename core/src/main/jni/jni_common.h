#ifndef JNI_COMMON_H_
#define JNI_COMMON_H_

#include <jni.h>

#include <json.hpp>

#define JNI_VERSION JNI_VERSION_1_6

#define JNI_METHOD_NAME(className, methodName) Java_io_github_zncmn_mediasoup_##className##_##methodName

#define JNI_DEFINE_METHOD(type, className, methodName, args...) JNIEXPORT type JNICALL JNI_METHOD_NAME(className, methodName)(JNIEnv * env, jobject, ##args)

namespace mediasoupclient
{
using nlohmann::json;
} // namespace mediasoupclient

#endif // JNI_COMMON_H_
