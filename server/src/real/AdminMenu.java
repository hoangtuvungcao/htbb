package real;

import server.TeamServer;

/**
 * BANG SETTING CHO ADMIN (popup trong game)
 *
 * Admin go trong chat: setting
 * -> hien popup menu nhieu cap: chiem thanh / ti le drop / xem cau hinh.
 *
 * Cach noi vao code goc (da lam san):
 *   1) Map.doChat            : go "setting" -> AdminMenu.moMenu(player)
 *   2) Map.doMenu            : dau ham    -> if (AdminMenu.xuLyChon(player, idNpc, idMenu, idOptionMenu)) return;
 *   3) Map.doProcessInputText: dau ham    -> if (AdminMenu.xuLyNhapText(player, idMenu, text)) return;
 *
 * LUU VINH VIEN: moi thay doi o bang nay co hieu luc NGAY va duoc GHI THANG vao
 * bang SQL `cau_hinh`, nen van con sau khi restart. Cung mot bang nay sua duoc tu xa
 * qua HTBB-Web /admin/cauhinh - server tu doc lai moi 10 giay nen sua tren web cung ap
 * dung duoc trong game ma khong can go lenh 8 (nap lai), chi la khong tuc thi bang.
 * => Bang setting trong game, trang web, va server luon la cung mot nguon du lieu.
 * Xem server.CauHinh.
 */
public class AdminMenu {

    /** Id NPC ao danh cho bang setting. Khong trung voi npc nao dang dung (-2, -21, -514..-519). */
    public static final int ID_NPC = -1000;

    // Cac cap menu
    private static final int MENU_GOC = 0;
    private static final int MENU_CHIEM_THANH = 1;
    private static final int MENU_DROP = 3;

    /** Id o dialog nhap text (khong trung 0,1,2,98,99 dang dung). */
    public static final int NHAP_GIO_CHIEM_THANH = 111;
    public static final int NHAP_TI_LE_DROP = 112;
    public static final int NHAP_LENH = 113;

    /**
     * Huong dan cho o nhap lenh. Dung o nhap thay vi menu vi menu mo ngay sau khi go chat
     * thi client con o che do chat, phim mui tren bi o chat an -> phai dung chuot moi chon duoc.
     * O nhap thi go bang ban phim toan bo, xong nhan Enter.
     */
    private static final String HUONG_DAN
            = "Gõ số + tham số:\n"
            + "1 = chiếm Gym sau 2 phút\n"
            + "2 <giờ> <phút> [ngày] = đặt giờ chiếm Gym\n"
            + "3 = trả về lịch chuẩn 21h T3+T7\n"
            + "5 <xu> <lượng> <sựkiện> <bình> <ngọc> <đồ> <chiaXu>\n"
            + "6 = xem cấu hình hiện tại\n"
            + "8 = nạp lại ngay từ CSDL (không cần chờ tối đa 10 giây)\n"
            + "(mọi thay đổi được ghi vào CSDL, còn sau restart, sửa được cả trên trang quản trị web)";

    private AdminMenu() {
    }

    // ============================ MO MENU ============================

    /**
     * Mo o nhap lenh - dieu khien hoan toan bang ban phim (khong can chuot).
     * Day la duong mac dinh khi go "setting" trong chat.
     */
    public static void moONhapLenh(final Char player) {
        if (player == null || !player.isAdmin) {
            return;
        }
        player.sendMessage(MessageCreator.createMsgInputText(player.id, NHAP_LENH, HUONG_DAN, 0));
    }

    public static void moMenu(final Char player) {
        if (player == null || !player.isAdmin) {
            return;
        }
        final String[] menu = {
            "Chiếm Gym",
            "Tỉ lệ drop train quái",
            "Xem cấu hình hiện tại"
        };
        player.sendMessage(MessageCreator.createMsgMenu(menu, ID_NPC, MENU_GOC));
    }

    // ========================= XU LY CHON ==========================

