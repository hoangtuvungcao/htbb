package com.htbb.pokemon;

/**
 * HUONG DAN TAN THU (nhanh huong-dan): luat thuan (khong I/O) cho 10 buoc dau chuoi chinh (data_quest_moi 901..910,
 * ma HD.01..HD.10). Lop noi game: real.HuongDanTanThuGame. Noi dung (ten, loi thoai, muc tieu, thuong) nam het trong
 * CSDL - sql/htbb.sql (khoi them_huong_dan_tan_thu.sql); o day chi co luat.
 *
 * <pre>
 *  Buoc huong dan  = cac dong DAU chuoi chinh co ma "HD." (dem tu dau, dung o dong dau tien khong phai HD.) - them / bot
 *                    buoc chi la viec du lieu, khong doi chi so chuoi chinh cua nhan vat cu (chi so = vi tri trong chuoi).
 *  Buoc hanh dong  = buoc kieu 2 (thu thap) co kieu_muc_tieu la mot su kien game (dung_thuoc, hoc_chieu, nang_cap,
 *                    tha_cau...): moi lan su kien khop -> +1 vat pham nhiem vu "dau tap" (data_vat_pham_nhiem_vu) - client
 *                    0.9.7 hien tien do x/y nhu nhiem vu nhat do, khong can goi tin moi.
 *  Bo qua          = mot lan, co xac nhan; nhay toi buoc dau tien sau huong dan va NHAN DU thuong cac buoc con lai (thuoc,
 *                    trang bi, xu, EXP) - khong mat gi can ve sau.
 *  Moi             = nhan vat dang o buoc 1 chua nhan, vua vao lang (ban do nap 0 / 70 / 80): menu Lam / Bo qua (mot lan
 *                    moi phien). Nhan vat cu da qua huong dan: khong ep, chi Pokemon cap thap ({@link #CAP_XEM_LAI}) thay
 *                    muc "Huong dan tan thu" (xem lai cong dung NPC) trong So tay.
 * </pre>
 * Kiem thu: test/KiemHuongDanTanThu.
 */
public final class HuongDanTanThu {

    /** Tien to ma nhiem vu huong dan (data_quest_lau_dai.ma). */
    public static final String TIEN_TO = "HD.";
    /** Ban do nap cua ba thi tran khoi dau (Pewter / Blackthorn / Cianwood) - moi NPC huong dan dung o ca ba. */
    public static final int[] LANG = {0, 70, 80};
    /** Pokemon tu cap nay tro xuong (da qua huong dan) thay muc xem lai huong dan trong So tay. */
    public static final int CAP_XEM_LAI = 15;
    /** Id hop xac nhan bo qua (goi -32, chua dung o Map.doPopup). */
    public static final int POPUP_BO_QUA = 96;
    /** Menu NPC ao So tay (-527): moi khi vao lang / muc huong dan / danh sach NPC. */
    public static final int MENU_MOI = 40;
    public static final int MENU_SO_TAY = 41;
    public static final int MENU_NPC = 42;

    /** Kieu muc tieu "hanh dong" cua buoc huong dan (data_quest_lau_dai.kieu_muc_tieu) - moi kieu co diem moc that. */
    public static final String[] KIEU_HANH_DONG = {"dung_thuoc", "hoc_chieu", "nang_cap", "tha_cau", "cau_ca", "pokedex_gap"};

    private HuongDanTanThu() {
    }

    /** Ma nhiem vu la buoc huong dan. */
    public static boolean laMa(final String ma) {
        return ma != null && ma.startsWith(TIEN_TO);
    }

    /** So buoc huong dan = so ma "HD." lien tiep tu dau chuoi chinh (theo thu tu chi so). */
    public static int soBuoc(final String[] maTheoThuTu) {
        if (maTheoThuTu == null) {
            return 0;
        }
        int n = 0;
        while (n < maTheoThuTu.length && laMa(maTheoThuTu[n])) {
            n++;
        }
        return n;
    }

    /** Kieu muc tieu co phai hanh dong duoc dem bang su kien game khong. */
    public static boolean laKieuHanhDong(final String kieu) {
        if (kieu == null) {
            return false;
        }
        for (final String k : KIEU_HANH_DONG) {
            if (k.equals(kieu)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Su kien game (kieu, thamSo) co tinh cho buoc (kieuBuoc, mucTieuId) khong. muc_tieu_id rong = bat ky tham so; nhieu
     * gia tri cach dau phay (vd thuoc "1,2,3,21,22").
     */
    public static boolean khop(final String kieuBuoc, final String mucTieuId, final String kieu, final String thamSo) {
        if (kieuBuoc == null || kieu == null || !kieuBuoc.equals(kieu) || !laKieuHanhDong(kieu)) {
            return false;
        }
        if (mucTieuId == null || mucTieuId.trim().isEmpty()) {
            return true;
        }
        final String t = thamSo == null ? "" : thamSo.trim();
        for (final String m : mucTieuId.split(",")) {
            if (m.trim().equals(t)) {
                return true;
            }
        }
        return false;
    }

    /** Chi so chuoi chinh {@code idChinh} con trong huong dan (bo qua duoc). */
    public static boolean dangHuongDan(final int idChinh, final int soBuoc) {
        return soBuoc > 0 && idChinh >= 0 && idChinh < soBuoc;
    }

    /** Ban do nap la mot trong ba lang khoi dau. */
    public static boolean laLang(final int banDoNap) {
        for (final int l : LANG) {
            if (l == banDoNap) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moi Lam / Bo qua khi vao lang: nhan vat moi tinh (buoc 1, chua nhan), o lang, chua moi trong phien nay.
     * @param trangThai NewCharQuest.state_quest (0 = chua nhan)
     */
    public static boolean nenMoi(final int idChinh, final int trangThai, final int banDoNap, final int soBuoc, final boolean daMoi) {
        return !daMoi && soBuoc > 0 && idChinh == 0 && trangThai == 0 && laLang(banDoNap);
    }

    /** Muc "Huong dan tan thu" (xem lai cong dung NPC) trong So tay: dang huong dan, hoac Pokemon cap thap da qua. */
    public static boolean coMucXemLai(final int cap, final int idChinh, final int soBuoc) {
        return dangHuongDan(idChinh, soBuoc) || cap <= CAP_XEM_LAI;
    }

    /** Chi so cac buoc con lai se phat thuong khi bo qua: tu buoc hien tai (chua tra) toi het huong dan. */
    public static int[] buocConLai(final int idChinh, final int soBuoc) {
        if (!dangHuongDan(idChinh, soBuoc)) {
            return new int[0];
        }
        final int[] kq = new int[soBuoc - idChinh];
        for (int i = 0; i < kq.length; i++) {
            kq[i] = idChinh + i;
        }
        return kq;
    }

    /** Thay {khoa} trong mau chu (CSDL) theo cap khoa / gia tri. */
    public static String thay(final String mau, final String... khoaGiaTri) {
        if (mau == null) {
            return "";
        }
        String s = mau;
        for (int i = 0; i + 1 < khoaGiaTri.length; i += 2) {
            s = s.replace("{" + khoaGiaTri[i] + "}", khoaGiaTri[i + 1] == null ? "" : khoaGiaTri[i + 1]);
        }
        return s;
    }

    /** "1.234" (dau cham hang nghin, nhu dong thuong cua nhiem vu). */
    public static String so(final long n) {
        final String s = String.valueOf(Math.abs(n));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (n < 0 ? "-" : "") + b;
    }
}
