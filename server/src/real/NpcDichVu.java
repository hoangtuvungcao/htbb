package real;

import io.Message;

/**
 * Menu dich vu cua NPC tinh khong co menu rieng trong game goc (client gui goi 23 voi loai NPC, server tra menu -30
 * voi id NPC = loai; nguoi choi chon -> MapDoMenu goi {@link #xuLyMenu}).
 *
 *   - Y ta Chansey (loai 0, Trung tam Pokemon cua thi tran): hoi phuc HP / PP mien phi, mua thuoc hoi phuc.
 *     Truoc day goi 23 loai 0 khong co nhanh nao -> client nhan goi rong, bam Chansey khong co gi xay ra.
 *   - Meowth thuong nhan / doi xu (loai 22 / 31): ngan hang - kho xu / luong, chuyen luong, cua hang va nap
 *     (khi bat nap). Truoc day muc "Nap xu" cua client la nap SMS cu (danh sach rong -> "tam khoa").
 *
 * Moi muc kiem nguoi choi dang dung canh dung NPC (NpcTinhBanDo) - goi tu xa khong lam gi ngoai bao loi.
 */
public final class NpcDichVu {

    public static final int NPC_Y_TA = 0;
    public static final int NPC_MEOWTH = 22;
    public static final int NPC_MEOWTH_DOI_XU = 31;
    public static final int NPC_LAPRAS = 10;
    public static final int NPC_PIDGEOT = 7;
    /** Gia mot chuyen bay Pidgeot (xu, truoc giam theo thu nhap BaiLuyenTap.giaDichChuyen) - bang tuyen bay cu 500..750. */
    static final long GIA_BAY = 750L;
    /** Ve tau (potion 20) - mua o Lapras (goi 81, Map.doBuyTicket). */
    static final int VE_TAU = 20;

    /** Danh sach thuoc ban o Trung tam / tap hoa (id potionTemplates, cung danh sach Kecleon - goi 23 loai 1). */
    public static final int[] THUOC_BAN = {1, 2, 3, 4, 5, 6, 14, 15, 16, 17, 18, 19};

    private NpcDichVu() {
    }

