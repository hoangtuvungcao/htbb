package real;

/**
 * Kho xu/luong o NPC Tong Quan (them 21/09/2026, theo yeu cau nguoi dung): cho gui xu/
 * luong "dang giu" vao kho rieng cua nhan vat, rut ra lai bat cu luc nao. Ly do chinh:
 * luong nhat duoc tu train quai bi chan boi Char.MAX_LUONG_MO_DANG_GIU/
 * MAX_LUONG_KHOA_DANG_GIU (50k dang giu trong nguoi) - xem Char.addLuongTrainQuai. Gui
 * bot vao kho de "dang giu" tut xuong duoi tran, train tiep se lai nhan duoc luong.
 *
 * Khong phi, khong han muc gui/rut (theo yeu cau nguoi dung) - khac CharChuyenLuong (co
 * han muc NHAN/ngay vi la chuyen GIUA hai nhan vat, con kho la CUA RIENG mot nguoi).
 *
 * Menu con NHIEU CAP (rut gon 22/09/2026 theo yeu cau nguoi dung "menu dang hoi nhieu",
 * bam tiep moi sang menu ke): "Kho xu/luong" o NPC Tong Quan (-10) -> NPC_ID (-522, 3
 * muc: xem so du / Gui / Rut) -> bam Gui thi mo NPC_ID_GUI (-523, chon xu/luong mo/luong
 * khoa) hoac bam Rut thi mo NPC_ID_RUT (-524, cung 3 lua chon) -> cuoi cung moi toi hop
 * thoai nhap so (MessageCreator.createMsgInputText, dispatch qua MapNhapText). Xem quy
 * uoc "idNpc == -" trong Map.doMenu. Vi tri muc "Kho xu/luong" trong menu NPC -10 ghi
 * trong Char.idxKhoXuLuong, giong het idxChuyenLuong/idxThuongDuaTop.
 */
public final class KhoXuLuong {

    /** Virtual npcId cho menu goc (xem so du / Gui / Rut). */
    public static final int NPC_ID = -522;
    /** Virtual npcId cho submenu "Gui" (chon xu / luong mo / luong khoa). */
    public static final int NPC_ID_GUI = -523;
    /** Virtual npcId cho submenu "Rut" (chon xu / luong mo / luong khoa). */
    public static final int NPC_ID_RUT = -524;

    public static final int INPUT_GUI_XU = -32087;
    public static final int INPUT_RUT_XU = -32088;
    public static final int INPUT_GUI_LUONG_MO = -32089;
    public static final int INPUT_RUT_LUONG_MO = -32090;
    public static final int INPUT_GUI_LUONG_KHOA = -32091;
    public static final int INPUT_RUT_LUONG_KHOA = -32092;

    private KhoXuLuong() {
    }

    /** Mo menu goc cua kho - goi tu MapDoMenu khi player bam muc "Kho xu/luong" o NPC -10. */
    public static void mo(final Char player) {
        final String[] menu = new String[]{
            "Số dư kho: " + player.khoXu + " xu, " + player.khoLuong + " lượng mở, " + player.khoLuongKhoa + " lượng khoá",
            "Gửi vào kho",
            "Rút từ kho"
        };
        player.sendMessage(MessageCreator.createMsgMenu(menu, NPC_ID, 0));
    }

