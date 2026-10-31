package com.htbb.pokemon;

/**
 * BUOC NHIEM VU HO TONG (kieu "ho_tong", nhanh sua-ho-tong) - luat thuan, khong I/O. Lop noi game: real.HoTongNhiemVuGame.
 *
 * <pre>
 *  Bao loi 31/10/2026 (kazuhas4 cap 49, N-3.36 "Hộ tống Gói hàng từ Fuchsia tới Kecleon an toàn (0/1)"): "đến bản đồ rồi
 *  mà không có hướng dẫn". Buoc ho_tong chi dem khi giao xe hang van tieu cu (Char.doResponseTieu -> DiemMocNhiemVu.khiHoTong)
 *  nhung:
 *    - chu nhiem vu ghi "từ Fuchsia" - Fuchsia khong co gi; xe hang chi nhan o "Tổng tiêu đầu" (npc -50) trong 3 lang khoi
 *      dau Pewter 0 / Blackthorn 70 / Cianwood 80, va so tay khong noi o dau;
 *    - muc "Nhận hàng giao" an khi het luot van tieu HOAC myCountry != inCountry (dieu kien nuoc cu), xu ly chon muc con tu
 *      choi im lang ngoai 9h-21h / duoi cap 40 / khac nuoc;
 *    - noi giao duy nhat la "Dịch trạm" (npc -51) o Đường Victory 118 - ban do khoa cap 69 + cua ai CA-11.3: nguoi cap 12..68
 *      (N-1..N-4, TV-10.3) KHONG BAO GIO toi duoc;
 *    - giao xong van khong dem: NhiemVuMau.khop so tham so "" voi muc_tieu_id ("1" = Gói hàng, chi la ten vat).
 *  Sua: nguoi DANG LAM buoc ho_tong luon thay muc nhan hang o Tổng tiêu đầu (bo qua dieu kien nuoc / gio / cap, het luot thi
 *  duoc MOT luot mien phi moi ngay), giao duoc cho Kecleon tạp hoá trong lang (dung chu nhiem vu "tới Kecleon") hoac Dịch
 *  trạm nhu cu; so tay ghi ro noi nhan + noi giao kem o va huong.
 * </pre>
 * Kiem thu: test/KiemHoTong.
 */
public final class HoTongNhiemVu {

    /** Kieu muc tieu trong data_quest_lau_dai.kieu_muc_tieu. */
    public static final String KIEU = "ho_tong";
    public static final String TEN_NPC_NHAN = "Tổng tiêu đầu";
    public static final String TEN_NPC_GIAO = "Kecleon tạp hoá";
    public static final String TEN_DICH_TRAM = "Dịch trạm";
    /** Muc menu nhan hang (RequestNPCInfoHandler -50, idMenu 0 muc 0). */
    public static final String MUC_NHAN = "Nhận hàng giao";
    /** Ban do nap cua 3 lang co Tổng tiêu đầu (RealController.doMenuNpcNhanTieu, CharCapNhat.updateNearChar). */
    public static final int[] LANG = {0, 70, 80};
    /** Đường Victory: Dịch trạm (MapKhoiTao, npc -51), o giao 51..59 x 16..24 (TraHang). */
    public static final int BAN_DO_DICH_TRAM = 118;
    public static final int[] O_DICH_TRAM = {51, 16, 59, 24};
    /** Khung gio va cap toi thieu cua van tieu thuong (nguoi khong co nhiem vu). */
    public static final int GIO_MO = 9;
    public static final int GIO_DONG = 21;
    public static final int CAP_TOI_THIEU = 40;

    private HoTongNhiemVu() {
    }

    public static boolean laLang(final int banDoNap) {
        for (final int l : LANG) {
            if (l == banDoNap) {
                return true;
            }
        }
        return false;
    }

    /**
     * Menu Tổng tiêu đầu co muc nhan hang: con luot va dung nuoc (luat cu), HOAC dang lam buoc ho_tong (khong xet nuoc /
     * luot - luot mien phi cap luc nhan, xem {@link #mienPhi}).
     */
    public static boolean hienMucNhan(final int luot, final int myCountry, final int inCountry, final boolean dangLam) {
        return dangLam || luot > 0 && myCountry == inCountry;
    }

    /** Chu muc nhan hang: "Nhận hàng giao (2 lượt)" / "Nhận hàng giao (hàng nhiệm vụ)" khi het luot. */
    public static String chuMucNhan(final int luot, final boolean dangLam) {
        return MUC_NHAN + (luot > 0 ? " (" + luot + " lượt)" : dangLam ? " (hàng nhiệm vụ)" : "");
    }

