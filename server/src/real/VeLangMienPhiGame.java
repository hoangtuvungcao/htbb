package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.VeDichChuyen;
import com.htbb.pokemon.VeLangMienPhi;

/**
 * VE LANG MIEN PHI + QUA HUY HIEU DAU TIEN - phan GAME (luat thuan: com.htbb.pokemon.VeLangMienPhi). Chi dung menu /
 * thong bao co san nen chay tren moi client dang phat hanh:
 *
 * <pre>
 *  So tay huan luyen (NPC ao -527 menu 0): dong {@link SoTayLauDai#MUC_VE_LANG} (ngay sau MUC_GOC co dinh, truoc muc
 *  huong dan tan thu) "Về làng miễn phí (còn N lượt)" -> {@link #chon}: CUNG lang dich (VeDichChuyenGame.lang), CUNG
 *  duong di (VeDichChuyenGame.DI = Map.move2Map), CUNG moi dieu cam (VeDichChuyenGame.lyDoChan) va hoi chieu 60 giay
 *  CHUNG voi Ve dich chuyen (VeDichChuyenGame.LAN_CUOI), CUNG khoa chong bam dup (VeDichChuyenGame.DANG). Tru 1 luot
 *  truoc khi di, khong toi noi thi hoan luot + bo hoi chieu.
 *  Thang Gym (GymLauDai.khiHa): {@link #khiThangGym} +luot them; {@link #quaHuyHieuDau} 1 Ve dich chuyen cho huy hieu
 *  dau tien (co l4, moi nhan vat 1 lan). Nhan vat cu da co huy hieu: {@link #khiVaoGame} (SelectCharHandler) trao bu.
 *  Tui da giu {@link VeDichChuyen#GIU_TOI_DA} ve: KHONG trao, giu co = 0 de lan dang nhap / thang Gym sau trao lai.
 * </pre>
 */
public final class VeLangMienPhiGame {

    private VeLangMienPhiGame() {
    }

    static int hom() {
        return com.htbb.pokemon.NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
    }

    /** Luot con lai hom nay; -1 = khong doc duoc ho so. */
    public static int conLai(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        return h == null ? -1 : VeLangMienPhi.conLai(h, hom(), VeLangMienPhi.moiNgay(), VeLangMienPhi.toiDa());
    }

    /** Nhan dong So tay. */
    static String nhanSoTay(final Char p) {
        final int n = conLai(p);
        return n < 0 ? VeLangMienPhi.TEN : VeLangMienPhi.nhan(n);
    }

    /** Ly do cam cua Ve dich chuyen, doi ten vat pham thanh ten tinh nang. */
    static String lyDoChan(final Char p) {
        final String ly = VeDichChuyenGame.lyDoChan(p);
        if (ly == null) {
            return null;
        }
        if (ly.startsWith(VeDichChuyen.TEN + " đang hồi")) {
            return "Về làng (miễn phí / " + VeDichChuyen.TEN + ") đang hồi" + ly.substring((VeDichChuyen.TEN + " đang hồi").length());
        }
        return ly.replace(VeDichChuyen.TEN, VeLangMienPhi.TEN);
    }

