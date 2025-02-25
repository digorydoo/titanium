#!/bin/bash
#
# make-proper.sh
#
# Run this script to set up the project after having cloned it, or when updating a stale project directory.
# It pulls the latest sources of kutils and titanium, builds all the sources and re-imports all the assets.
# NOTE: All assets under assets/generated will be DELETED and re-created from originals under assets/private.

set -e

SELF_DIR="$(realpath .)"
KUTILS_DIR="../kutils"
ASSETS_DIR="assets"
OS_TYPE="$(uname -o)"

if [[ ! -d "$ASSETS_DIR" ]]; then
   echo 2>&1 "Cannot access assets dir: $ASSETS_DIR"
   echo 2>&1 "The above path should be a symbolic link that points to the separate titanium-assets repository."
   echo 2>&1 "The titanium-assets repository is NOT publicly available on Github at this time."
   exit 1
fi

ASSETS_DIR="$(realpath "$ASSETS_DIR")"
GIT_STATUS="$(git status --porcelain)"

if [[ "$GIT_STATUS" != "" ]]; then
   echo 2>&1 "Working directory of titanium is not clean:"
   echo 2>&1 "$GIT_STATUS"
   echo 2>&1 "Please commit uncommitted changes first."
   exit 1
fi

if [[ ! -d "$KUTILS_DIR" ]]; then
   echo 2>&1 "Cannot access directory of kutils: $KUTILS_DIR"
   echo 2>&1 "kutils is a separate repository that is publicly available. Please consult README.md!"
   exit 1
fi

KUTILS_DIR="$(realpath "$KUTILS_DIR")"
cd "$KUTILS_DIR"
GIT_STATUS="$(git status --porcelain)"

if [[ "$GIT_STATUS" != "" ]]; then
   echo 2>&1 "Working directory of kutils is not clean:"
   echo 2>&1 "$GIT_STATUS"
   echo 2>&1 "Please commit uncommitted changes in kutils first."
   exit 1
fi

echo "Pulling latest sources of kutils (branch develop)..."
git checkout develop
git pull --ff-only || (
   echo 2>&1 "Warning: Failed to pull latest sources of kutils:"
   echo 2>&1 "$KUTILS_DIR"
   exit 1
)

cd "$SELF_DIR"/kutils

if [[ "$OS_TYPE" == "Darwin" ]]; then
   # Under macOS, kutils is symbolically linked into titanium's source tree.
   if [[ -L "src" ]]; then
      rm src || true
   fi
   ln -s ../../kutils/main/src src
else
   # Under Windows, symbolic links are poorly supported, so we copy kutils into titanium's source tree instead.
   echo "Copying kutils sources here..."
   if [[ -d "src" ]]; then
      rm -rf src || true
   fi
   cp -r "$KUTILS_DIR"/main/src kutils/
fi

cd ..

echo "Pulling latest sources of titanium..."
git pull --ff-only || (
   echo 2>&1 "Warning: Failed to pull latest sources of titanium!"
)

echo "Cleaning..."
./gradlew clean

if [[ -d "$ASSETS_DIR"/generated ]]; then
   rm -rf "$ASSETS_DIR"/generated
fi

mkdir "$ASSETS_DIR"/generated

# Only build import_asset, because if main was built now, its post-build.sh would fail.
echo "Building import_asset..."
./gradlew import_asset:build

echo "Importing Collada..."
./import-asset.sh collada \
   --out-dir="$ASSETS_DIR"/generated/mesh/ \
   --overwrite \
   "$ASSETS_DIR"/private/collada/*.dae

echo "Importing brick textures..."
./import-asset.sh brick-textures \
   --out-file="$ASSETS_DIR"/generated/textures/tiles-town.png \
   --overwrite \
   --padding=2 \
   --arrange-across=9 \
   "$ASSETS_DIR"/private/textures-tiles-town/*.png

echo "Building rest of sources..."
./gradlew build

echo "Done."
