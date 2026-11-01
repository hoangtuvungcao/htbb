/* Minimal jni_md.h for cross-compiling the JNI glue for Windows (x86_64-w64-mingw32-gcc) on a machine
 * whose JDK only ships include/linux. Same definitions as the JDK's include/win32/jni_md.h
 * (jint = 32-bit, jlong = 64-bit, __stdcall is a no-op on x86_64). A real JDK's include/win32 works too
 * (build.sh: JNI_WIN32_INCLUDE=...). Written for Peerova, not copied from the JDK. */
#ifndef PEEROVA_JNI_MD_WIN32_H
#define PEEROVA_JNI_MD_WIN32_H
#ifndef JNIEXPORT
#define JNIEXPORT __declspec(dllexport)
#endif
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall
typedef int jint;
typedef long long jlong;
typedef signed char jbyte;
#endif
