#!/bin/bash

set -e

OS_TYPE="$(uname -o)"

if [[ "$OS_TYPE" == "Cygwin" ]]; then
   BASEDIR="$HOME/Develop/zz-private/titanium"
else
   BASEDIR="$HOME/Develop/titanium"
fi

if [[ ! -d "$BASEDIR" ]]; then
   echo 2>&1 "Cannot access BASEDIR: $BASEDIR"
   exit 1
fi

JAR="$BASEDIR/import_asset/build/libs/import_asset.jar"

if [[ "$OS_TYPE" == "Cygwin" ]]; then
   JAR="$(cygpath -w "$JAR")"
fi

if [[ ! -f "$JAR" ]]; then
   echo 2>&1 "Cannot access JAR: $JAR"
   exit 1
fi

GENERATEDDIR="$BASEDIR/assets/generated"

cd "$BASEDIR"

if [[ ! -d "$GENERATEDDIR" ]]; then
   mkdir "$GENERATEDDIR"
fi

if [[ ! -d "$GENERATEDDIR"/mesh ]]; then
   mkdir "$GENERATEDDIR"/mesh
fi

if [[ ! -d "$GENERATEDDIR"/textures ]]; then
   mkdir "$GENERATEDDIR"/textures
fi

java -jar "$JAR" "$@"