    /**
     * Ly do khong nhan duoc xe hang (null = nhan duoc). Nguoi dang lam buoc ho_tong bo qua gio / cap / nuoc; ai cung phai
     * dung trong lang co Tổng tiêu đầu va chuc nang dang mo.
     */
    public static String lyDoKhongNhan(final boolean moVanTieu, final int gio, final int cap, final boolean dungNuoc,
            final int banDoNap, final boolean dangLam, final boolean boGio) {
        if (!moVanTieu) {
            return "Chức năng đang bảo trì";
        }
        if (!laLang(banDoNap)) {
            return "Chỉ nhận hàng ở " + TEN_NPC_NHAN + " trong làng (Pewter, Blackthorn, Cianwood).";
        }
        if (dangLam) {
            return null;
        }
        if ((gio < GIO_MO || gio >= GIO_DONG) && !boGio) {
            return "Chỉ có thể nhận hàng từ " + GIO_MO + "h đến " + GIO_DONG + "h hàng ngày";
        }
        if (cap < CAP_TOI_THIEU) {
            return "Chức năng chỉ dành cho Pokémon từ cấp độ " + CAP_TOI_THIEU + " trở lên";
        }
        if (!dungNuoc) {
            return "Chỉ nhận hàng ở làng của vùng bạn.";
        }
        return null;
    }

    /**
     * Het luot nhung dang lam buoc ho_tong va hom nay chua dung luot mien phi -> cap 1 luot (gioi han mot lan / ngay / nhan
     * vat: xe hang mien phi bi cuop / het gio thi khong nhan lai mai duoc).
     */
    public static boolean mienPhi(final int luot, final boolean dangLam, final int ngayDaCap, final int homNay) {
        return luot < 1 && dangLam && ngayDaCap != homNay;
    }

    /** Chi so bang HP / thu xe hang theo cap ((cap - 40) / 10), kep vao [0, n-1] - cap &lt; 40 (nhiem vu) va cap 100. */
    public static int chiSoCap(final int cap, final int n) {
        final int i = (cap - CAP_TOI_THIEU) / 10;
        return Math.max(0, Math.min(n - 1, i));
    }

    static String o(final int[] o) {
        return o == null ? "" : " (ô " + o[0] + "," + o[1] + ")";
    }

    /**
     * Chi dan noi nhan + noi giao (So tay "Đang làm", luc nhan nhiem vu, bao khi nhan xe hang).
     *
     * @param tenLang   lang co Tổng tiêu đầu nen toi (lang dang dung, khong thi lang nha)
     * @param oNhan     o Tổng tiêu đầu ({x, y}) hoac null
     * @param oKecleon  o Kecleon tạp hoá cung lang hoac null
     * @param tenDichTram ten ban do Dịch trạm
     * @param coXe      dang co xe hang (chi con buoc giao)
     * @param chiDuong  "" hoac doan chi duong toi noi can toi ("còn khoảng 12 ô về phía Đông")
     */
    public static String chiDan(final String tenLang, final int[] oNhan, final int[] oKecleon, final String tenDichTram,
            final boolean coXe, final String chiDuong) {
        final String lang = tenLang == null || tenLang.isEmpty() ? "làng khởi đầu" : tenLang;
        final String dich = tenDichTram == null || tenDichTram.isEmpty() ? "Đường Victory" : tenDichTram;
        final String giao = "Giao hàng: đi cạnh xe hàng tới " + TEN_NPC_GIAO + " ở " + lang + o(oKecleon)
                + " rồi nói chuyện với Kecleon, hoặc " + TEN_DICH_TRAM + " ở " + dich + " (ô " + (O_DICH_TRAM[0] + O_DICH_TRAM[2]) / 2
                + "," + (O_DICH_TRAM[1] + O_DICH_TRAM[3]) / 2 + "). Bị cướp không tính.";
        final String duong = chiDuong == null || chiDuong.isEmpty() ? "" : " (" + chiDuong + ")";
        if (coXe) {
            return "Đang có xe hàng. " + giao + duong;
        }
        return "Nhận hàng: nói chuyện với " + TEN_NPC_NHAN + " ở " + lang + o(oNhan) + ", chọn \"" + MUC_NHAN + "\"" + duong
                + ". " + giao;
    }
}
