package com.htbb.pokemon;

/**
 * CHUYEN LUYEN (chu game 05/11/2026): chuyen muc nang cap +N cua mot mon (nguon) sang mon khac (nhan) o Machamp tho ren
 * (NPC loai 2, menu "Chuyển luyện"). Lop THUAN (khong DB / mang) - real.ChuyenLuyenGame la phan game; luat + so lieu
 * o docs/thiet-ke-game.md "Chuyển luyện".
 *
 * Luat:
 *   - nguon va nhan CUNG o trang bi (vu khi type 3..7 la mot o; o khac phai cung type 0..2 / 8..12 / 19), cap mon nhan
 *     &gt;= cap mon nguon, +hien tai cua mon nhan &lt; +N cua nguon, ca hai trong hanh trang, khong rao ban / giao dich,
 *     khong thoi trang / thu cuoi / do thu / bao vat huyen thoai / do co han;
 *   - ket qua: nhan = +(N-1) (mat 1 cap), nguon ve +0 va GIU LAI. Dung Bua chuyen luyen ({@link #soBua}) thi nhan = +N;
 *   - xu = {@link #XU_PHAN_TRAM_MAC_DINH}% tong xu MOT lan thu len +1..+R cua mon nhan (R = cap ket qua; bang
 *     data_nang_cap_lau_dai theo bac cap mon nhan, buoc +11..+15 nhan he so bac cao nhu nang cap that). Re hon nang lai
 *     rat nhieu (nang lai con tinh lan truot);
 *   - tu ket qua +10 tro len them da luyen kim = {@link #DA_PHAN_TRAM_MAC_DINH}% tong da mot lan thu len +10..+R (lam
 *     tron len);
 *   - khoa: nguon khoa -&gt; nhan khoa (khoa do mac -&gt; khoa do mac; khoa ly do khac -&gt; khoa vinh vien). Dung da / bua
 *     khoa -&gt; nhan khoa vinh vien (nhu nang cap bang nguyen lieu khoa).
 */
public final class ChuyenLuyen {

    public static final String K_BAT = "chuyen_luyen.bat";
    public static final String K_XU_PHAN_TRAM = "chuyen_luyen.xu_phan_tram";
    public static final String K_DA_PHAN_TRAM = "chuyen_luyen.da_phan_tram";
    public static final String[] KHOA = {K_BAT, K_XU_PHAN_TRAM, K_DA_PHAN_TRAM};

    /** % tong xu mot lan thu +1..+R (mac dinh 35). */
    public static final int XU_PHAN_TRAM_MAC_DINH = 35;
    /** % tong da mot lan thu +10..+R (mac dinh 50). */
    public static final int DA_PHAN_TRAM_MAC_DINH = 50;
    /** Ket qua tu +DA_TU tro len moi can da luyen kim. */
    public static final int DA_TU = 10;
    /** data_item id "Bùa chuyển luyện" (type 28) - PHAI khop sql/htbb.sql (khoi them_chuyen_luyen.sql). */
    public static final int ID_BUA = 1262;
    /** Ma mon cua hang luong (nap_cua_hang.ma) + cach trao. */
    public static final String MA_CUA_HANG = "bua_chuyen_luyen";
    public static final int GIA_LUONG = 20;
    public static final int GIA_LUONG_KHOA = 20;
    /** Nhom o "vu khi" (type 3..7 deu mac o vu khi). */
    public static final int O_VU_KHI = 3;

    private ChuyenLuyen() {
    }

    /** O trang bi cua type mon: vu khi 3..7 -&gt; {@link #O_VU_KHI}; 0..2, 8..12, 19 -&gt; chinh type; con lai -1. */
    public static int o(final int type) {
        if (type >= 3 && type <= 7) {
            return O_VU_KHI;
        }
        if (type >= 0 && type <= 12 || type == 19) {
            return type;
        }
        return -1;
    }

    /** Thong tin mot mon cho phan kiem (phan game dien tu real.Item). */
    public static final class Mon {
        public final int id, type, capDo, plus;
        /** Dang rao ban / ky gui / trong giao dich. */
        public final boolean dangBan;
        /** Thoi trang / vu khi thoi trang / than khi / canh / suu tap / trang tri / co han / bao vat. */
        public final boolean dacBiet;

        public Mon(final int id, final int type, final int capDo, final int plus, final boolean dangBan, final boolean dacBiet) {
            this.id = id;
            this.type = type;
            this.capDo = capDo;
            this.plus = plus;
            this.dangBan = dangBan;
            this.dacBiet = dacBiet;
        }
    }

