package javax.microedition.lcdui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import java.util.ArrayDeque;

/**
 * Cầu nối MIDP <-> Android cho bản APK Huyền Thoại Bảo Bối (code game = code bản jar J2ME).
 *
 * - Độ phân giải logic: màn thật chia một HỆ SỐ NGUYÊN (cạnh ngắn ~ 320..400 px logic) rồi phóng lên
 *   bằng láng giềng gần nhất -> pixel art sắc nét, không méo; phần dư (< hệ số) là viền đen.
 * - Một luồng sự kiện (LuongSuKien) chạy tuần tự: chạm, phím, callSerially, paint - giống máy J2ME.
 * - Khung hình vẽ vào Bitmap logic rồi đưa lên SurfaceView (lockHardwareCanvas, không lọc).
 */
public final class NenAndroid {
    public static final String TAG = "HTBB";
    /** cạnh ngắn logic nhỏ nhất (px): hệ số = cạnh ngắn thật / CANH_NGAN (làm tròn xuống) */
    public static int CANH_NGAN = 320;
    /** cạnh dài logic nhỏ nhất (px): máy tính bảng 4:3 không bị phóng quá to (2048x1536 -> 682x512 x3) */
    public static int CANH_DAI = 640;
    /** vùng an toàn (tai thỏ / bo góc) theo px logic: trái, trên, phải, dưới - HUD tránh các vùng này */
    public static volatile int anToanTrai, anToanTren, anToanPhai, anToanDuoi;
    private static volatile int[] anToanThat = new int[4];

    public static volatile Activity hoatDong;
    /** Context ứng dụng (đọc assets, lưu RMS) - còn sống cả khi Activity bị tạo lại. */
    public static volatile Context ungDung;
    public static volatile int rong = 360;
    public static volatile int cao = 640;
    public static volatile int heSo = 1;
    public static volatile int lechX;
    public static volatile int lechY;
    private static volatile int thatW;
    private static volatile int thatH;

    private static final Object KHOA_MAT = new Object();
    private static SurfaceHolder mat;
    private static LuongSuKien luong;
    private static AlertDialog hopNhap;

    private NenAndroid() {
    }

    public static synchronized LuongSuKien luong() {
        if (luong == null) {
            luong = new LuongSuKien();
            luong.start();
        }
        return luong;
    }

    // ---------------- kích thước ----------------

    /** Tính độ phân giải logic từ cỡ vùng vẽ thật. true = đổi so với trước. */
    public static boolean tinhKichThuoc(int w, int h) {
        if (w <= 0 || h <= 0) return false;
        int ngan = Math.min(w, h);
        int dai = Math.max(w, h);
        int s = Math.max(1, Math.min(ngan / CANH_NGAN, dai / CANH_DAI));
        int lw = w / s;
        int lh = h / s;
        thatW = w;
        thatH = h;
        lechX = (w - lw * s) / 2;
        lechY = (h - lh * s) / 2;
        heSo = s;
        boolean doi = lw != rong || lh != cao;
        rong = lw;
        cao = lh;
        tinhAnToan();
        return doi;
    }

    /** Vùng an toàn thật (px màn) từ WindowInsets (cutout + thanh hệ thống) -> px logic. */
    public static void datAnToan(int trai, int tren, int phai, int duoi) {
        anToanThat = new int[]{trai, tren, phai, duoi};
        tinhAnToan();
    }

    private static void tinhAnToan() {
        int s = Math.max(1, heSo);
        int[] a = anToanThat;
        anToanTrai = (a[0] + s - 1) / s;
        anToanTren = (a[1] + s - 1) / s;
        anToanPhai = (a[2] + s - 1) / s;
        anToanDuoi = (a[3] + s - 1) / s;
    }

    /** Lớp vẽ đè (HUD cảm ứng HD) - vẽ sau khung game, có thể vẽ lại 60 hình/giây độc lập với game. */
    public interface LopPhu {
        /** Vẽ lớp phủ (toạ độ logic). */
        void ve(Graphics g);

        /** true = đang có hoạt hình -> vẽ lại ~60 hình/giây. */
        boolean dangDong();

        /** Trước / sau khi game vẽ một khung (thu thập thẻ, nút theo ngữ cảnh). */
        void khungMoi();

