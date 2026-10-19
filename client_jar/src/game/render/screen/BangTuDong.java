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
            return "Tự chọn";
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
            d1 = System.currentTimeMillis() - TuDong.lucXin > 5000L ? "Máy chủ chưa hỗ trợ Tự động đánh" : "Đang tải...";
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

    // ------------------------------------------------------------------ 0.9.6 PC / APK: tấm cài đặt kiểu Pokémon GO

    /** Giây -> "4:37" (giờ:phút) hoặc "37 phút". */
    static String gioGon(int giay) {
        int h = giay / 3600, p = giay % 3600 / 60;
        if (h > 0) {
            return new StringBuffer().append(h).append(':').append(p < 10 ? "0" : "").append(p).toString();
        }
        return new StringBuffer().append(p).append(" phút").toString();
    }

    /** Dòng trạng thái lớn của thẻ bật / tắt. */
    static String trangThaiLon() {
        if (TuDong.dang) {
            return TuDong.coServer ? "Đang đánh · " + gioGon(TuDong.conLai()) : "Đang đánh";
        }
        return TuDong.choBat ? "Đang bật..." : "Chưa bật";
    }

    /** Vị trí kéo thanh trượt (ô {x, y, w, h} lúc vẽ) để việc chạm đọc lại. */
    private static final int[] O_HP = new int[4], O_MP = new int[4], O_VUNG = new int[4];

    private static int[] khungMoi() {
        int k = game.pokemon.Bang.cach();
        int w2 = rongCot2();
        int cTrai = caoThe() + k + caoChieu();
        int cPhai = 6 * (game.pokemon.Bang.caoHang() + k) - k;
        int noi = w2 > 0 ? Math.max(cTrai, cPhai) : cTrai + k + cPhai;
        return game.pokemon.Bang.oTam(0, game.pokemon.Bang.caoDauTam(false) + noi + 2);
    }

    /** Cao thẻ bật / tắt. */
    private static int caoThe() {
        return 8 + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.TIEU_DE) + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 10
                + game.pokemon.Bang.co(26) + 8;
    }

    private static int rChieu() {
        return Math.max(game.pokemon.VungCham.MIN / 2 + 1, game.pokemon.ChuDe.dt() ? 21 : 17);
    }

    private static int caoChieu() {
        return game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + 8 + 2 * rChieu() + 6 + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 8;
    }

    /** Bề ngang cột phải (0 = một cột: tấm hẹp). */
    private static int rongCot2() {
        int w = game.pokemon.Bang.rongTamToiDa() - 2 * game.pokemon.ChuDe.DEM;
        return w >= 360 ? (w - game.pokemon.Bang.cach()) / 2 : 0;
    }

    private static void veMoi(Graphics g) {
        final int k = game.pokemon.Bang.cach();
        game.pokemon.KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, game.pokemon.ChuDe.PHU_NEN);
        // nền chặn: chạm ngoài tấm = đóng, trong tấm (chỗ trống) = không gì (HUD bên dưới không nhận chạm)
        game.pokemon.VungCham.them(g, 0, 0, Canvas.w, Canvas.h, new game.model.IAction() {
            public void perform() {
                mo = false;
            }
        }, "nen-ngoai");
        int[] o = khungMoi();
        game.pokemon.VungCham.them(g, o[0], o[1], o[2], o[3], new game.model.IAction() {
            public void perform() {
            }
        }, "nen-tam");
        int[] c = game.pokemon.Bang.khungTam(g, o[2], o[3], "Tự động đánh", null, 0, null, new game.model.IAction() {
            public void perform() {
                mo = false;
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);
            }
        });
        int x = c[0], y = c[1], w = c[2];
        int w2 = rongCot2();
        int w1 = w2 > 0 ? w - w2 - k : w;
        // ---- thẻ bật / tắt
        int ht = caoThe();
        game.pokemon.Bang.the(g, x, y, w1, ht);
        boolean chay = TuDong.dang || TuDong.choBat;
        int vai = game.pokemon.ChuDe.TIEU_DE;
        int yy = y + 6;
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(trangThaiLon(), vai, w1 - 20), x + w1 / 2, yy, vai,
                TuDong.dang ? game.pokemon.ChuDe.CHINH_TOI : game.pokemon.ChuDe.CHU, 2);
        yy += game.pokemon.ChuDe.cao(vai) + 1;
        int hPhu = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 6;
        if (!TuDong.coServer) {
            if (System.currentTimeMillis() - TuDong.lucXin > 5000L) {
                game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat("Máy chủ chưa hỗ trợ Tự động đánh", game.pokemon.ChuDe.PHU, w1 - 16), x + w1 / 2, yy, hPhu,
                        game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
            } else {
                game.pokemon.Bang.chipCho(g, x + w1 / 2, yy, "Đang tải");
            }
        } else {
            String d2;
            if ((TuDong.svCo & TuDong.CO_TINH_NANG) == 0) {
                d2 = "Máy chủ đang tạm khoá";
            } else if (TuDong.dang && TuDong.trangThai != null && TuDong.trangThai.length() > 0) {
                d2 = TuDong.trangThai;
            } else if (TuDong.thongBao != null && TuDong.thongBao.length() > 0) {
                d2 = TuDong.thongBao;
            } else {
                d2 = "Hôm nay còn " + gioGon(TuDong.conLai()) + " / " + gioGon(TuDong.svToiDa) + " · chỉ khi online";
            }
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(d2, game.pokemon.ChuDe.PHU, w1 - 16), x + w1 / 2, yy, hPhu,
                    game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
        }
        int hb = game.pokemon.Bang.co(26);
        int wb = Math.min(w1 - 24, game.pokemon.ChuDe.dt() ? 240 : 200);
        game.pokemon.Bang.nut(g, x + (w1 - wb) / 2, y + ht - hb - 8, wb, hb, chay ? "Dừng" : "Bắt đầu",
                chay ? game.pokemon.Bang.NGUY : game.pokemon.Bang.CHINH, viec(BAT), "td-bat");
        // ---- thứ tự chiêu: 4 đĩa tròn có số
        int yc = y + ht + k;
        int hc = caoChieu();
        game.pokemon.Bang.the(g, x, yc, w1, hc);
        game.pokemon.ChuDe.chu(g, "Thứ tự chiêu", x + 10, yc + 4, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
        int rc = rChieu();
        int buoc = (w1 - 20) / 4;
        int cy = yc + 8 + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + rc;
        MainChar m = mc();
        for (int i = 0; i < 4; i++) {
            int cx = x + 10 + buoc * i + buoc / 2;
            int sk = TuDong.thuTu[i];
            boolean an = game.pokemon.Bang.dangAnTron(g, cx, cy, rc);
            int r = an ? rc * 24 / 25 : rc;
            if (sk >= 0 && m != null) {
                int mau = game.pokemon.KieuO.mauHe(PokeChieu.he(m.clazz, sk));
                GiaoDien.khoi(g, cx - r - 2, cy - r - 1, 2 * r + 4, 2 * r + 4, r + 2, game.pokemon.ChuDe.BONG);
                GiaoDien.khoi(g, cx - r - 2, cy - r - 2, 2 * r + 4, 2 * r + 4, r + 2, 0xFFFFFF);
                int oc = game.pokemon.AnhUI.oChieu(m.clazz, sk);
                if (oc < 0 || game.pokemon.ChuUI.bo == null || !game.pokemon.ChuUI.bo.chieu(g, oc, true, cx - r, cy - r, 2 * r, false)) {
                    GiaoDien.khoi(g, cx - r, cy - r, 2 * r, 2 * r, r, mau);
                    game.pokemon.KieuO.oChieuSk(g, cx - r * 7 / 10, cy - r * 7 / 10, r * 7 / 5, r * 7 / 5, m.clazz, sk, false);
                }
            } else {
                game.pokemon.Bang.diaTrang(g, cx, cy, r, an);
                game.pokemon.ChuUI b = game.pokemon.ChuUI.bo;
                if (b == null || !b.net(g, cx, cy, r / 3, game.pokemon.ChuDe.CHU_PHU, 2)) {
                    g.setColor(game.pokemon.ChuDe.CHU_PHU);
                    g.fillRect(cx - r / 3, cy, 2 * (r / 3), 1);
                    g.fillRect(cx, cy - r / 3, 1, 2 * (r / 3));
                }
            }
            // huy hiệu số thứ tự
            int rs = game.pokemon.ChuDe.dt() ? 7 : 6;
            GiaoDien.khoi(g, cx + r * 7 / 10 - rs, cy - r * 7 / 10 - rs, 2 * rs, 2 * rs, rs, 0xFFFFFF);
            GiaoDien.khoi(g, cx + r * 7 / 10 - rs + 1, cy - r * 7 / 10 - rs + 1, 2 * rs - 2, 2 * rs - 2, rs - 1, game.pokemon.ChuDe.CHINH);
            game.pokemon.ChuDe.chuGiua(g, String.valueOf(i + 1), cx + r * 7 / 10, cy - r * 7 / 10 - rs, 2 * rs, game.pokemon.ChuDe.NHAN_NHO, 0xFFFFFF, 2);
            String ten = tenChieu(sk);
            game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(ten, game.pokemon.ChuDe.PHU, buoc - 4), cx, cy + rc + 4, game.pokemon.ChuDe.PHU,
                    sk >= 0 ? game.pokemon.ChuDe.CHU : game.pokemon.ChuDe.CHU_PHU, 2);
            game.pokemon.VungCham.them(g, cx - buoc / 2 + k / 2, cy - rc, buoc - k, 2 * rc + 4 + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU), viec(CHIEU + i), "td-chieu-" + i);
        }
        // ---- cột phải: hàng thanh trượt (thuốc HP / PP, vùng đánh), hàng công tắc, hàng chọn nhặt đồ
        int xr = w2 > 0 ? x + w1 + k : x, wr = w2 > 0 ? w2 : w;
        int yr = w2 > 0 ? y : yc + hc + k;
        int nh = game.pokemon.Bang.caoHang();
        boolean thuoc = TuDong.choThuoc();
        hangTruot(g, xr, yr, wr, nh, "Thuốc HP", 0, thuoc);
        yr += nh + k;
        hangTruot(g, xr, yr, wr, nh, "Thuốc PP", 1, thuoc);
        yr += nh + k;
        hangTruot(g, xr, yr, wr, nh, "Vùng đánh", 2, true);
        yr += nh + k;
        game.pokemon.Bang.hang(g, xr, yr, wr, nh, 0, false, null, null);
        game.pokemon.Bang.hangCongTac(g, xr, yr, wr, nh, "Về hồi phục khi hết thuốc", null, TuDong.veHoiPhuc, false, viec(HOI), "td-hoi");
        yr += nh + k;
        game.pokemon.Bang.hang(g, xr, yr, wr, nh, 0, false, null, null);
        game.pokemon.Bang.hangCongTac(g, xr, yr, wr, nh, "Tự hồi sinh", !TuDong.tuHoiSinh ? null : TuDong.hoiSinhLuong ? "Dùng cả vật phẩm lượng" : "Thuốc hồi sinh thường",
                TuDong.tuHoiSinh, false, viec(HOI_SINH), "td-hoi-sinh");
        yr += nh + k;
        // nhặt đồ: chạm = mức kế tiếp (Không nhặt -> Thuốc + xu -> Thuốc, xu, đá -> Tất cả)
        game.pokemon.Bang.hang(g, xr, yr, wr, nh, 0, false, viec(NHAT), "td-nhat");
        game.pokemon.ChuDe.chuGiua(g, "Nhặt đồ", xr + 8, yr, nh, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
        String tn = TuDong.TEN_NHAT[TuDong.nhat];
        int wn = game.pokemon.ChuDe.rong(tn, game.pokemon.ChuDe.NHAN) + 18, hn = Math.min(nh - 8, game.pokemon.Bang.co(18));
        int xn = xr + wr - 8 - wn;
        GiaoDien.khoi(g, xn, yr + (nh - hn) / 2, wn, hn, hn / 2, TuDong.nhat > 0 ? game.pokemon.ChuDe.CHINH_NHAT : game.pokemon.ChuDe.NEN_PHU);
        game.pokemon.ChuDe.chuGiua(g, tn, xn + wn / 2, yr + (nh - hn) / 2, hn, game.pokemon.ChuDe.NHAN,
                TuDong.nhat > 0 ? game.pokemon.ChuDe.CHINH_TOI : game.pokemon.ChuDe.CHU_PHU, 2);
    }

    /** Mức vùng đánh được phép (máy chủ giới hạn bán kính). */
    private static int soVung() {
        int toiDa = TuDong.svBanKinh > 0 ? TuDong.svBanKinh / 16 : 99;
        int n = 1;
        for (int i = 1; i < TuDong.VUNG_O.length; i++) {
            if (TuDong.VUNG_O[i] <= toiDa) {
                n = i + 1;
            }
        }
        return n;
    }

    private static int chiSoVung() {
        for (int i = 0; i < TuDong.VUNG_O.length; i++) {
            if (TuDong.VUNG_O[i] == TuDong.vungO) {
                return Math.min(i, soVung() - 1);
            }
        }
        return 0;
    }

    /**
     * Hàng thanh trượt: nhãn trái, thanh giữa, giá trị phải (một hàng cao nh). loai 0 = thuốc HP (0..90 %), 1 = thuốc PP,
     * 2 = vùng đánh (mức VUNG_O). bat = false: xám, "Máy chủ tắt".
     */
    private static void hangTruot(Graphics g, int x, int y, int w, int nh, String nhan, final int loai, boolean bat) {
        game.pokemon.Bang.hang(g, x, y, w, nh, 0, false, null, null);
        int wl = Math.max(game.pokemon.ChuDe.rong("Vùng đánh", game.pokemon.ChuDe.NHAN), game.pokemon.ChuDe.rong(nhan, game.pokemon.ChuDe.NHAN)) + 14;
        int wv = game.pokemon.ChuDe.rong("Máy chủ tắt", game.pokemon.ChuDe.SO) + 12;
        game.pokemon.ChuDe.chuGiua(g, nhan, x + 8, y, nh, game.pokemon.ChuDe.NHAN, bat ? game.pokemon.ChuDe.CHU : game.pokemon.ChuDe.CHU_NHAT, 0);
        int[] o = loai == 0 ? O_HP : loai == 1 ? O_MP : O_VUNG;
        o[0] = x + wl;
        o[1] = y + 2;
        o[2] = w - wl - wv;
        o[3] = nh - 4;
        int max = loai == 2 ? soVung() - 1 : 90, buoc = loai == 2 ? 1 : 10;
        int gt = loai == 0 ? TuDong.nguongHp : loai == 1 ? TuDong.nguongMp : chiSoVung();
        int hien = game.pokemon.Bang.thanhTruot(g, o[0], o[1], o[2], o[3], gt, 0, max, buoc, !bat, new game.model.IAction() {
            public void perform() {
                int v = game.pokemon.Bang.giaTriTha();
                if (loai == 0) {
                    TuDong.nguongHp = v;
                } else if (loai == 1) {
                    TuDong.nguongMp = v;
                } else {
                    TuDong.vungO = TuDong.VUNG_O[Math.max(0, Math.min(TuDong.VUNG_O.length - 1, v))];
                }
                TuDong.luu();
            }
        }, loai == 0 ? "td-thuoc-hp" : loai == 1 ? "td-thuoc-pp" : "td-vung");
        String sv = !bat ? "Máy chủ tắt" : loai == 2 ? TuDong.VUNG_O[Math.max(0, Math.min(TuDong.VUNG_O.length - 1, hien))] + " ô"
                : hien <= 0 ? "Tắt" : "< " + hien + "%";
        game.pokemon.ChuDe.chuGiua(g, sv, x + w - 8, y, nh, game.pokemon.ChuDe.SO, bat && (loai == 2 || hien > 0) ? game.pokemon.ChuDe.CHINH_TOI : game.pokemon.ChuDe.CHU_PHU, 1);
    }

    /** Việc chạm mục m (cùng lam(m) của phím). */
    private static game.model.IAction viec(final int m) {
        return new game.model.IAction() {
            public void perform() {
                chon = m;
                lam(m);
            }
        };
    }

    // ------------------------------------------------------------------ chạm

    static void tick() {
        boolean click = Canvas.isPointerClick;
        if (click || Canvas.isPointerDown) {
            dungPhim = false;
        }
        if (moi()) {
            // 0.9.6: mọi chạm qua VungCham (đăng ký lúc vẽ); thanh trượt đọc Canvas.dangGiu (không bị xoá ở đây)
            Canvas.isPointerDown = false;
            Canvas.isPointerClick = false;
            return;
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
