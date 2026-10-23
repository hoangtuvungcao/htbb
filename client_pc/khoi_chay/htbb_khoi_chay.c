/*
 * HTBB.exe - trình mở game Huyền Thoại Bảo Bối cho Windows (0.9.11, thay Launch4j 3.50).
 *
 * Lỗi 0.9.10: Launch4j 3.50 (đầu ANSI) báo "This application requires a Java Runtime Environment" khi thư mục game có
 * chữ ngoài bảng mã ANSI của máy (vd C:\Users\Nguyễn Văn Á\AppData\Local\Programs\HTBB - cài cho riêng người dùng, hay
 * giải nén zip ra Desktop / Downloads của tài khoản tên có dấu). Chạy thẳng runtime\bin\javaw.exe cũng hỏng: trình
 * java.exe / javaw.exe và cả jvm.dll lấy đường dẫn của chính mình bằng API ANSI ("Error: could not find java.dll").
 *
 * Trình này CHỨA máy ảo Java ngay trong tiến trình HTBB.exe (như java.exe): nạp <thư mục>\runtime\bin\server\jvm.dll
 * bằng LoadLibraryExW, gọi JNI_CreateJavaVM rồi htbb.pc.Chay.main(String[]) (tham số Unicode qua NewString). Bảng mã
 * ANSI của tiến trình là UTF-8 (manifest activeCodePage, Windows 10 1903+) nên mọi API ANSI bên trong JVM thấy đúng
 * đường dẫn có dấu; thêm nữa thư mục được đổi sang tên ngắn 8.3 (chỉ ASCII) khi ổ đĩa có - chạy được cả Windows cũ.
 * Thư mục làm việc = thư mục game, jar theo đường tương đối (HTBB-PC.jar).
 * Tham số --htbb-go-loi: ghi nhật ký %TEMP%\htbb-khoi-chay.log.
 *
 * Biên dịch (Linux, client_pc/dong_goi_windows.sh): x86_64-w64-mingw32-gcc -O2 -municode -mwindows -I<jdk>/include
 *   -I<jdk>/include/win32 -o HTBB.exe htbb_khoi_chay.c htbb.res.o  (htbb.rc: biểu tượng, phiên bản, manifest)
 */
#ifndef UNICODE
#define UNICODE
#endif
#ifndef _UNICODE
#define _UNICODE
#endif
#include <windows.h>
#include <shellapi.h>
#include <wchar.h>
#include <jni.h>

#define HTBB_JAR "HTBB-PC.jar"
#define HTBB_LOP "htbb/pc/Chay"
#define DAI 32768

typedef jint(JNICALL *TaoMay)(JavaVM **, void **, void *);

static HANDLE nhatKy = INVALID_HANDLE_VALUE;
static wchar_t thu[DAI], ngan[DAI], jvm[DAI], s[DAI];
static char b8[DAI * 3];
static int soTs;
static wchar_t **ts;
static TaoMay taoMay;
static int maThoat;

static void ghi(const wchar_t *x) {
    if (nhatKy == INVALID_HANDLE_VALUE) {
        return;
    }
    int n = WideCharToMultiByte(CP_UTF8, 0, x, -1, b8, sizeof(b8), NULL, NULL);
    if (n > 1) {
        DWORD w;
        WriteFile(nhatKy, b8, (DWORD) (n - 1), &w, NULL);
        WriteFile(nhatKy, "\r\n", 2, &w, NULL);
    }
}

static int coTep(const wchar_t *p) {
    DWORD a = GetFileAttributesW(p);
    return a != INVALID_FILE_ATTRIBUTES && !(a & FILE_ATTRIBUTE_DIRECTORY);
}

static int ascii(const wchar_t *x) {
    for (; *x; x++) {
        if (*x > 126) {
            return 0;
        }
    }
    return 1;
}

static void baoLoi(const wchar_t *chu) {
    ghi(chu);
    MessageBoxW(NULL, chu, L"Huyền Thoại Bảo Bối", MB_OK | MB_ICONERROR);
}

/* Luồng chạy main (như java.exe: không chạy trên luồng đầu tiên, ngăn xếp đủ lớn). */
static DWORD WINAPI chayMain(LPVOID k) {
    (void) k;
    static const char *tuyChon[] = {
        "-Xms64m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-Dsun.java2d.uiScale=1", "-Dsun.java2d.d3d=true",
        "-Dsun.java2d.noddraw=false", "-Djava.class.path=" HTBB_JAR, "-Dsun.java.launcher=SUN_STANDARD",
        "-Dsun.java.command=" HTBB_JAR, "-Dhtbb.khoiChay=HTBB.exe"
    };
    int n = (int) (sizeof(tuyChon) / sizeof(tuyChon[0]));
    JavaVMOption o[16];
    for (int i = 0; i < n; i++) {
        o[i].optionString = (char *) tuyChon[i];
        o[i].extraInfo = NULL;
    }
    JavaVMInitArgs a;
    a.version = JNI_VERSION_10;
    a.nOptions = n;
    a.options = o;
    a.ignoreUnrecognized = JNI_FALSE;
    JavaVM *may = NULL;
    JNIEnv *env = NULL;
    jint r = taoMay(&may, (void **) &env, &a);
    if (r != JNI_OK || env == NULL) {
        _snwprintf(s, DAI, L"Không khởi động được Java đi kèm (JNI_CreateJavaVM = %d):\n%ls", (int) r, jvm);
        s[DAI - 1] = 0;
        baoLoi(s);
        maThoat = 1;
        return 1;
    }
    ghi(L"JVM OK");
    jclass lop = (*env)->FindClass(env, HTBB_LOP);
    jmethodID m = lop == NULL ? NULL : (*env)->GetStaticMethodID(env, lop, "main", "([Ljava/lang/String;)V");
    if (m == NULL) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
        }
        _snwprintf(s, DAI, L"Không thấy lớp game trong:\n%ls\\" HTBB_JAR L"\n\nHãy cài lại / giải nén lại đủ thư mục HTBB.", thu);
        s[DAI - 1] = 0;
        baoLoi(s);
        maThoat = 1;
        (*may)->DestroyJavaVM(may);
        return 1;
    }
    jclass lopChuoi = (*env)->FindClass(env, "java/lang/String");
    int dau = 1, so = 0;
    for (int i = dau; i < soTs; i++) {
        if (wcscmp(ts[i], L"--htbb-go-loi") != 0) {
            so++;
        }
    }
    jobjectArray mang = (*env)->NewObjectArray(env, so, lopChuoi, NULL);
    for (int i = dau, j = 0; i < soTs; i++) {
        if (wcscmp(ts[i], L"--htbb-go-loi") == 0) {
            continue;
        }
        jstring c = (*env)->NewString(env, (const jchar *) ts[i], (jsize) wcslen(ts[i]));
        (*env)->SetObjectArrayElement(env, mang, j++, c);
    }
    ghi(L"main");
    (*env)->CallStaticVoidMethod(env, lop, m, mang);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        maThoat = 1;
    }
    (*may)->DetachCurrentThread(may);
    (*may)->DestroyJavaVM(may);   /* chờ mọi luồng không phải daemon (cửa sổ game) kết thúc */
    return 0;
}