        void khungXong();
    }

    public static volatile LopPhu lopPhu;
    private static Thread nhip;

    /** Gắn lớp phủ + nhịp ~60 Hz: khi lớp phủ đang hoạt hình thì ghép lại màn (không gọi game paint). */
    public static synchronized void ganLopPhu(LopPhu lp) {
        lopPhu = lp;
        if (nhip != null || Build.VERSION.SDK_INT < 29) return;
        nhip = new Thread("HTBB-nhip") {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(16);
                        LopPhu l = lopPhu;
                        if (l != null && mat != null && l.dangDong()) luong().yeuCauSoan();
                    } catch (Throwable e) {
                        // tiếp tục
                    }
                }
            }
        };
        nhip.setDaemon(true);
        nhip.start();
    }

    /** Báo đổi cỡ màn cho Canvas đang hiện (trên luồng sự kiện). */
    public static void baoDoiCo() {
        luong().them(new Runnable() {
            public void run() {
                Canvas c = Display.canvasCuoi;
                if (c != null) {
                    c.sizeChanged(rong, cao);
                    c.repaint();
                }
            }
        });
    }

    // ---------------- bề mặt vẽ ----------------

    public static void datMat(SurfaceHolder h) {
        synchronized (KHOA_MAT) {
            mat = h;
        }
        Canvas c = Display.canvasCuoi;
        if (c != null) c.repaint();
    }

    public static void boMat() {
        synchronized (KHOA_MAT) {
            mat = null;
        }
    }

    // ---------------- nhập ----------------

    /** Chạm: kieu 0 = nhấn, 1 = kéo, 2 = thả. Toạ độ màn thật. */
    public static void cham(final int kieu, float x, float y) {
        int s = heSo;
        int lx = (int) ((x - lechX) / s);
        int ly = (int) ((y - lechY) / s);
        if (lx < 0) lx = 0;
        if (ly < 0) ly = 0;
        if (lx >= rong) lx = rong - 1;
        if (ly >= cao) ly = cao - 1;
        final int fx = lx;
        final int fy = ly;
        luong().them(new Runnable() {
            public void run() {
                Displayable d = Display.hienTai;
                if (!(d instanceof Canvas)) return;
                Canvas c = (Canvas) d;
                if (kieu == 0) c.pointerPressed(fx, fy);
                else if (kieu == 1) c.pointerDragged(fx, fy);
                else c.pointerReleased(fx, fy);
            }
        });
    }

    /** Chạm toạ độ LOGIC, gọi ngay (đang ở luồng sự kiện - HUD chuyển cú chạm không trúng nút cho game). */
    public static void chamNgay(int kieu, int lx, int ly) {
        Displayable d = Display.hienTai;
        if (!(d instanceof Canvas)) return;
        Canvas c = (Canvas) d;
        if (lx < 0) lx = 0;
        if (ly < 0) ly = 0;
        if (lx >= rong) lx = rong - 1;
        if (ly >= cao) ly = cao - 1;
        if (kieu == 0) c.pointerPressed(lx, ly);
        else if (kieu == 1) c.pointerDragged(lx, ly);
        else c.pointerReleased(lx, ly);
    }

    /** Phím J2ME gọi ngay trên luồng sự kiện (HUD: cần điều khiển ảo, nút chiêu). */
    public static void phimNgay(int kieu, int ma) {
        Displayable d = Display.hienTai;
        if (!(d instanceof Canvas)) return;
        Canvas c = (Canvas) d;
        if (kieu == 0) c.keyPressed(ma);
        else if (kieu == 1) c.keyRepeated(ma);
        else c.keyReleased(ma);
    }

    /** Rung phản hồi rất nhẹ khi bấm nút HUD (tick hệ thống nếu có). */
    public static void rungNhe() {
        Activity a = hoatDong;
        if (a == null) return;
        try {
            Vibrator v = (Vibrator) a.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= 29) v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            else v.vibrate(VibrationEffect.createOneShot(8, 60));
        } catch (Throwable e) {
            // không rung được
        }
    }

    /** Phím J2ME: kieu 0 = nhấn, 1 = giữ lặp, 2 = thả. */
    public static void phim(final int kieu, final int ma) {
        luong().them(new Runnable() {
            public void run() {
                Displayable d = Display.hienTai;
                if (!(d instanceof Canvas)) return;
                Canvas c = (Canvas) d;
                if (kieu == 0) c.keyPressed(ma);
                else if (kieu == 1) c.keyRepeated(ma);
                else c.keyReleased(ma);
            }
        });
    }

    public static void anHien(final boolean hien) {
        luong().them(new Runnable() {
            public void run() {
                Canvas c = Display.canvasCuoi;
                if (c == null) return;
                if (hien) {
                    c.showNotify();
                    c.repaint();
                } else {
                    c.hideNotify();
                }
            }
        });
    }

    public static boolean rung(int ms) {
        Activity a = hoatDong;
        if (a == null || ms <= 0) return false;
        try {
            Vibrator v = (Vibrator) a.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return false;
            v.vibrate(VibrationEffect.createOneShot(Math.min(ms, 1000), VibrationEffect.DEFAULT_AMPLITUDE));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // ---------------- hộp nhập chữ (TextBox) ----------------

    static void hienNhap(final TextBox tb) {
        final Activity a = hoatDong;
        if (a == null) return;
        a.runOnUiThread(new Runnable() {
            public void run() {
                if (hopNhap != null) {
                    hopNhap.dismiss();
                    hopNhap = null;
                }
                final EditText et = new EditText(a);
                et.setText(tb.getString());
                et.setSelection(et.getText().length());
                et.setSingleLine(true);
                et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(tb.getMaxSize())});
                int c = tb.getConstraints();
                int kieu;
                switch (c & TextField.CONSTRAINT_MASK) {
                    case TextField.NUMERIC: kieu = InputType.TYPE_CLASS_NUMBER; break;
                    case TextField.DECIMAL: kieu = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL; break;
                    case TextField.PHONENUMBER: kieu = InputType.TYPE_CLASS_PHONE; break;
                    case TextField.EMAILADDR: kieu = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; break;
                    case TextField.URL: kieu = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI; break;
                    default: kieu = InputType.TYPE_CLASS_TEXT; break;
                }
                if ((c & TextField.PASSWORD) != 0) {
                    kieu = (kieu & InputType.TYPE_CLASS_NUMBER) != 0
                            ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD
                            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }
                et.setInputType(kieu);
                // màn ngang: để bàn phím dùng ô nhập toàn màn hình (extract UI) - hộp thoại bị bàn phím ép mất chỗ
                et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et.setHint(tb.getTitle() != null && tb.getTitle().length() > 0 ? tb.getTitle()
                        : (c & TextField.PASSWORD) != 0 ? "Mật khẩu" : "Nhập chữ");
                Command ok = null;
                Command huy = null;
                for (int i = 0; i < tb.lenh.size(); i++) {
                    Command cm = (Command) tb.lenh.elementAt(i);
                    int t = cm.getCommandType();
                    if (t == Command.BACK || t == Command.CANCEL || t == Command.EXIT || t == Command.STOP) {
                        if (huy == null) huy = cm;
                    } else if (ok == null) {
                        ok = cm;
                    }
                }
                final Command fOk = ok;
                final Command fHuy = huy;
                AlertDialog.Builder b = new AlertDialog.Builder(a, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                String tieuDe = tb.getTitle();
                b.setTitle(tieuDe != null && tieuDe.length() > 0 ? tieuDe
                        : (c & TextField.PASSWORD) != 0 ? "Nhập mật khẩu" : "Nhập chữ");
                b.setView(et);
                b.setPositiveButton(nhan(ok, "OK"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        xong(tb, et.getText().toString(), fOk);
                    }
                });
                b.setNegativeButton(nhan(huy, "Huỷ"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        xong(tb, null, fHuy);
                    }
                });
                b.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface d) {
                        xong(tb, null, fHuy);
                    }
                });
                final AlertDialog dlg = b.create();
                et.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
                    public boolean onEditorAction(android.widget.TextView v, int id, android.view.KeyEvent ev) {
                        if (id == EditorInfo.IME_ACTION_DONE || ev != null) {
                            dlg.dismiss();
                            xong(tb, et.getText().toString(), fOk);
                            return true;
                        }
                        return false;
                    }
                });
                if (dlg.getWindow() != null) {
                    dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                hopNhap = dlg;
                dlg.show();
                et.requestFocus();
            }
        });
    }

    /** Nhãn nút hộp nhập: lệnh J2ME tiếng Anh ("OK"/"Cancel"/"Back") hiện bằng tiếng Việt. */
    private static String nhan(Command c, String macDinh) {
        String l = c == null ? null : c.getLabel();
        if (l == null || l.length() == 0) return macDinh;
        if ("Cancel".equalsIgnoreCase(l) || "Back".equalsIgnoreCase(l)) return "Huỷ";
        if ("OK".equalsIgnoreCase(l)) return "Xong";
        return l;
    }

    private static void xong(final TextBox tb, final String chu, final Command cm) {
        synchronized (NenAndroid.class) {
            if (hopNhap == null) return; // đã xử lý (OK rồi onCancel)
            hopNhap = null;
        }
        if (chu != null) tb.setString(chu);
        luong().them(new Runnable() {
            public void run() {
                if (cm != null) {
                    tb.goiLenh(cm);
                } else if (Display.canvasCuoi != null && Display.hienTai == tb) {
                    Display.getDisplay(null).setCurrent(Display.canvasCuoi);
                }
            }
        });
    }

    static void anNhap() {
        final Activity a = hoatDong;
        if (a == null) return;
        a.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog d;
                synchronized (NenAndroid.class) {
                    d = hopNhap;
                    hopNhap = null;
                }
                if (d != null) d.dismiss();
            }
        });
    }

    // ---------------- luồng sự kiện + vẽ ----------------

    public static final class LuongSuKien extends Thread {
        private final ArrayDeque<Runnable> hang = new ArrayDeque<Runnable>();
        private boolean choVe;
        private long soYeuCau;
        private long soDaVe;
        private Graphics g;
        private int loiVe;
        private android.graphics.RenderNode nutGame;
        private boolean coKhung;
        private boolean choSoan;
        private Graphics gPhu;

        LuongSuKien() {
            super("HTBB-su-kien");
            setDaemon(true);
        }

        public void them(Runnable r) {
            synchronized (this) {
                hang.addLast(r);
                notifyAll();
            }
        }

        void yeuCauVe() {
            synchronized (this) {
                if (!choVe) {
                    choVe = true;
                    soYeuCau++;
                }
                notifyAll();
            }
        }

        /** serviceRepaints: chờ khung đang yêu cầu vẽ xong (tối đa 250 ms để không treo game). */
        void choVe() {
            if (Thread.currentThread() == this) {
                boolean can;
                synchronized (this) {
                    can = choVe;
                    choVe = false;
                }
                if (can) ve();
                return;
            }
            synchronized (this) {
                if (!choVe) return;
                long dich = soYeuCau;
                long het = System.currentTimeMillis() + 250;
                while (soDaVe < dich) {
                    long con = het - System.currentTimeMillis();
                    if (con <= 0) break;
                    try {
                        wait(con);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        public void run() {
            while (true) {
                Runnable r = null;
                long dich = 0;
                synchronized (this) {
                    while (hang.isEmpty() && !choVe && !choSoan) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (!hang.isEmpty()) {
                        r = hang.pollFirst();
                    } else if (choVe) {
                        choVe = false;
                        choSoan = false;
                        dich = soYeuCau;
                    } else {
                        choSoan = false;
                        dich = -1;
                    }
                }
                if (r == null && dich < 0) {
                    try {
                        soan(Display.canvasCuoi, rong, cao);
                    } catch (Throwable e) {
                        Log.e(TAG, "loi ghep", e);
                    }
                    continue;
                }
                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable e) {
                        Log.e(TAG, "loi su kien", e);
                    }
                } else {
                    try {
                        ve();
                    } catch (Throwable e) {
                        Log.e(TAG, "loi ve", e);
                    }
                    synchronized (this) {
                        if (soDaVe < dich) soDaVe = dich;
                        notifyAll();
                    }
                }
            }
        }

        /** Vẽ khung game (Canvas.paint) rồi ghép lên màn. */
        private void ve() {
            Canvas c = Display.canvasCuoi;
            if (c == null) return;
            synchronized (KHOA_MAT) {
                if (mat == null) return; // app đang ẩn: như J2ME, không gọi paint
            }
            int w = rong;
            int h = cao;
            if (Build.VERSION.SDK_INT >= 29) {
                // HD: game ghi lệnh vẽ vào RenderNode theo toạ độ logic; ghép = phát lại ở độ phân giải thật
                if (nutGame == null) {
                    nutGame = new android.graphics.RenderNode("htbb-game");
                    nutGame.setClipToBounds(true);
                }
                nutGame.setPosition(0, 0, w, h);
                android.graphics.RecordingCanvas rc = nutGame.beginRecording(w, h);
                try {
                    if (g == null) g = new Graphics(rc, w, h);
                    g.ganCanvas(rc, w, h);
                    LopPhu lp = lopPhu;
                    if (lp != null) lp.khungMoi();
                    c.paint(g);
                    if (lp != null) lp.khungXong();
                } catch (Throwable e) {
                    if (loiVe++ < 20) Log.e(TAG, "loi paint", e);
                } finally {
                    nutGame.endRecording();
                }
                coKhung = true;
                soan(c, w, h);
                return;
            }
            // API 26-28: vẽ thẳng lên bề mặt (không giữ khung để vẽ lại HUD riêng)
            synchronized (KHOA_MAT) {
                android.graphics.Canvas sc = khoaMat();
                if (sc == null) return;
                try {
                    sc.drawColor(0xFF000000);
                    int sv = sc.save();
                    sc.translate(lechX, lechY);
                    sc.scale(heSo, heSo);
                    try {
                        if (g == null) g = new Graphics(sc, w, h);
                        g.ganCanvas(sc, w, h);
                        LopPhu lp = lopPhu;
                        if (lp != null) lp.khungMoi();
                        c.paint(g);
                        if (lp != null) lp.khungXong();
                    } catch (Throwable e) {
                        if (loiVe++ < 20) Log.e(TAG, "loi paint", e);
                    }
                    sc.restoreToCount(sv);
                    veLopPhu(sc, w, h);
                } finally {
                    moMat(sc);
                }
            }
        }

        /** Ghép khung game đã ghi + lớp phủ HUD lên màn thật (gọi cả khi chỉ HUD đổi). */
        private void soan(Canvas c, int w, int h) {
            if (nutGame == null || !coKhung) return;
            synchronized (KHOA_MAT) {
                android.graphics.Canvas sc = khoaMat();
                if (sc == null) return;
                try {
                    sc.drawColor(0xFF000000);
                    int sv = sc.save();
                    sc.translate(lechX, lechY);
                    sc.scale(heSo, heSo);
                    sc.drawRenderNode(nutGame);
                    sc.restoreToCount(sv);
                    veLopPhu(sc, w, h);
                } finally {
                    moMat(sc);
                }
            }
        }

        private void veLopPhu(android.graphics.Canvas sc, int w, int h) {
            LopPhu lp = lopPhu;
            if (lp == null) return;
            int sv = sc.save();
            sc.translate(lechX, lechY);
            sc.scale(heSo, heSo);
            try {
                if (gPhu == null) gPhu = new Graphics(sc, w, h);
                gPhu.ganCanvas(sc, w, h);
                lp.ve(gPhu);
            } catch (Throwable e) {
                if (loiVe++ < 20) Log.e(TAG, "loi ve lop phu", e);
            }
            sc.restoreToCount(sv);
        }

        private android.graphics.Canvas khoaMat() {
            SurfaceHolder m = mat;
            if (m == null) return null;
            Surface s = m.getSurface();
            if (s == null || !s.isValid()) return null;
            try {
                return Build.VERSION.SDK_INT >= 26 ? s.lockHardwareCanvas() : m.lockCanvas();
            } catch (Throwable e) {
                return null;
            }
        }

        private void moMat(android.graphics.Canvas sc) {
            try {
                SurfaceHolder m = mat;
                if (m != null) m.getSurface().unlockCanvasAndPost(sc);
            } catch (Throwable e) {
                // bề mặt vừa bị huỷ
            }
        }

        /** Chỉ vẽ lại lớp phủ (HUD hoạt hình 60 hình/giây) - dùng lại khung game đã ghi. */
        public void yeuCauSoan() {
            synchronized (this) {
                if (choSoan) return;
                choSoan = true;
                notifyAll();
            }
        }
    }
}
