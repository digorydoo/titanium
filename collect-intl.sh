#!/bin/bash

set -e
JAR="./tool-collect-intl/build/libs/tool-collect-intl.jar"

if [[ ! -f "$JAR" ]]; then
   2>&1 echo "JAR not built: $JAR"
   exit 1
fi

java -jar "$JAR" "$@"
