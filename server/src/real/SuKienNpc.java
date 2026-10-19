package real;

import server.TeamServer;

/**
 * NPC SU KIEN (-49): MOT thu tu uu tien duy nhat cho ca menu (RequestNPCInfoHandler goi 23) lan xu ly (RealController /
 * MapDoMenu goi -30).
 *
 * Truoc 19/10/2026 ba noi tu chon su kien theo thu tu KHAC nhau: menu lay su kien CUOI cung dang bat (Mini Illumise >
 * Tet > Noel 2023 > Gio To > Halloween > Trung Thu > He...), RealController lay He 2017 / Mini TRUOC, MapDoMenu lay
 * Gio To truoc Tet, Noel (khong phai Noel 2023) va He truoc Halloween. Hai su kien cung bat (lich nam chong nhau, hoac
 * Trung Thu bat bang sv.isSuKienTrungThul2016) thi nguoi choi thay menu su kien nay ma bam lai chay ham su kien kia;
 * Noel theo lich ("noel") con khong co menu rieng (hien menu mac dinh, xu ly theo Noel). Gio moi noi hoi {@link #dangChay}.
 *
 * NPC hien tren ban do: bot dang ky luc bat may (Trung Thu "Hằng Nga", Mini) + NPC goi -50 o Map.sendInfoNpc
 * ({@link #tenNpcGoi50} - them Tet, Halloween, Gio To ma truoc chi co trong plugins/addNPC.java khong ai goi).
 */
public final class SuKienNpc {

    public static final int KHONG = 0;
    public static final int MINI_ILLUMISE = 1;
    public static final int TET = 2;
    public static final int NOEL = 3;
    public static final int GIO_TO = 4;
    public static final int HALLOWEEN = 5;
    public static final int TRUNG_THU = 6;
    public static final int NHA_GIAO = 7;
    public static final int LONG_DEN_HOA_SEN = 8;
    public static final int HE = 9;

    private SuKienNpc() {
    }

    /** Su kien dang "so huu" NPC -49 (uu tien tren xuong), KHONG neu khong co. */
    public static int dangChay() {
        if (Char.isSuKienMiniChucNu()) {
            return MINI_ILLUMISE;
        }
        if (Char.isSuKienTet2017()) {
            return TET;
        }
        if (Char.isSuKienNoel2023() || Char.isSuKienNoel()) {
            return NOEL;
        }
        if (Char.isSuKienGioTo2016()) {
            return GIO_TO;
        }
        if (Char.isSuKienHaloween2016()) {
            return HALLOWEEN;
        }
        if (Char.isSuKienTrungThul2016()) {
            return TRUNG_THU;
        }
        if (Char.isSuKien2011l2016()) {
            return NHA_GIAO;
        }
        if (Char.isBanLongDenHoaSen()) {
            return LONG_DEN_HOA_SEN;
        }
        if (Char.isSuKienHe2017()) {
            return HE;
        }
        return KHONG;
    }

    /** Menu goc (idMenu 0) cua NPC -49 cho su kien dang chay; null = khong co su kien. */
    public static String[] menu(final Char p) {
        switch (dangChay()) {
            case MINI_ILLUMISE:
                return new String[]{"Tìm Illumise", "Đổi nguyên liệu", "Đổi ruy băng đặc biệt", "Đổi ruy băng vật lý", "Đổi Sandshrew (7 ngày)"};
            case TET:
                return new String[]{"Nhận bánh chưng", "Nhận bánh tét", "Dâng bánh chưng", "Đổi nguyên liệu", "Làm pháo",
                    "Đăng ký nấu bánh chưng", "Đăng ký nấu bánh tét", "Top pháo hoa"};
            case NOEL: {
                // MapSuKien.doMenuNoel2016: 0..6, muc 7 = qua Dem Noel (chi khi dang Dem Noel)
                final String[] m = {"Đổi kem", "Đổi Stantler", "Đổi nguyên liệu", "Đổi túi quà may mắn", "Đổi áo Absol",
                    "Đổi áo Oricorio", "Đổi quà đặc biệt"};
                if (Char.isDemNoel()) {
                    final String[] d = java.util.Arrays.copyOf(m, m.length + 1);
                    d[m.length] = "Nhận quà đêm Noel";
                    return d;
                }
                return m;
            }
            case GIO_TO:
                if (TeamServer.isServerHoaLu() || TeamServer.isServerLocal()) {
                    return new String[]{"Đổi quà", "Đổi nguyên liệu", "Điểm dùng lượng (" + p.luongxainhanRuong + ")",
                        "Đổi rương kho báu (" + p.luongxai + " điểm)", "Đổi xu"};
                }
                return new String[]{"Đổi quà", "Đổi nguyên liệu"};
            case HALLOWEEN:
                return new String[]{"Làm hộp quà", "Đổi nguyên liệu", "Đổi điểm lượng (" + p.luongxainhanRuong + " điểm)"};
            case TRUNG_THU:
                return new String[]{"Làm bánh nướng", "Làm bánh dẻo", "Đổi hộp bánh", "Đổi nguyên liệu", "Nhận lồng đèn",
                    "Đổi pet (20 bánh dẻo + 70 lượng)", "Bảng xếp hạng", "Cán mốc thả đèn",
                    "Lồng đèn rồng (20 bánh dẻo + 100 lượng)", "Gậy Ánh Trăng (20 bánh dẻo + 150 lượng)",
                    "Suicune (20 bánh dẻo + 150 lượng)", "Đổi Cỏ hồi sinh (10 bánh dẻo + 30 lượng, tối đa 5 lần)",
                    // "diem tieu xai" (2 luong = 1 diem, xem Char.subLuong)
                    "Đổi điểm lấy lồng đèn (100 điểm, hiện có " + (p.luongDaXaiTrungThu / 2) + " điểm)",
                    // dong THONG TIN cua rieng nguoi dang mo menu - bam vao chi nhac lai so luot
                    "Số lượt thả đèn của tôi: " + data.DatabaseCanMocThaDen.layCaNhan(p.charDBID)};
            case NHA_GIAO:
                return new String[]{"Làm rương quà", "Đổi nguyên liệu", "Điểm nạp", "Lồng đèn hoa sen",
                    "Chổi băng (" + Map.totalChoiBan + ")", "Chổi độc (" + Map.totalChoiBan + ")", "Chổi điện (" + Map.totalChoiBan + ")"};
            case LONG_DEN_HOA_SEN:
                return new String[]{"Làm rương quà", "Đổi nguyên liệu", "Điểm nạp", "Lồng đèn hoa sen"};
            case HE:
                return new String[]{"Tìm trẻ lạc", "Đổi nguyên liệu", "Top pháo"};
            default:
                return null;
        }
    }

