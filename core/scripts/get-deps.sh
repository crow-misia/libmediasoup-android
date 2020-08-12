#!/usr/bin/env bash

set -ue -o pipefail

export LC_ALL=C

CURDIR=$(cd $(dirname $0)/..; pwd)

. $CURDIR/../VERSIONS

# webrtc
rm -rf $CURDIR/deps/webrtc
mkdir -p $CURDIR/deps/webrtc
if [ ! -e $CURDIR/deps/webrtc.${WEBRTC_VERSION}.tar.xz ]; then
  curl -Lo $CURDIR/deps/webrtc.${WEBRTC_VERSION}.tar.xz https://github.com/crow-misia/libwebrtc-bin/releases/download/${WEBRTC_VERSION}/libwebrtc-android.tar.xz
fi
pushd $CURDIR/deps/webrtc
  tar xf $CURDIR/deps/webrtc.${WEBRTC_VERSION}.tar.xz
  rm -rf aar
  mv jar/*.jar lib/
  rm -rf jar
  rm -rf include/base/ios
  rm -rf include/base/mac
  rm -rf include/base/test
  rm -rf include/base/third_party
  rm -rf include/base/win
  rm -rf include/build
  rm -rf include/buildtools
  rm -rf include/call/test
  rm -rf include/examples
  rm -rf include/modules/audio_coding
  rm -rf include/modules/audio_device/android
  rm -rf include/modules/audio_device/dummy
  rm -rf include/modules/audio_device/linux
  rm -rf include/modules/audio_device/mac
  rm -rf include/modules/audio_device/win
  rm -rf include/modules/audio_mixer
  rm -rf include/modules/congestion_controller
  rm -rf include/modules/desktop_capture
  rm -rf include/modules/pacing
  rm -rf include/modules/remote_bitrate_estimator
  rm -rf include/modules/third_party
  rm -rf include/modules/utility
  rm -rf include/modules/video_capture
  rm -rf include/modules/video_processing
  rm -rf include/rtc_base/test*
  rm -rf include/rtc_tools
  rm -rf include/sdk/android/native_unittests
  rm -rf include/sdk/objc
  rm -rf include/stats
  rm -rf include/test
  rm -rf include/testing
  rm -rf include/tools
  rm -rf include/third_party/a[c-z]*
  rm -rf include/third_party/[b-z]*
popd

# libmediasoupclient
rm -rf $CURDIR/deps/libmediasoupclient
if [ ! -e $CURDIR/deps/libmediasoupclient.${LIBMEDIASOUPCLIENT_VERSION}.tar.gz ]; then
  curl -Lo $CURDIR/deps/libmediasoupclient.${LIBMEDIASOUPCLIENT_VERSION}.tar.gz https://github.com/versatica/libmediasoupclient/archive/${LIBMEDIASOUPCLIENT_VERSION}.tar.gz
fi
pushd $CURDIR/deps
  tar xf $CURDIR/deps/libmediasoupclient.${LIBMEDIASOUPCLIENT_VERSION}.tar.gz
  mv libmediasoupclient-${LIBMEDIASOUPCLIENT_VERSION} libmediasoupclient
popd
pushd $CURDIR/deps/libmediasoupclient
  patch -u -p1 < $CURDIR/scripts/libsdptransform_disable_test.patch
  patch -u -p1 < $CURDIR/scripts/libmediasoupclient_add_virtual_deconstructor.patch
  patch -u -p1 < $CURDIR/scripts/libmediasoupclient_whole_archive.patch
popd

# JSON for Modern C++
rm -f $CURDIR/deps/libmediasoupclient/deps/libsdptransform/include/json.hpp
if [ ! -e $CURDIR/deps/json-${JSON_FOR_MODERN_CPP_VERSION}.hpp ]; then
  curl -Lo $CURDIR/deps/json-${JSON_FOR_MODERN_CPP_VERSION}.hpp https://github.com/nlohmann/json/releases/download/v${JSON_FOR_MODERN_CPP_VERSION}/json.hpp
fi
cp $CURDIR/deps/json-${JSON_FOR_MODERN_CPP_VERSION}.hpp $CURDIR/deps/libmediasoupclient/deps/libsdptransform/include/json.hpp
