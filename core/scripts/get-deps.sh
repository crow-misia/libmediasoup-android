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
  mv jar/*.jar lib/
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
  patch -u -p1 < $CURDIR/scripts/libmediasoupclient_whole_archive.patch
  patch -u -p1 < $CURDIR/scripts/libmediasoupclient_cpp17.patch
  patch -u -p1 < $CURDIR/scripts/libmediasoupclient_m100.patch
popd