    /**
     * So tay -> "Về làng miễn phí": dich chuyen ve lang vung minh, tru 1 luot khi da toi. true = da toi va da tru dung 1
     * luot.
     */
    public static boolean chon(final Char p) {
        if (p == null) {
            return false;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            DungVatPham.loi(p, "Sổ tay tạm thời không đọc được, thử lại sau.");
            return false;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        if (!VeDichChuyenGame.DANG.add(k)) {
            return false; // goi thu hai trong luc goi dau dang chay (chung khoa voi Ve dich chuyen)
        }
        try {
            final int hom = hom();
            final int moiNgay = VeLangMienPhi.moiNgay();
            final int toiDa = VeLangMienPhi.toiDa();
            if (VeLangMienPhi.conLai(h, hom, moiNgay, toiDa) <= 0) {
                DungVatPham.loi(p, "Hết lượt " + VeLangMienPhi.TEN + " hôm nay. Thắng Gym để nhận thêm lượt (giữ tối đa " + toiDa
                        + "), hoặc dùng " + VeDichChuyen.TEN + " / Dây thoát hiểm.");
                return false;
            }
            final String ly = lyDoChan(p);
            if (ly != null) {
                DungVatPham.loi(p, ly);
                return false;
            }
            final int[] d = VeDichChuyenGame.lang(p);
            final int den = d[0];
            if (den == VeDichChuyenGame.banDoDang(p)) {
                DungVatPham.loi(p, "Bạn đang ở " + VeDichChuyenGame.ten(den) + " rồi. Không tốn lượt.");
                return false;
            }
            if (!VeDichChuyenGame.CO.co(den)) {
                DungVatPham.loi(p, VeDichChuyenGame.ten(den) + " đang tạm đóng.");
                return false;
            }
            if (KhoaBanDoGame.chan(p, den)) {
                return false; // KhoaBanDoGame da bao ly do, khong tru luot
            }
            final String hoenn = VeDichChuyenGame.lyDoHoenn(p, den);
            if (hoenn != null) {
                DungVatPham.loi(p, hoenn);
                return false;
            }
            final int loai = VeLangMienPhi.tru(h, hom, moiNgay, toiDa);
            if (loai == VeLangMienPhi.KHONG) {
                DungVatPham.loi(p, "Hết lượt " + VeLangMienPhi.TEN + " hôm nay.");
                return false;
            }
            final int tu = VeDichChuyenGame.banDoDang(p);
            final long bay = System.currentTimeMillis();
            VeDichChuyenGame.LAN_CUOI.put(k, Long.valueOf(bay)); // hoi chieu CHUNG voi Ve dich chuyen
            boolean toi = false;
            try {
                VeDichChuyenGame.DI.di(p, d[1], d[2], den);
                toi = VeDichChuyenGame.banDoDang(p) == den || (p.map != null && p.map.mapIDLoadMap == den) || p.mapID == den;
            } catch (final Throwable e) {
                com.htbb.log.Nhat.loi("VE_LANG_MIEN_PHI", "dich chuyen " + p.charname + " -> " + den, e);
            }
            if (!toi) {
                VeLangMienPhi.hoan(h, hom, loai);
                VeDichChuyenGame.LAN_CUOI.remove(k, Long.valueOf(bay));
                DungVatPham.loi(p, "Chưa về được " + VeDichChuyenGame.ten(den) + ". Lượt " + VeLangMienPhi.TEN + " vẫn còn nguyên.");
                return false;
            }
            LauDaiNhanVat.luuNgay(p, h); // luot da dung ghi ngay: tat may chu dot ngot khong tra lai luot
            final int con = VeLangMienPhi.conLai(h, hom, moiNgay, toiDa);
            VeDichChuyenGame.ghi("ve lang mien phi " + p.charname + " " + tu + " -> " + den + " ("
                    + (loai == VeLangMienPhi.LUOT_NGAY ? "luot ngay" : "luot them") + ") con " + con);
            DungVatPham.bao(p, "Đã về " + VeDichChuyenGame.ten(den) + " miễn phí. Còn " + con + " lượt " + VeLangMienPhi.TEN
                    + (con == 0 ? " (lượt mới mỗi ngày, thắng Gym được thêm lượt)." : "."));
            return true;
        } finally {
            VeDichChuyenGame.DANG.remove(k);
        }
    }

    // ------------------------------------------------------------------ Gym

    /** GymLauDai.khiHa: +luot them. Tra {luot con lai sau khi cong, so luot vua cong}. */
    static int[] khiThangGym(final Char p, final HoSoLauDai h) {
        final int toiDa = VeLangMienPhi.toiDa();
        final int cong = VeLangMienPhi.congThangGym(h, VeLangMienPhi.thangGym(), toiDa);
        return new int[]{VeLangMienPhi.conLai(h, hom(), VeLangMienPhi.moiNgay(), toiDa), cong};
    }

    /**
     * Trao 1 Ve dich chuyen cho huy hieu Gym dau tien (moi nhan vat 1 lan, co l4 trong ho so). Tra cau thong bao (da
     * trao / tui day), null = khong co gi de trao. Khong gui gi.
     */
    static String quaHuyHieuDau(final Char p, final HoSoLauDai h) {
        if (p == null || h == null || p.potions == null || p.potions.length <= VeDichChuyen.ID) {
            return null;
        }
        synchronized (h) {
            if (!VeLangMienPhi.canQuaHuyHieu(h)) {
                return null;
            }
            h.vlQuaHuyHieu = 1; // giu cho TRUOC (hai luong cung luc chi mot luong qua duoc), tui day thi tra lai
            h.ban = true;
        }
        final boolean day;
        final int co;
        synchronized (p) {
            day = p.potions[VeDichChuyen.ID] >= VeDichChuyen.GIU_TOI_DA;
            if (!day) {
                p.potions[VeDichChuyen.ID]++;
            }
            co = p.potions[VeDichChuyen.ID];
        }
        if (day) {
            synchronized (h) {
                h.vlQuaHuyHieu = 0;
                h.ban = true;
            }
            VeDichChuyenGame.ghi("qua huy hieu dau " + p.charname + ": tui day " + co + " ve, hoan lai sau");
            return "Hành trang đã giữ tối đa " + VeDichChuyen.GIU_TOI_DA + " " + VeDichChuyen.TEN
                    + ": quà huy hiệu đầu tiên (1 vé) sẽ được trao khi còn chỗ (lần đăng nhập sau hoặc lần thắng Gym tới).";
        }
        LauDaiNhanVat.luuNgay(p, h); // co da trao ghi ngay: khong trao lai sau khi khoi dong lai
        VeDichChuyenGame.ghi("qua huy hieu dau " + p.charname + ": +1 ve, con " + co);
        try {
            data.Database.instance.saveOrtherLog("", p.charname, "qua huy hieu dau +1 ve dich chuyen", "laudai");
        } catch (final Throwable ignored) {
        }
        VeDichChuyenGame.guiTui(p);
        return "Nhận thêm 1 " + VeDichChuyen.TEN + " (quà huy hiệu đầu tiên). Dùng trong Hành trang.";
    }

    /** SelectCharHandler sau khi nap tui: nhan vat da co huy hieu ma chua nhan qua huy hieu dau -> trao bu. */
    public static void khiVaoGame(final Char p) {
        try {
            final HoSoLauDai h = LauDaiNhanVat.lay(p);
            if (h == null) {
                return;
            }
            final String s = quaHuyHieuDau(p, h);
            if (s != null) {
                LauDaiNhanVat.bao(p, s);
            }
        } catch (final Throwable e) {
            System.out.println("VeLangMienPhiGame.khiVaoGame: " + e);
        }
    }
}
