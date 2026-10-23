package real;

import data.Animal;
import data.Database;

/**
 * SAO GOI / TRUNG THU CUOI dang "thuoc" (potions[]) -&gt; thu cuoi VINH VIEN trong O thu (0.9.14).
 *
 * <p>Loi cu (chu nhan 0.9.13: mua "Sao goi Tauros" o cua hang, vao Hanh trang bam Su dung khong duoc): shop 45..49
 * (data_shop) giao potions[64..68] qua nhanh chung doBuySpecialItem (potions[id + 19]); bam Su dung (lenh 22) roi vao
 * nhanh "Khong the su dung" cua doUsePotion / MapDungThuoc - chi NPC ap trung (MapDoMenu NPC -6, menu "Machamp,
 * Arcanine...") moi doi sao thanh thu. Gio mot bang duy nhat {@link #THU}: ca lenh 22 (DungVatPham.dungThuoc) va NPC
 * ap trung deu goi {@link #tao}: kiem O thu (QuanLyThuGame.chanNhan, 0.9.12) TRUOC khi tru, tru dung 1, tao Animal cap
 * 1 khong han, luu, ghi log.
 */
public final class ThuCuoiTuTui {

    /** {id thuoc, Animal.idImg} ; ten = {@link #TEN}. Thu tu = thu tu menu NPC ap trung (MapDoMenu -6) 0..9. */
    static final int[][] THU = {
        {64, 2}, {65, 1}, {66, 3}, {67, 4}, {68, 0}, {86, 5}, {70, 8}, {74, Animal.NGUA_XUONG}, {72, Animal.HEO}, {115, Animal.LAN},
    };
    static final String[] TEN = {
        "Tauros", "Arcanine", "Mightyena", "Pidgeot", "Rapidash", "Moltres", "Articuno", "Spectrier", "Piloswine", "Suicune",
    };

    private ThuCuoiTuTui() {
    }

    static int viTri(final int thuoc) {
        for (int i = 0; i < THU.length; i++) {
            if (THU[i][0] == thuoc) {
                return i;
            }
        }
        return -1;
    }

    public static boolean la(final int thuoc) {
        return viTri(thuoc) >= 0;
    }

    /** Thuoc o menu NPC ap trung {@code chon} (0..9), -1 ngoai bang. */
    static int thuocMenu(final int chon) {
        return chon >= 0 && chon < THU.length ? THU[chon][0] : -1;
    }

    public static String ten(final int thuoc) {
        final int i = viTri(thuoc);
        return i < 0 ? "" : TEN[i];
    }

    /**
     * Dung 1 sao / trung {@code thuoc}: null = da tao thu (da tru 1, da bao); khac = ly do KHONG tao (khong tru, da bao
     * loi qua DungVatPham). {@code nguon} vao log.
     */
    public static String tao(final Char p, final int thuoc, final String nguon) {
        final int i = viTri(thuoc);
        if (p == null || i < 0) {
            return "Không phải vật phẩm gọi thú cưỡi.";
        }
        if (p.potions == null || thuoc >= p.potions.length || p.potions[thuoc] <= 0) {
            final String s = "Chưa có " + tenVatPham(thuoc) + ".";
            DungVatPham.loi(p, s);
            guiTui(p);
            return s;
        }
        if (QuanLyThuGame.chanNhan(p, nguon + " " + TEN[i])) { // o thu day: bao cau chuan, ghi log, KHONG tru
            guiTui(p);
            return QuanLyThuGame.cauDay(p);
        }
        p.potions[thuoc]--;
        final Animal a = new Animal();
        a.name = TEN[i];
        a.idImg = (byte) THU[i][1];
        a.createAtt();
        a.level = 1;
        a.place = 0;
        a.ownerId = p.charDBID;
        a.id = p.getIDItem();
        p.animal.add(a);
        guiTui(p);
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
        } catch (final Exception ignored) {
        }
        if (Database.instance != null) {
            try {
                Database.instance.saveAnimal(a);
                Database.instance.saveOrtherLog("tob_log_other_animal", p.charname,
                        "Tao " + a.name + " tu " + tenVatPham(thuoc) + " (" + nguon + ") | " + a.getAttribute() + "_con_" + p.potions[thuoc], "crt");
            } catch (final Exception ignored) {
            }
        }
        DungVatPham.bao(p, "Đã nhận thú cưỡi " + a.name + " (vĩnh viễn). Xem ở Ô thú để cưỡi.");
        return null;
    }

    static String tenVatPham(final int thuoc) {
        final String t = DungVatPham.tenThuoc(thuoc);
        return "vật phẩm".equals(t) ? "vật phẩm gọi " + ten(thuoc) : t;
    }

    static void guiTui(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
    }
}
