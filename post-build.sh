#!/bin/bash
# This script is called from main/build.gradle after each build.

set -e

OS_TYPE="$(uname -o)"
BUILD_DIR="./main/build"
SRC_DIR="./assets"
DST_DIR="$BUILD_DIR/assets"

if [[ ! -d "$BUILD_DIR" ]]; then
   echo >&2 "Build directory does not exist: $BUILD_DIR"
   exit 1
fi

if [[ ! -d "$SRC_DIR" ]]; then
   echo >&2 "Assets directory to copy from does not exist: $SRC_DIR"
   exit 1
fi

if [[ ! -d "$SRC_DIR"/generated ]]; then
   echo >&2 "Directory of generated assets does not exist. Run import-asset.sh first! ($SRC_DIR/generated)"
   exit 1
fi

if [[ -d "$DST_DIR" ]]; then
   # Clean assets dir first
   rm -rf "$DST_DIR"
fi

mkdir "$DST_DIR"

# $1: asset src dir
# $2: asset dst dir
function installAssets {
   echo "Copying $2..."
   SRC_ASSET_DIR="$SRC_DIR/$1"
   DST_ASSET_DIR="$DST_DIR/$2"

   if [[ ! -d "$SRC_ASSET_DIR" ]]; then
      echo >&2 "Asset subdirectory to copy from does not exist: $SRC_ASSET_DIR"
      exit 1
   fi

   if [[ ! -d "$DST_ASSET_DIR" ]]; then
      mkdir "$DST_ASSET_DIR"
   fi

   for FILEPATH in "$SRC_ASSET_DIR"/*; do
      FILENAME=$(basename "$FILEPATH")

      if [[ "$FILENAME" == "*" ]]; then
         echo >&2 "Warning: No such file: $FILEPATH"
      else
         if [[ -e "$DST_ASSET_DIR"/"$FILENAME" ]]; then
            echo >&2 "File already exists: $DST_ASSET_DIR/$FILENAME"
            echo >&2 "Is there a clash of filenames with the generated assets?"
            exit 1
         fi

         # The build should point to the repository's assets by symblic link to avoid having to copy everything.
         # Unfortunately, java cannot follow cygwin symbolic links, so we need to copy under cygwin.

         if [[ "$OS_TYPE" == "Cygwin" ]]; then
            cp "$(realpath "$FILEPATH")" "$DST_ASSET_DIR"/"$FILENAME"
         else
            ln -s "$(realpath "$FILEPATH")" "$DST_ASSET_DIR"/"$FILENAME"
         fi
      fi
   done
}

installAssets fonts fonts
installAssets gellists gellists
installAssets generated/mesh mesh
installAssets generated/textures textures
installAssets playfields playfields
installAssets shaders shaders
installAssets sounds sounds
installAssets textures textures
