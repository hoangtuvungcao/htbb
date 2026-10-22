package real;

import data.Database;

/**
 * Bai luyen cong (them 22/09/2026, theo yeu cau nguoi dung): dich chuyen nhanh toi 6 bai
 * quai theo chuc (3x/4x/5x/6x/7x/8x), mo tu menu "Khac" cua NPC Tong Quan (idNpc=-516,
 * xem Map.java case type==-10000 va MapDoMenu.java idNpc==-516).
 *
 * DANH SACH BAI: chon tu bao cao ra soat file map/map<N> (agent doi chieu voi data_monster,
 * xem lich su trao doi) - moi bai la MAP CAO NHAT trong tung chuc (vi du 3x co ca Loc tri
 * 30-32/Son lam 33-36/Phong chau 37-39, chon Phong chau vi cao nhat). Toa do la 1 o quai
 * spawn gan tam ban do nhat (chac chan di duoc, khong bi ket tuong), doc truc tiep tu file
 * map bang script rieng, KHONG phai nguoi dung cung cap. Ten map 28 ("Canh rung dong") da
 * duoc nguoi dung xac nhan tay 22/09/2026 (khong co trong bang ten chuan Map.namMap).
 *
 * PHI/DIEU KIEN (theo yeu cau nguoi dung, tu dien giai cac diem chua ro - xem lai voi nguoi
 * dung neu sai):
 *   - Nguoi choi thuong (vip==0) PHAI co The mua ban (Item.THE_MUA_BAN, id 675) trong ruong.
 *     Vip>=1 (Ca Cung/Dai Gia/Vip) MIEN thẻ.
 *   - Phi xu: Vip (player.vip==3) MIEN PHI. Ca Cung/Dai Gia/nguoi thuong (vip 0-2) deu tra
 *     dung bang gia ben duoi (nguoi dung chi noi "Ca Cung mat phi giong nguoi thuong co
 *     the", KHONG noi Dai Gia gia khac - nen dung CHUNG mot bang gia cho ca 3 hang khong-Vip).
 *   - Chenh lech level: |level nhan vat - level dai dien cua bai| > 5 thi khong bay duoc.
 *   - Cooldown 10s giua hai lan bay (tinh tu lan bay GAN NHAT, khong phan biet bai nao).
 */
public final class BaiLuyenTap {

    /** Virtual npcId cho submenu chon bai (xem quy uoc "idNpc == -" trong Map.doMenu). */
    public static final int NPC_ID = -525;

    private static final class Bai {
        final String ten;
        final String tenMap;
        final int mapId;
        final int tileX;
        final int tileY;
        final int levelDaiDien;
        final int giaXu;

        Bai(final String ten, final String tenMap, final int mapId, final int tileX, final int tileY,
                final int levelDaiDien, final int giaXu) {
            this.ten = ten;
            this.tenMap = tenMap;
            this.mapId = mapId;
            this.tileX = tileX;
            this.tileY = tileY;
            this.levelDaiDien = levelDaiDien;
            this.giaXu = giaXu;
        }
    }

    private static final Bai[] DANH_SACH = {
        new Bai("Bãi 3x", "Phong châu", 13, 35, 47, 35, 10000),
        new Bai("Bãi 4x", "Hồi hồ", 114, 61, 67, 45, 10000),
        new Bai("Bãi 5x", "Tế giang", 19, 39, 22, 55, 15000),
        new Bai("Bãi 6x", "Chiến hạm", 22, 29, 34, 65, 20000),
        new Bai("Bãi 7x", "Đỗ đông", 24, 30, 39, 75, 25000),
        new Bai("Bãi 8x", "Cánh rừng đông", 28, 44, 31, 85, 30000),
    };

    /**
     * Chenh lech level toi da giua nhan vat va bai de con duoc bay toi.
     *
     * Sua 22/09/2026 (yeu cau nguoi dung, phat hien qua vi du "lv61 chon Bai 6x"): moc
     * levelDaiDien o tren truoc la level QUAI THAT SU cua map cao nhat trong tung chuc (vd
     * Bai 6x = Chien ham, quai 68-70, moc=69) - lam +-5 chi lot level ~64-74, loai mat nua
     * duoi cua chinh chuc do (lv61 la "6x" that nhung bi tu choi). Doi moc ve TAM CHUC
     * (35/45/55/65/75/85) de +-5 khop dung dai "Nx" = level N0..N9.
     */
    private static final int CHENH_LECH_LEVEL_TOI_DA = 5;

