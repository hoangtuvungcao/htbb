package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import data.Database;

/**
 * Mot lan thu nang cap +k theo thiet ke dai han (GD2 buoc 2.5/2.6). Dung CHUNG cho ca hai
 * duong: MapEpNgoc.doFinishImbue (tay) va CharCapNhat.doAutoImbue (tu dong) - CODING_CONVENTION
 * muc 3. Chi goi khi LauDai.bat().
 *
 * Khac ban cu:
 *   - gia theo bac do x muc (ThongSoLauDai.CHI_PHI_NC), da luyen kim 1/2/4 vien;
 *   - pity: moi lan truot lien tiep o cung mon cong ceil(ti le goc / 3) diem %;
 *   - truot duoi +7 giu cap, tu +7 mat 1 cap TRU KHI dung bua bao ho (luong khoa, bang
 *     BUA_BAO_HO_LUONG_KHOA). "Bua" = dat bat ky ngoc bao hiem 0-4 vao o nang cap (ban toi
 *     thieu: khong them item moi, khong doi client); ngoc bao hiem KHONG bi tru, chi tru luong khoa;
 *   - chi so tang theo % chi so goc (3%/cap toi +10, 5%/cap tu +11);
 *   - KIEM du xu + da + luong khoa ROI moi tru, tat ca trong Char.KHOA_TIEN (truNeuDu).
 */
public final class NangCapLauDaiGame {

    /** Ket qua cho noi goi hien thong bao theo kieu cua minh. */
    public static final int THIEU = -1;
    public static final int TRUOT = 0;
    public static final int THANH_CONG = 1;
    public static final int DA_TOI_DA = 2;

    private NangCapLauDaiGame() {
    }

    /** (bac * 100 + cong) -> {id item nguyen lieu phu, so luong} tu data_noi_dung_vat_lieu_nang_cap. */
    static volatile java.util.Map<Integer, int[]> NL_PHU = java.util.Collections.emptyMap();
    /** Mon "Manh tinh hoa" (doi noi dung, id 1038 - tim theo ten). Do Hoan my len +11..+15 can them 1. */
    static volatile int ID_MANH_TINH_HOA = -1;

    /** Nap nguyen lieu phu (TeamServer, bat co, SAU khi nap template vat pham). Ten -> id theo data_item. */
    public static void napNguyenLieuPhu() {
        final java.util.Map<Integer, int[]> m = new java.util.HashMap<Integer, int[]>();
        final java.util.Map<String, Integer> theoTen = new java.util.HashMap<String, Integer>();
        try {
            for (final ItemTemplates t : Map.itemTemplates.get(5).values()) {
                if (t != null && t.type == 28 && t.id >= 1001 && t.name != null) {
                    theoTen.put(t.name.trim(), Integer.valueOf(t.id));
                }
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] chua co template vat pham: " + e);
        }
        for (final Object[] d : data.DatabaseLauDai.napVatLieuNangCap()) {
            final Integer id = theoTen.get(((String) d[2]).trim());
            if (id != null && ((Integer) d[3]).intValue() > 0) {
                m.put(Integer.valueOf(((Integer) d[0]).intValue() * 100 + ((Integer) d[1]).intValue()),
                        new int[]{id.intValue(), ((Integer) d[3]).intValue()});
            }
        }
        final Integer mth = theoTen.get("Mảnh tinh hoa");
        ID_MANH_TINH_HOA = mth == null ? -1 : mth.intValue();
        NL_PHU = java.util.Collections.unmodifiableMap(m);
        System.out.println("[LAU DAI] nguyen lieu phu nang cap: " + m.size() + " dong, manh tinh hoa id " + ID_MANH_TINH_HOA);
    }

