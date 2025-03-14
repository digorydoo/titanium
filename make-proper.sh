#!/bin/bash
#
# make-proper.sh
#
# Run this script to set up the project after having cloned it, or when updating a stale project directory.
# It pulls the latest sources of kutils and titanium, builds all the sources and re-imports all the assets.
# NOTE: All assets under assets/generated will be DELETED and re-created from originals under assets/private.

set -e

RED=$'\e[31m'
YELLOW=$'\e[33m'
PLAIN=$'\e[0m'

SELF_DIR="$(realpath .)"
OS_TYPE="$(uname -o)"

# --------------------------------------------------------------------------------------------------------------------

echo "${YELLOW}Preparing assets repo$PLAIN"

ASSETS_DIR="../titanium-assets"

if [[ ! -d "$ASSETS_DIR" ]]; then
   echo 2>&1 "Cannot access assets dir: $ASSETS_DIR"
   echo 2>&1 "assets is a separate repository that IS NOT CURRENTLY AVAILABLE ON GITHUB! Cannot continue."
   exit 1
fi

ASSETS_DIR="$(realpath "$ASSETS_DIR")"
echo "Path: $ASSETS_DIR"
cd "$ASSETS_DIR"

ASSETS_STATUS="$(git status --porcelain)"

if [[ "$ASSETS_STATUS" != "" ]]; then
   echo 2>&1 "${RED}Warning:${PLAIN}: Working directory is not clean:"
   echo 2>&1 "$ASSETS_STATUS"
   echo 2>&1 "Continuing without pulling latest changes."
else
   ASSETS_BRANCH="$(git rev-parse --abbrev-ref=strict HEAD)"
   echo "Assets repo is on branch ${YELLOW}${ASSETS_BRANCH}${PLAIN}, pulling..."

   git pull --ff-only || (
      echo 2>&1 "${RED}Warning:${PLAIN} Failed to pull latest changes!"
   )
fi

cd "$SELF_DIR"

if [[ "$OS_TYPE" == "Darwin" ]]; then
   # Under macOS, assets is symbolically linked into titanium's source tree.
   echo "Creating symbolic link..."
   if [[ -L "assets" ]]; then
      rm assets || true
   fi
   ln -s ../titanium-assets assets
else
   # Under Windows, symbolic links are poorly supported, so we copy kutils into titanium's source tree instead.
   echo "Copying assets here..."
   if [[ -d "assets" ]]; then
      rm -rf assets || true
   fi
   cp -r "$ASSETS_DIR" assets/
fi

# --------------------------------------------------------------------------------------------------------------------

echo
echo "${YELLOW}Preparing kutils repo$PLAIN"

KUTILS_DIR="../kutils"

if [[ ! -d "$KUTILS_DIR" ]]; then
   echo 2>&1 "Cannot access kutils dir: $KUTILS_DIR"
   echo 2>&1 "kutils is a separate repository that is available on github. Please clone kutils first!"
   exit 1
fi

KUTILS_DIR="$(realpath "$KUTILS_DIR")"
echo "Path: $KUTILS_DIR"
cd "$KUTILS_DIR"

KUTILS_STATUS="$(git status --porcelain)"

if [[ "$KUTILS_STATUS" != "" ]]; then
   echo 2>&1 "${RED}Warning:${PLAIN} Working directory is not clean:"
   echo 2>&1 "$KUTILS_STATUS"
   echo 2>&1 "Continuing without pulling latest changes."
else
   KUTILS_BRANCH="$(git rev-parse --abbrev-ref=strict HEAD)"
   echo "kutils repo is on branch ${YELLOW}${KUTILS_BRANCH}${PLAIN}, pulling..."

   git pull --ff-only || (
      echo 2>&1 "${RED}Warning:${PLAIN} Failed to pull latest changes!"
   )
fi

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

cd "$SELF_DIR"

# --------------------------------------------------------------------------------------------------------------------

echo
echo "${YELLOW}Preparing titanium repo$PLAIN"

SELF_STATUS="$(git status --porcelain)"

if [[ "$SELF_STATUS" != "" ]]; then
   echo 2>&1 "${RED}Warning:${PLAIN} Working directory is not clean:"
   echo 2>&1 "$SELF_STATUS"
   echo 2>&1 "Continuing without pulling latest changes."
else
   SELF_BRANCH="$(git rev-parse --abbrev-ref=strict HEAD)"
   echo "titanium repo is on branch ${YELLOW}${SELF_BRANCH}${PLAIN}, pulling..."

   git pull --ff-only || (
      echo 2>&1 "${RED}Warning:${PLAIN} Failed to pull latest changes!"
   )
fi

# --------------------------------------------------------------------------------------------------------------------

echo
echo "${YELLOW}Importing assets$PLAIN"

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

# --------------------------------------------------------------------------------------------------------------------

echo
echo "${YELLOW}Building the rest of titanium$PLAIN"

echo "Building rest of sources..."
./gradlew build

echo "Done. You should now be able to run the game with: ./gradlew main:run"
