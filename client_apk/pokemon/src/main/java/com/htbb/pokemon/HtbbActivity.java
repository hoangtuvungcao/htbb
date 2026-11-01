package com.htbb.pokemon;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import javax.microedition.lcdui.NenAndroid;
import javax.microedition.midlet.MIDlet;

/**
 * Activity duy nhất của APK Huyền Thoại Bảo Bối: một SurfaceView toàn màn hình chạy MIDlet game.GameMidlet
 * (code bản jar) qua lớp tương thích MIDP (javax.microedition.*, NenAndroid).
 */
public class HtbbActivity extends Activity {
    private static MIDlet midlet;
    private static boolean dangChay;
    private ManHinh manHinh;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        NenAndroid.hoatDong = this;
        NenAndroid.ungDung = getApplicationContext();
        // nhanh p2p: danh tinh Peerova co dinh cua may (khoa client trong bo nho rieng cua app) -> may chu thay cung 127.a.b.c
        if (System.getProperty("htbb.p2p.key") == null) {
            System.setProperty("htbb.p2p.key", new java.io.File(new java.io.File(getFilesDir(), "p2p"), "peerova-client.key").getAbsolutePath());
        }
        // như máy J2ME: lỗi không bắt ở luồng phụ của game (tải dữ liệu, mạng...) chỉ dừng luồng đó,
        // không làm chết cả ứng dụng (Android mặc định giết tiến trình)
        final Thread.UncaughtExceptionHandler goc = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                if (t == Looper.getMainLooper().getThread() || goc == null) {
                    if (goc != null) goc.uncaughtException(t, e);
                    return;
                }
                Log.e(NenAndroid.TAG, "loi luong " + t.getName() + " (bo qua nhu J2ME)", e);
            }
        });
        System.setProperty("microedition.platform", "Android/" + Build.MODEL);
        System.setProperty("microedition.configuration", "CLDC-1.1");
        System.setProperty("microedition.profiles", "MIDP-2.0");
        System.setProperty("microedition.locale", "vi-VN");
        System.setProperty("microedition.encoding", "UTF-8");
        System.setProperty("microedition.media.version", "1.1"); // MMAPI có (Manager -> MediaPlayer)
        System.setProperty("supports.mixing", "true");
        MIDlet.khiThoat = new Runnable() {
            public void run() {
                thoat();
            }
        };
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 28) {
            // vẽ cả vùng tai thỏ (không viền đen); HUD tự tránh qua NenAndroid.anToan*
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.layoutInDisplayCutoutMode = Build.VERSION.SDK_INT >= 30
                    ? WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    : WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            w.setAttributes(lp);
        }
        com.htbb.pokemon.hd.Hud.khoiTao();
        com.htbb.pokemon.hd.AmThanhHd.khoiTao(this);
        manHinh = new ManHinh(this);
        setContentView(manHinh);
        anThanhHeThong();
    }

    private void anThanhHeThong() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            w.setDecorFitsSystemWindows(false);
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean coFocus) {
        super.onWindowFocusChanged(coFocus);
        if (coFocus) anThanhHeThong();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NenAndroid.hoatDong = this;
        // 0.9.20: quay lại từ trang "Cho phép từ nguồn này" -> cài tiếp bản mới đã tải (không bấm "Cài lại")
        CapNhatApk.khiQuayLai(this);
        if (dangChay) {
            NenAndroid.anHien(true);
            NenAndroid.luong().them(new Runnable() {
                public void run() {
                    try {
                        MIDlet.batDau(midlet);
                    } catch (Throwable e) {
                        Log.e(NenAndroid.TAG, "startApp", e);
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        if (dangChay) {
            NenAndroid.anHien(false);
            NenAndroid.luong().them(new Runnable() {
                public void run() {
                    // 0.9.22: rời app giữa lúc kéo ngắm / kéo chiêu vào ô = huỷ (không bắn / không gán khi quay lại)
                    game.pokemon.NgamTay.huy();
                    game.pokemon.KeoChieu.huy();
                    MIDlet.tamDung(midlet);
                }
            });
        }
        super.onPause();
    }

    /** Lần đầu có bề mặt vẽ (đã biết cỡ màn): tạo MIDlet trên luồng sự kiện. */
    private void batDauGame() {
        if (dangChay) return;
        dangChay = true;
        NenAndroid.luong().them(new Runnable() {
            public void run() {
                try {
                    midlet = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(midlet);
                } catch (Throwable e) {
                    Log.e(NenAndroid.TAG, "khong chay duoc game", e);
                }
            }
        });
    }

    private void thoat() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                finishAndRemoveTask();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 300);
            }
        });
    }

    // ---------------- phím ----------------

    /** Mã phím J2ME (0 = không dùng). */
    static int maJ2me(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.KEYCODE_DPAD_UP: return -1;
            case KeyEvent.KEYCODE_DPAD_DOWN: return -2;
            case KeyEvent.KEYCODE_DPAD_LEFT: return -3;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return -4;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A: return -5;
            case KeyEvent.KEYCODE_SOFT_LEFT:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_F1:
            case KeyEvent.KEYCODE_BUTTON_START: return -6;
            case KeyEvent.KEYCODE_SOFT_RIGHT:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_F2:
            case KeyEvent.KEYCODE_BUTTON_B: return -7;
            case KeyEvent.KEYCODE_DEL: return -8;
            case KeyEvent.KEYCODE_STAR: return 42;
            case KeyEvent.KEYCODE_POUND: return 35;
            case KeyEvent.KEYCODE_SPACE: return 32;
            default:
                break;
        }
        if (k >= KeyEvent.KEYCODE_0 && k <= KeyEvent.KEYCODE_9) return 48 + (k - KeyEvent.KEYCODE_0);
        if (k >= KeyEvent.KEYCODE_NUMPAD_0 && k <= KeyEvent.KEYCODE_NUMPAD_9) return 48 + (k - KeyEvent.KEYCODE_NUMPAD_0);
        int u = e.getUnicodeChar();
        if (u > 32 && u < 127) return u;
        return 0;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.KEYCODE_VOLUME_UP || k == KeyEvent.KEYCODE_VOLUME_DOWN || k == KeyEvent.KEYCODE_VOLUME_MUTE) {
            return super.dispatchKeyEvent(e);
        }
        int m = maJ2me(e);
        if (m == 0 || !dangChay) return super.dispatchKeyEvent(e);
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            NenAndroid.phim(e.getRepeatCount() > 0 ? 1 : 0, m);
        } else if (e.getAction() == KeyEvent.ACTION_UP) {
            NenAndroid.phim(2, m);
        }
        return true;
    }

    // ---------------- màn vẽ ----------------

    static final class ManHinh extends SurfaceView implements SurfaceHolder.Callback {
        private final HtbbActivity a;
        private int ngonChinh = -1;

        ManHinh(HtbbActivity a) {
            super(a);
            this.a = a;
            getHolder().addCallback(this);
            setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                    int l = 0, t = 0, r = 0, b = 0;
                    if (Build.VERSION.SDK_INT >= 28 && in.getDisplayCutout() != null) {
                        android.view.DisplayCutout dc = in.getDisplayCutout();
                        l = dc.getSafeInsetLeft();
                        t = dc.getSafeInsetTop();
                        r = dc.getSafeInsetRight();
                        b = dc.getSafeInsetBottom();
                    }
                    // góc màn bo tròn: chừa thêm ~2% cạnh ngắn ở hai bên
                    int bo = Math.min(v.getWidth(), v.getHeight()) / 50;
                    NenAndroid.datAnToan(Math.max(l, bo), t, Math.max(r, bo), b);
                    return in;
                }
            });
            setFocusable(true);
            setFocusableInTouchMode(true);
        }

        public void surfaceCreated(SurfaceHolder h) {
        }

        public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) {
            // game chỉ chạy ngang (sensorLandscape): lúc mở app bề mặt có thể còn dọc một nhịp trước khi
            // xoay - chờ bề mặt ngang mới khởi tạo, để game không dựng giao diện theo cỡ dọc
            if (w < hh && !dangChay) {
                return;
            }
            boolean doi = NenAndroid.tinhKichThuoc(w, hh);
            NenAndroid.datMat(h);
            if (!dangChay) {
                a.batDauGame();
            } else if (doi) {
                NenAndroid.baoDoiCo();
            }
        }

        public void surfaceDestroyed(SurfaceHolder h) {
            NenAndroid.boMat();
        }

        /** Đa chạm: HUD HD giữ nút / cần điều khiển; cú chạm khác chuyển cho game (một con trỏ J2ME). */
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            com.htbb.pokemon.hd.Hud.chamAndroid(e);
            return true;
        }
    }

    static Context ctx() {
        return NenAndroid.ungDung;
    }
}
