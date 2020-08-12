#define MSC_CLASS "logger"

#include "logger.h"

#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

#include <Logger.hpp>

#define TAG "mediasoupclient-jni"

using namespace webrtc;

using Logger = mediasoupclient::Logger;

namespace mediasoupclient
{

extern jmethodID loggerOnLogMethod;

class LogHandlerInterfaceJNI : public Logger::LogHandlerInterface
{
public:
  LogHandlerInterfaceJNI(JNIEnv* env, const JavaParamRef<jobject>& j_handler_interface) : j_handler_interface_(env, j_handler_interface), jni_tag_(NativeToJavaString(env, TAG)) {}

  void OnLog(Logger::LogLevel level, char* payload, size_t len) override
  {
    std::string message(payload, len);
    auto env = webrtc::AttachCurrentThreadIfNeeded();
    auto j_message = NativeToJavaString(env, message);
    env->CallVoidMethod(j_handler_interface_.obj(), loggerOnLogMethod, level, jni_tag_.obj(), j_message.obj());
  }

private:
  const ScopedJavaGlobalRef<jobject> j_handler_interface_;
  const ScopedJavaGlobalRef<jstring> jni_tag_;
};

extern "C"
{

  JNI_DEFINE_METHOD(jlong, Logger, nativeSetHandler, jobject j_handler)
  {
    MSC_TRACE();

    auto* handler = new LogHandlerInterfaceJNI(env, JavaParamRef<jobject>(env, j_handler));
    Logger::SetHandler(reinterpret_cast<Logger::LogHandlerInterface*>(handler));
    return NativeToJavaPointer(handler);
  }

  JNI_DEFINE_METHOD(void, Logger, nativeSetLogLevel, jint j_level)
  {
    MSC_TRACE();

    Logger::SetLogLevel(static_cast<Logger::LogLevel>(j_level));
  }

  JNI_DEFINE_METHOD(void, Logger, nativeDispose, jlong j_handler)
  {
    MSC_TRACE();

    delete reinterpret_cast<LogHandlerInterfaceJNI*>(j_handler);
  }
}

} // namespace mediasoupclient