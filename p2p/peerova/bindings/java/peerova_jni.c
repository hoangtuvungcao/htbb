/* JNI glue: dev.peerova.Peerova (Java) -> peerova C ABI (peerova.h).
 *
 * Built into libpeerova_jni (.so / .dll / .dylib), which links against libpeerova.
 * Strings cross the boundary as UTF-8 byte[] (the C side appends the NUL) instead of jstring, so
 * JNI's "modified UTF-8" never touches tickets, config JSON or error text.
 * Every native method takes the node handle (peerova_node* widened to jlong); the Java side guarantees
 * the handle is non-zero and not yet freed. Build: see build.sh (desktop) or the Android CMakeLists.txt. */
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "peerova.h"

#define NODE(h) ((peerova_node*)(intptr_t)(h))

static jclass    g_event_cls;   /* dev/peerova/Peerova$Event (global ref) */
static jmethodID g_event_ctor;  /* (IJIZ[B)V */

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    (void)reserved;
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
    jclass c = (*env)->FindClass(env, "dev/peerova/Peerova$Event");
    if (!c) return JNI_ERR;
    g_event_cls = (jclass)(*env)->NewGlobalRef(env, c);
    (*env)->DeleteLocalRef(env, c);
    g_event_ctor = (*env)->GetMethodID(env, g_event_cls, "<init>", "(IJIZ[B)V");
    if (!g_event_ctor) return JNI_ERR;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    (void)reserved;
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) == JNI_OK && g_event_cls)
        (*env)->DeleteGlobalRef(env, g_event_cls);
}

/* byte[] (UTF-8, no NUL) -> malloc'd C string, or NULL for null array. */
static char* dup_bytes(JNIEnv* env, jbyteArray a) {
    if (!a) return NULL;
    jsize n = (*env)->GetArrayLength(env, a);
    char* s = (char*)malloc((size_t)n + 1);
    if (!s) return NULL;
    (*env)->GetByteArrayRegion(env, a, 0, n, (jbyte*)s);
    s[n] = 0;
    return s;
}

static jbyteArray cstr_bytes(JNIEnv* env, const char* s) {
    if (!s) return NULL;
    jsize n = (jsize)strlen(s);
    jbyteArray a = (*env)->NewByteArray(env, n);
    if (a) (*env)->SetByteArrayRegion(env, a, 0, n, (const jbyte*)s);
    return a;
}

JNIEXPORT jlong JNICALL Java_dev_peerova_Peerova_nServerStart(JNIEnv* env, jclass cls, jbyteArray json) {
    (void)cls;
    char* cfg = dup_bytes(env, json);
    peerova_node* n = peerova_server_start(cfg);
    free(cfg);
    return (jlong)(intptr_t)n;
}

JNIEXPORT jlong JNICALL Java_dev_peerova_Peerova_nClientConnect(JNIEnv* env, jclass cls, jbyteArray ticket, jbyteArray json) {
    (void)cls;
    char* t = dup_bytes(env, ticket);
    char* cfg = dup_bytes(env, json);
    peerova_node* n = peerova_client_connect(t, cfg);
    free(t); free(cfg);
    return (jlong)(intptr_t)n;
}

/* Ticket string ("pvt_...") or NULL. peerova_ticket returns the size incl. NUL; call once to size, once to fill. */
JNIEXPORT jbyteArray JNICALL Java_dev_peerova_Peerova_nTicket(JNIEnv* env, jclass cls, jlong h) {
    (void)cls;
    int32_t need = peerova_ticket(NODE(h), NULL, 0);
    if (need <= 0) return NULL;
    char* buf = (char*)malloc((size_t)need);
    if (!buf) return NULL;
    peerova_ticket(NODE(h), buf, need);
    jbyteArray a = cstr_bytes(env, buf);
    free(buf);
    return a;
}

