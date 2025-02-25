#!/bin/bash
#
# make-bundle.sh
#
# Creates a bundled macOS application from the JAR. The JVM is embedded.
# @see https://docs.oracle.com/en/java/javase/14/docs/specs/man/jpackage.html

set -e

OS_TYPE="$(uname -o)"

# Check if we're in the correct directory before doing anything
if [[ ! -f ./make-bundle.sh ]]; then
   echo 2>&1 "Error: The working directory does not seem to be the project root."
   exit 1
fi

# Run gradle to make sure everything's ready for bundling
./gradlew main:build

MAIN_JAR_NAME="main.jar"
MAIN_JAR="main/build/libs/$MAIN_JAR_NAME"
COPIED_ASSETS_DIR="main/build/assets"
OUTPUT_BASE_DIR="build"
DIST_DIR="$OUTPUT_BASE_DIR/dist"
JPACKAGE_TEMP_DIR="$OUTPUT_BASE_DIR/tmp/jpackage"
ICON_TEMP_DIR="$OUTPUT_BASE_DIR/tmp/icon"

if [[ ! -f "$MAIN_JAR" ]]; then
   echo 2>&1 "Error: $MAIN_JAR_NAME not found: $MAIN_JAR"
   echo 2>&1 "Gradle should have created this, so something's odd."
   exit 1
fi

if [[ ! -d "$COPIED_ASSETS_DIR" ]]; then
   echo 2>&1 "Error: Cannot access the copied assets directory: $COPIED_ASSETS_DIR"
   echo 2>&1 "Gradle post-build step should have created this, so something's odd."
   exit 1
fi

if [[ ! -d "$OUTPUT_BASE_DIR" ]]; then
   mkdir "$OUTPUT_BASE_DIR"
fi

if [[ -d "$DIST_DIR" ]]; then
   rm -rf "$DIST_DIR" || echo 2>&1 "Warning: Cannot remove: $DIST_DIR"
fi

if [[ -d "$JPACKAGE_TEMP_DIR" ]]; then
   rm -rf "$JPACKAGE_TEMP_DIR" || echo 2>&1 "Warning: Cannot remove: $JPACKAGE_TEMP_DIR"
fi

if [[ -d "$ICON_TEMP_DIR" ]]; then
   rm -rf "$ICON_TEMP_DIR" || echo 2>&1 "Warning: Cannot remove: $ICON_TEMP_DIR"
fi

APP_VERSION="1.0.0"
COPYRIGHT="Copyright 2025 by Digory Doolittle"
DESCRIPTION="Game"
EXE_NAME="Titanium"
MAIN_CLASS="ch.digorydoo.titanium.main.app.MainKt"
VENDOR="Digory Doolittle"

if [[ "$OS_TYPE" == "Darwin" ]]; then
   JAVA_OPTIONS="-XstartOnFirstThread"
else
   JAVA_OPTIONS=""
fi

# -------------------------------------------------------------------------------------------------

echo "Making icon..."

mkdir -p "$ICON_TEMP_DIR"

ICON_PNG="assets/private/bundle/app-icon.png"
ICON_BUNDLE="$ICON_TEMP_DIR"/app-icon.icns

if [[ ! -f "$ICON_PNG" ]]; then
   echo 2>&1 "Error: Cannot access icon PNG file: $ICON_PNG"
   exit 1
fi

if [[ -f "$ICON_BUNDLE" ]]; then
   rm "$ICON_BUNDLE" || echo 2>&1 "Warning: Cannot remove: $ICON_BUNDLE"
fi

if [[ "$OS_TYPE" == "Darwin" ]]; then
   ICONSET_DIR="$ICON_TEMP_DIR"/app-icon.iconset

   if [[ -d "$ICONSET_DIR" ]]; then
      rm -rf "$ICONSET_DIR" || echo 2>&1 "Warning: Cannot remove: $ICONSET_DIR"
   fi

   mkdir "$ICONSET_DIR"

   function convertPNG {
      WIDTH="$1"
      HEIGHT="$2"
      NAME="$3"
      sips -z "$WIDTH" "$HEIGHT" "$ICON_PNG" --out "$ICONSET_DIR"/icon_"$NAME".png >/dev/null || (
         echo "Error: sips failed: w=$WIDTH, h=$HEIGHT, n=$NAME"
         exit 1
      )
   }

   convertPNG 16 16 "16x16"
   convertPNG 32 32 "16x16@2x"
   convertPNG 32 32 "32x32"
   convertPNG 64 64 "32x32@2x"
   convertPNG 128 128 "128x128"
   convertPNG 256 256 "128x128@2x"
   convertPNG 256 256 "256x256"
   convertPNG 512 512 "256x256@2x"
   convertPNG 512 512 "512x512"

   # $ICON_PNG is expected to be 1024x1024
   cp "$ICON_PNG" "$ICONSET_DIR"/icon_512x512@2x.png

   iconutil -c icns -o "$ICON_BUNDLE" "$ICONSET_DIR" || (
      echo 2>&1 "Error: Failed to convert iconset ($ICONSET_DIR) to icns ($ICON_BUNDLE)"
      exit 1
   )
