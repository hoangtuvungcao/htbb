package real;

import com.htbb.pokemon.KinhTeLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.QuaHoatDongLogic;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabaseLauDai;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Qua hoat dong theo TAI KHOAN + lich dang nhap 7 ngay + lich thang (yeu cau 17/10/2026, thay
 * TichLuyOnline "dang cap nhat"). Logic thuan o com.htbb.pokemon.QuaHoatDongLogic, luu
 * tob_qua_online (1 dong / user_id). Chi chay khi LauDai.bat().
 *
 * Diem goi: CharCapNhat.doPlayerMove + ExpLauDai (hanh dong -> cong giay), CharCapNhat doi ngay
 * (-> {@link #khiDoiNgay}), So tay huan luyen muc "Quà hoạt động" (-> {@link #moMenu}).
 */
public final class QuaHoatDong {

    /** user_id -> {ngay, giay, moc, chuoiBuoc, chuoiNgay, thang, ngayThang, thangDaThuong}. */
    private static final ConcurrentHashMap<Integer, int[]> CACHE = new ConcurrentHashMap<Integer, int[]>();
    private static final ConcurrentHashMap<Integer, Long> LAN_CUOI = new ConcurrentHashMap<Integer, Long>();

    private QuaHoatDong() {
    }

    static int[] lay(final Char p) {
        if (p == null || p.userID <= 0) {
            return null;
        }
        final Integer k = Integer.valueOf(p.userID);
        int[] d = CACHE.get(k);
        if (d == null) {
            d = DatabaseLauDai.napQuaHoatDong(p.userID);
            if (d == null) {
                return null;
            }
            final int[] cu = CACHE.putIfAbsent(k, d);
            if (cu != null) {
                d = cu;
            }
        }
        final int hom = NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
        synchronized (d) {
            if (d[0] != hom) {
                d[0] = hom;
                d[1] = 0;
                d[2] = 0;
            }
        }
        return d;
    }

    static void luu(final int userId, final int[] d) {
        DatabaseLauDai.luuQuaHoatDong(userId, d);
    }

    /** Mot hanh dong (di chuyen / danh) cua nhan vat: cong giay hoat dong cho tai khoan. */
    public static void hanhDong(final Char p) {
        if (!LauDai.bat() || p == null || p.isBot != -1) {
            return;
        }
        final long bay = com.htbb.pokemon.DongHo.bay();
        final Long truoc = LAN_CUOI.put(Integer.valueOf(p.userID), Long.valueOf(bay));
        final int g = QuaHoatDongLogic.congGiay(truoc == null ? 0L : truoc.longValue(), bay);
        if (g <= 0) {
            return;
        }
        final int[] d = lay(p);
        if (d == null) {
            return;
        }
        final boolean luuNgay;
        synchronized (d) {
            final int cu = d[1] / 60;
            d[1] += g;
            luuNgay = d[1] / 60 != cu && (d[1] / 60) % 5 == 0; // luu moi 5 phut
        }
        if (luuNgay) {
            luu(p.userID, d);
        }
    }

    /** Doi ngay: chuoi 7 ngay + lich thang (1 lan / ngay / tai khoan). */
    public static void khiDoiNgay(final Char p) {
        if (!LauDai.bat()) {
            return;
        }
        final int[] d = lay(p);
        if (d == null) {
            return;
        }
        final int hom = NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
        final int thangNay = hom / 100;
        final int[] kq;
        boolean thuongThang = false;
        synchronized (d) {
            final int cach = d[4] <= 0 ? 1 : NangLuong.cachNgay(d[4], hom);
            kq = QuaHoatDongLogic.chuoi(d[3], cach);
            if (kq[1] <= 0) {
                return;
            }
            d[3] = kq[0];
            d[4] = hom;
            if (d[5] != thangNay) {
                d[5] = thangNay;
                d[6] = 0;
            }
            d[6]++;
            if (d[6] >= QuaHoatDongLogic.NGAY_THANG_CAN && d[7] != thangNay) {
                d[7] = thangNay;
                thuongThang = true;
            }
        }
        luu(p.userID, d); // danh dau truoc, phat sau
        p.addLuongLock(kq[1]);
        // Tang toc (ban khoa, TangToc): ngay 3 -> 1 Ve EXP x1,5, ngay 7 -> 1 Bua the luc.
        if (kq[0] == 3) {
            TangTocGame.thuongKhoa(p, com.htbb.pokemon.TangToc.VE_15, 1, "lich dang nhap ngay 3");
        } else if (kq[0] == 0) {
            TangTocGame.thuongKhoa(p, com.htbb.pokemon.TangToc.BUA, 1, "lich dang nhap ngay 7");
        }
        NhiemVuEngine.suKien(p, "dang_nhap", "", 1);
        LauDaiNhanVat.bao(p, "Lịch đăng nhập: nhận " + kq[1] + " lượng khoá (ngày " + (kq[0] == 0 ? 7 : kq[0]) + "/7)."
                + (kq[2] == 1 ? " Chuỗi đã bắt đầu lại do nghỉ quá 1 ngày." : ""));
        Database.instance.saveOrtherLog("", p.charname, "lich dang nhap +" + kq[1] + " lk (tk " + p.userID + ")", "laudai");
        if (thuongThang) {
            try {
                DatabaseGiftCode.applyItemString(p, "DANHHIEU2:" + LauDai.so("qua.thang.danh_hieu", 8767) + ":1:43200");
                LauDaiNhanVat.bao(p, "Lịch tháng: đủ " + QuaHoatDongLogic.NGAY_THANG_CAN + " ngày, nhận danh hiệu 30 ngày!");
                Database.instance.saveOrtherLog("", p.charname, "lich thang " + thangNay + " danh hieu", "laudai");
            } catch (final Exception e) {
                System.out.println("QuaHoatDong lich thang: " + e);
            }
        }
    }

    static void moMenu(final Char p) {
        final int[] d = lay(p);
        if (d == null) {
            return;
        }
        final String[] menu = new String[QuaHoatDongLogic.MOC_PHUT.length];
        for (int i = 0; i < menu.length; i++) {
            final boolean da = (d[2] & (1 << i)) != 0;
            menu[i] = "Hoạt động " + QuaHoatDongLogic.MOC_PHUT[i] + " phút (" + Math.min(d[1] / 60, QuaHoatDongLogic.MOC_PHUT[i])
                    + "/" + QuaHoatDongLogic.MOC_PHUT[i] + ")" + (da ? " - đã nhận" : "");
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, SoTayLauDai.NPC_ID, SoTayLauDai.MENU_QUA_HOAT_DONG));
    }

    /** Nhan moc {@code i} (kiem lai tat ca o day). */
    static String nhan(final Char p, final int i) {
        final String chanTv = com.htbb.pokemon.ThanhVien.chan(p, "qua_online"); // thanh vien: qua online
        if (chanTv != null) {
            return chanTv;
        }
        if (i < 0 || i >= QuaHoatDongLogic.MOC_PHUT.length) {
            return "Mốc không hợp lệ.";
        }
        final int[] d = lay(p);
        if (d == null) {
            return "Tạm thời không nhận được, thử lại sau.";
        }
        synchronized (d) {
            if ((d[2] & (1 << i)) != 0) {
                return "Tài khoản đã nhận mốc này hôm nay.";
            }
            if (d[1] < QuaHoatDongLogic.MOC_PHUT[i] * 60) {
                return "Chưa hoạt động đủ " + QuaHoatDongLogic.MOC_PHUT[i] + " phút (đứng yên không tính).";
            }
            d[2] |= 1 << i;
        }
        luu(p.userID, d);
        final String thuong = QuaHoatDongLogic.thuongMoc(i, RoiLauDai.ID_DA, GemTemplate.ID_MATERIAL_LOW[5][Map.random(GemTemplate.ID_MATERIAL_LOW[5].length)]);
        final long xu = LauDai.xuMoiGio(p.lvDetail.lv) * QuaHoatDongLogic.MOC_XU_PHAN_TRAM_GIO[i] / 100L;
        try {
            DatabaseGiftCode.applyItemString(p, thuong);
            if (xu > 0L) {
                p.addXu(xu, "qua hoat dong");
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharGemItem(p));
        } catch (final Exception e) {
            System.out.println("QuaHoatDong.nhan: " + e);
        }
        Database.instance.saveOrtherLog("", p.charname, "qua hoat dong moc " + i + ": " + thuong + " xu " + xu, "laudai");
        return "Nhận quà hoạt động " + QuaHoatDongLogic.MOC_PHUT[i] + " phút.";
    }

    static int luongKhoaToiDaNgay() {
        int s = 0;
        for (final int x : QuaHoatDongLogic.MOC_LUONG_KHOA) {
            s += x;
        }
        return s + KinhTeLauDai.luongKhoaDangNhap(6);
    }
}