/* Connect code ("K7Q2XD") or NULL if the server has none (yet). Same size-then-fill pattern as nTicket. */
JNIEXPORT jbyteArray JNICALL Java_dev_peerova_Peerova_nRoomCode(JNIEnv* env, jclass cls, jlong h) {
    (void)cls;
    int32_t need = peerova_room_code(NODE(h), NULL, 0);
    if (need <= 0) return NULL;
    char* buf = (char*)malloc((size_t)need);
    if (!buf) return NULL;
    jbyteArray a = NULL;
    if (peerova_room_code(NODE(h), buf, need) > 0) { buf[need - 1] = 0; a = cstr_bytes(env, buf); }
    free(buf);
    return a;
}

/* Sends len bytes of data[off..]. Small payloads are copied through a stack buffer (no malloc on the
 * hot path). Returns 0 on success, non-zero on error (text via nLastError). */
JNIEXPORT jint JNICALL Java_dev_peerova_Peerova_nSend(JNIEnv* env, jclass cls, jlong h, jlong peer, jint channel,
                                                 jbyteArray data, jint off, jint len, jboolean reliable) {
    (void)cls;
    jbyte stackbuf[1280];
    jbyte* buf = len <= (jint)sizeof stackbuf ? stackbuf : (jbyte*)malloc((size_t)len);
    if (!buf) return -1;
    if (len > 0) (*env)->GetByteArrayRegion(env, data, off, len, buf);
    if ((*env)->ExceptionCheck(env)) { if (buf != stackbuf) free(buf); return -1; }
    int32_t r = peerova_send(NODE(h), (uint32_t)peer, (uint8_t)channel, (const uint8_t*)buf, (uint32_t)len, reliable ? 1 : 0);
    if (buf != stackbuf) free(buf);
    return r;
}

/* Zero-copy send straight from a direct ByteBuffer's memory. */
JNIEXPORT jint JNICALL Java_dev_peerova_Peerova_nSendDirect(JNIEnv* env, jclass cls, jlong h, jlong peer, jint channel,
                                                       jobject direct, jint off, jint len, jboolean reliable) {
    (void)cls;
    uint8_t* p = (uint8_t*)(*env)->GetDirectBufferAddress(env, direct);
    if (!p) return -1;
    return peerova_send(NODE(h), (uint32_t)peer, (uint8_t)channel, p + off, (uint32_t)len, reliable ? 1 : 0);
}

/* Returns a new Event, or null if none arrived (timeout_ms <= 0: non-blocking). Payload is copied into a Java byte[] before returning
 * (the native pointer is only valid until the next poll). */
JNIEXPORT jobject JNICALL Java_dev_peerova_Peerova_nPoll(JNIEnv* env, jclass cls, jlong h, jint timeout_ms) {
    (void)cls;
    peerova_event ev;
    int got = timeout_ms > 0 ? peerova_poll_wait(NODE(h), &ev, (uint32_t)timeout_ms) : peerova_poll(NODE(h), &ev);
    if (!got) return NULL;
    jbyteArray data = (*env)->NewByteArray(env, (jsize)ev.len);
    if (!data) return NULL; /* OOM pending */
    if (ev.len) (*env)->SetByteArrayRegion(env, data, 0, (jsize)ev.len, (const jbyte*)ev.data);
    jobject o = (*env)->NewObject(env, g_event_cls, g_event_ctor, (jint)ev.type, (jlong)ev.peer,
                                  (jint)ev.channel, (jboolean)(ev.reliable != 0), data);
    (*env)->DeleteLocalRef(env, data);
    return o;
}

JNIEXPORT void JNICALL Java_dev_peerova_Peerova_nDisconnect(JNIEnv* env, jclass cls, jlong h, jlong peer) {
    (void)env; (void)cls;
    peerova_disconnect(NODE(h), (uint32_t)peer);
}

JNIEXPORT jlong JNICALL Java_dev_peerova_Peerova_nPeerCount(JNIEnv* env, jclass cls, jlong h) {
    (void)env; (void)cls;
    return (jlong)peerova_peer_count(NODE(h));
}

JNIEXPORT void JNICALL Java_dev_peerova_Peerova_nFree(JNIEnv* env, jclass cls, jlong h) {
    (void)env; (void)cls;
    peerova_free(NODE(h));
}

JNIEXPORT jbyteArray JNICALL Java_dev_peerova_Peerova_nLastError(JNIEnv* env, jclass cls) {
    (void)cls;
    return cstr_bytes(env, peerova_last_error());
}

