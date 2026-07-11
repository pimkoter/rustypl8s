#!/usr/bin/env bash

# ==============================================================================
# RustyPl8s Complete Build Script (Rust Core -> Android APK)
# ==============================================================================

set -e # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
APK_PATH="$SCRIPT_DIR/app/build/outputs/apk/debug/app-debug.apk"

echo "===================================================================="
echo " 🏗️  Step 1: Compiling Rust Core & Generating Bindings"
echo "===================================================================="

# Run the existing compilation script
if [ -f "$SCRIPT_DIR/compile_android.sh" ]; then
    bash "$SCRIPT_DIR/compile_android.sh"
else
    echo "ERROR: compile_android.sh not found!"
    exit 1
fi

echo ""
echo "===================================================================="
echo " 🤖 Step 2: Assembling Android APK (Gradle)"
echo "===================================================================="

# Build the debug APK using the gradle installed in the nix-shell
gradle :app:assembleDebug --no-daemon

echo ""
echo "===================================================================="
echo " ✅ SUCCESS!"
echo "===================================================================="
if [ -f "$APK_PATH" ]; then
    FINAL_APK="$SCRIPT_DIR/rustyPl8s-debug.apk"
    cp "$APK_PATH" "$FINAL_APK"

    echo "Your installable APK is ready at:"
    echo "$FINAL_APK"
    echo ""
    echo "To install on a connected phone, run:"
    echo "adb install $FINAL_APK"
else
    echo "ERROR: APK was not found at $APK_PATH"
    exit 1
fi
