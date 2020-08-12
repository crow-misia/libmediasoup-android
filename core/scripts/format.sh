#!/usr/bin/env bash

set -ue -o pipefail

export LC_ALL=C

CURDIR=$(cd $(dirname $0)/..; pwd)

pushd ${CURDIR}

# Run clang-format -i on 'include' and 'src' folders.
for dir in "src/main/jni" "src/androidTest/jni"; do
  find ${dir} -maxdepth 1 \( -name '*.cpp' -o -name '*.h' \) -exec 'clang-format' -i '{}' \;
done

popd