JNIEXPORT jint JNICALL Java_dev_peerova_Peerova_nLastErrorCode(JNIEnv* env, jclass cls) {
    (void)env; (void)cls;
    return (jint)peerova_last_error_code();
}

JNIEXPORT jbyteArray JNICALL Java_dev_peerova_Peerova_nVersion(JNIEnv* env, jclass cls) {
    (void)cls;
    return cstr_bytes(env, peerova_version());
}

/* ---- port tunnel (dev.peerova.PeerovaTunnel) ---- */
#define TUN(h) ((peerova_tunnel*)(intptr_t)(h))

typedef int32_t (*tun_str_fn)(peerova_tunnel*, char*, int32_t);

static jbyteArray tun_str(JNIEnv* env, jlong h, tun_str_fn f) {
    int32_t need = f(TUN(h), NULL, 0);
    if (need <= 0) return NULL;
    char* buf = (char*)malloc((size_t)need);
    if (!buf) return NULL;
    int32_t got = f(TUN(h), buf, need);
    jbyteArray a = (got > 0 && got <= need) ? cstr_bytes(env, buf) : NULL;
    free(buf);
    return a;
}

JNIEXPORT jlong JNICALL Java_dev_peerova_PeerovaTunnel_nServer(JNIEnv* env, jclass cls, jbyteArray json) {
    (void)cls;
    char* cfg = dup_bytes(env, json);
    peerova_tunnel* t = peerova_tunnel_server(cfg);
    free(cfg);
    return (jlong)(intptr_t)t;
}

JNIEXPORT jlong JNICALL Java_dev_peerova_PeerovaTunnel_nClient(JNIEnv* env, jclass cls, jbyteArray ticket, jbyteArray json) {
    (void)cls;
    char* tk = dup_bytes(env, ticket);
    char* cfg = dup_bytes(env, json);
    peerova_tunnel* t = peerova_tunnel_client(tk, cfg);
    free(tk); free(cfg);
    return (jlong)(intptr_t)t;
}

JNIEXPORT jint JNICALL Java_dev_peerova_PeerovaTunnel_nState(JNIEnv* env, jclass cls, jlong h) {
    (void)env; (void)cls;
    return peerova_tunnel_state(TUN(h));
}

JNIEXPORT jint JNICALL Java_dev_peerova_PeerovaTunnel_nWait(JNIEnv* env, jclass cls, jlong h, jint ms) {
    (void)env; (void)cls;
    return peerova_tunnel_wait(TUN(h), ms < 0 ? 0u : (uint32_t)ms);
}

JNIEXPORT jint JNICALL Java_dev_peerova_PeerovaTunnel_nLocalPort(JNIEnv* env, jclass cls, jlong h, jint i) {
    (void)env; (void)cls;
    return peerova_tunnel_local_port(TUN(h), i);
}

JNIEXPORT jbyteArray JNICALL Java_dev_peerova_PeerovaTunnel_nTicket(JNIEnv* env, jclass cls, jlong h) {
    (void)cls;
    return tun_str(env, h, peerova_tunnel_ticket);
}

JNIEXPORT jbyteArray JNICALL Java_dev_peerova_PeerovaTunnel_nError(JNIEnv* env, jclass cls, jlong h) {
    (void)cls;
    return tun_str(env, h, peerova_tunnel_error);
}

JNIEXPORT jbyteArray JNICALL Java_dev_peerova_PeerovaTunnel_nNextLog(JNIEnv* env, jclass cls, jlong h) {
    (void)cls;
    return tun_str(env, h, peerova_tunnel_next_log);
}

JNIEXPORT jlong JNICALL Java_dev_peerova_PeerovaTunnel_nPeerCount(JNIEnv* env, jclass cls, jlong h) {
    (void)env; (void)cls;
    return (jlong)peerova_tunnel_peer_count(TUN(h));
}

JNIEXPORT void JNICALL Java_dev_peerova_PeerovaTunnel_nFree(JNIEnv* env, jclass cls, jlong h) {
    (void)env; (void)cls;
    peerova_tunnel_free(TUN(h));
}