    /** So da luyen kim {@code idLkd} dung duoc theo kieu (0 thuong, 1 khoa, 2 ca hai). */
    static int soDa(final Char p, final int idLkd, final int kieu) {
        if (idLkd < 0 || idLkd >= p.listGemitem.length) {
            return 0;
        }
        if (kieu == 0) {
            return p.listGemitem[idLkd];
        }
        if (kieu == 1) {
            return p.listGemitemLock[idLkd];
        }
        return p.listGemitem[idLkd] + p.listGemitemLock[idLkd];
    }

    /** Tru {@code n} da, kieu 2 tru khoa truoc. @return true neu co da khoa bi tru (do bi khoa). */
    static boolean truDa(final Char p, final int idLkd, final int kieu, final int n) {
        if (kieu == 0) {
            p.delGem(idLkd, n, false);
            return false;
        }
        if (kieu == 1) {
            p.delGem(idLkd, n, true);
            return true;
        }
        final int khoa = Math.min(n, p.listGemitemLock[idLkd]);
        if (khoa > 0) {
            p.delGem(idLkd, khoa, true);
        }
        if (n - khoa > 0) {
            p.delGem(idLkd, n - khoa, false);
        }
        return khoa > 0;
    }

    /**
     * Thu nang {@code item} len +1.
     * @param idLkd  id ngoc luyen kim dung lam "da"
     * @param kieu   0 thuong, 1 khoa, 2 ca hai
     * @param coBua  nguoi choi dat ngoc bao hiem (= dung bua bao ho)
     * @param ra     [0] = thong bao cho nguoi choi
     * @return THIEU / TRUOT / THANH_CONG / DA_TOI_DA
     */
    public static int thu(final Char p, final Item item, final int idLkd, final int kieu, final boolean coBua,
            final String[] ra) {
        if (item.plus >= 15) {
            ra[0] = "Vật phẩm đã đạt +15.";
            return DA_TOI_DA;
        }
        final int k = item.plus + 1;
        final int capDo = item.getTemplate() != null ? item.getTemplate().level : item.level;
        final long xu = NangCapLauDai.chiPhi(capDo, k);
        final int da = NangCapLauDai.da(k);
        // Bua bao ho THAT (item, GD2b): co thi tru 1 bua thay cho luong khoa; khong co thi luong khoa nhu cu.
        final int idBua = coBua && NangCapLauDai.bua(k) > 0 ? VatPhamDungLauDai.buaChoMuc(k) : 0;
        final boolean dungItemBua = idBua > 0 && VatPhamDungLauDai.demItem(p, idBua) > 0;
        final int bua = coBua && !dungItemBua ? NangCapLauDai.bua(k) : 0;
        final boolean coBaoHo = coBua && NangCapLauDai.bua(k) > 0;
        // Nguyen lieu phu tu +11 (Bui sao / Manh sao / Manh thien thach / Vay rong) + Manh tinh hoa cho do Hoan my.
        final int[] nl = NL_PHU.get(Integer.valueOf(NangCapLauDai.bacTheoCap(capDo) * 100 + k));
        final int mth = k >= 11 && item.colorName == Item.COLOR_GREEN ? ID_MANH_TINH_HOA : -1;
        final boolean[] daKhoa = {false};
        final boolean du = p.truNeuDu(
                () -> p.xu >= xu && soDa(p, idLkd, kieu) >= da && p.lockLuong >= bua
                        && (nl == null || VatPhamDungLauDai.demItem(p, nl[0]) >= nl[1])
                        && (mth < 0 || VatPhamDungLauDai.demItem(p, mth) >= 1),
                () -> {
                    p.subXu(xu, true, "nang cap lau dai +" + k);
                    daKhoa[0] = truDa(p, idLkd, kieu, da);
                    if (bua > 0) {
                        p.subLuongLock(bua);
                    }
                    if (nl != null) {
                        VatPhamDungLauDai.truItem(p, nl[0], nl[1]);
                    }
                    if (mth >= 0) {
                        VatPhamDungLauDai.truItem(p, mth, 1);
                    }
                });
        if (!du) {
            ra[0] = "Lên +" + k + " cần " + xu + " xu, " + da + " đá luyện kim" + (bua > 0 ? ", " + bua + " lượng khoá (bùa)" : "")
                    + (nl != null ? ", " + nl[1] + " " + tenItem(nl[0]) : "") + (mth >= 0 ? ", 1 " + tenItem(mth) : "") + ".";
            return THIEU;
        }
        if (daKhoa[0]) {
            item.lock = 1;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        final int truot = h != null ? h.truot(item.id) : 0;
        final int tiLe = NangCapLauDai.tiLe(k, truot);
        final boolean thanhCong = NangCapLauDai.thanhCong(Map.random(100), tiLe);
        final int cu = item.plus;
        if (thanhCong) {
            doiChiSo(item, cu, k);
            item.plus = (byte) k;
            if (h != null) {
                h.datTruot(item.id, 0);
            }
            ra[0] = "Thành công +" + k + " (tỉ lệ " + tiLe + "%).";
            NhiemVuEngine.suKien(p, "nang_cap_cong", String.valueOf(k), 1);
            NhiemVuEngine.datMuc(p, "thanh_tuu_nang_cap", k);
        } else {
            boolean baoHo = bua > 0;
            if (!baoHo && coBaoHo && dungItemBua && NangCapLauDai.capSauTruot(cu, k, false) < cu) {
                baoHo = VatPhamDungLauDai.truItem(p, idBua, 1); // chi tieu bua khi THAT SU can (truot tu +7)
            }
            final int sau = NangCapLauDai.capSauTruot(cu, k, baoHo);
            if (sau < cu) {
                doiChiSo(item, cu, sau);
                item.plus = (byte) sau;
            }
            if (h != null) {
                h.datTruot(item.id, truot + 1);
            }
            ra[0] = "Thất bại +" + k + " (tỉ lệ " + tiLe + "%, lần sau +" + (NangCapLauDai.tiLe(k, truot + 1) - tiLe) + "%)"
                    + (sau < cu ? ", giảm còn +" + sau : "") + ".";
        }
        try {
            Database.instance.saveOrtherLog("tob_log_other_kham", p.charname,
                    "laudai imbue " + item.getTemplate().name + " " + cu + "->" + item.plus + " tl" + tiLe + " xu" + xu + " da" + da + " bua" + bua,
                    thanhCong ? "ibok" : "fib");
        } catch (final Exception ignored) {
        }
        return thanhCong ? THANH_CONG : TRUOT;
    }

    /**
     * Doi chi so tu muc +tu sang +den: tinh lai tu chi so GOC (chi so hien tai / (1 + tong% o
     * +tu)) nen len roi xuong khong troi so.
     */
    static void doiChiSo(final Item item, final int tu, final int den) {
        if (item.atb == null) {
            return;
        }
        final int pcTu = NangCapLauDai.tongChiSoPhanTram(tu);
        final int pcDen = NangCapLauDai.tongChiSoPhanTram(den);
        for (int i = 0; i < item.atb.length && i < 10; i++) {
            final int hien = item.atb[i];
            if (hien <= 0) {
                continue;
            }
            final long goc = Math.max(1L, Math.round(hien * 100.0 / (100 + pcTu)));
            long moi = Math.round(goc * (100 + pcDen) / 100.0);
            if (den > tu && moi <= hien) {
                moi = hien + 1; // do cap thap van thay tang
            }
            item.atb[i] = (short) Math.max(0L, Math.min(Short.MAX_VALUE, moi));
        }
    }

    static String tenItem(final int id) {
        try {
            return Map.itemTemplates.get(5).get(Integer.valueOf(id)).name;
        } catch (final Exception e) {
            return "#" + id;
        }
    }

    /** Co gang: LauDai.bat() de noi goi khong phai import. */
    public static boolean bat() {
        return LauDai.bat();
    }
}