    /** Cooldown giua hai lan bay, xem Char.timeCuoiBayBaiLuyenCong. */
    public static final long COOLDOWN_MS = 10000L;

    /** Phi (xu) moi lan "Auto Ve Map Up" tu dong bay lai vi tri up, tru Vip. */
    public static final long PHI_AUTO_VE_MAP_UP = 15000L;

    private BaiLuyenTap() {
    }

    /**
     * GD2 2.8: khi bat co sv.lau_dai, gia dich chuyen = 3% thu nhap 1 gio o cap nguoi dung
     * (KinhTeLauDai.phiDichChuyen) thay bang gia co dinh. Dung chung cho xa phu (Map),
     * bai luyen cong va auto ve map up.
     */
    public static long giaDichChuyen(final Char p, final long giaCu) {
        if (!com.htbb.pokemon.LauDai.bat() || p == null || p.lvDetail == null) {
            return giaCu;
        }
        return com.htbb.pokemon.KinhTeLauDai.phiDichChuyen(p.lvDetail.lv);
    }

    public static String[] danhSachTen() {
        final String[] ten = new String[DANH_SACH.length];
        for (int i = 0; i < DANH_SACH.length; i++) {
            ten[i] = DANH_SACH[i].ten;
        }
        return ten;
    }

    /** Mo submenu chon bai - goi tu MapDoMenu khi player bam muc "Bãi luyện công" o NPC -10. */
    public static void mo(final Char player) {
        player.sendMessage(MessageCreator.createMsgMenu(danhSachTen(), NPC_ID, 0));
    }

    /** Xu ly chon bai trong submenu (idNpc == NPC_ID). */
    public static void doMenu(final Char player, final int idOptionMenu) {
        if (idOptionMenu < 0 || idOptionMenu >= DANH_SACH.length) {
            return;
        }
        final Bai bai = DANH_SACH[idOptionMenu];

        final long ngay = System.currentTimeMillis();
        final long conLai = player.timeCuoiBayBaiLuyenCong + COOLDOWN_MS - ngay;
        if (conLai > 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Cần chờ " + ((conLai + 999) / 1000) + " giây nữa mới có thể bay tiếp.", ""));
            return;
        }

        if (Math.abs(player.lvDetail.lv - bai.levelDaiDien) > CHENH_LECH_LEVEL_TOI_DA) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Cấp của bạn chênh lệch quá " + CHENH_LECH_LEVEL_TOI_DA
                            + " so với " + bai.ten + ", không thể bay đến.", ""));
            return;
        }

        // Nguoi choi thuong (khong hang) phai co The mua ban, hang Ca Cung tro len (vip>=1)
        // duoc mien - xem chu thich dau file.
        if (player.vip < 1 && !player.checkItemTheMuaBan()) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Cần có thẻ bán đồ tại chỗ trong hành trang để sử dụng bãi luyện tập.", ""));
            return;
        }

        // Vip (hang 3) mien phi hoan toan, cac hang con lai (0/1/2) tra dung bang gia.
        if (player.vip < 3) {
            final long gia = giaDichChuyen(player, bai.giaXu);
            if (!player.truXuNeuDu(gia, false, "bai luyen cong " + bai.ten)) {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Không đủ " + gia + " xu để bay đến " + bai.ten + ".", ""));
                return;
            }
        }

        player.timeCuoiBayBaiLuyenCong = ngay;
        player.map.move2Map(player, bai.tileX, bai.tileY, bai.mapId, player.inCountry);
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã bay đến " + bai.ten + " (" + bai.tenMap + ").", ""));
        Database.instance.saveOrtherLog("", player.charname,
                player.charname + " bay den " + bai.ten + " (" + bai.tenMap + ")", "bailuyencong");
    }

    /**
     * True neu map nay la mot trong 6 bai luyen cong - dung de biet co ap dung "Auto Ve Map
     * Up" khi chet hay khong (xem Char.actorDie()).
     */
    public static boolean isMapBai(final int mapId) {
        for (final Bai b : DANH_SACH) {
            if (b.mapId == mapId) {
                return true;
            }
        }
        return false;
    }
}
