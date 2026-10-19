package game.render.screen;

import game.model.MainChar;
import game.pokemon.GiaoDien;
import game.pokemon.PokeChieu;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Bảng "Tự động đánh" (bản jar / APK) kiểu Pokémon như Menu nhanh: Bắt đầu / Dừng, thứ tự 4 chiêu, ngưỡng thuốc
 * HP / MP, lọc nhặt đồ, về hồi phục, vùng đánh; dòng trên cùng là giờ còn lại hôm nay do MÁY CHỦ báo (lệnh -100).
 * Logic + lưu cài đặt ở TuDong (code mod dùng chung). Mở từ Menu nhanh, menu chính (hàng đầu), phím # -> "Tự động
 * đánh", hoặc bảng Auto gốc của game. Đóng bằng nút x, chạm ra ngoài, phím mềm phải, # hoặc *.
 */
final class BangTuDong {
    static boolean mo;
    private static int chon;
    private static int dau;
    private static boolean dungPhim;

    private static final int BAT = 0, CHIEU = 1, HP = 5, MP = 6, NHAT = 7, HOI = 8, VUNG = 9, HOI_SINH = 10;
    private static final int SO_MUC = 11;
    private static final int[] HP_MUC = new int[]{0, 30, 50, 70};
    private static final int[] MP_MUC = new int[]{0, 20, 30, 50};

    private BangTuDong() {
    }

