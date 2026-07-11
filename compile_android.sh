#!/usr/bin/env bash

# ==============================================================================
# RustyPl8s Android Compilation & Bindgen Script
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$SCRIPT_DIR"
CORE_DIR="$REPO_ROOT/core"
APP_DIR="$REPO_ROOT/app"

# 1. Setup Android Targets
echo "Adding Rust Android targets..."
rustup target add aarch64-linux-android x86_64-linux-android

# 2. Configure NDK Environment
if [ -z "$ANDROID_NDK_ROOT" ]; then
  echo "ERROR: ANDROID_NDK_ROOT is not set."
  echo "Please set it: export ANDROID_NDK_ROOT=/path/to/your/android-sdk/ndk/XX.X.XXXXXXX"
  exit 1
fi

OS_NAME=$(uname | tr '[:upper:]' '[:lower:]')
TOOLCHAIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/$OS_NAME-x86_64/bin

pushd "$CORE_DIR" >/dev/null || {
  echo "ERROR: Could not find core directory at $CORE_DIR"
  exit 1
}

# 3. Compile for Android Targets
# We try to auto-detect the clang versioned wrapper (e.g., ...android34-clang)
# Using sort -V to get the highest version available
AARCH64_CLANG=$(find "$TOOLCHAIN" -name "aarch64-linux-android*-clang" | sort -V | tail -n 1)
X86_64_CLANG=$(find "$TOOLCHAIN" -name "x86_64-linux-android*-clang" | sort -V | tail -n 1)

if [ -z "$AARCH64_CLANG" ] || [ -z "$X86_64_CLANG" ]; then
  echo "ERROR: Could not find Android clang wrappers in $TOOLCHAIN"
  exit 1
fi

echo "Compiling for aarch64 (Physical Devices) using $AARCH64_CLANG..."
CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER="$AARCH64_CLANG" \
  AR_aarch64_linux_android="$TOOLCHAIN/llvm-ar" \
  RANLIB_aarch64_linux_android="$TOOLCHAIN/llvm-ranlib" \
  CC_aarch64_linux_android="$AARCH64_CLANG" \
  cargo build --release --target aarch64-linux-android

echo "Compiling for x86_64 (Emulators) using $X86_64_CLANG..."
CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER="$X86_64_CLANG" \
  AR_x86_64_linux_android="$TOOLCHAIN/llvm-ar" \
  RANLIB_x86_64_linux_android="$TOOLCHAIN/llvm-ranlib" \
  CC_x86_64_linux_android="$X86_64_CLANG" \
  cargo build --release --target x86_64-linux-android

# 4. Generate Kotlin Bindings via UniFFI internal project dependency
echo "Generating Kotlin bindings..."
cargo run --features=uniffi/cli --bin uniffi-bindgen generate --library \
  target/aarch64-linux-android/release/librustypl8s_core.so \
  --language kotlin \
  --out-dir "$APP_DIR/src/main/java/com/example/rustypl8s/"

# 5. Move Native Libraries to jniLibs
echo "Organizing native libraries into jniLibs..."
mkdir -p "$APP_DIR/src/main/jniLibs/arm64-v8a"
mkdir -p "$APP_DIR/src/main/jniLibs/x86_64"

cp target/aarch64-linux-android/release/librustypl8s_core.so "$APP_DIR/src/main/jniLibs/arm64-v8a/"
cp target/x86_64-linux-android/release/librustypl8s_core.so "$APP_DIR/src/main/jniLibs/x86_64/"

popd >/dev/null

echo "=============================================================================="
echo "DONE! Rust core is ready. Open Android Studio and Run the 'app' module."
echo "=============================================================================="