    /** Xu ly chon muc trong menu goc (idNpc == NPC_ID): xem so du / mo submenu Gui / Rut. */
    public static void doMenu(final Char player, final int idOptionMenu) {
        switch (idOptionMenu) {
            case 0:
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Kho hiện có: " + player.khoXu + " xu, " + player.khoLuong
                                + " lượng mở, " + player.khoLuongKhoa + " lượng khoá.", ""));
                return;
            case 1:
                player.sendMessage(MessageCreator.createMsgMenu(
                        new String[]{"Gửi xu", "Gửi lượng (mở)", "Gửi lượng (khoá)"}, NPC_ID_GUI, 0));
                return;
            case 2:
                player.sendMessage(MessageCreator.createMsgMenu(
                        new String[]{"Rút xu", "Rút lượng (mở)", "Rút lượng (khoá)"}, NPC_ID_RUT, 0));
                return;
            default:
        }
    }

    /** Xu ly chon muc trong submenu "Gui" (idNpc == NPC_ID_GUI). */
    public static void doMenuGui(final Char player, final int idOptionMenu) {
        switch (idOptionMenu) {
            case 0:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_GUI_XU, 0, "Nhập số xu muốn gửi vào kho", 1));
                return;
            case 1:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_GUI_LUONG_MO, 0, "Nhập số lượng (mở) muốn gửi vào kho", 1));
                return;
            case 2:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_GUI_LUONG_KHOA, 0, "Nhập số lượng (khoá) muốn gửi vào kho", 1));
                return;
            default:
        }
    }

    /** Xu ly chon muc trong submenu "Rut" (idNpc == NPC_ID_RUT). */
    public static void doMenuRut(final Char player, final int idOptionMenu) {
        switch (idOptionMenu) {
            case 0:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_RUT_XU, 0, "Nhập số xu muốn rút từ kho", 1));
                return;
            case 1:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_RUT_LUONG_MO, 0, "Nhập số lượng (mở) muốn rút từ kho", 1));
                return;
            case 2:
                player.sendMessage(MessageCreator.createMsgInputText(
                        INPUT_RUT_LUONG_KHOA, 0, "Nhập số lượng (khoá) muốn rút từ kho", 1));
                return;
            default:
        }
    }

    /**
     * Doc so nguyen duong tu chuoi nhap - null (kem gui thong bao loi) neu khong hop le.
     * KHONG chan tran o day: xu dung Char.xu/khoXu kieu long (co the vuot Integer.MAX_
     * VALUE that su o nhan vat lau nam), luong moi la int nen phai tu chan tran RIENG o
     * cac ham goi luong (xem docSoDuongLuong) - gop chung mot tran se lam gui/rut xu bi
     * gioi han oan xuong ~2,1 ty du xu dang giu co the vuot moc do.
     */
    private static Long docSoDuong(final Char player, final String text) {
        final String tho = (text == null) ? "" : text.trim();
        long so;
        try {
            so = Long.parseLong(tho);
        } catch (final Exception e) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số không hợp lệ.", ""));
            return null;
        }
        if (so <= 0L) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số phải lớn hơn 0.", ""));
            return null;
        }
        return so;
    }

    /** Nhu docSoDuong() nhung chan them tran Integer.MAX_VALUE - danh cho luong (int). */
    private static Long docSoDuongLuong(final Char player, final String text) {
        final Long so = docSoDuong(player, text);
        if (so == null) {
            return null;
        }
        if (so.longValue() > (long) Integer.MAX_VALUE) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số quá lớn.", ""));
            return null;
        }
        return so;
    }

    public static void doNhapGuiXu(final Char player, final String text) {
        final Long soXu = docSoDuong(player, text);
        if (soXu == null) {
            return;
        }
        if (!player.guiKhoXu(soXu)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu để gửi.", ""));
            return;
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã gửi " + soXu + " xu vào kho. Kho hiện có " + player.khoXu + " xu.", ""));
    }

    public static void doNhapRutXu(final Char player, final String text) {
        final Long soXu = docSoDuong(player, text);
        if (soXu == null) {
            return;
        }
        if (!player.rutKhoXu(soXu)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Kho không đủ xu để rút.", ""));
            return;
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã rút " + soXu + " xu từ kho. Kho còn lại " + player.khoXu + " xu.", ""));
    }

    public static void doNhapGuiLuong(final Char player, final String text, final boolean khoa) {
        final Long soLuong = docSoDuongLuong(player, text);
        if (soLuong == null) {
            return;
        }
        if (!player.guiKhoLuong(soLuong.intValue(), khoa)) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không đủ lượng " + (khoa ? "khoá" : "mở") + " để gửi.", ""));
            return;
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã gửi " + soLuong + " lượng " + (khoa ? "khoá" : "mở") + " vào kho. Kho hiện có "
                        + (khoa ? player.khoLuongKhoa : player.khoLuong) + " lượng " + (khoa ? "khoá" : "mở") + ".", ""));
    }

    public static void doNhapRutLuong(final Char player, final String text, final boolean khoa) {
        final Long soLuong = docSoDuongLuong(player, text);
        if (soLuong == null) {
            return;
        }
        if (!player.rutKhoLuong(soLuong.intValue(), khoa)) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Kho không đủ lượng " + (khoa ? "khoá" : "mở") + " để rút.", ""));
            return;
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã rút " + soLuong + " lượng " + (khoa ? "khoá" : "mở") + " từ kho. Kho còn lại "
                        + (khoa ? player.khoLuongKhoa : player.khoLuong) + " lượng " + (khoa ? "khoá" : "mở") + ".", ""));
    }
}