int WINAPI wWinMain(HINSTANCE h, HINSTANCE t, LPWSTR l, int n) {
    (void) h;
    (void) t;
    (void) l;
    (void) n;
    static wchar_t exe[DAI];
    ts = CommandLineToArgvW(GetCommandLineW(), &soTs);
    for (int i = 1; ts != NULL && i < soTs; i++) {
        if (wcscmp(ts[i], L"--htbb-go-loi") == 0) {
            wchar_t tam[MAX_PATH + 1];
            DWORD k = GetTempPathW(MAX_PATH, tam);
            if (k > 0 && k < MAX_PATH - 24) {
                wcscat(tam, L"htbb-khoi-chay.log");
                nhatKy = CreateFileW(tam, GENERIC_WRITE, FILE_SHARE_READ, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
            }
        }
    }
    DWORD k = GetModuleFileNameW(NULL, exe, DAI);
    if (k == 0 || k >= DAI) {
        baoLoi(L"Không đọc được đường dẫn HTBB.exe.");
        return 1;
    }
    ghi(exe);
    _snwprintf(s, DAI, L"bang ma ANSI: %u", (unsigned) GetACP());
    ghi(s);
    wcscpy(thu, exe);
    wchar_t *g = wcsrchr(thu, L'\\');
    if (g == NULL) {
        baoLoi(L"Đường dẫn HTBB.exe lạ.");
        return 1;
    }
    *g = 0;
    /* tên ngắn 8.3 (ASCII) nếu ổ đĩa có */
    DWORD m = GetShortPathNameW(thu, ngan, DAI);
    if (m == 0 || m >= DAI || !ascii(ngan)) {
        wcscpy(ngan, thu);
    }
    ghi(ngan);
    _snwprintf(jvm, DAI, L"%ls\\runtime\\bin\\server\\jvm.dll", ngan);
    jvm[DAI - 1] = 0;
    _snwprintf(s, DAI, L"%ls\\" HTBB_JAR, thu);
    s[DAI - 1] = 0;
    if (!coTep(jvm)) {
        _snwprintf(s, DAI, L"Thiếu Java đi kèm game:\n%ls\\runtime\n\nHãy cài lại / giải nén lại đủ thư mục HTBB.", thu);
        s[DAI - 1] = 0;
        baoLoi(s);
        return 1;
    }
    if (!coTep(s)) {
        wchar_t c[DAI];
        _snwprintf(c, DAI, L"Thiếu tệp game:\n%ls\n\nHãy cài lại / giải nén lại đủ thư mục HTBB.", s);
        c[DAI - 1] = 0;
        baoLoi(c);
        return 1;
    }
    SetCurrentDirectoryW(ngan);
    /* runtime\bin (vcruntime140.dll, java.dll ...) đứng đầu đường tìm DLL */
    _snwprintf(s, DAI, L"%ls\\runtime\\bin", ngan);
    s[DAI - 1] = 0;
    SetDllDirectoryW(s);
    HMODULE dll = LoadLibraryExW(jvm, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
    if (dll == NULL) {
        _snwprintf(s, DAI, L"Không nạp được Java đi kèm (mã lỗi %lu):\n%ls", (unsigned long) GetLastError(), jvm);
        s[DAI - 1] = 0;
        baoLoi(s);
        return 1;
    }
    taoMay = (TaoMay) (void *) GetProcAddress(dll, "JNI_CreateJavaVM");
    if (taoMay == NULL) {
        baoLoi(L"jvm.dll đi kèm hỏng (thiếu JNI_CreateJavaVM).");
        return 1;
    }
    ghi(L"nap jvm.dll OK");
    HANDLE lu = CreateThread(NULL, 8 * 1024 * 1024, chayMain, NULL, STACK_SIZE_PARAM_IS_A_RESERVATION, NULL);
    if (lu == NULL) {
        baoLoi(L"Không tạo được luồng chạy game.");
        return 1;
    }
    WaitForSingleObject(lu, INFINITE);
    CloseHandle(lu);
    if (nhatKy != INVALID_HANDLE_VALUE) {
        CloseHandle(nhatKy);
    }
    return maThoat;
}
