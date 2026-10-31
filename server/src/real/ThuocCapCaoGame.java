package real;

import com.htbb.pokemon.ThuocCapCao;
import java.util.concurrent.ConcurrentHashMap;

/**
 * THUOC CAP CAO BANG XU - phan GAME (luat thuan: com.htbb.pokemon.ThuocCapCao). Chi dung menu -30 co san nen chay tren
 * moi client (J2ME cu / moi, Unity) - cua so thuoc goi 23 chi biet gia 20 dong data_potion dau nen khong dung duoc cho
 * binh 93..95 (giong Ve dich chuyen / Thuoc hoi sinh: menu may chu + gia tinh luc mua).
 *
 * <pre>
 *  Y ta Chansey (goi 23 loai 0) muc "Thuốc cấp cao (cấp 30+)" / Kecleon tap hoa (1 / 26) muc cung ten
 *    -&gt; menu {@link #NPC_ID} idMenu 0: moi bac 1 dong "Ten +N HP: X xu" (bac chua mo: "(cấp N+)") + "Huỷ"
 *    -&gt; chon bac da mo -&gt; menu idMenu 1 + bac: "Mua 10 / 50 / 100 / 200 bình: X xu" + "Huỷ"
 *    -&gt; chon goi -&gt; kiem lai: dung canh NPC ban thuoc (Chansey / Kecleon - goi mua tu che o
 *       noi khac / nha Machamp bi chan nhu goi 24 sai cho - NpcTinhBanDo.dungCanh), cap mo, gia khong doi tu luc xem, cho trong o (999) / hanh trang, du xu
 *       -&gt; tru xu (XU_DEM = xu ra / sink) + cong potions[id] trong MOT khoa (Char.truNeuDu).
 * </pre>
 */
public final class ThuocCapCaoGame {

    /** NPC ao menu thuoc cap cao (idMenu 0 = danh sach bac, 1 + bac = chon so binh). */
    public static final int NPC_ID = -575;
    public static final String TEN_MUC = "Thuốc cấp cao (cấp " + ThuocCapCao.CAP_MO_DAU + "+)";

    /** charDBID -&gt; {bac, gia} vua hien o menu chon so binh (gia doi theo cap giua luc xem va luc chon -&gt; hoi lai). */
    static final ConcurrentHashMap<Integer, long[]> DA_HIEN = new ConcurrentHashMap<Integer, long[]>();

    private ThuocCapCaoGame() {
    }

    // ------------------------------------------------------------------ menu

    /** Danh sach bac (idMenu 0). Chua dung canh Chansey / Kecleon thi chi bao. */
    public static void moDanhSach(final Char p) {
        if (p == null) {
            return;
        }
        if (!oCho(p)) {
            DungVatPham.loi(p, "Hãy đến gần Y tá Chansey hoặc Kecleon tạp hoá để mua thuốc.");
            return;
        }
        final int cap = cap(p);
        final String[] m = new String[ThuocCapCao.soBac() + 1];
        for (int b = 0; b < ThuocCapCao.soBac(); b++) {
            m[b] = ThuocCapCao.mo(b, cap)
                    ? ten(b) + " " + ThuocCapCao.hoiChu(b) + ": " + so(ThuocCapCao.giaTheoCap(b, cap)) + " xu"
                    : ten(b) + " " + ThuocCapCao.hoiChu(b) + " (cấp " + ThuocCapCao.CAP[b] + "+)";
        }
        m[ThuocCapCao.soBac()] = "Huỷ";
        DungVatPham.baoNhe(p, ThuocCapCao.coBacMo(cap)
                ? "Thuốc cấp cao bán bằng xu, mở theo cấp (bạn đang cấp " + cap + "). Giá theo bình."
                : "Thuốc cấp cao mở từ cấp " + ThuocCapCao.CAP_MO_DAU + " (bạn đang cấp " + cap + ").");
        p.sendMessage(MessageCreator.createMsgMenu(m, NPC_ID, 0));
    }

    /** Menu chon so binh cua bac {@code bac} (idMenu 1 + bac). */
    static void moSoBinh(final Char p, final int bac) {
        final int cap = cap(p);
        if (!ThuocCapCao.hopLe(bac)) {
            return;
        }
        if (!ThuocCapCao.mo(bac, cap)) {
            DungVatPham.loi(p, ten(bac) + " mở từ cấp " + ThuocCapCao.CAP[bac] + " (bạn đang cấp " + cap + ").");
            return;
        }
        final long gia = ThuocCapCao.giaTheoCap(bac, cap);
        DA_HIEN.put(Integer.valueOf(p.charDBID), new long[]{bac, gia});
        final String[] m = new String[ThuocCapCao.SO_MUA.length + 1];
        for (int i = 0; i < ThuocCapCao.SO_MUA.length; i++) {
            m[i] = "Mua " + ThuocCapCao.SO_MUA[i] + " bình: " + so(gia * ThuocCapCao.SO_MUA[i]) + " xu";
        }
        m[ThuocCapCao.SO_MUA.length] = "Huỷ";
        DungVatPham.baoNhe(p, ten(bac) + " (" + ThuocCapCao.hoiChu(bac) + "): " + so(gia) + " xu/bình. Đang có "
                + co(p, bac) + "/" + ThuocCapCao.TOI_DA_O + ".");
        p.sendMessage(MessageCreator.createMsgMenu(m, NPC_ID, 1 + bac));
    }

