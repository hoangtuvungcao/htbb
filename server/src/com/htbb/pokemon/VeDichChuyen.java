package com.htbb.pokemon;

/**
 * VE DICH CHUYEN (chu game duyet 24/10/2026) - luat THUAN, khong I/O. Lop noi game: real.VeDichChuyenGame.
 *
 * <pre>
 *  Vat pham: potions[162] (data_potion 162, khoi SQL them_ve_dich_chuyen.sql), dung 1 lan, khong giao dich (isTrade 0).
 *  Mua:      Kecleon tap hoa (NPC tinh loai 1 / 26) -> "Vé dịch chuyển", CHI bang XU (sink). Gia theo cap nguoi mua =
 *            KinhTeLauDai.phiDichChuyen(cap) = phi.dich_chuyen_phan_tram_gio (mac dinh 3) % thu nhap 1 gio o cap do
 *            (LauDai.phanTramGio) - cung gia mot chuyen Pidgeot. Cua so thuoc goi 23 cua client chi hien gia co dinh
 *            (bang 20 dong price luc dang nhap) nen ve ban qua menu may chu + gia tinh luc mua (giong Thuoc hoi sinh).
 *  Dung:     Hanh trang -> Sử dụng -> menu may chu: lang khoi dau cua vung (dau tien) + "Thành phố đã mở" (chi ban do
 *            khong bi KhoaBanDo chan), {@link #MOI_TRANG} dong / trang. Chon -> dich chuyen, tru DUNG 1 ve khi den noi.
 *  Cam:      dang guc, Gym, pho ban / phong rieng, PvP / dau truong / cup, giao hang, khan PK / ten do, hoi chieu
 *            {@link #HOI_CHIEU_MS} giua hai lan dung.
 * </pre>
 * Kiem thu: test/KiemVeDichChuyen.
 */
public final class VeDichChuyen {

    /** id data_potion / o potions[]. */
    public static final int ID = 162;
    public static final String TEN = "Vé dịch chuyển";
    /** Hoi chieu giua hai lan dung thanh cong (giu theo nhan vat ca khi thoat / vao lai trong phien may chu). */
    public static final long HOI_CHIEU_MS = 60000L;
    /** Giu toi da trong tui (mua them bi chan). Nho de mua re o cap thap roi dung o cap cao khong dang ke. */
    public static final int GIU_TOI_DA = 20;
    /** Cac goi mua o Kecleon. */
    public static final int[] SO_MUA = {1, 5, 10};
    /** So diem den moi trang menu "Thành phố đã mở" (client J2ME hien 3 dong / man, cuon duoc; + 2 dong dieu huong). */
    public static final int MOI_TRANG = 5;
    /** Gia du phong khi bang thu nhap chua nap (= gia bay Pidgeot cu). */
    public static final long GIA_DU_PHONG = 750L;

    private VeDichChuyen() {
    }

    /** Gia 1 ve (xu) o cap {@code cap}: % thu nhap 1 gio (phi.dich_chuyen_phan_tram_gio), toi thieu 1. */
    public static long gia(final int cap) {
        try {
            return Math.max(1L, KinhTeLauDai.phiDichChuyen(cap));
        } catch (final RuntimeException e) {
            return GIA_DU_PHONG; // bang data_cap_do_lau_dai chua nap
        }
    }

    /** Con bao nhieu giay hoi chieu (0 = dung duoc). {@code lanCuoi} &lt;= 0 = chua dung. */
    public static int giayCho(final long lanCuoi, final long bay) {
        if (lanCuoi <= 0L) {
            return 0;
        }
        final long con = lanCuoi + HOI_CHIEU_MS - bay;
        return con <= 0L ? 0 : (int) ((con + 999L) / 1000L);
    }

    /** Mua them {@code n} ve co vuot {@link #GIU_TOI_DA} khong. */
    public static boolean vuotTui(final int dangCo, final int n) {
        return n <= 0 || dangCo + n > GIU_TOI_DA;
    }

    public static int soTrang(final int soDiem) {
        return Math.max(1, (soDiem + MOI_TRANG - 1) / MOI_TRANG);
    }

    public static int kepTrang(final int soDiem, final int trang) {
        return Math.max(0, Math.min(soTrang(soDiem) - 1, trang));
    }

    /**
     * Cac dong menu trang {@code trang} (ten diem den da ghep san) + dieu huong: "Trang sau (k/n)", "Trang trước",
     * "Quay lại". Tra ve mang dong; {@link #chiSo} doi lua chon -&gt; chi so diem den.
     */
    public static String[] menuTrang(final String[] ten, final int trang) {
        final int tr = kepTrang(ten.length, trang);
        final int n = soTrang(ten.length);
        final java.util.List<String> m = new java.util.ArrayList<String>();
        for (int i = tr * MOI_TRANG; i < Math.min(ten.length, (tr + 1) * MOI_TRANG); i++) {
            m.add(ten[i]);
        }
        if (tr + 1 < n) {
            m.add("Trang sau (" + (tr + 2) + "/" + n + ")");
        }
        m.add(tr > 0 ? "Trang trước" : "Quay lại");
        return m.toArray(new String[0]);
    }

    /** Lua chon {@code chon} o trang {@code trang}: &gt;= 0 chi so diem den; -1 trang sau; -2 trang truoc / quay lai. */
    public static int chiSo(final int soDiem, final int trang, final int chon) {
        final int tr = kepTrang(soDiem, trang);
        final int tu = tr * MOI_TRANG;
        final int tren = Math.min(soDiem, tu + MOI_TRANG) - tu;
        if (chon >= 0 && chon < tren) {
            return tu + chon;
        }
        final boolean coSau = tr + 1 < soTrang(soDiem);
        if (coSau && chon == tren) {
            return -1;
        }
        if (chon == tren + (coSau ? 1 : 0)) {
            return -2;
        }
        return -3; // ngoai menu
    }

    /** Ly do cam dung (null = cho). Dau vao la trang thai da doc san - game ghep o real.VeDichChuyenGame. */
    public static String lyDo(final boolean guc, final boolean gym, final boolean phongRieng, final boolean pvp,
            final boolean giaoHang, final boolean pk, final int giayCho) {
        if (guc) {
            return "Pokémon đang gục: hãy hồi sinh trước rồi mới dùng " + TEN + ".";
        }
        if (gym) {
            return "Đang đánh Gym: không dùng được " + TEN + ".";
        }
        if (phongRieng) {
            return "Đang ở hầm ngục / phó bản / phòng riêng: không dùng được " + TEN + ".";
        }
        if (pk) {
            return "Đang bật PK hoặc mang tên đỏ: không dùng được " + TEN + ".";
        }
        if (pvp) {
            return "Đang trong trận giữa người chơi (PvP, đấu trường, cúp): không dùng được " + TEN + ".";
        }
        if (giaoHang) {
            return "Đang giao hàng: không dùng được " + TEN + ".";
        }
        if (giayCho > 0) {
            return TEN + " đang hồi: chờ " + giayCho + " giây nữa.";
        }
        return null;
    }
}