    /** Mon dung lam NGUON duoc khong (null = duoc). */
    public static String lyDoNguon(final Mon a) {
        if (a == null) {
            return "Không tìm thấy vật phẩm trong hành trang.";
        }
        if (a.dacBiet || o(a.type) < 0) {
            return "Món này không chuyển luyện được (chỉ trang bị thường; thời trang, thú cưỡi, bảo vật, đồ có hạn có cách riêng).";
        }
        if (a.dangBan) {
            return "Món đang rao bán / giao dịch, hãy rút về trước.";
        }
        if (a.plus < 1 || a.plus > 15) {
            return "Món nguồn chưa nâng cấp (+0).";
        }
        return null;
    }

    /** Cap {@code b} nhan muc +N cua {@code a} duoc khong (null = duoc, chua tinh lua chon co / khong bua). */
    public static String lyDo(final Mon a, final Mon b) {
        final String l = lyDoNguon(a);
        if (l != null) {
            return l;
        }
        if (b == null) {
            return "Không tìm thấy món nhận trong hành trang.";
        }
        if (a.id == b.id) {
            return "Hãy chọn hai món khác nhau.";
        }
        if (b.dacBiet || o(b.type) < 0) {
            return "Món nhận không chuyển luyện được (chỉ trang bị thường).";
        }
        if (b.dangBan) {
            return "Món nhận đang rao bán / giao dịch, hãy rút về trước.";
        }
        if (o(a.type) != o(b.type)) {
            return "Hai món phải cùng loại ô trang bị (đồ cầm tay sang đồ cầm tay, khăn sang khăn...).";
        }
        if (b.capDo < a.capDo) {
            return "Món nhận phải có cấp từ " + a.capDo + " trở lên.";
        }
        if (b.plus >= a.plus) {
            return "Món nhận phải đang thấp hơn +" + a.plus + ".";
        }
        return null;
    }

    /** Cap mon nhan sau khi chuyen: co bua giu +N, khong bua +(N-1). */
    public static int capKetQua(final int n, final boolean coBua) {
        return Math.max(0, coBua ? n : n - 1);
    }

    /** Lua chon co / khong bua co lam mon nhan TANG cap khong (khong tang = khong cho chon). */
    public static boolean coLoi(final Mon a, final Mon b, final boolean coBua) {
        return lyDo(a, b) == null && capKetQua(a.plus, coBua) > b.plus;
    }

    /** So Bua chuyen luyen can de giu nguyen +N: +1..+9 = 1, +10..+12 = 2, +13..+15 = 3. */
    public static int soBua(final int n) {
        return n <= 9 ? 1 : n <= 12 ? 2 : 3;
    }

    /**
     * Xu chuyen sang muc +R: {@code phanTram}% tong {@code chiPhiMuc[k]} (xu mot lan thu len +k, k = 1..R), lam tron len.
     * @param chiPhiMuc chi so 1..15 (chi so 0 bo qua)
     */
    public static long xu(final long[] chiPhiMuc, final int r, final int phanTram) {
        long tong = 0L;
        for (int k = 1; k <= Math.min(15, r) && chiPhiMuc != null && k < chiPhiMuc.length; k++) {
            tong += Math.max(0L, chiPhiMuc[k]);
        }
        final int pt = Math.max(0, Math.min(100, phanTram));
        return (tong * pt + 99L) / 100L;
    }

    /**
     * Da luyen kim chuyen sang muc +R: 0 neu R &lt; {@link #DA_TU}; con lai {@code phanTram}% tong {@code daMuc[k-1]}
     * (k = 10..R), lam tron len, toi thieu 1 khi phanTram &gt; 0.
     * @param daMuc ThongSoLauDai.DA_NC (chi so k-1)
     */
    public static int da(final int[] daMuc, final int r, final int phanTram) {
        if (r < DA_TU || daMuc == null) {
            return 0;
        }
        int tong = 0;
        for (int k = DA_TU; k <= Math.min(15, r) && k - 1 < daMuc.length; k++) {
            tong += Math.max(0, daMuc[k - 1]);
        }
        final int pt = Math.max(0, Math.min(200, phanTram));
        if (pt == 0) {
            return 0;
        }
        return Math.max(1, (tong * pt + 99) / 100);
    }

    /**
     * Khoa cua mon nhan sau khi chuyen: {lock, daMac}.
     * @param nguonLock / nguonDaMac khoa cua mon nguon
     * @param nhanLock / nhanDaMac khoa hien tai cua mon nhan
     * @param dungKhoa da dung da / bua khoa
     */
    public static int[] khoaSau(final int nguonLock, final int nguonDaMac, final int nhanLock, final int nhanDaMac,
            final boolean dungKhoa) {
        if (dungKhoa || nguonLock != 0 && nguonDaMac == 0) {
            return new int[]{1, 0}; // khoa vinh vien (khong pha khoa duoc)
        }
        if (nguonLock != 0) {
            // nguon chi khoa do mac: nhan it nhat khoa do mac (nhan da khoa vinh vien thi giu)
            return nhanLock != 0 ? new int[]{nhanLock, nhanDaMac} : new int[]{1, 1};
        }
        return new int[]{nhanLock, nhanDaMac};
    }
}
