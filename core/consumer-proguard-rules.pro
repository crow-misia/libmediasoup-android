-keep class org.webrtc.** {
  @**.CalledByNative <init>(...);
  @**.CalledByNative <methods>;
  @**.CalledByNativeUnchecked <init>(...);
  @**.CalledByNativeUnchecked <methods>;
  native <methods>;
}

-keepclasseswithmembers public enum org.webrtc.**$* {
  **[] $VALUES;
  public *;
}

-keepclasseswithmembers class org.webrtc.voiceengine.BuildInfo { *; }
-keep class org.webrtc.voiceengine.WebRtcAudioManager {
  <init>(...);
  <methods>;
}
-keep class org.webrtc.voiceengine.WebRtcAudioTrack {
  <init>(...);
  <methods>;
}
-keep class org.webrtc.voiceengine.WebRtcAudioRecord {
  <init>(...);
  <methods>;
}

-keep class io.github.zncmn.mediasoup.** {
  @**.CalledByNative <init>(...);
  @**.CalledByNative <methods>;
  native <methods>;
}
