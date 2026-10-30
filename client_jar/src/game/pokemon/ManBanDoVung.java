package game.pokemon;

import game.model.IAction;
import game.model.Quest;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * MÀN BẢN ĐỒ VÙNG (người chơi 30/10/2026: "thêm cái mở bản đồ vùng để xem cho tiện"). Lối vào: chạm bản đồ nhỏ (HUD PC /
 * APK), Menu › Bản đồ vùng, phím * (máy J2ME bấm phím, không nhắm người chơi), menu chữ Cài đặt › Bản đồ vùng.
 * Tấm có tab Kanto | Johto | Hoenn (mở ở vùng đang đứng), bản đồ BanDoVung, dòng dưới: điểm đang chọn (chạm điểm / phím
 * 2 8) + điều kiện khoá, dấu sao nhiệm vụ; J2ME thêm dòng phím. Đóng: nút X, phím mềm phải / Esc, phím * lần nữa.
 * Chỉ xem - không dịch chuyển.
 */
public final class ManBanDoVung extends ManBang {
    public static ManBanDoVung me;
    /** điểm đang chọn của tab hiện tại (-1 = điểm đang đứng / không) */
    int chon = -1;

    public static ManBanDoVung gI() {
        if (me == null) {
            me = new ManBanDoVung();
        }
        return me;
    }

