#ifndef JNI_UTIL_H_
#define JNI_UTIL_H_

#include <jni.h>
#include <sdk/android/native_api/jni/java_types.h>
#include <sdk/android/native_api/jni/scoped_java_ref.h>

using namespace webrtc;

namespace mediasoupclient
{

#define WITH_PACKAGE_NAME(className) "io/github/zncmn/mediasoup/" #className
#define CLASS_NAME_FOR_PARAMETER(className) "L" WITH_PACKAGE_NAME(className) ";"

void init(JNIEnv *env);

inline jclass findClass(JNIEnv *env, const std::string &name)
{
  jclass localRef = env->FindClass(name.c_str());
  CHECK_EXCEPTION(env) << "error during FindClass: " << name;
  return reinterpret_cast<jclass>(env->NewGlobalRef(localRef));
}

inline jmethodID findMethod(JNIEnv *env, jclass clazz, const std::string &name, const std::string &sig)
{
  jmethodID localRef = env->GetMethodID(clazz, name.c_str(), sig.c_str());
  CHECK_EXCEPTION(env) << "error during GetMethodID: " << name;
  return localRef;
}

inline int throwException(JNIEnv *env, const char *className, const char *message)
{
  ScopedJavaLocalRef<jclass> clazz(env, env->FindClass(className));
  CHECK_EXCEPTION(env) << "error during FindClass: " << className;
  auto localRef = clazz.obj();
  if (localRef == nullptr)
  {
    // Class not found
    return -1;
  }
  if (env->ThrowNew(localRef, message) != JNI_OK)
  {
    // an exception, most
    // likely OOM
    return -1;
  }
  return 0;
}

inline int throwNullPointerException(JNIEnv *env, const char *message)
{
  return throwException(env, "java/lang/NullPointerException", message);
}

inline int throwIllegalArgumentException(JNIEnv *env, const char *message)
{
  return throwException(env, "java/lang/IllegalArgumentException", message);
}

inline int throwIllegalStateException(JNIEnv *env, const char *message)
{
  return throwException(env, "java/lang/IllegalStateException", message);
}

inline int throwMediasoupException(JNIEnv *env, const char *message)
{
  return throwException(env, WITH_PACKAGE_NAME(MediasoupException), message);
}

template <typename F>
inline auto handleNativeCrash(JNIEnv *env, F f) noexcept -> absl::optional<decltype(f())>
{
  try
  {
    return f();
  }
  catch (std::exception &e)
  {
    throwMediasoupException(env, e.what());
  }
  catch (...)
  {
    throwMediasoupException(env, "Unidentified exception thrown (not derived from std::exception)");
  }
  return {};
}

template <typename F>
inline void handleNativeCrashNoReturn(JNIEnv *env, F f) noexcept
{
  try
  {
    f();
  }
  catch (std::exception &e)
  {
    throwMediasoupException(env, e.what());
  }
  catch (...)
  {
    throwMediasoupException(env, "Unidentified exception thrown (not derived from std::exception)");
  }
}

} // namespace mediasoupclient

#endif // JNI_UTIL_H_