    /**
     * @return true neu lua chon nay thuoc bang setting (khong cho code goc xu ly tiep)
     */
    public static boolean xuLyChon(final Char player, final int idNpc, final int idMenu, final int idOptionMenu) {
        if (idNpc != ID_NPC) {
            return false;
        }
        if (player == null || !player.isAdmin) {
            return true;
        }
        try {
            switch (idMenu) {
                case MENU_GOC:
                    switch (idOptionMenu) {
                        case 0:
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{
                                "Bắt đầu sau 2 phút",
                                "Đặt giờ cụ thể (nhập tay)",
                                "Trả về lịch chuẩn 21h thứ 3 + thứ 7",
                                "Xem giờ đang set"
                            }, ID_NPC, MENU_CHIEM_THANH));
                            break;
                        case 1:
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{
                                "Giảm một nửa tất cả",
                                "Tăng gấp đôi tất cả",
                                "Về mức trong server.ini",
                                "Tắt hẳn rơi xu + lượng",
                                "Nhập tay từng loại"
                            }, ID_NPC, MENU_DROP));
                            break;
                        case 2:
                            player.sendMessage(MessageCreator.createServerAlertMessage(xemCauHinh(player), ""));
                            break;
                        default:
                            break;
                    }
                    return true;

                case MENU_CHIEM_THANH:
                    xuLyChiemThanh(player, idOptionMenu);
                    return true;

                case MENU_DROP:
                    xuLyDrop(player, idOptionMenu);
                    return true;

                default:
                    return true;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static void xuLyChiemThanh(final Char player, final int idOptionMenu) {
        final java.util.Calendar cl = java.util.Calendar.getInstance();
        switch (idOptionMenu) {
            case 0: { // bat dau sau 2 phut
                cl.add(java.util.Calendar.MINUTE, 2);
                MapTown.TimeAdminCheat = cl.get(java.util.Calendar.HOUR_OF_DAY);
                MapTown.minuteAdminCheat = cl.get(java.util.Calendar.MINUTE);
                MapTown.dayAdminCheat = "";
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Chiếm Gym sẽ bắt đầu lúc " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
                        + " (khoảng 2 phút nữa). Phiên kéo dài 1 tiếng."
                        + ghiFile(server.CauHinh.luuGioChiemThanh()), ""));
                break;
            }
            case 1: // nhap tay
                player.sendMessage(MessageCreator.createMsgInputText(player.id, NHAP_GIO_CHIEM_THANH,
                        "Nhập giờ phút [ngày]. VD: 21 00  hoặc  21 00 2026-07-28  hoặc  21 00 Sat", 0));
                break;
            case 2: // tra ve lich chuan
                MapTown.TimeAdminCheat = -1;
                MapTown.minuteAdminCheat = -1;
                MapTown.dayAdminCheat = "";
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Đã trả chiếm Gym về lịch chuẩn: 21h thứ 3 và thứ 7."
                        + ghiFile(server.CauHinh.luuGioChiemThanh()), ""));
                break;
            default:
                player.sendMessage(MessageCreator.createServerAlertMessage(moTaGioChiemThanh(), ""));
                break;
        }
    }

    private static void xuLyDrop(final Char player, final int idOptionMenu) {
        switch (idOptionMenu) {
            case 0:
                doiTiLe(1, 2);
                break;
            case 1:
                doiTiLe(2, 1);
                break;
            case 2:
                Monster.PC_TILE_XU = TeamServer.tileGoc[0];
                Monster.PC_TILE_LUONG = TeamServer.tileGoc[1];
                Monster.PC_TILE_ITEM_SUKIEN = TeamServer.tileGoc[2];
                Monster.PC_TILE_POTION = TeamServer.tileGoc[3];
                Monster.PC_TILE_NGOC = TeamServer.tileGoc[4];
                Monster.PC_TILE_ITEM = TeamServer.tileGoc[5];
                Map.DIV_DROP_XU = TeamServer.tileGoc[6];
                break;
            case 3:
                Monster.PC_TILE_XU = 0;
                Monster.PC_TILE_LUONG = 0;
                break;
            default:
                player.sendMessage(MessageCreator.createMsgInputText(player.id, NHAP_TI_LE_DROP,
                        "Nhập: xu luong itemSuKien potion ngoc item chiaXu\nVD: 5 10 10 15 15 10 30", 0));
                return;
        }
        player.sendMessage(MessageCreator.createServerAlertMessage(
                moTaTiLeDrop() + ghiFile(server.CauHinh.luuTiLeDrop()), ""));
    }

    /** Cau thong bao ket qua ghi CSDL, gan vao cuoi thong bao cho admin. */
    private static String ghiFile(final boolean ok) {
        return ok
                ? "\n(đã lưu vào CSDL - giữ nguyên sau restart, cũng hiện trên trang quản trị web)"
                : "\n(CHƯA lưu được vào CSDL - kiểm tra kết nối, thay đổi sẽ mất khi restart)";
    }

    private static void doiTiLe(final int nhan, final int chia) {
        Monster.PC_TILE_XU = kep(Monster.PC_TILE_XU * nhan / chia);
        Monster.PC_TILE_LUONG = kep(Monster.PC_TILE_LUONG * nhan / chia);
        Monster.PC_TILE_ITEM_SUKIEN = kep(Monster.PC_TILE_ITEM_SUKIEN * nhan / chia);
        Monster.PC_TILE_POTION = kep(Monster.PC_TILE_POTION * nhan / chia);
        Monster.PC_TILE_NGOC = kep(Monster.PC_TILE_NGOC * nhan / chia);
        Monster.PC_TILE_ITEM = kep(Monster.PC_TILE_ITEM * nhan / chia);
    }

    private static int kep(final int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 100) {
            return 100;
        }
        return v;
    }

    // ======================= XU LY NHAP TEXT =======================

    /**
     * @return true neu text nay thuoc bang setting
     */
    public static boolean xuLyNhapText(final Char player, final int idMenu, final String text) {
        if (idMenu != NHAP_GIO_CHIEM_THANH && idMenu != NHAP_TI_LE_DROP && idMenu != NHAP_LENH) {
            return false;
        }
        if (player == null || !player.isAdmin) {
            return true;
        }
        if (idMenu == NHAP_LENH) {
            xuLyLenh(player, text);
            return true;
        }
        try {
            final String[] tach = text.trim().split("\\s+");
            if (idMenu == NHAP_GIO_CHIEM_THANH) {
                MapTown.TimeAdminCheat = Integer.parseInt(tach[0]);
                MapTown.minuteAdminCheat = Integer.parseInt(tach[1]);
                MapTown.dayAdminCheat = (tach.length > 2) ? tach[2] : "";
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        moTaGioChiemThanh() + ghiFile(server.CauHinh.luuGioChiemThanh()), ""));
            } else {
                Monster.PC_TILE_XU = kep(Integer.parseInt(tach[0]));
                Monster.PC_TILE_LUONG = kep(Integer.parseInt(tach[1]));
                Monster.PC_TILE_ITEM_SUKIEN = kep(Integer.parseInt(tach[2]));
                Monster.PC_TILE_POTION = kep(Integer.parseInt(tach[3]));
                Monster.PC_TILE_NGOC = kep(Integer.parseInt(tach[4]));
                Monster.PC_TILE_ITEM = kep(Integer.parseInt(tach[5]));
                if (tach.length > 6) {
                    Map.DIV_DROP_XU = Math.max(1, Integer.parseInt(tach[6]));
                }
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        moTaTiLeDrop() + ghiFile(server.CauHinh.luuTiLeDrop()), ""));
            }
        } catch (final Exception e) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Nội dung nhập không hợp lệ.", ""));
        }
        return true;
    }

    /**
     * Xu ly lenh go tu o nhap. Xong lai mo lai o nhap de admin sua tiep,
     * khong phai go "setting" lai tu dau.
     */
    private static void xuLyLenh(final Char player, final String text) {
        String ketQua;
        try {
            final String[] t = text.trim().split("\\s+");
            final int lenh = Integer.parseInt(t[0]);
            switch (lenh) {
                case 1:
                    xuLyChiemThanh(player, 0);
                    ketQua = moTaGioChiemThanh();
                    break;
                case 2:
                    MapTown.TimeAdminCheat = Integer.parseInt(t[1]);
                    MapTown.minuteAdminCheat = Integer.parseInt(t[2]);
                    MapTown.dayAdminCheat = (t.length > 3) ? t[3] : "";
                    ketQua = moTaGioChiemThanh() + ghiFile(server.CauHinh.luuGioChiemThanh());
                    break;
                case 3:
                    MapTown.TimeAdminCheat = -1;
                    MapTown.minuteAdminCheat = -1;
                    MapTown.dayAdminCheat = "";
                    ketQua = moTaGioChiemThanh() + ghiFile(server.CauHinh.luuGioChiemThanh());
                    break;
                case 5:
                    Monster.PC_TILE_XU = kep(Integer.parseInt(t[1]));
                    Monster.PC_TILE_LUONG = kep(Integer.parseInt(t[2]));
                    Monster.PC_TILE_ITEM_SUKIEN = kep(Integer.parseInt(t[3]));
                    Monster.PC_TILE_POTION = kep(Integer.parseInt(t[4]));
                    Monster.PC_TILE_NGOC = kep(Integer.parseInt(t[5]));
                    Monster.PC_TILE_ITEM = kep(Integer.parseInt(t[6]));
                    if (t.length > 7) {
                        Map.DIV_DROP_XU = Math.max(1, Integer.parseInt(t[7]));
                    }
                    ketQua = moTaTiLeDrop() + ghiFile(server.CauHinh.luuTiLeDrop());
                    break;
                case 8:
                    ketQua = server.CauHinh.napLai() + "\n---\n" + xemCauHinh(player);
                    break;
                default:
                    ketQua = xemCauHinh(player);
                    break;
            }
        } catch (final Exception e) {
            ketQua = "Nội dung nhập không hợp lệ.";
        }
        player.sendMessage(MessageCreator.createServerAlertMessage(ketQua, ""));
        // Mo lai o nhap de tiep tuc sua
        player.sendMessage(MessageCreator.createMsgInputText(player.id, NHAP_LENH, HUONG_DAN, 0));
    }

    // ========================== HIEN THI ==========================

    private static String moTaGioChiemThanh() {
        if (MapTown.TimeAdminCheat < 0) {
            return "Chiếm Gym đang theo lịch chuẩn: 21h thứ 3 và thứ 7.";
        }
        return "Chiếm Gym: " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
                + (MapTown.dayAdminCheat.isEmpty() ? " - mỗi ngày" : " - ngày " + MapTown.dayAdminCheat)
                + ". Phiên kéo dài 1 tiếng.";
    }

    private static String moTaTiLeDrop() {
        return "Tỉ lệ drop train quái (% so với gốc):"
                + "\nXu: " + Monster.PC_TILE_XU
                + "\nLượng: " + Monster.PC_TILE_LUONG
                + "\nItem sự kiện: " + Monster.PC_TILE_ITEM_SUKIEN
                + "\nBình/potion: " + Monster.PC_TILE_POTION
                + "\nNgọc: " + Monster.PC_TILE_NGOC
                + "\nTrang bị: " + Monster.PC_TILE_ITEM
                + "\nChia số xu rơi: " + Map.DIV_DROP_XU;
    }

    private static String xemCauHinh(final Char player) {
        return moTaGioChiemThanh()
                + "\n---\n" + moTaTiLeDrop()
                + "\nHạn mức lượng train/ngày: " + Char.MAX_LUONG_TRAIN_NGAY
                + " (mở đã nhận " + player.luongMoTrainNgay + ", khoá đã nhận " + player.luongKhoaTrainNgay + ")"
                + "\nPokémon tối đa/tài khoản: " + data.Database.MAX_CHAR_PER_ACCOUNT;
    }
}