    /** Xu ly chon muc NPC -49 (moi idMenu) theo su kien dang chay. */
    public static void xuLy(final Map map, final Char p, final int idNpc, final int idMenu, final int chon) {
        switch (dangChay()) {
            case MINI_ILLUMISE:
                doMenuSuKienMiniChucNu.doMenuSuKienMiniChucNu(p, idNpc, idMenu, chon);
                break;
            case TET:
                MapSuKien.domenuTet2017(map, p, idNpc, idMenu, chon);
                break;
            case NOEL:
                MapSuKien.doMenuNoel2016(map, p, idNpc, idMenu, chon);
                break;
            case GIO_TO:
                MapSuKien.doMenuGioTo2020(map, p, idNpc, idMenu, chon);
                break;
            case HALLOWEEN:
            case NHA_GIAO:
            case LONG_DEN_HOA_SEN:
                // muc 3 "Lồng đèn hoa sen" chi ban khi CA 20/11 lan dot ban long den hoa sen cung bat (ham cu im lang)
                if (idMenu == 0 && chon == 3 && (!Char.isSuKien2011l2016() || !Char.isBanLongDenHoaSen())) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Lồng đèn hoa sen chưa mở bán đợt này. Hẹn bạn lần sau!", ""));
                    return;
                }
                MapSuKien.doMenuHalloween2016(map, p, idNpc, idMenu, chon);
                break;
            case TRUNG_THU:
                if (idMenu == 0 && chon == 13) { // dong thong tin "So luot tha den cua toi"
                    p.sendMessage(MessageCreator.createServerAlertMessage(
                            "Bạn đã thả đèn " + data.DatabaseCanMocThaDen.layCaNhan(p.charDBID) + " lượt trong mùa Trung Thu này.", ""));
                    return;
                }
                MapSuKien.doMenuTrungThu2016(map, p, idNpc, idMenu, chon);
                break;
            case HE:
                doMenuSuKienHe2017.doMenuSuKienHe2017(p, idNpc, idMenu, chon);
                break;
            default:
                p.sendMessage(MessageCreator.createServerAlertMessage("Sự kiện đã kết thúc. Hẹn gặp lại bạn ở sự kiện sau!", ""));
                break;
        }
    }

    /**
     * NPC -49 goi -50 cho Map.sendInfoNpc: {ten, idImg} cho su kien dang chay KHONG co bot dang ky san (Trung Thu, Mini
     * co bot rieng); null = khong can. Hinh theo du_lieu/bang_npc_su_kien.csv (14 Meowth, 20 Gardevoir, 24 Delibird).
     */
    public static Object[] tenNpcGoi50() {
        switch (dangChay()) {
            case TET:
                return new Object[]{"Meowth chúc Tết", 14};
            case NOEL:
                return new Object[]{"Delibird ông già Noel", 24};
            case GIO_TO:
                return new Object[]{"Meowth lễ hội Đền Hùng", 14};
            case HALLOWEEN:
                return new Object[]{"Gardevoir phù thuỷ", 20};
            case NHA_GIAO:
            case LONG_DEN_HOA_SEN:
                return new Object[]{"Meowth tri ân thầy cô", 14};
            case HE:
                return new Object[]{"Gardevoir mùa hè", 20};
            case TRUNG_THU:
            case MINI_ILLUMISE:
                return coBotSuKien() ? null : new Object[]{"Litleo rước đèn", 21};
            default:
                return null;
        }
    }

    /** Da co bot dang ky -49 (tao luc bat may khi Trung Thu / Mini dang bat) trong Map.npcVilage. */
    static boolean coBotSuKien() {
        for (int i = 0; i < Map.npcVilage.size(); i++) {
            final Char c = Map.npcVilage.get(i);
            if (c != null && c.isBot == -49) {
                return true;
            }
        }
        return false;
    }
}