elif [[ "$OS_TYPE" == "Cygwin" ]]; then
   ICON_BUNDLE="$ICON_TEMP_DIR"/app-icon.ico

   if [[ -f "$ICON_BUNDLE" ]]; then
      rm "$ICON_BUNDLE" || echo 2>&1 "Warning: Cannot remove: $ICON_BUNDLE"
   fi

   # Note: /bin/convert is part of ImageMagick for cygwin. Don't confuse with Windows CONVERT.EXE!
   /bin/convert "$ICON_PNG" -verbose -define icon:auto-resize=256,64,48,32,16 "$ICON_BUNDLE"
   echo "Created icon: $ICON_BUNDLE"
else
   echo 2>&1 "Don't know how to build icon for OS: $OS_TYPE"
   exit 1
fi

if [[ ! -f "$ICON_BUNDLE" ]]; then
   echo 2>&1 "Error: Icon wasn't generated: $ICON_BUNDLE"
   exit 1
fi

# -------------------------------------------------------------------------------------------------

echo "Making bundle..."

# The input directory must contain MAIN_JAR as well as all of our game assets.
# jpackage does not allow multiple input directories, so copy everything into the COPIED_ASSETS_DIR.
# Note that the assets in COPIED_ASSETS_DIR may be symbolic links to our repository's assets except when under cygwin
# (see post-build.sh). jpackage should copy them as real files.
cp "$MAIN_JAR" "$COPIED_ASSETS_DIR"/"$MAIN_JAR_NAME"

if [[ "$OS_TYPE" == "Darwin" ]]; then
   MAC_PCK_ID="ch.digorydoo.titanium"
   MAC_PCK_NAME="$EXE_NAME" # the name that appears in the menu bar
   MAC_APP_CATEGORY="game"
   BUNDLE_TYPE="app-image"                    # valid values are: app-image, dmg, pkg
   RESOURCE_DIR="assets/private/bundle/macos" # contains our Info.plist

   jpackage \
      --type "$BUNDLE_TYPE" \
      --app-version "$APP_VERSION" \
      --vendor "$VENDOR" \
      --copyright "$COPYRIGHT" \
      --description "$DESCRIPTION" \
      --name "$EXE_NAME" \
      --dest "$DIST_DIR" \
      --temp "$JPACKAGE_TEMP_DIR" \
      --icon "$ICON_BUNDLE" \
      --main-class "$MAIN_CLASS" \
      --java-options "$JAVA_OPTIONS" \
      --main-jar "$MAIN_JAR_NAME" \
      --input "$COPIED_ASSETS_DIR" \
      --mac-package-identifier "$MAC_PCK_ID" \
      --mac-package-name "$MAC_PCK_NAME" \
      --mac-app-category "$MAC_APP_CATEGORY" \
      --resource-dir "$RESOURCE_DIR"
   # --verbose
   # --mac-sign
   # --mac-signing-key-user-name "$MAC_SIGNING_KEY_USERNAME"
   # --mac-entitlements "$MAC_ENTITLEMENTS"

   RUNTIME_RELEASE_FILE="$DIST_DIR"/Titanium.app/Contents/runtime/Contents/Home/release
   open "$DIST_DIR"
elif [[ "$OS_TYPE" == "Cygwin" ]]; then
   BUNDLE_TYPE="app-image"

   # The resource dir does not contain anything useful at the moment,
   # but we could add Titanium.properties to declare executable properties.
   RESOURCE_DIR="assets/private/bundle/windows"

   jpackage \
      --type "$BUNDLE_TYPE" \
      --app-version "$APP_VERSION" \
      --vendor "$VENDOR" \
      --copyright "$COPYRIGHT" \
      --description "$DESCRIPTION" \
      --name "$EXE_NAME" \
      --dest "$DIST_DIR" \
      --temp "$JPACKAGE_TEMP_DIR" \
      --icon "$ICON_BUNDLE" \
      --main-class "$MAIN_CLASS" \
      --java-options "$JAVA_OPTIONS" \
      --main-jar "$MAIN_JAR_NAME" \
      --input "$COPIED_ASSETS_DIR" \
      --resource-dir "$RESOURCE_DIR" \
      --win-console
   # --verbose

   RUNTIME_RELEASE_FILE="$DIST_DIR"/Titanium/runtime/release
   explorer "$(cygpath --windows "$DIST_DIR")" || true
else
   echo 2>&1 "Don't know how to invoke jpackage for OS: $OS_TYPE"
   exit 1
fi

echo -n "Packaging succeeded. Was using JRE "
fgrep "JAVA_VERSION" "$RUNTIME_RELEASE_FILE" || echo "with an unknown version"

rm "$COPIED_ASSETS_DIR"/"$MAIN_JAR_NAME" || (
   echo 2>&1 "Warning: Cannot remove copied jar: $COPIED_ASSETS_DIR/$MAIN_JAR_NAME"
)

echo "Done. Bundle is at: $DIST_DIR"