    /** MapDoMenu (qua NpcDichVu.xuLyMenu): true = menu cua lop nay. */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (idNpc != NPC_ID) {
            return false;
        }
        if (p == null || chon < 0) {
            return true;
        }
        if (idMenu == 0) {
            if (chon < ThuocCapCao.soBac()) {
                if (!oCho(p)) {
                    DungVatPham.loi(p, "Hãy đến gần Y tá Chansey hoặc Kecleon tạp hoá để mua thuốc.");
                    return true;
                }
                moSoBinh(p, chon);
            }
            return true;
        }
        final int bac = idMenu - 1;
        if (ThuocCapCao.hopLe(bac) && chon < ThuocCapCao.SO_MUA.length) {
            mua(p, bac, ThuocCapCao.SO_MUA[chon]);
        }
        return true;
    }

    // ------------------------------------------------------------------ mua

    /** Mua {@code muon} binh bac {@code bac}. true = da tru xu va cong binh. */
    public static boolean mua(final Char p, final int bac, final int muon) {
        if (p == null || !ThuocCapCao.hopLe(bac) || muon <= 0) {
            return false;
        }
        if (!oCho(p)) {
            // goi menu tu che / da roi khoi cua hang: giong goi 24 sai cho (5db68468) - khong tru, ghi nghi van
            DungVatPham.loi(p, "Món này không bán ở đây. Hãy đến gần Y tá Chansey hoặc Kecleon tạp hoá.");
            try {
                data.NghiVanGianLan.ghiVatPham(p, "hackbitem", "mua thuoc cap cao id " + ThuocCapCao.ID[bac]
                        + " khong o canh NPC ban thuoc (map " + p.mapID + ")");
            } catch (final Throwable ignored) {
            }
            return false;
        }
        final int cap = cap(p);
        if (!ThuocCapCao.mo(bac, cap)) {
            DungVatPham.loi(p, ten(bac) + " mở từ cấp " + ThuocCapCao.CAP[bac] + " (bạn đang cấp " + cap + ").");
            return false;
        }
        final long gia = ThuocCapCao.giaTheoCap(bac, cap);
        final long[] hien = DA_HIEN.get(Integer.valueOf(p.charDBID));
        if (hien == null || hien[0] != bac || hien[1] != gia) {
            DungVatPham.loi(p, "Giá đã đổi theo cấp mới, xem lại giá.");
            moSoBinh(p, bac);
            return false;
        }
        final int id = ThuocCapCao.ID[bac];
        if (p.potions == null || id >= p.potions.length) {
            return false;
        }
        final int[] n = new int[1];
        final long[] tong = new long[1];
        final int[] lyDo = new int[1]; // 1 o day, 2 tui day, 3 thieu xu
        final boolean ok = p.truNeuDu(() -> {
            final int dang = p.potions[id];
            n[0] = ThuocCapCao.soMuaDuoc(dang, muon);
            tong[0] = gia * n[0];
            if (n[0] <= 0) {
                lyDo[0] = 1;
                return false;
            }
            if (dang <= 0 && p.isFullInventory()) {
                lyDo[0] = 2;
                return false;
            }
            if (p.getxu() < tong[0]) {
                lyDo[0] = 3;
                return false;
            }
            return true;
        }, () -> {
            p.subXu(tong[0], true, "mua thuoc cap cao"); // XU_DEM: xu ra (sink)
            p.potions[id] += n[0];
        });
        if (!ok) {
            if (lyDo[0] == 1) {
                DungVatPham.loi(p, "Ô " + ten(bac) + " đã đủ " + ThuocCapCao.TOI_DA_O + " bình, không mua thêm được.");
            } else if (lyDo[0] == 2) {
                DungVatPham.loi(p, "Hành trang đã đầy.");
            } else {
                DungVatPham.loi(p, "Không đủ " + so(tong[0]) + " xu để mua " + n[0] + " " + ten(bac) + ".");
            }
            return false;
        }
        try {
            data.Database.instance.saveLogThongKeChiTieu(ten(bac), 0, n[0], tong[0], "xu");
        } catch (final Throwable ignored) {
        }
        try {
            com.htbb.log.Nhat.thongTin("THUOC_CAP_CAO", "mua " + p.charname + " cap " + cap + " id " + id + " x" + n[0] + " = " + tong[0] + " xu");
        } catch (final Throwable ignored) {
        }
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Throwable ignored) {
        }
        DungVatPham.bao(p, "Đã mua " + n[0] + " " + ten(bac) + ", trả " + so(tong[0]) + " xu. Đang có " + p.potions[id] + ".");
        return true;
    }

    // ------------------------------------------------------------------ phu

    /** Dung canh Chansey / Kecleon (trong nha: nha co NPC do; ngoai troi: trong 7 o) - NpcTinhBanDo.dungCanh. */
    static boolean oCho(final Char p) {
        return NpcTinhBanDo.dungCanh(p, NpcTinhBanDo.NPC_BAN_THUOC);
    }

    /** Ten trong tui (data_potion, admin doi duoc), mac dinh ThuocCapCao.TEN. */
    static String ten(final int bac) {
        try {
            final PotionTemplate2 t = Map.potionTemplates2.get(ThuocCapCao.ID[bac]);
            if (t != null && t.name != null && !t.name.trim().isEmpty()) {
                return t.name.trim();
            }
        } catch (final Throwable ignored) {
        }
        return ThuocCapCao.TEN[bac];
    }

    static int cap(final Char p) {
        try {
            return p.lvDetail.lv;
        } catch (final Throwable e) {
            return 1;
        }
    }

    static int co(final Char p, final int bac) {
        final int id = ThuocCapCao.ID[bac];
        return p.potions == null || p.potions.length <= id ? 0 : p.potions[id];
    }

    static String so(final long n) {
        return com.htbb.pokemon.HoiSinh.so(n);
    }

    /** Test: xoa trang thai theo nhan vat. */
    public static void quen(final int charDBID) {
        DA_HIEN.remove(Integer.valueOf(charDBID));
    }
}
