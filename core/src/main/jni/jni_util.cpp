#include "jni_util.h"

#include <jni.h>

#include <cstddef>

namespace mediasoupclient
{

jclass bufferClass;
jclass consumerClass;
jclass dataConsumerClass;
jclass dataProducerClass;
jclass logHandlerInterfaceClass;
jclass producerClass;
jclass recvTransportClass;
jclass sendTransportClass;
jclass consumerListenerClass;
jclass dataConsumerListenerClass;
jclass dataProducerListenerClass;
jclass producerListenerClass;
jclass sendTransportListenerClass;
jclass transportListenerClass;

jmethodID bufferConstructorMethod;
jmethodID consumerConstructorMethod;
jmethodID dataConsumerConstructorMethod;
jmethodID dataProducerConstructorMethod;
jmethodID producerConstructorMethod;
jmethodID recvTransportConstructorMethod;
jmethodID sendTransportConstructorMethod;

jmethodID consumerListenerOnTransportCloseMethod;
jmethodID dataConsumerListenerOnConnectingMethod;
jmethodID dataConsumerListenerOnOpenMethod;
jmethodID dataConsumerListenerOnClosingMethod;
jmethodID dataConsumerListenerOnCloseMethod;
jmethodID dataConsumerListenerOnMessageMethod;
jmethodID dataConsumerListenerOnTransportCloseMethod;
jmethodID dataProducerListenerOnOpenMethod;
jmethodID dataProducerListenerOnCloseMethod;
jmethodID dataProducerListenerOnBufferedAmountChangeMethod;
jmethodID dataProducerListenerOnTransportCloseMethod;

jmethodID producerListenerOnTransportCloseMethod;

jmethodID transportListenerOnConnectMethod;
jmethodID transportListenerOnConnectionStateChangeMethod;

jmethodID sendTransportListenerOnProduceMethod;
jmethodID sendTransportListenerOnProduceDataMethod;

jmethodID loggerOnLogMethod;

void init(JNIEnv* env)
{
  // class
  bufferClass = findClass(env, "org/webrtc/DataChannel$Buffer");
  consumerClass = findClass(env, WITH_PACKAGE_NAME(Consumer));
  dataConsumerClass = findClass(env, WITH_PACKAGE_NAME(DataConsumer));
  dataProducerClass = findClass(env, WITH_PACKAGE_NAME(DataProducer));
  logHandlerInterfaceClass = findClass(env, WITH_PACKAGE_NAME(Logger$LogHandlerInterface));
  producerClass = findClass(env, WITH_PACKAGE_NAME(Producer));
  recvTransportClass = findClass(env, WITH_PACKAGE_NAME(RecvTransport));
  sendTransportClass = findClass(env, WITH_PACKAGE_NAME(SendTransport));
  consumerListenerClass = findClass(env, WITH_PACKAGE_NAME(Consumer$Listener));
  dataConsumerListenerClass = findClass(env, WITH_PACKAGE_NAME(DataConsumer$Listener));
  dataProducerListenerClass = findClass(env, WITH_PACKAGE_NAME(DataProducer$Listener));
  producerListenerClass = findClass(env, WITH_PACKAGE_NAME(Producer$Listener));
  sendTransportListenerClass = findClass(env, WITH_PACKAGE_NAME(SendTransport$Listener));
  transportListenerClass = findClass(env, WITH_PACKAGE_NAME(Transport$Listener));

  // constructor
  bufferConstructorMethod = findMethod(env, bufferClass, "<init>", "(Ljava/nio/ByteBuffer;Z)V");
  consumerConstructorMethod = findMethod(env, consumerClass, "<init>", "(J)V");
  dataConsumerConstructorMethod = findMethod(env, dataConsumerClass, "<init>", "(J)V");
  dataProducerConstructorMethod = findMethod(env, dataProducerClass, "<init>", "(J)V");
  producerConstructorMethod = findMethod(env, producerClass, "<init>", "(J)V");
  recvTransportConstructorMethod = findMethod(env, recvTransportClass, "<init>", "(J)V");
  sendTransportConstructorMethod = findMethod(env, sendTransportClass, "<init>", "(J)V");

  // consumer listener
  consumerListenerOnTransportCloseMethod = findMethod(env, consumerListenerClass, "onTransportClose", "(" CLASS_NAME_FOR_PARAMETER(Consumer) ")V");

  // data consumer listener
  dataConsumerListenerOnConnectingMethod = findMethod(env, dataConsumerListenerClass, "onConnecting", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) ")V");
  dataConsumerListenerOnOpenMethod = findMethod(env, dataConsumerListenerClass, "onOpen", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) ")V");
  dataConsumerListenerOnClosingMethod = findMethod(env, dataConsumerListenerClass, "onClosing", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) ")V");
  dataConsumerListenerOnCloseMethod = findMethod(env, dataConsumerListenerClass, "onClose", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) ")V");
  dataConsumerListenerOnMessageMethod = findMethod(env, dataConsumerListenerClass, "onMessage", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) "Lorg/webrtc/DataChannel$Buffer;)V");
  dataConsumerListenerOnTransportCloseMethod = findMethod(env, dataConsumerListenerClass, "onTransportClose", "(" CLASS_NAME_FOR_PARAMETER(DataConsumer) ")V");

  // data producer listener
  dataProducerListenerOnOpenMethod = findMethod(env, dataProducerListenerClass, "onOpen", "(" CLASS_NAME_FOR_PARAMETER(DataProducer) ")V");
  dataProducerListenerOnCloseMethod = findMethod(env, dataProducerListenerClass, "onClose", "(" CLASS_NAME_FOR_PARAMETER(DataProducer) ")V");
  dataProducerListenerOnBufferedAmountChangeMethod = findMethod(env, dataProducerListenerClass, "onBufferedAmountChange", "(" CLASS_NAME_FOR_PARAMETER(DataProducer) "J)V");
  dataProducerListenerOnTransportCloseMethod = findMethod(env, dataProducerListenerClass, "onTransportClose", "(" CLASS_NAME_FOR_PARAMETER(DataProducer) ")V");

  // producer listener
  producerListenerOnTransportCloseMethod = findMethod(env, producerListenerClass, "onTransportClose", "(" CLASS_NAME_FOR_PARAMETER(Producer) ")V");

  // transport
  transportListenerOnConnectMethod = findMethod(env, transportListenerClass, "onConnect", "(" CLASS_NAME_FOR_PARAMETER(Transport) "Ljava/lang/String;)V");
  transportListenerOnConnectionStateChangeMethod = findMethod(env, transportListenerClass, "onConnectionStateChange", "(" CLASS_NAME_FOR_PARAMETER(Transport) "Ljava/lang/String;)V");

  // send transport
  sendTransportListenerOnProduceMethod =
    findMethod(env, sendTransportListenerClass, "onProduce", "(" CLASS_NAME_FOR_PARAMETER(Transport) "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
  sendTransportListenerOnProduceDataMethod =
    findMethod(env, sendTransportListenerClass, "onProduceData", "(" CLASS_NAME_FOR_PARAMETER(Transport) "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

  // logger
  loggerOnLogMethod = findMethod(env, logHandlerInterfaceClass, "onLog", "(ILjava/lang/String;Ljava/lang/String;)V");
}

} // namespace mediasoupclient
