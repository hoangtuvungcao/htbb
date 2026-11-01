# Builds libpeerova_jni.so (JNI glue, bindings/java/peerova_jni.c) for the current ANDROID_ABI and links it
# against the prebuilt libpeerova.so in ../jniLibs/${ANDROID_ABI}/ (see build-android-libs.sh).
cmake_minimum_required(VERSION 3.18)
project(peerova_jni C)

# Override with -DPEEROVA_REPO=/path/to/peerova if this module is copied out of the repo.
set(PEEROVA_REPO "${CMAKE_CURRENT_LIST_DIR}/../../../../../.." CACHE PATH "peerova repo root")
set(PEEROVA_JNILIBS "${CMAKE_CURRENT_LIST_DIR}/../jniLibs" CACHE PATH "dir with <abi>/libpeerova.so")

add_library(peerova SHARED IMPORTED)
# cargo cdylibs carry no SONAME: IMPORTED_NO_SONAME makes the link use -lpeerova so DT_NEEDED is
# "libpeerova.so" (resolved from the APK lib dir) instead of an absolute build-machine path.
set_target_properties(peerova PROPERTIES
    IMPORTED_LOCATION "${PEEROVA_JNILIBS}/${ANDROID_ABI}/libpeerova.so"
    IMPORTED_NO_SONAME TRUE)

add_library(peerova_jni SHARED "${PEEROVA_REPO}/bindings/java/peerova_jni.c")
target_include_directories(peerova_jni PRIVATE "${PEEROVA_REPO}/crates/peerova-ffi/include")
target_compile_options(peerova_jni PRIVATE -O2 -Wall -Wextra)
# 16 KB page alignment (required for new apps targeting Android 15+).
target_link_options(peerova_jni PRIVATE "-Wl,-z,max-page-size=16384")
target_link_libraries(peerova_jni peerova log)
