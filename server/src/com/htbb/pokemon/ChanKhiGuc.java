package com.htbb.pokemon;

/**
 * Lenh client bi tu choi khi nhan vat dang guc (HP &lt;= 0), truoc khi vao bo xu ly (RealController.processMessage).
 * Client that che bang hop hoi sinh, nhung client sua (hoac bot cb2 31/10/2026: guc roi van noi chuyen NPC, cuoi Arcanine
 * ve lang, mua do, qua cong) dung duoc nhan vat guc de di chuyen / mua ban mien phi. Chan: noi chuyen / menu NPC (gom
 * Arcanine / Pidgeot / Lapras, "Về làng" trong So tay), hop nhap so / popup cua NPC, warp, mua / ban / gian hang / kho /
 * ren / khảm, giao dich, qua cong. Van cho moi duong hoi sinh: hop moi -101 (tan thu, thuoc, xu, luong khoa, Trung tam,
 * dong doi), lenh 31 "Về làng" client cu, menu cu {@link #MENU_HOI_SINH}, popup cu cua chinh minh (-32), dung Cỏ / Thuốc
 * hồi sinh (29 / 22 - bo xu ly rieng da chan vat pham khac khi guc), xac nhan nap ban do (12 voi -500), tu ve Trung tam
 * theo gio (may chu tu chay); chat, ping, dang xuat, di chuyen (may chu bo qua khi guc).
 */
public final class ChanKhiGuc {

    public static final String CAU = "Bạn đã bị hạ gục – hãy hồi sinh trước.";

    /**
     * Menu -30 cua hoi sinh: -802 (menu cu "Dùng cỏ hồi sinh / Hs bằng xu", Map / CharVongLap -&gt; HoiSinhGame.chanCu),
     * -803 / -804 / -805 (real.HoiSinhGame MENU_CU / MENU_DONG_DOI / MENU_MUA_THUOC).
     */
    public static final int[] MENU_HOI_SINH = {-802, -803, -804, -805};

    /** Lenh luon chan khi guc (khong phu thuoc noi dung). */
    static final int[] LENH_CHAN = {
        23, 52, 53, 55,                 // noi chuyen / nhan nhiem vu / menu NPC
        24, 28, 85, -76,                // mua / ban o NPC, mua vat pham dac biet, cua hang So tay
        62, 92, 94, 95,                 // gian hang ky gui
        66,                             // giao dich
        68, 69, 70,                     // kho do
        74, 78,                         // da quy / nguyen lieu
        71, 72, 75, 76, 77, -34, -35, -36, -68, -69, // tho ren
        -31,                            // tra loi hop nhap so cua NPC (so luong ve / hang)
        -33,                            // di qua diem dich chuyen (warp)
        // 29 (dung vat pham) KHONG chan o day: DungVatPham.dung da xu ly guc (khiGuc: chi vat pham hoi sinh - Cỏ / Thuốc
        // hồi sinh trong tui; con lai "Hãy hồi sinh trước"). 22 (thuoc): DungVatPham.dungThuoc chi cho Thuoc hoi sinh 119.
    };

    private ChanKhiGuc() {
    }

    /**
     * Lenh {@code cmd} co bi chan khi guc khong. {@code dau} = short dau tien cua goi (id NPC voi -30, id ban do dich
     * voi 12, id nguoi gui popup voi -32; Integer.MIN_VALUE neu goi rong). {@code idMinh} = Char.id cua nguoi choi.
     */
    public static boolean chan(final int cmd, final int dau, final int idMinh) {
        if (cmd == -32) {
            // tra loi popup: chi cho popup cua chinh minh (popup hoi sinh cu -1 / 1 cua client cu gui idActor = minh);
            // hop NPC / hop 95 (thach dau Gym, nhan cua ai...) bi chan
            return dau != (short) idMinh;
        }
        return chan(cmd, dau);
    }

    /** Nhu {@link #chan(int, int, int)} cho lenh khong phu thuoc nguoi choi (-32 luon chan). */
    public static boolean chan(final int cmd, final int dau) {
        if (cmd == -32) {
            return true;
        }
        if (cmd == -30) {
            for (final int m : MENU_HOI_SINH) {
                if (dau == m) {
                    return false;
                }
            }
            return true;
        }
        if (cmd == 12) {
            return dau != -500 && dau != Integer.MIN_VALUE; // -500: client bao da nap xong ban do (sau hoi sinh ve lang)
        }
        for (final int c : LENH_CHAN) {
            if (c == cmd) {
                return true;
            }
        }
        return false;
    }

    /** Short dau tien cua goi (khong lam mat vi tri doc), Integer.MIN_VALUE neu khong doc duoc. */
    public static int docDau(final io.Message m) {
        if (m == null || m.dis == null) {
            return Integer.MIN_VALUE;
        }
        try {
            m.dis.mark(4);
            try {
                return m.dis.readShort();
            } finally {
                m.dis.reset();
            }
        } catch (final Exception e) {
            return Integer.MIN_VALUE;
        }
    }
}
