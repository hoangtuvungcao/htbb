package real;

import server.TeamServer;

/**
 * BANG SETTING CHO ADMIN (popup trong game)
 *
 * Admin go trong chat: setting
 * -> hien popup menu nhieu cap: chiem thanh / skin thu nuoi / ti le drop / xem cau hinh.
 *
 * Cach noi vao code goc (da lam san):
 *   1) Map.doChat            : go "setting" -> AdminMenu.moMenu(player)
 *   2) Map.doMenu            : dau ham    -> if (AdminMenu.xuLyChon(player, idNpc, idMenu, idOptionMenu)) return;
 *   3) Map.doProcessInputText: dau ham    -> if (AdminMenu.xuLyNhapText(player, idMenu, text)) return;
 *
 * LUU VINH VIEN: moi thay doi o bang nay co hieu luc NGAY va duoc GHI THANG vao
 * server.ini, nen van con sau khi restart. Chieu nguoc lai cung dung: sua tay
 * trong server.ini roi go lenh 8 (nap lai) la ap dung ngay, khong can restart.
 * => Bang setting va server.ini luon la cung mot nguon du lieu.
 * Xem server.CauHinh.
 */
public class AdminMenu {

    /** Id NPC ao danh cho bang setting. Khong trung voi npc nao dang dung (-2, -21, -514..-519). */
    public static final int ID_NPC = -1000;

    // Cac cap menu
    private static final int MENU_GOC = 0;
    private static final int MENU_CHIEM_THANH = 1;
    private static final int MENU_SKIN = 2;
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
            + "1 = chiếm thành sau 2 phút\n"
            + "2 <giờ> <phút> [ngày] = đặt giờ chiếm thành\n"
            + "3 = trả về lịch chuẩn 21h T3+T7\n"
            + "4 <0-4> = chọn skin thú (4 -1 để bỏ)\n"
            + "5 <xu> <lượng> <sựkiện> <bình> <ngọc> <đồ> <chiaXu>\n"
            + "6 = xem cấu hình hiện tại\n"
            + "7 <tên nv> <0-4> = tặng skin thú nuôi (-1 = thu hồi)\n"
            + "8 = nạp lại server.ini (sau khi sửa tay)\n"
            + "(mọi thay đổi được ghi vào server.ini, còn sau restart)";

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
            "Chiếm thành",
            "Skin thú nuôi (buff nhân vật)",
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
                                SkinThuNuoi.moTa(0) + " - +2000HP +3000MP",
                                SkinThuNuoi.moTa(1) + " - +200 chính xác, phản đòn",
                                SkinThuNuoi.moTa(2) + " - +20% tất cả chỉ số, lá chắn",
                                SkinThuNuoi.moTa(3) + " - +5% xuyên giáp, nộ khi HP thấp",
                                SkinThuNuoi.moTa(4) + " - +20% thủ, +15% hấp thụ",
                                "Bỏ skin đang mang"
                            }, ID_NPC, MENU_SKIN));
                            break;
                        case 2:
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{
                                "Giảm một nửa tất cả",
                                "Tăng gấp đôi tất cả",
                                "Về mức trong server.ini",
                                "Tắt hẳn rơi xu + lượng",
                                "Nhập tay từng loại"
                            }, ID_NPC, MENU_DROP));
                            break;
                        case 3:
                            player.sendMessage(MessageCreator.createServerAlertMessage(xemCauHinh(player), ""));
                            break;
                        default:
                            break;
                    }
                    return true;

                case MENU_CHIEM_THANH:
                    xuLyChiemThanh(player, idOptionMenu);
                    return true;

                case MENU_SKIN:
                    // Admin thu skin cho chinh minh (khong can so huu). Tang skin cho
                    // nguoi choi thi dung lenh chat "giveskin <ten> <0-4>".
                    if (idOptionMenu >= 0 && idOptionMenu <= 4) {
                        SkinThuNuoi.datSkin(player, idOptionMenu);
                    } else {
                        SkinThuNuoi.datSkin(player, SkinThuNuoi.KHONG_CO);
                    }
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
                        "Chiếm thành sẽ bắt đầu lúc " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
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
                        "Đã trả chiếm thành về lịch chuẩn: 21h thứ 3 và thứ 7."
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

    /** Cau thong bao ket qua ghi file, gan vao cuoi thong bao cho admin. */
    private static String ghiFile(final boolean ok) {
        return ok
                ? "\n(đã lưu vào server.ini - giữ nguyên sau restart)"
                : "\n(CHƯA lưu được server.ini - kiểm tra quyền ghi file, thay đổi sẽ mất khi restart)";
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
                case 4: {
                    final int idSkin = Integer.parseInt(t[1]);
                    SkinThuNuoi.datSkin(player, idSkin);
                    ketQua = "Skin: " + SkinThuNuoi.moTa(player.idSkinThuNuoi);
                    break;
                }
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
                case 7: {
                    // 7 <tên nhân vật> <0-4> = tặng skin thú nuôi (sự kiện). -1 = thu hồi hết.
                    final Char nhan = CharManager.instance.getCharByCharName(t[1].trim());
                    final int idSkin = Integer.parseInt(t[2].trim());
                    if (nhan == null) {
                        ketQua = "Không thấy nhân vật " + t[1].trim() + " đang online.";
                    } else if (idSkin < 0) {
                        SkinThuNuoi.thuHoiHetSkin(nhan);
                        ketQua = "Đã thu hồi hết skin của " + nhan.charname + ".";
                    } else if (SkinThuNuoi.tangSkin(nhan, idSkin)) {
                        ketQua = "Đã tặng " + SkinThuNuoi.moTa(idSkin) + " cho " + nhan.charname + ".";
                    } else {
                        ketQua = nhan.charname + " đã có skin này rồi (hoặc id sai).";
                    }
                    break;
                }
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
            return "Chiếm thành đang theo lịch chuẩn: 21h thứ 3 và thứ 7.";
        }
        return "Chiếm thành: " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
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
                + "\n---\nSkin đang mang: " + SkinThuNuoi.moTa(player.idSkinThuNuoi)
                + "\nSkin chỉ nhận qua sự kiện/giftcode (SKIN:<0-4>), không bán."
                + "\nHạn mức lượng train/ngày: " + Char.MAX_LUONG_TRAIN_NGAY
                + " (mở đã nhận " + player.luongMoTrainNgay + ", khoá đã nhận " + player.luongKhoaTrainNgay + ")"
                + "\nNhân vật tối đa/tài khoản: " + data.Database.MAX_CHAR_PER_ACCOUNT;
    }
}