    /** RequestNPCInfoHandler: goi 23 cua loai NPC {@code loai}. true = da tra loi. */
    public static boolean moMenu(final Char p, final int loai) {
        if (p == null) {
            return false;
        }
        switch (loai) {
            case NPC_Y_TA:
                if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_Y_TA)) {
                    bao(p, "Hãy đến gần Y tá Chansey ở Trung tâm Pokémon.");
                    return true;
                }
                p.sendMessage(MessageCreator.createMsgMenu(new String[]{
                    "Hồi phục Pokémon (miễn phí)",
                    "Mua thuốc hồi phục",
                }, NPC_Y_TA, 0));
                return true;
            case NPC_PIDGEOT:
                moBay(p);
                return true;
            case NPC_MEOWTH:
            case NPC_MEOWTH_DOI_XU:
                if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_NGAN_HANG)) {
                    bao(p, "Hãy đến gần Meowth để dùng ngân hàng.");
                    return true;
                }
                p.sendMessage(MessageCreator.createMsgMenu(menuNganHang(), loai, 0));
                return true;
            default:
                return false;
        }
    }

    static String[] menuNganHang() {
        if (com.htbb.pokemon.NapTien.bat()) {
            return new String[]{"Kho xu, lượng", "Chuyển lượng", com.htbb.pokemon.NapTienGame.TEN_MUC};
        }
        return new String[]{"Kho xu, lượng", "Chuyển lượng"};
    }

    /** Cac ben do Lapras co the toi tu ban do {@code tuLoad} (moi ban do co NPC Lapras, tru ban do hien tai). */
    static java.util.List<Integer> benDo(final int tuLoad) {
        final java.util.List<Integer> kq = new java.util.ArrayList<Integer>();
        for (final Integer id : NpcTinhBanDo.banDoCo(NPC_LAPRAS)) {
            if (id.intValue() != tuLoad) {
                kq.add(id);
            }
        }
        return kq;
    }

    /**
     * Map.doUp2Board (goi 82 "Lên tàu") o ben KHONG phai Ben tau 106 (Littleroot, Sootopolis, Route 124, Hang duoi
     * bien...): truoc day im lang. Gio Lapras cho do toi ben Lapras khac (mot ve tau moi chuyen).
     */
    public static void moBenDo(final Char p) {
        if (!NpcTinhBanDo.gan(p, NPC_LAPRAS)) {
            bao(p, "Hãy đến gần Lapras ở bến đò.");
            return;
        }
        final java.util.List<Integer> ben = benDo(NpcTinhBanDo.banDoNap(p));
        if (ben.isEmpty()) {
            bao(p, "Hiện chưa có bến đò nào khác.");
            return;
        }
        final String[] menu = new String[ben.size()];
        for (int i = 0; i < menu.length; i++) {
            menu[i] = KhoaBanDoGame.nhanDiem(p, ben.get(i), "Đến " + tenBanDo(ben.get(i))[0]);
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_LAPRAS, 0));
    }

    static void diDo(final Char p, final int chon) {
        if (!NpcTinhBanDo.gan(p, NPC_LAPRAS)) {
            bao(p, "Hãy đến gần Lapras ở bến đò.");
            return;
        }
        if (p.monster != null) {
            bao(p, "Không thể đi đò khi đang giao hàng.");
            return;
        }
        final java.util.List<Integer> ben = benDo(NpcTinhBanDo.banDoNap(p));
        if (chon < 0 || chon >= ben.size()) {
            return;
        }
        final int den = ben.get(chon);
        final Map mapDen = RealController.mapList.get(den);
        final int[] o = NpcTinhBanDo.oNpc(den, NPC_LAPRAS);
        if (mapDen == null || o == null) {
            bao(p, "Bến đò này đang tạm đóng.");
            return;
        }
        // o dung canh Lapras ben kia (uu tien ngay duoi NPC 2 o, giong buoc xuong thuyen)
        int x = o[0];
        int y = o[1] + 2;
        outer:
        for (int r = 0; r <= 4; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) == r && mapDen.canMove((o[0] + dx) * 16 + 8, (o[1] + 2 + dy) * 16 + 8)) {
                        x = o[0] + dx;
                        y = o[1] + 2 + dy;
                        break outer;
                    }
                }
            }
        }
        if (KhoaBanDoGame.chan(p, den)) {
            return; // chua tru ve
        }
        synchronized (p) {
            if (p.potions[VE_TAU] <= 0) {
                bao(p, "Bạn chưa có vé tàu. Hãy mua vé ở Lapras.");
                return;
            }
            p.potions[VE_TAU]--;
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        p.map.move2Map(p, x, y, den, p.inCountry);
    }

    /** Diem bay Pidgeot o Hoenn: moi thi tran Hoenn co Trung tam Pokemon (Chansey), tru ban do hien tai. */
    static java.util.List<Integer> diemBay(final int tuLoad) {
        final java.util.List<Integer> kq = new java.util.ArrayList<Integer>();
        for (final com.htbb.pokemon.BanDoHoenn.BanDo b : com.htbb.pokemon.BanDoHoenn.ds()) {
            if (b.id != tuLoad && com.htbb.pokemon.HoiSinh.coTrungTam(b) && RealController.mapList.get(b.id) != null) {
                kq.add(b.id);
            }
        }
        return kq;
    }

    /**
     * Pidgeot cho bay (NPC tinh 7) o ban do KHONG co tuyen bay san (Map.idXaphu -1, vd Fortree - Hoenn): client moi
     * (GameScr "Đi đến") xin server; tuyen cu (Kanto/Johto, RealController.xaphu) van do client tu dung.
     */
    static void moBay(final Char p) {
        if (!NpcTinhBanDo.gan(p, NPC_PIDGEOT)) {
            bao(p, "Hãy đến gần Pidgeot để bay.");
            return;
        }
        final java.util.List<Integer> ds = diemBay(NpcTinhBanDo.banDoNap(p));
        if (ds.isEmpty()) {
            bao(p, "Pidgeot chưa có điểm bay nào từ đây.");
            return;
        }
        final long gia = BaiLuyenTap.giaDichChuyen(p, GIA_BAY);
        final String[] menu = new String[ds.size()];
        for (int i = 0; i < menu.length; i++) {
            menu[i] = KhoaBanDoGame.nhanDiem(p, ds.get(i), tenBanDo(ds.get(i))[0] + " (" + gia + " xu)");
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_PIDGEOT, 0));
    }

    static void bay(final Char p, final int chon) {
        if (!NpcTinhBanDo.gan(p, NPC_PIDGEOT)) {
            bao(p, "Hãy đến gần Pidgeot để bay.");
            return;
        }
        if (p.monster != null) {
            bao(p, "Không thể bay khi đang giao hàng.");
            return;
        }
        final java.util.List<Integer> ds = diemBay(NpcTinhBanDo.banDoNap(p));
        if (chon < 0 || chon >= ds.size()) {
            return;
        }
        final int den = ds.get(chon);
        final int[] diem = com.htbb.pokemon.HoiSinh.diemTrungTamHoenn(den, new com.htbb.pokemon.HoiSinh.DiDuoc() {
            @Override
            public boolean o(final int map, final int x, final int y) {
                final Map m = RealController.mapList.get(Integer.valueOf(map));
                return m != null && m.canMove(x * 16 + 8, y * 16 + 8);
            }
        });
        if (diem == null || diem[0] != den) {
            bao(p, "Điểm bay này đang tạm đóng.");
            return;
        }
        if (KhoaBanDoGame.chan(p, den)) {
            return; // chua tru xu
        }
        final long gia = BaiLuyenTap.giaDichChuyen(p, GIA_BAY);
        if (!p.truXuNeuDu(gia, true, "pidgeot bay")) {
            bao(p, "Cần " + gia + " xu để bay.");
            return;
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        p.map.move2Map(p, diem[1], diem[2], den, p.inCountry);
    }

    /** MapDoMenu: true neu {@code idNpc} la NPC cua lop nay (da xu ly). */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (idNpc == NPC_LAPRAS) {
            if (idMenu == 0) {
                diDo(p, chon);
            }
            return true;
        }
        if (idNpc == NPC_PIDGEOT) {
            if (idMenu == 0) {
                bay(p, chon);
            }
            return true;
        }
        if (idNpc == NPC_Y_TA) {
            if (idMenu != 0) {
                return true;
            }
            if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_Y_TA)) {
                bao(p, "Hãy đến gần Y tá Chansey ở Trung tâm Pokémon.");
                return true;
            }
            if (chon == 0) {
                hoiPhuc(p);
            } else if (chon == 1) {
                guiShopThuoc(p);
            }
            return true;
        }
        if (idNpc == NPC_MEOWTH || idNpc == NPC_MEOWTH_DOI_XU) {
            if (idMenu != 0) {
                return true;
            }
            if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_NGAN_HANG)) {
                bao(p, "Hãy đến gần Meowth để dùng ngân hàng.");
                return true;
            }
            switch (chon) {
                case 0:
                    KhoXuLuong.mo(p);
                    break;
                case 1:
                    CharChuyenLuong.moNhapTen(p);
                    break;
                case 2:
                    if (com.htbb.pokemon.NapTien.bat()) {
                        com.htbb.pokemon.NapTienGame.moGoc(p);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }

    /** Hoi day HP + PP (Trung tam Pokemon). Dang guc thi chi bao - hoi sinh di qua hop thoai hoi sinh (-101). */
    static void hoiPhuc(final Char p) {
        if (p.isDie() || p.hp <= 0) {
            bao(p, "Pokémon đang gục. Hãy chọn cách hồi sinh trước.");
            return;
        }
        if (p.hp >= p.maxhp && p.mp >= p.maxmp) {
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Chansey: Pokémon của bạn đang khoẻ mạnh, không cần hồi phục."));
            return;
        }
        final int hpCong = Math.max(0, p.maxhp - p.hp);
        final int mpCong = Math.max(0, p.maxmp - p.mp);
        p.hp = p.maxhp;
        p.mp = p.maxmp;
        try {
            final Message m = MessageCreator.createNew_HMP_Message(p, Math.min(hpCong, Short.MAX_VALUE));
            p.sendMessage(m);
            p.sendToNearPlayer(m);
        } catch (final java.io.IOException ignored) {
        }
        MessageCreator.createMsgUseHpMP(p, Math.min(mpCong, Short.MAX_VALUE), 4);
        p.sendMessage(MessageCreator.createMsgChat(p.id, "Chansey: Pokémon của bạn đã hồi phục hoàn toàn. Chúc may mắn!"));
    }

    /** Cua so thuoc (goi 23 kieu 0, giong Kecleon): mua bang goi 24, chay o than OfflineMap qua NpcTinhBanDo. */
    static void guiShopThuoc(final Char p) {
        try {
            final Message m = new Message(23);
            m.dos.writeByte(0);
            m.dos.writeByte(THUOC_BAN.length);
            for (final int id : THUOC_BAN) {
                m.dos.writeByte(id);
            }
            p.sendMessage(m);
        } catch (final java.io.IOException ignored) {
        }
    }

    /** Nhan menu dich chuyen = ten ban do that (SQL data_map_ten, ban sao theo khu), khong dung ten viet tay. */
    public static String[] tenBanDo(final int... ids) {
        final String[] kq = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            final String t = TenBanDoMayChu.tenDayDu(ids[i]);
            kq[i] = t.isEmpty() ? Map.getNameMap(ids[i]) : t;
        }
        return kq;
    }

    static void bao(final Char p, final String chu) {
        p.sendMessage(MessageCreator.createServerAlertMessage(chu, ""));
    }
}
