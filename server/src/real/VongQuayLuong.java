package real;

/**
 * "Vong quay luong" (them 19/09/2026, theo yeu cau nguoi dung): mo trong menu NPC Tong
 * Quan (npcType -10), dat cuoc bang luong (500-2000/nguoi, toi da 20 nguoi/luot, thue
 * co dinh 25%). Co che dat cuoc/chon nguoi thang GIONG HET he "Vong quay thuong/vip"
 * (Char.doXoso/doXosoLow) da co san - xem Char.doXosoLuong/infoXosoLuong/
 * startThreadXosoLuong.
 *
 * Menu duoc mo qua 1 idMenu RIENG (MENU_GOC) bat bang top-level guard trong
 * MapDoMenu.java (giong cach real.SuKienDuaTop.MENU_CHON_THUONG dang lam) thay vi
 * chen vao chuoi if/else cua NPC "-15" (ruong) dang dung cho "Vong quay thuong/vip" -
 * khoi do da qua chang chit nhieu tinh nang khong lien quan (dau gia black friday,
 * doi he ngu hanh, thao ngoc kham...) nen them 1 nhanh moi vao do rui ro cao hon nhieu
 * so voi mo mot loi rieng, sach se qua NPC Tong Quan.
 */
public final class VongQuayLuong {

    /** idMenu rieng cho submenu {"Thong tin","Tham gia","Huong dan"} cua NPC -10 - khong
     * duoc trung idMenu nao khac NPC -10 dang dung (0 = menu goc, 1..14/20+ = cac menu
     * con co san, 100/101 = SuKienWorldCup, 102 = SuKienDuaTop.MENU_CHON_THUONG). */
    public static final int MENU_GOC = 103;
    public static final int INPUT_SO_LUONG = -32056;

    private VongQuayLuong() {
    }

    public static void moMenuGoc(final Char player) {
        player.sendMessage(MessageCreator.createMsgMenu(
                new String[]{"Thông tin", "Tham gia", "Hướng dẫn"}, -10, MENU_GOC));
    }

    public static void doMenu(final Char player, final int idOptionMenu) {
        if (idOptionMenu == 0) {
            player.infoXosoLuong();
        } else if (idOptionMenu == 1) {
            player.sendMessage(MessageCreator.createMsgInputText(
                    INPUT_SO_LUONG, 0, "Nhập số lượng muốn đặt cược", 1));
        } else if (idOptionMenu == 2) {
            player.sendMessage(MessageCreator.createMsgPopUp(player.id, 100, huongDan()));
        }
    }

    public static void doNhapSoLuong(final Char player, final String soGoc) {
        final int soLuong;
        try {
            soLuong = Integer.parseInt(soGoc == null ? "" : soGoc.trim());
        } catch (final Exception e) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng không hợp lệ.", ""));
            return;
        }
        player.doXosoLuong(soLuong);
    }

    private static String huongDan() {
        return "Giá trị đặt thấp nhất mỗi người là " + Char.MIN_BID_XOSO_LUONG + " lượng\n"
                + "Giá trị đặt cao nhất mỗi người là " + Char.MAX_BID_XOSO_LUONG + " lượng\n"
                + "Mỗi người chơi chỉ được phép đặt 1 lần/1 lượt quay.\n"
                + "Tối đa " + Char.MAX_PLAYER_XOSO_LUONG + " người tham gia mỗi lượt.\n"
                + "Mỗi 2 phút bắt đầu một vòng quay.\n"
                + "10 giây cuối sẽ khoá không cho tham gia thêm.\n"
                + "Người chiến thắng nhận toàn bộ tiền cược của mọi người sau khi trừ thuế "
                + Char.TAX_PC_XOSO_LUONG + "%.";
    }
}