    /** Mở ở vùng đang đứng (không rõ vùng: vùng có điểm nhiệm vụ, không thì Kanto). */
    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManBanDoVung m = gI();
        int v = BanDoVung.vungCua(BanDoVung.mapHienTai());
        if (v < 0) {
            v = BanDoVung.vungCua(BanDoVung.mapNhiemVu());
        }
        m.moBang();
        m.chonTab(v < 0 ? BanDoVung.KANTO : v);
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentScreen == me;
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        this.chon = BanDoVung.diem(t, BanDoVung.mapHienTai());
        if (this.chon < 0) {
            this.chon = BanDoVung.diem(t, BanDoVung.mapNhiemVu());
        }
    }

    /** Đổi sang tab vùng v (0 Kanto, 1 Johto, 2 Hoenn). */
    public void vung(int v) {
        chonTab(v);
    }

    protected String tieuDe() {
        return "Bản đồ vùng";
    }

    protected String[] tabs() {
        return BanDoVung.TEN_VUNG;
    }

    public void update() {
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        // phím điều hướng (game action); phím số 2 4 6 8 / * ở keyPress (Canvas chỉ đổi phím số ở màn chơi)
        if (Canvas.isKeyPressed(4)) {
            buoc(4);
        } else if (Canvas.isKeyPressed(6)) {
            buoc(6);
        } else if (Canvas.isKeyPressed(2)) {
            buoc(2);
        } else if (Canvas.isKeyPressed(8)) {
            buoc(8);
        } else if (Canvas.isKeyPressed(10)) {
            dong();
        }
    }

    /** 4 / 6 = vùng trước / sau, 2 / 8 = điểm trước / sau. */
    void buoc(int phim) {
        int n = BanDoVung.soDiem(this.tab);
        if (phim == 4) {
            chonTab((this.tab + 2) % 3);
        } else if (phim == 6) {
            chonTab((this.tab + 1) % 3);
        } else if (phim == 2) {
            this.chon = this.chon <= 0 ? n - 1 : this.chon - 1;
        } else if (phim == 8) {
            this.chon = this.chon < 0 || this.chon >= n - 1 ? 0 : this.chon + 1;
        }
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        switch (keyCode) {
            case 50:
            case 52:
            case 54:
            case 56:
                buoc(keyCode - 48);
                return true;
            case 42:
                dong();   // phím * lần nữa = đóng (mở bằng *)
                return true;
            default:
                return super.keyPress(keyCode);
        }
    }

    /** Dòng phím cho máy J2ME (vẽ bằng font ảnh, không chữ thật). */
    static boolean dongPhim() {
        return ChuUI.bo == null;
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        final int v = this.tab;
        int n = BanDoVung.soDiem(v);
        if (this.chon >= n) {
            this.chon = -1;
        }
        int hd = ChuDe.cao(ChuDe.NHAN) + 4;
        Quest q = DiemNhiemVu.nhiemVu();
        int mapNv = q == null ? -1 : q.dichMap;
        String nv = null;
        if (mapNv >= 0) {
            int vNv = BanDoVung.vungCua(mapNv);
            String noi = q.dichNoi != null && q.dichNoi.length() > 0 ? q.dichNoi : BanDoVung.tenMap(mapNv);
            nv = "Nhiệm vụ: " + noi + (vNv >= 0 && vNv != v ? " (" + BanDoVung.TEN_VUNG[vNv] + ")" : "");
        }
        String phim = dongPhim() ? "4 6: vùng  2 8: chọn  phím *: đóng" : null;
        String thongTin = thongTin(v);
        // dòng dưới: thông tin điểm (trái) + nhiệm vụ (phải) chung một hàng khi đủ rộng
        int wNv = nv == null ? 0 : ChuDe.rong(nv, ChuDe.NHAN) + 22;
        int wTt = ChuDe.rong(thongTin, ChuDe.NHAN) + 8;
        boolean mot = nv == null || wTt + wNv + 12 <= w;
        int soDong = (mot ? 1 : 2) + (phim != null ? 1 : 0);
        int hDuoi = soDong * hd + 4;
        int hMap = h - hDuoi - 2;
        BanDoVung.ve(g, v, x, y, w, hMap, this.chon);
        final int mx = x, my = y, mw = w, mh = hMap;
        VungCham.them(g, mx, my, mw, mh, new IAction() {
            public void perform() {
                int i = BanDoVung.diemTai(Canvas.px, Canvas.py);
                if (i >= 0) {
                    ManBanDoVung.this.chon = i;
                    AmThanh.phat(AmThanh.XAC_NHAN);
                }
            }
        }, "bdv-ban-do");
        int yd = y + hMap + 4;
        if (mot) {
            ChuDe.chuGiua(g, ChuDe.cat(thongTin, ChuDe.NHAN, w - wNv - 8), x + 2, yd, hd, ChuDe.NHAN, ChuDe.CHU, 0);
            if (nv != null) {
                veNhiemVu(g, nv, x + w - wNv, yd, wNv, hd);
            }
            yd += hd;
        } else {
            ChuDe.chuGiua(g, ChuDe.cat(thongTin, ChuDe.NHAN, w - 4), x + 2, yd, hd, ChuDe.NHAN, ChuDe.CHU, 0);
            yd += hd;
            veNhiemVu(g, nv, x, yd, Math.min(w, wNv), hd);
            yd += hd;
        }
        if (phim != null) {
            ChuDe.chuGiua(g, ChuDe.cat(phim, ChuDe.PHU, w - 4), x + 2, yd, hd, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        }
    }

    /** "★ Nhiệm vụ: ..." - sao vẽ hình (font ảnh J2ME không có ★). */
    static void veNhiemVu(Graphics g, String nv, int x, int y, int w, int h) {
        int cy = y + h / 2;
        DiemNhiemVu.sao(g, x + 8, cy, 7, GiaoDien.VIEN);
        DiemNhiemVu.sao(g, x + 8, cy, 5, GiaoDien.VANG);
        ChuDe.chuGiua(g, ChuDe.cat(nv, ChuDe.NHAN, w - 20), x + 18, y, h, ChuDe.NHAN, ChuDe.CHINH_TOI, 0);
    }

    /** Dòng thông tin: điểm đang chọn (đang đứng / khoá + điều kiện), hoặc nơi đang đứng khi ở vùng khác. */
    String thongTin(int v) {
        int cur = BanDoVung.diem(v, BanDoVung.mapHienTai());
        int i = this.chon >= 0 ? this.chon : cur;
        if (i < 0) {
            int m = BanDoVung.mapHienTai();
            int vm = BanDoVung.vungCua(m);
            String t = BanDoVung.tenMap(m);
            if (t.length() == 0) {
                return "Chạm một điểm để xem tên";
            }
            return "Bạn đang ở: " + t + (vm >= 0 && vm != v ? " (" + BanDoVung.TEN_VUNG[vm] + ")" : "");
        }
        int id = BanDoVung.idDiem(v, i);
        String ten = BanDoVung.ten(v, i);
        if (i == cur) {
            String o = BanDoVung.tenMap(BanDoVung.mapHienTai());
            return "Bạn đang ở: " + (o.length() > 0 && !o.equals(ten) ? o + " (gần " + ten + ")" : ten);
        }
        if (KhoaMap.khoa(id)) {
            String yc = KhoaMap.yeuCau(id);
            return ten + ": " + (yc.length() > 0 ? yc : "đang khoá");
        }
        return ten;
    }
}