    /** Mở bảng (và xin máy chủ trạng thái mới). */
    static void mo() {
        TuDong.nap();
        MenuNhanh.mo = false;
        mo = true;
        chon = 0;
        dau = 0;
        dungPhim = !Nen.coNutUp();
        TuDong.xinTrangThai();
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.MO);
    }

    // ------------------------------------------------------------------ chữ từng mục

    private static MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    private static String tenChieu(int s) {
        MainChar m = mc();
        if (s < 0) {
            return "tự chọn";
        }
        String t = m == null ? null : PokeChieu.ten(m.clazz, s);
        return t == null ? "chiêu " + (s + 1) : t;
    }

    private static String ten(int m) {
        switch (m) {
            case BAT:
                return TuDong.dang || TuDong.choBat ? "Dừng tự động" : "Bắt đầu";
            case HP:
                if (!TuDong.choThuoc()) {
                    return "Thuốc: máy chủ tắt";
                }
                return TuDong.nguongHp <= 0 ? "Thuốc HP: tắt" : "Thuốc HP < " + TuDong.nguongHp + "%";
            case MP:
                if (!TuDong.choThuoc()) {
                    return "Thuốc MP: máy chủ tắt";
                }
                return TuDong.nguongMp <= 0 ? "Thuốc MP: tắt" : "Thuốc MP < " + TuDong.nguongMp + "%";
            case NHAT:
                return "Nhặt: " + TuDong.TEN_NHAT[TuDong.nhat];
            case HOI:
                return TuDong.veHoiPhuc ? "Về hồi phục: bật" : "Về hồi phục: tắt";
            case VUNG:
                return "Vùng: " + TuDong.banKinh() / 16 + " ô";
            case HOI_SINH:
                return !TuDong.tuHoiSinh ? "Tự hồi sinh: tắt" : (TuDong.hoiSinhLuong ? "Tự hồi sinh: cả đồ lượng" : "Tự hồi sinh: thuốc thường");
            default:
                return (m - CHIEU + 1) + ". " + tenChieu(TuDong.thuTu[m - CHIEU]);
        }
    }

    /** 1 bật (xanh lá), 0 tắt (xám), -1 lệnh (xanh dương), -2 dừng (đỏ). */
    private static int trangThai(int m) {
        switch (m) {
            case BAT:
                return TuDong.dang || TuDong.choBat ? -2 : 1;
            case HP:
                return TuDong.choThuoc() && TuDong.nguongHp > 0 ? 1 : 0;
            case MP:
                return TuDong.choThuoc() && TuDong.nguongMp > 0 ? 1 : 0;
            case NHAT:
                return TuDong.nhat > 0 ? 1 : 0;
            case HOI:
                return TuDong.veHoiPhuc ? 1 : 0;
            case HOI_SINH:
                return TuDong.tuHoiSinh ? 1 : 0;
            case VUNG:
                return -1;
            default:
                return -1;
        }
    }

    // ------------------------------------------------------------------ đổi cài đặt

    private static int sau(int[] ds, int v) {
        for (int i = 0; i < ds.length; ++i) {
            if (ds[i] == v) {
                return ds[(i + 1) % ds.length];
            }
        }
        return ds[0];
    }

    /** Chiêu tiếp theo cho ô thứ i: tự chọn -> các chiêu đánh đã học (không trùng ô khác) -> tự chọn. */
    private static void doiChieu(int i) {
        byte[] ds = TuDong.chieuDanh(mc());
        int hien = TuDong.thuTu[i];
        int vt = -1;
        for (int k = 0; k < ds.length; ++k) {
            if (ds[k] == hien) {
                vt = k;
            }
        }
        for (int buoc = vt + 1; buoc <= ds.length; ++buoc) {
            if (buoc == ds.length) {
                TuDong.thuTu[i] = -1;
                return;
            }
            boolean trung = false;
            for (int j = 0; j < TuDong.thuTu.length; ++j) {
                trung |= j != i && TuDong.thuTu[j] == ds[buoc];
            }
            if (!trung) {
                TuDong.thuTu[i] = ds[buoc];
                return;
            }
        }
        TuDong.thuTu[i] = -1;
    }

    private static void lam(int m) {
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);
        switch (m) {
            case BAT:
                TuDong.batTat();
                if (TuDong.choBat) {
                    mo = false; // đóng bảng: bắt đầu đánh ngay khi máy chủ cho
                }
                return;
            case HP:
                if (TuDong.choThuoc()) {
                    TuDong.nguongHp = sau(HP_MUC, TuDong.nguongHp);
                }
                break;
            case MP:
                if (TuDong.choThuoc()) {
                    TuDong.nguongMp = sau(MP_MUC, TuDong.nguongMp);
                }
                break;
            case NHAT:
                TuDong.nhat = (TuDong.nhat + 1) % TuDong.TEN_NHAT.length;
                break;
            case HOI:
                TuDong.veHoiPhuc = !TuDong.veHoiPhuc;
                break;
            case HOI_SINH:
                // tắt -> thuốc thường (xu) -> cả vật phẩm lượng (đồng ý riêng, máy chủ còn phải cho) -> tắt
                if (!TuDong.tuHoiSinh) {
                    TuDong.tuHoiSinh = true;
                    TuDong.hoiSinhLuong = false;
                } else if (!TuDong.hoiSinhLuong) {
                    TuDong.hoiSinhLuong = true;
                } else {
                    TuDong.tuHoiSinh = false;
                    TuDong.hoiSinhLuong = false;
                }
                break;
            case VUNG: {
                int toiDa = TuDong.svBanKinh > 0 ? TuDong.svBanKinh / 16 : 99;
                int v = sau(TuDong.VUNG_O, TuDong.vungO);
                if (v > toiDa) {
                    v = TuDong.VUNG_O[0];
                }
                TuDong.vungO = v;
                break;
            }
            default:
                doiChieu(m - CHIEU);
                break;
        }
        TuDong.luu();
    }

    // ------------------------------------------------------------------ bố cục

    private static int cot() {
        return Canvas.w >= 200 ? 2 : 1;
    }

    private static boolean moi() {
        return game.pokemon.ChuUI.bo != null;
    }

    /** khe giữa các ô (bảng mới: đủ khoảng cách chạm) */
    private static int khe() {
        return moi() ? Math.max(4, game.pokemon.VungCham.CACH) : 3;
    }

    private static int rongBang() {
        int w = Canvas.w - 12;
        int max = moi() ? 340 : 260;
        return w > max ? max : w;
    }

    private static int rongO() {
        int c = cot();
        return (rongBang() - 12 - (c - 1) * Math.max(4, khe())) / c;
    }

    private static int caoO() {
        int h = Font.blackFont.getHeight() + 5;
        return moi() && game.pokemon.VungCham.MIN > h ? game.pokemon.VungCham.MIN : h;
    }

    private static int caoChu() {
        return Font.borderFont.getHeight() + 1;
    }

    /** Hàng (theo bố cục): mục BAT chiếm cả hàng 0, các mục sau xếp theo cột. */
    private static int hangCua(int m) {
        return m == BAT ? 0 : 1 + (m - 1) / cot();
    }

    private static int soHang() {
        return hangCua(SO_MUC - 1) + 1;
    }

    private static int dauBang() {
        return (moi() ? game.pokemon.ChuDe.caoDau() + 4 : 24) + 2 * caoChu() + 4;
    }

    private static int hangHien() {
        int conCho = Canvas.h - Screen.deltaY - 8 - dauBang() - 6;
        int n = conCho / (caoO() + khe());
        if (n < 2) {
            n = 2;
        }
        return n > soHang() ? soHang() : n;
    }

    private static int caoBang() {
        return dauBang() + hangHien() * (caoO() + khe()) + 4;
    }

    private static int xBang() {
        return (Canvas.w - rongBang()) / 2;
    }

    private static int yBang() {
        int y = (Canvas.h - Screen.deltaY - caoBang()) / 2;
        return y < 2 ? 2 : y;
    }

    private static int xO(int m) {
        if (m == BAT) {
            return xBang() + 6;
        }
        return xBang() + 6 + ((m - 1) % cot()) * (rongO() + Math.max(4, khe()));
    }

    private static int rongMuc(int m) {
        return m == BAT ? rongBang() - 12 : rongO();
    }

    /** y của mục (có thể ngoài vùng hiện nếu cuộn). */
    private static int yO(int m) {
        return yBang() + dauBang() + (hangCua(m) - dau) * (caoO() + khe());
    }

    private static boolean hien(int m) {
        int h = hangCua(m) - dau;
        return h >= 0 && h < hangHien();
    }

    private static void cuonToi(int m) {
        int h = hangCua(m);
        if (h < dau) {
            dau = h;
        }
        if (h >= dau + hangHien()) {
            dau = h - hangHien() + 1;
        }
    }

    private static boolean trong(int x, int y, int x0, int y0, int w, int h) {
        return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
    }

    // ------------------------------------------------------------------ vẽ

    static void ve(Graphics g) {
        Canvas.resetTrans(g);
        if (moi()) {
            veMoi(g);
            return;
        }
        int x0 = xBang();
        int y0 = yBang();
        int w = rongBang();
        int hB = caoBang();
        GiaoDien.bang(g, x0, y0, w, hB);
        GiaoDien.dai(g, x0 + 4, y0 + 4, w - 8, 17, "Tự động đánh");
        // nút đóng x
        int xd = x0 + w - 20;
        GiaoDien.khoi(g, xd, y0 + 6, 13, 13, 6, GiaoDien.VIEN);
        GiaoDien.khoi(g, xd + 1, y0 + 7, 11, 11, 5, GiaoDien.TRANG);
        g.setColor(GiaoDien.DO);
        g.drawLine(xd + 4, y0 + 10, xd + 8, y0 + 14);
        g.drawLine(xd + 8, y0 + 10, xd + 4, y0 + 14);
        g.drawLine(xd + 5, y0 + 10, xd + 9, y0 + 14);
        g.drawLine(xd + 9, y0 + 10, xd + 5, y0 + 14);
        // dòng giờ còn lại (máy chủ báo) + thông báo / ghi chú
        String d1;
        if (!TuDong.coServer) {
            d1 = System.currentTimeMillis() - TuDong.lucXin > 5000L ? "Máy chủ chưa hỗ trợ Tự động đánh" : "Đang hỏi máy chủ...";
        } else if ((TuDong.svCo & TuDong.CO_TINH_NANG) == 0) {
            d1 = "Máy chủ đang tạm khoá";
        } else {
            d1 = "Hôm nay còn " + TuDong.gio(TuDong.conLai()) + " / " + TuDong.gio(TuDong.svToiDa);
        }
        String d2 = TuDong.dang ? "Đang chạy: " + TuDong.trangThai : TuDong.thongBao;
        if (d2 == null || d2.length() == 0) {
            d2 = "Chỉ khi online - EXP theo năng lượng";
        }
        int yChu = y0 + 24;
        Font.borderFont.drawString(g, GiaoDien.catChu(Font.borderFont, d1, w - 12), x0 + w / 2, yChu, Font.CENTER);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, d2, w - 12), x0 + w / 2, yChu + caoChu(), Font.CENTER);
        int hO = caoO();
        for (int m = 0; m < SO_MUC; ++m) {
            if (!hien(m)) {
                continue;
            }
            int tt = trangThai(m);
            int x = xO(m);
            int y = yO(m);
            int wO = rongMuc(m);
            if (m == chon && (dungPhim || !Nen.coNutUp())) {
                GiaoDien.khoi(g, x - 2, y - 2, wO + 4, hO + 4, hO / 2 + 2, GiaoDien.VANG);
            }
            int mau = tt == 1 ? GiaoDien.LA_TOI : (tt == 0 ? 0xD8DEE8 : (tt == -2 ? GiaoDien.DO : GiaoDien.XANH));
            int mau2 = tt == 1 ? GiaoDien.LA : (tt == 0 ? GiaoDien.TRANG : (tt == -2 ? GiaoDien.DO_SANG : GiaoDien.XANH_SANG));
            if (m == BAT && tt == 1) {
                mau = GiaoDien.LA_TOI;
                mau2 = GiaoDien.LA;
            }
            GiaoDien.khoi(g, x, y, wO, hO, hO / 2, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, x + 1, y + 1, wO - 2, hO - 2, hO / 2 - 1, mau2, mau);
            Font f = tt == 0 ? Font.blackFont : Font.borderFont;
            // ô chiêu: icon chiêu (PNG) bên trái chữ
            int ic = 0;
            if (m >= CHIEU && m < HP && TuDong.thuTu[m - CHIEU] >= 0 && mc() != null && game.pokemon.ChuUI.bo != null) {
                ic = hO - 4;
                game.pokemon.KieuO.oChieuSk(g, x + hO / 2 - 2, y + 2, ic, ic, mc().clazz, TuDong.thuTu[m - CHIEU], false);
                ic += 2;
            }
            String s = GiaoDien.catChu(f, ten(m), wO - 10 - ic);
            f.drawString(g, s, x + ic + (wO - ic) / 2, y + (hO - f.getHeight()) / 2 + 1, Font.CENTER);
        }
        // mũi tên cuộn (màn nhỏ)
        if (dau > 0 || dau + hangHien() < soHang()) {
            g.setColor(GiaoDien.VIEN);
            int xm = x0 + w - 10;
            if (dau > 0) {
                g.fillTriangle(xm, y0 + dauBang() - 6, xm - 4, y0 + dauBang() - 1, xm + 4, y0 + dauBang() - 1);
            }
            if (dau + hangHien() < soHang()) {
                g.fillTriangle(xm, y0 + hB - 1, xm - 4, y0 + hB - 6, xm + 4, y0 + hB - 6);
            }
        }
        Canvas.resetTrans(g);
        g.drawImage(game.render.Res.imgCmdBar, 0, Canvas.h + 3, Graphics.BOTTOM | Graphics.LEFT);
        if (dungPhim || !Nen.coNutUp()) {
            Font.borderFont.drawString(g, "Chọn", 5, Canvas.h - Screen.deltaY, Font.LEFT);
        }
        Font.borderFont.drawString(g, "Đóng", Canvas.w - 5, Canvas.h - Screen.deltaY, Font.RIGHT);
    }

    /** 0.9.5 PC / APK: bảng sáng chung (đầu đỏ mảnh + X), nút phẳng theo trạng thái, icon chiêu PNG. */
    private static void veMoi(Graphics g) {
        int x0 = xBang(), y0 = yBang(), w = rongBang(), hB = caoBang();
        game.pokemon.KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, game.pokemon.ChuDe.PHU_NEN);
        game.pokemon.Bang.khung(g, x0, y0, w, hB, "Tự động đánh", null, 0, null, null);
        int hd = game.pokemon.ChuDe.caoDau();
        // X (chạm xử lý ở tick: góc phải trên 24x24 -> đóng)
        game.pokemon.Bang.nutDong(g, x0 + w - 1 - hd, y0 + 1, hd, null, true);
        String d1;
        if (!TuDong.coServer) {
            d1 = System.currentTimeMillis() - TuDong.lucXin > 5000L ? "Máy chủ chưa hỗ trợ Tự động đánh" : "Đang hỏi máy chủ...";
        } else if ((TuDong.svCo & TuDong.CO_TINH_NANG) == 0) {
            d1 = "Máy chủ đang tạm khoá";
        } else {
            d1 = "Hôm nay còn " + TuDong.gio(TuDong.conLai()) + " / " + TuDong.gio(TuDong.svToiDa);
        }
        String d2 = TuDong.dang ? "Đang chạy: " + TuDong.trangThai : TuDong.thongBao;
        if (d2 == null || d2.length() == 0) {
            d2 = "Chỉ khi online · EXP theo năng lượng";
        }
        int yChu = y0 + hd + 4;
        game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(d1, game.pokemon.ChuDe.NHAN, w - 12), x0 + w / 2, yChu, caoChu(),
                game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 2);
        game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(d2, game.pokemon.ChuDe.PHU, w - 12), x0 + w / 2, yChu + caoChu(), caoChu(),
                game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
        int hO = caoO();
        for (int m = 0; m < SO_MUC; ++m) {
            if (!hien(m)) {
                continue;
            }
            int tt = trangThai(m);
            int x = xO(m), y = yO(m), wO = rongMuc(m);
            if (m == chon && (dungPhim || !Nen.coNutUp())) {
                GiaoDien.khoi(g, x - 2, y - 2, wO + 4, hO + 4, game.pokemon.ChuDe.BO_NUT + 2, game.pokemon.ChuDe.VANG);
            }
            int kieu = tt == 1 ? game.pokemon.Bang.XAC_NHAN : tt == 0 ? game.pokemon.Bang.TRUNG : tt == -2 ? game.pokemon.Bang.CHINH : game.pokemon.Bang.PHU;
            game.pokemon.Bang.nut(g, x, y, wO, hO, "", kieu, null, null);
            int ic = 0;
            if (m >= CHIEU && m < HP && TuDong.thuTu[m - CHIEU] >= 0 && mc() != null) {
                ic = hO - 6;
                game.pokemon.KieuO.oChieuSk(g, x + 4, y + 3, ic, ic, mc().clazz, TuDong.thuTu[m - CHIEU], false);
                ic += 4;
            }
            int mau = tt == 0 ? game.pokemon.ChuDe.CHU : 0xFFFFFF;
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(ten(m), game.pokemon.ChuDe.NHAN, wO - 10 - ic), x + ic + (wO - ic) / 2, y, hO,
                    game.pokemon.ChuDe.NHAN, mau, 2);
        }
        if (dau > 0 || dau + hangHien() < soHang()) {
            g.setColor(game.pokemon.ChuDe.CHU_PHU);
            int xm = x0 + w - 10;
            if (dau > 0) {
                g.fillTriangle(xm, y0 + dauBang() - 6, xm - 4, y0 + dauBang() - 1, xm + 4, y0 + dauBang() - 1);
            }
            if (dau + hangHien() < soHang()) {
                g.fillTriangle(xm, y0 + hB - 1, xm - 4, y0 + hB - 6, xm + 4, y0 + hB - 6);
            }
        }
    }

    // ------------------------------------------------------------------ chạm

    static void tick() {
        boolean click = Canvas.isPointerClick;
        if (click || Canvas.isPointerDown) {
            dungPhim = false;
        }
        Canvas.isPointerDown = false;
        Canvas.isPointerClick = false;
        if (!click) {
            return;
        }
        int x0 = xBang();
        int y0 = yBang();
        int w = rongBang();
        int hB = caoBang();
        int sx = moi() ? game.pokemon.ChuDe.caoDau() + 2 : 24;
        if (!trong(Canvas.px, Canvas.py, x0, y0, w, hB) || trong(Canvas.px, Canvas.py, x0 + w - sx, y0, sx, sx)) {
            mo = false;
            return;
        }
        // nửa trên / dưới vùng mũi tên: cuộn
        if (dau > 0 && trong(Canvas.px, Canvas.py, x0 + w - 18, y0 + dauBang() - 10, 18, 12)) {
            --dau;
            return;
        }
        if (dau + hangHien() < soHang() && trong(Canvas.px, Canvas.py, x0 + w - 18, y0 + hB - 10, 18, 10)) {
            ++dau;
            return;
        }
        for (int m = 0; m < SO_MUC; ++m) {
            if (!hien(m)) {
                continue;
            }
            int x = xO(m);
            int y = yO(m);
            if (trong(Canvas.px, Canvas.py, x, y, rongMuc(m), caoO()) && trong(Canvas.pxLast, Canvas.pyLast, x - 4, y - 4, rongMuc(m) + 8, caoO() + 8)) {
                chon = m;
                lam(m);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ phím

    static boolean phim(int keyCode) {
        dungPhim = true;
        if (keyCode == -7 || keyCode == -22 || keyCode == 35 || keyCode == 42) {
            mo = false;
            return true;
        }
        if (keyCode == -5 || keyCode == 53 || keyCode == 10 || keyCode == -6 || keyCode == -21) {
            lam(chon);
            return true;
        }
        int ga = 0;
        try {
            ga = Canvas.instance.getGameAction(keyCode);
        } catch (Throwable e) {
            // phím không có hành động game
        }
        int c = cot();
        if (keyCode == 50 || ga == javax.microedition.lcdui.Canvas.UP) {
            chon = chon == BAT ? SO_MUC - 1 : (chon - c < 1 ? BAT : chon - c);
        } else if (keyCode == 56 || ga == javax.microedition.lcdui.Canvas.DOWN) {
            chon = chon == BAT ? 1 : (chon + c >= SO_MUC ? BAT : chon + c);
        } else if (keyCode == 52 || ga == javax.microedition.lcdui.Canvas.LEFT) {
            chon = (chon - 1 + SO_MUC) % SO_MUC;
        } else if (keyCode == 54 || ga == javax.microedition.lcdui.Canvas.RIGHT) {
            chon = (chon + 1) % SO_MUC;
        } else if (ga == javax.microedition.lcdui.Canvas.FIRE) {
            lam(chon);
        }
        cuonToi(chon);
        return true;
    }

    // ------------------------------------------------------------------ thanh trạng thái (dưới khung HP)

    static void veThanh(Graphics g, String s, int y) {
        if (Canvas.currentScreen != Canvas.gameScr || mo) {
            return;
        }
        Canvas.resetTrans(g);
        Font f = Font.borderFont;
        String c = GiaoDien.catChu(f, s, Canvas.w - 24);
        int w = f.getWidth(c) + 14;
        int h = f.getHeight() + 3;
        int x = 4;
        GiaoDien.khoi(g, x, y, w, h, h / 2, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, h / 2 - 1, TuDong.dang ? GiaoDien.LA : GiaoDien.XANH_SANG, TuDong.dang ? GiaoDien.LA_TOI : GiaoDien.XANH);
        f.drawString(g, c, x + 7, y + 2, Font.LEFT);
    }
}
