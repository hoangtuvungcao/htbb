/* jni_md.h for Windows (x86/x64), same definitions as OpenJDK's include/win32/jni_md.h.
 * Used only to cross-compile peerova_jni.dll with mingw-w64 on Linux (the Linux JDK ships only include/linux). */
#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_
#define JNIEXPORT __declspec(dllexport)
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall
typedef long jint;
typedef __int64 jlong;
typedef signed char jbyte;
#endif
