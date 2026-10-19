package htbb.capnhat;

import game.pokemon.CapNhatNen;
import java.io.File;
import java.io.IOException;

/**
 * Phần CHUNG của bộ cập nhật PC / Android (0.9.7): luồng tải riêng, trạng thái cho hộp game.pokemon.CapNhat, huỷ, lỗi
 * thành chữ cho người chơi. Lớp con chỉ biết: cài được ở chỗ đang chạy không ({@link #coTheCai}), tải về đâu
 * ({@link #noiTai}) và cài thế nào ({@link #cai}, chạy trên luồng tải; trả câu báo người chơi).
 */
public abstract class CapNhatChung implements CapNhatNen {
    protected volatile int trangThai = CHUA;
    protected volatile long da, tong;
    protected volatile String loi = "", ghiChu = "";
    protected volatile boolean huy;
    protected volatile File tepXong;
    protected volatile String phienBan = "";
    private Thread luong;
    /** lượt tải (huỷ rồi tải lại: luồng cũ không ghi đè trạng thái lượt mới) */
    private volatile int luot;

    /** Nền tảng cài được tệp ở link này (đuôi đúng, chỗ đang chạy cho phép). */
    protected abstract boolean coTheCai(String url);

    /** Tệp đích (thư mục ghi được). */
    protected abstract File noiTai(String url, String phienBan) throws IOException;

    /** Cài tệp đã kiểm (luồng tải). Trả câu báo người chơi. */
    protected abstract String cai(File tep) throws Exception;

    public boolean hoTro(String url, String sha256) {
        return TaiVe.urlHopLe(url) && TaiVe.shaHopLe(sha256) && coTheCai(url);
    }

    /** Tên tệp từ link (bỏ ?query), chỉ chữ an toàn. */
    public static String tenTep(String url, String macDinh) {
        String u = url == null ? "" : url;
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        int h = u.indexOf('#');
        if (h >= 0) {
            u = u.substring(0, h);
        }
        String t = u.substring(u.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._-]", "_");
        while (t.startsWith(".")) {
            t = t.substring(1);
        }
        return t.length() < 3 ? macDinh : t;
    }

    public synchronized void batDau(final String url, final String sha256, final String phienBan) {
        if (trangThai == DANG_TAI || trangThai == DANG_KIEM) {
            return;
        }
        huy = false;
        da = 0;
        tong = 0;
        loi = "";
        ghiChu = "";
        tepXong = null;
        this.phienBan = phienBan == null ? "" : phienBan;
        trangThai = DANG_TAI;
        final int l = ++luot;
        luong = new Thread(new Runnable() {
            public void run() {
                chay(url, sha256, l);
            }
        }, "HTBB-CapNhat");
        luong.setDaemon(true);
        luong.start();
    }

    void chay(String url, String sha256, final int l) {
        try {
            File dich = noiTai(url, phienBan);
            File f = TaiVe.tai(url, sha256, dich, new TaiVe.TienDo() {
                public void tienDo(long d, long t) {
                    if (l == luot) {
                        da = d;
                        tong = t;
                    }
                }

                public void dangKiem() {
                    if (l == luot) {
                        trangThai = DANG_KIEM;
                    }
                }
            }, new TaiVe.Huy() {
                public boolean daHuy() {
                    return huy || l != luot;
                }
            });
            if (huy || l != luot) {
                throw new IOException("Đã huỷ");
            }
            tepXong = f;
            String g = cai(f);
            ghiChu = g == null ? "" : g;
            trangThai = XONG;
        } catch (Throwable e) {
            if (l != luot) {
                return;
            }
            String m = e.getMessage();
            loi = m == null || m.length() == 0 ? e.getClass().getSimpleName() : m;
            trangThai = huy ? CHUA : LOI;
        }
    }

    /** Chờ luồng tải xong (kiểm thử). */
    public void cho(long ms) throws InterruptedException {
        Thread t = luong;
        if (t != null) {
            t.join(ms);
        }
    }

    public int trangThai() {
        return trangThai;
    }

    public long daTai() {
        return da;
    }

    public long tong() {
        return tong;
    }

    public String loi() {
        return loi;
    }

    public String ghiChu() {
        return ghiChu;
    }

    public synchronized void huy() {
        huy = true;
        luot++;
        if (trangThai != XONG) {
            trangThai = CHUA;
        }
    }

    public boolean caiLai() {
        final File f = tepXong;
        if (f == null || !f.isFile()) {
            return false;
        }
        try {
            String g = cai(f);
            ghiChu = g == null ? "" : g;
            return true;
        } catch (Throwable e) {
            loi = String.valueOf(e.getMessage());
            trangThai = LOI;
            return true;
        }
    }
}
