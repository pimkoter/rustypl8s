{pkgs ? import <nixpkgs> {}}:
(pkgs.buildFHSEnv {
  name = "rustyPl8s-android-env";
  targetPkgs = pkgs:
    with pkgs; [
      # Core Rust compiler toolchain requirements
      rustup
      cargo
      rustc
      pkg-config
      openssl
      gnumake
      gcc

      # System libraries the NDK binaries need to execute
      glibc
      zlib
      ncurses
      libxml2

      # Android build tools
      gradle
      jdk17
    ];

  profile = ''
    # 1. Map your local Android Studio layout paths
    export ANDROID_HOME="/home/pim/Android/Sdk"
    export ANDROID_NDK_ROOT="/home/pim/Android/Sdk/ndk/30.0.15729638"

    # 2. Automatically sync toolchain and architectures
    rustup default stable
    rustup target add aarch64-linux-android x86_64-linux-android

    # 3. Direct Cargo straight to the NDK compiler wrappers
    export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android34-clang"
    export CARGO_TARGET_AARCH64_LINUX_ANDROID_AR="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar"
    export CARGO_TARGET_AARCH64_LINUX_ANDROID_RANLIB="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ranlib"
    export CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android34-clang"
    export CARGO_TARGET_X86_64_LINUX_ANDROID_AR="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar"
    export CARGO_TARGET_X86_64_LINUX_ANDROID_RANLIB="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ranlib"

    echo "===================================================================="
    echo " 🏋️  rustyPl8s Android FHS Shell Active (NixOS Path-Fix Engaged)"
    echo "===================================================================="
  '';
}).env
