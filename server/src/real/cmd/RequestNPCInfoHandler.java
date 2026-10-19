// 
// Decompiled by Procyon v0.6.0
// 
package real.cmd;

import java.io.IOException;
import real.CharChienTruong;
import real.Item;
import data.Animal;
import data.Database;
import java.util.Calendar;
import data.NewClan;
import real.MapChienTruongMoba;
import real.Map;
import data.CharCooking;
import real.MessageCreator;
import real.Char;
import real.CharManager;
import server.TeamServer;
import io.Message;
import io.Session;
import static real.Char.ID_MAT_NA;
import static real.Char.getDayTime;
import real.GemTemplate;


public class RequestNPCInfoHandler implements ICommandHandler {

    /** NPC cua doi (Cao nguyen Indigo): nguoi choi chua co doi thi bao thay vi im lang. */
    static final String CHUA_CO_DOI = "Bạn chưa có đội. Hãy tạo hoặc gia nhập một đội để dùng chức năng này.";

    public static int[] idMapLoad;
    public static int[][] xyNPC;
    public static int[][] posHoaTieu;
    public static int[][] IDMAP_HOA_TIEU;
    public static short[][] ID_MATERIAL_LOW;
    public static short[][] ID_MATERIAL_HIGHT;
    public static int[][] ID_TU_BINH;
    public static short[] ID_NGOC_RONG;
    public static short[] ID_XUONG;
    public static short[] ID_BOT;
    public static short[] ID_DUC;
    public static short[][] ID_DA_NGU_HOP;
    public static short[] ID_NGOC_HUYEN_MINH;

    static {
        GemTemplate.TYPE_IMBUE = 0;
        GemTemplate.TYPE_KHAM = 1;
        GemTemplate.TYPE_DUC_LO = 2;
        GemTemplate.TYPE_CREATE_ITEM = 3;
        GemTemplate.TYPE_TU_BINH = 4;
        GemTemplate.TYPE_HOP_ITEM_ANIMAL = 5;
        GemTemplate.TYPE_ADD_NEW_ATTRIBUTE = 6;
        GemTemplate.TYPE_LOCK_ANIMAL = 7;
        GemTemplate.TYPE_LOCK_ITEM = 8;
        GemTemplate.TYPE_BOT = 9;
        GemTemplate.TYPE_DA_TIEN_GIAI = 10;
        GemTemplate.TYPE_UNKNOW = 11;
        GemTemplate.TYPE_UNKNOW1 = 12;
        GemTemplate.LKD_30 = 11;
        GemTemplate.LKD_35 = 66;
        GemTemplate.LKD_40 = 245;
        GemTemplate.DA_TIEN_GIAI = 250;
        GemTemplate.BOT_XANH_LA = 249;
        GemTemplate.BOT_TRANG = 246;
        GemTemplate.BOT_XANH = 247;
        GemTemplate.BOT_DO = 248;
        GemTemplate.DUC_TUONG_KHAM_1 = 60;
        GemTemplate.DUC_TUONG_KHAM_2 = 61;
        GemTemplate.DUC_TUONG_KHAM_3 = 62;
        GemTemplate.DUC_TUONG_KHAM_4 = 63;
        GemTemplate.DUC_TUONG_KHAM_5 = 64;
        GemTemplate.DUC_TUONG_KHAM_6 = 65;
        GemTemplate.DA_MAY_MAN_C3 = 7;
        GemTemplate.DA_MAY_MAN_C4 = 155;
        GemTemplate.DA_MAY_MAN_C5 = 156;
        GemTemplate.XUONG_C6 = 234;
        GemTemplate.XUONG_C5 = 233;
        GemTemplate.XUONG_C4 = 232;
        GemTemplate.XUONG_C3 = 231;
        GemTemplate.XUONG_C2 = 230;
        GemTemplate.XUONG_C1 = 229;
        GemTemplate.VAI_C1 = 68;
        GemTemplate.SAT_C1 = 75;
        GemTemplate.NGOC_C1 = 82;
        GemTemplate.GO_THUONG_C1 = 89;
        GemTemplate.DA_MEM_C1 = 96;
        GemTemplate.DA_TACH_NL_DAC_BIET = 178;
        GemTemplate.NGOC_HUYEN_MINH_1 = 235;
        GemTemplate.NGOC_HUYEN_MINH_2 = 236;
        GemTemplate.NGOC_HUYEN_MINH_3 = 237;
        GemTemplate.NGOC_HUYEN_MINH_4 = 238;
        GemTemplate.NGOC_HUYEN_MINH_5 = 239;
        GemTemplate.NGOC_HUYEN_MINH_6 = 240;
        GemTemplate.DA_NGU_HOP_4 = 152;
        GemTemplate.DA_NGU_HOP_5 = 153;
        GemTemplate.DA_NGU_HOP_6 = 154;
        GemTemplate.DA_THUOC_TINH_05 = 228;
        GemTemplate.GIAY_PHAO = 251;
        GemTemplate.THUOC_PHAO = 252;
        GemTemplate.MANG_CAU = 253;
        GemTemplate.SUNG = 254;
        GemTemplate.DUA = 255;
        GemTemplate.DU_DU = 256;
        GemTemplate.XOAI = 257;
        GemTemplate.HOA_THACH_C1 = 258;
        GemTemplate.HOA_THACH_C2 = 259;
        GemTemplate.NGOC_1s = 260;
        GemTemplate.NGOC_2s = 261;
        GemTemplate.NGOC_3s = 262;
        GemTemplate.NGOC_4s = 263;
        GemTemplate.NGOC_5s = 264;
        GemTemplate.NGOC_6s = 265;
        GemTemplate.NGOC_7s = 266;
        GemTemplate.BAN_DO_KHO_BAU = 267;
        GemTemplate.ID_MATERIAL_LOW = new short[][]{{68, 75, 82, 89, 96}, {69, 76, 83, 90, 97}, {70, 77, 84, 91, 98}, {71, 78, 85, 92, 99}, {72, 79, 86, 93, 100}, {73, 80, 87, 94, 101}};
        GemTemplate.ID_MATERIAL_HIGHT = new short[][]{{103, 110, 117, 124, 131}, {104, 111, 118, 125, 132}, {105, 112, 119, 126, 133}, {106, 113, 120, 127, 134}, {107, 114, 121, 128, 135}, {108, 115, 122, 129, 136}};
        GemTemplate.ID_TU_BINH = new int[][]{{159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174}, {179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194}, {195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210}, {211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226}};
        GemTemplate.ID_NGOC_RONG = new short[]{GemTemplate.NGOC_1s, GemTemplate.NGOC_2s, GemTemplate.NGOC_3s, GemTemplate.NGOC_4s, GemTemplate.NGOC_5s, GemTemplate.NGOC_6s, GemTemplate.NGOC_7s};
        GemTemplate.ID_XUONG = new short[]{229, 230, 231, 232, 233, 234};
        GemTemplate.ID_BOT = new short[]{246, 247, 248, 249};
        GemTemplate.ID_DUC = new short[]{60, 61, 62, 63, 64, 65, 66};
        GemTemplate.ID_DA_NGU_HOP = new short[][]{{137, 138, 139, 140, 141, 142}, {143, 144, 145, 146, 147, 148}, {149, 150, 151, 152, 153, 154}};
        GemTemplate.ID_NGOC_HUYEN_MINH = new short[]{235, 236, 237, 238, 239, 240};
        RequestNPCInfoHandler.idMapLoad = new int[]{11, 13, 14, 15, 18, 19, 20, 23, 24, 10, 114, 5, 25};
        RequestNPCInfoHandler.xyNPC = new int[][]{{68, 63}, {12, 48}, {30, 6}, {9, 19}, {44, 54}, {55, 53}, {8, 42}, {47, 5}, {21, 5}, {71, 123}, {64, 8}, {44, 41}, {68, 63}, {68, 63}, {68, 63}};
        RequestNPCInfoHandler.posHoaTieu = new int[][]{{71, 123, 9, 19, 12, 48, 55, 53}, {9, 19, 8, 42, 47, 5, 44, 54, 64, 8}, {68, 63, 55, 53}, {71, 123, 44, 54}, {9, 19, 44, 54, 71, 123, 68, 63}, {30, 6, 68, 63, 21, 5, 12, 48, 64, 8}, {71, 123, 47, 5, 9, 19, 8, 42}, {71, 123, 64, 8, 44, 54, 8, 42}, {12, 48, 55, 53}, {68, 63, 30, 6, 55, 53, 64, 8}, {12, 48, 30, 6, 55, 53, 21, 5, 8, 42}};
        RequestNPCInfoHandler.IDMAP_HOA_TIEU = new int[][]{{10, 15, 13, 19}, {15, 20, 23, 18, 114}, {11, 19}, {10, 18}, {15, 18, 10, 11}, {14, 11, 24, 13, 114}, {10, 23, 15, 20}, {10, 114, 18, 20}, {13, 19}, {11, 14, 19, 114}, {13, 14, 19, 24, 20}};
    }

    public static int[] getXYMap(final int index, final int option) {
        final int[] xy = {RequestNPCInfoHandler.posHoaTieu[index][option * 2], RequestNPCInfoHandler.posHoaTieu[index][option * 2 + 1]};
        return xy;
    }

    public static int getIDMap(final int index, final int option) {
        return RequestNPCInfoHandler.IDMAP_HOA_TIEU[index][option];
    }

    @Override
    public void process(final Session session, final Message message) throws IOException {
        final Message m = new Message(23);
        final byte npcType = message.dis.readByte();
        if (TeamServer.isServerLocal()) {
            System.out.println("NPC REQUEST " + npcType);
        }
        //System.out.println("NPC REQUEST " + npcType);
        Char p = null;
        Message msg = null;
        String[] menu = {""};
        // NPC tinh co menu dich vu rieng: Y ta Chansey (0), Meowth ngan hang (22 / 31), Kangaskhan thu kho (9 / 30:
        // kho do + mo rong hanh trang) - xem real.NpcDichVu.
        if (npcType == real.NpcDichVu.NPC_Y_TA || npcType == real.NpcDichVu.NPC_PIDGEOT
                || npcType == real.NpcDichVu.NPC_MEOWTH || npcType == real.NpcDichVu.NPC_MEOWTH_DOI_XU
                || npcType == real.NpcDichVu.NPC_KANGASKHAN || npcType == real.NpcDichVu.NPC_KANGASKHAN_2) {
            if (real.NpcDichVu.moMenu(CharManager.instance.getByUserID(session.userID), npcType)) {
                return;
            }
        }
        switch (npcType) {
            // Npc ADMIN
            case -2: {
//                p = CharManager.instance.getByUserID(session.userID);
//                if (p != null) {
//                    menu = new String[]{
//                         "Hỗ trợ",
//                        "Nhận quà nạp tích lũy",
//                        "Bật phi phong VIP", 
//                        "Tinh luyện thần binh",
//                        "Kích ẩn mặt nạ cáo hoặc thỏ"
//                    };
//                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
//                    session.sendMessage(msg);
//                }
                // NPC nay chi admin thay (xem CharCapNhat.updateNearChar + Map.laNpcAnCuaAdmin).
                // Cham vao -> mo bang setting. Nguoi choi thuong khong mo duoc gi.
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.isAdmin) {
                    real.AdminMenu.moMenu(p);
                }
                return;
            }
            case -67: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && Char.isSuKienTet2017()) {
                    menu = new String[]{"Nhận lì xì", "Code lì xì", "Tưới nước"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                    return;
                }
                break;
            }
            case -65: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p == null) {
                    break;
                }
                if (!Char.isSuKienTet2017() && Char.isFinishAllSuKienTrungThul2016()) {
                    // Cay thong Noel (Abomasnow, Map.sendInfoNpc) dung chung id -65 nhung khong co menu rieng.
                    session.sendMessage(MessageCreator.createServerAlertMessage(Char.isSuKienNoel() || Char.isSuKienNoel2023()
                            ? "Cây thông Noel lấp lánh ánh đèn. Hãy gặp Delibird ông già Noel để đổi quà Giáng sinh!"
                            : "Sự kiện đã kết thúc. Hẹn gặp lại bạn ở sự kiện sau!", ""));
                    return;
                }
                if (Char.isSuKienTet2017()) {
                    final CharCooking c = Char.nauBanhChung.listCharCooking.get(p.charname);
                    final String nameoption = Char.nauBanhChung.getNameOption();
                    menu = new String[]{nameoption, "Số bánh góp: " + ((c != null) ? c.gop : 0), "Số bánh nhận: " + ((c != null) ? c.nbanh : 0)};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                if (!Char.isFinishAllSuKienTrungThul2016()) {
                    menu = new String[]{"Đốt đèn", "Thả đèn(20L+1 nến)", "Thả đèn(10tr xu+1 nến)", "Mua nến", "Đổi NLT (20 nến + 10kL)"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                    break;
                }
                break;
            }
            case -66: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && Char.isSuKienTet2017()) {
                    final CharCooking c = Char.nauBanhTet.listCharCooking.get(p.charname);
                    final String nameoption = Char.nauBanhTet.getNameOption();
                    menu = new String[]{nameoption, "Số bánh góp: " + ((c != null) ? c.gop : 0), "Số bánh nhận: " + ((c != null) ? c.nbanh : 0)};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -64: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.charCopy != null && !p.charCopy.isFollow() && Map.inRangeActor(p, p.charCopy, 50)) {
                    menu = new String[]{"Dẫn về"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -63: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Về làng"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -62: {
                
                return;
            }
            case -54: {
                return;
            }
            case -55: {
                return;
            }
            case -56: {
                return;
            }
            case -57: {
                return;
            }
            case -58: {
                return;
            }
            case -59: {
                return;
            }
            case -60: {
                return;
            }
            case -61: {
                return;
            }
            case -53:
            case -52: {
                return;
            }
            case -51: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.monster == null) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(
                            "Bạn không có hàng cần giao. Hãy nhận hàng ở Tổng tiêu đầu trong thị trấn rồi hộ tống tới đây.", ""));
                    return;
                }
                if (p != null && p.monster != null) {
                    menu = new String[]{"Trả hàng"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -50: {
                p = CharManager.instance.getByUserID(session.userID);
//                if (p != null) {
//                    menu = new String[]{"Xuống ngựa", "Đổi danh hiệu", "Đăng ký lôi đài", "Vào sảnh chờ", "Xem lôi đài"};
//                    msg = MessageCreator.createMsgMenu(menu, npcType, 10);
//                    session.sendMessage(msg);
//                }
                if (p == null) {
                    return;
                }
                // Truoc day nhanh "co luot van tieu" chi gui menu khi myCountry == inCountry,
                // con lai KHONG gui gi -> nguoi choi co luot van tieu ma dang o lanh tho nuoc
                // khac thi bam npc khong ra gi, mat luon ca muc Ho Tro.
                if (p.luot_van_tieu > 0 && p.myCountry == p.inCountry) {
                    menu = new String[]{"Nhận hàng giao (" + p.luot_van_tieu + " lượt)", "Xuống thú cưỡi", "Đổi danh hiệu", "Đăng ký sàn đấu", "Vào sảnh chờ", "Xem sàn đấu", "Hỗ trợ"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                } else {
                    menu = new String[]{"Xuống thú cưỡi", "Đổi danh hiệu", "Đăng ký sàn đấu", "Vào sảnh chờ", "Xem sàn đấu", "Hỗ trợ"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 10);
                }
                session.sendMessage(msg);
                return;

            }
            case -49: { // NPC su kien: menu theo MOT thu tu uu tien voi xu ly (real.SuKienNpc)
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = real.SuKienNpc.menu(p);
                    if (menu == null) {
                        session.sendMessage(MessageCreator.createServerAlertMessage("Sự kiện đã kết thúc. Hẹn gặp lại bạn ở sự kiện sau!", ""));
                        return;
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -101:
            case -100: {
                // Hoa Tieu moi cho map 26/113 (lo #4 loi.xlsx) - menu rieng, khong
                // dung mang IDMAP_HOA_TIEU dung chung voi -38..-48.
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    int[] dest = (npcType == -100) ? new int[]{114, 10} : new int[]{114, 15};
                    menu = new String[dest.length];
                    for (int i = 0; i < dest.length; ++i) {
                        menu[i] = Map.getNameMap(dest[i]);
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -48:
            case -47:
            case -46:
            case -45:
            case -44:
            case -43:
            case -42:
            case -41:
            case -40:
            case -39:
            case -38: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[RequestNPCInfoHandler.IDMAP_HOA_TIEU[npcType + 48].length];
                    for (int i = 0; i < RequestNPCInfoHandler.IDMAP_HOA_TIEU[npcType + 48].length; ++i) {
                        menu[i] = Map.getNameMap(RequestNPCInfoHandler.IDMAP_HOA_TIEU[npcType + 48][i]);
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -37: {
                if (!Map.openMarket) {
                    session.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Mua bán"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -99: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{
             
                        "Nhận quà nạp tích lũy",
                        "Bật ruy băng VIP", 
                        "Tinh luyện bảo vật",
                        "Kích ẩn mặt nạ cáo hoặc thỏ"
                    };
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -36: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    if (getDayTime(0L).compareTo("2023-11-05 00:00:00") >= 0) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Sự kiện nhận quà đã kết thúc."));
                        return;
                    }
                    if (p.lvDetail.lv < 40) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn phải đạt cấp độ 40 để nhận quà."));
                        return;
                    }
                    if (p.qua_npc == 0) {
                        int[] valuemn = {5, 5, 10, 30, 50};
                        int randomMN = Map.r.nextInt(valuemn.length);
                        Item newitem = Map.doCreateMatNa3(p, 10080, ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                        // session.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận " + newitem.getName() + " 7 ngày"));
                        p.qua_npc = 1;
                        Database.instance.saveOrtherLog("", p.charname, "nhan qua tang haloween", "hopquahlw");
                        Database.instance.UpdateQua_NPC(p);
                    } else {
                        //session.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà này rồi.", ""));
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận quà này rồi."));
                    }
                }
                return;
            }
            case -35: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    if (!Char.isSuKienTetduonglich2024()) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Sự kiện tết dương lịch 2024 đã kết thúc, vui lòng quay lại sau."));
                        return;
                    }

                    if (p.lvDetail.lv < 40) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn phải đạt cấp độ 40 để nhận quà."));
                        return;
                    }
                    final Char pKeoQua = p;
                    if (!pKeoQua.truNeuDu(
                            () -> pKeoQua.potions[137] >= 10 && pKeoQua.getLuong() >= 50,
                            () -> { pKeoQua.subLuong(50); pKeoQua.potions[137] -= 10; })) {
                        if (p.potions[137] < 10) {
                            p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn phải có ít nhất 10 kẹo để đổi quà"));
                        } else {
                            p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn phải có ít nhất 50 lượng mở để đổi quà"));
                        }
                        return;
                    }
                    int Qua = Map.ranDom(0, 20);
                    if (Qua == 9) {
                        long day = Map.ranDom(3, 7);
                        p.createHeo(day, true);
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        Database.instance.saveOrtherLog("", p.charname, "da doi duong khang", "duongkhang");
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận Piloswine " + day + " ngày."));
                        return;
                    }
                    if (Qua == 8) {
                        int day = Map.ranDom(3, 7);
                        int[] valuemn = {5, 5, 10, 30, 50};
                        int randomMN = Map.r.nextInt(valuemn.length);
                        Item newitem = Map.doCreateMatNa3(p, (60 * 24 * day), ID_MAT_NA[randomMN], randomMN, valuemn[randomMN]);
                        // session.sendMessage(MessageCreator.createServerAlertMessage("Nhận " + newitem.getName() + " 7 ngày", ""));
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận " + newitem.getName() + " " + day + " ngày"));
                        return;
                    }
                    if (Qua == 7) {
                        long day = Map.ranDom(3, 7);
                        p.createTuanLoc(day);
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        Database.instance.saveOrtherLog("", p.charname, "da doi linh thu tuan loc", "tuanloc");
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận thú cưỡi Stantler " + day + " ngày."));
                        return;
                    }
                    if (Qua == 6) {
                        long day = Map.ranDom(3, 7);
                        p.createPoro(day, true);
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        Database.instance.saveOrtherLog("", p.charname, "da doi dai bach thu", "dai bach thu");
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận Wooloo " + day + " ngày."));
                        return;
                    }
                    if (Qua == 5) {
                        long day = Map.ranDom(3, 7);
                        p.createNguaXuong(day, true);
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        Database.instance.saveOrtherLog("", p.charname, "da doi bach cot", "bach cot");
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận Spectrier " + day + " ngày."));
                        return;
                    }
                    if (Qua == 4) {
                        int luongLock = Map.ranDom(100, 1000);
                        // Bo sung kiem toan A6: qua addLuongLock (KHOA_TIEN, tran) + log, khong cong thang.
                        p.addLuongLock(luongLock);
                        Database.instance.saveOrtherLog("", p.charname, "doi keo nhan " + luongLock + " luong khoa", "doikeo");
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn đã nhận " + luongLock + " lượng khoá."));
                        return;
                    }
                    int StoneC = Map.random(GemTemplate.ID_MATERIAL_LOW[5].length);
                    for (int stone1 = 0; stone1 < 5; stone1++) {
                        p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][StoneC], 1, true);
                    }
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    //p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được 3 nguyên liệu sơ cấp 6 khóa", ""));
                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn nhận được 5 nguyên liệu sơ cấp 6 khóa"));
                }
                return;
            }
            case -34: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.map.nRegion > 0) {
                    menu = new String[p.map.nRegion];
                    for (int i = 0; i < p.map.nRegion; ++i) {
                        menu[i] = "Khu " + (i + 1);
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -32: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.myCountry < 2) {
                    // Chi Pokemon vung Hoenn (myCountry 2) chon vung o day (MapDoMenu case -32); vung khac doi o Tong quan.
                    session.sendMessage(MessageCreator.createServerAlertMessage("Chỉ Pokémon vùng Hoenn mới chọn vùng ở đây. Muốn đổi vùng hãy gặp Tổng quản (mục Chuyển vùng).", ""));
                    return;
                }
                if (p != null) {
                    menu = new String[]{"Chuyển sang " + Map.nameCountry[0], "Chuyển sang " + Map.nameCountry[1]};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -31: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    // DA GO 4 MUC khoi menu dau truong:
                    //   vi tri cu 2 "Dang ky chien truong"
                    //   vi tri cu 3 "Nhan qua khu lien dau"
                    //   vi tri cu 4 "Nhan qua Lien tram"
                    //   vi tri cu 5 "Nhan qua top tru"
                    // Phan xu ly cua chung o MapDoMenu case -31 GIU NGUYEN, khong danh so
                    // lai — MapDoMenu.VI_TRI_MENU_DAU_TRUONG doi vi tri hien thi ve vi tri
                    // goc. Muon bat lai muc nao: them lai chuoi vao day dung thu tu cu roi
                    // them vi tri goc tuong ung vao bang do.
                    menu = new String[]{"Thách đấu cá nhân", "Mê cung", "Núi châu báu"};
                    if (!Char.isFinishAllSuKienMini()) {
                        // Nhanh nay hien khong bao gio chay: isFinishAllSuKienMini() la
                        // getDayTime(0) > "2020-07-11" nen luon dung. Van cat 4 muc o day
                        // cho khop, de neu ai doi moc thoi gian thi menu khong lech bang.
                        menu = new String[]{"Thách đấu cá nhân", "Mê cung", "Núi châu báu", "Đổi áo Absol", "Đổi áo Oricorio", "Đổi quà đặc biệt"};
                    }
                    if (p.idArena > -1) {
                        menu[0] = "Vào lại đấu trường";
                    }
                    if (p.idTongKim > -1) {
                        menu[1] = "Vào lại chiến trường";
                    }
                    // Su kien Vu Lan: noi vao CUOI menu (sau moi muc tren, ke ca muc Mini
                    // neu con hien) chu khong chen vao giua, de VI_TRI_MENU_DAU_TRUONG
                    // (MapDoMenu) khong bi lech vi tri cac muc no da biet. Xem
                    // real/MapSuKien.doMenuDauTruongVuLan.
                    p.idxSuKienVuLanDauTruong = -1;
                    if (real.SuKien.bat(real.SuKien.VU_LAN)) {
                        String[] menuVuLan = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuVuLan, 0, menu.length);
                        menuVuLan[menu.length] = "Sự kiện Vu Lan";
                        p.idxSuKienVuLanDauTruong = menu.length;
                        menu = menuVuLan;
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -30: {
                return;
            }
            case -29: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.idClan <= -1 && !p.map.isMapLienDau()) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(CHUA_CO_DOI, ""));
                    return;
                }
                if (p != null && (p.idClan > -1 || p.map.isMapLienDau())) {
                    menu = new String[]{"Giao thẻ"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -28: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    p = CharManager.instance.getByUserID(session.userID);
                    menu = new String[]{"Thả boss"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -27: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    p = CharManager.instance.getByUserID(session.userID);
                    menu = new String[]{"Đăng ký tiệc cưới", "Vào tiệc cưới", "Khai tiệc"};
                    if (!p.nameHusband_wife.equals("")) {
                        menu = new String[]{"Đăng ký tiệc cưới", "Vào tiệc cưới", "Khai tiệc", "Ly dị (150 lượng)", "Đơn phương ly dị (300 lượng)"};
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -26: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    // Thi tran nha cua vung (mapstart MapDoMenu: Blackthorn 70/1701, Pewter 0/301, Cianwood 80/1901).
                    final int[][] lang = {{70, 1701}, {0, 301}, {80, 1901}};
                    menu = real.NpcDichVu.tenBanDo(lang[Math.max(0, Math.min(2, p.inCountry))]);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -24: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    p = CharManager.instance.getByUserID(session.userID);
                    if (p != null) {
                        if (!Map.openLog) {
                            menu = new String[]{"Mua vé số (1 triệu xu)", "Số đã mua", "Quay số", "Mua 2 vé quay số (10 lượng)", "Mua 2 vé quay số (10 triệu xu)", "Mua 2 vé quay số đặc biệt (10 lượng)"};
                            if (p.canReceiveGif35()) {
                                menu = new String[]{"Mua vé số (1 triệu xu)", "Số đã mua", "Quay số", "Mua 2 vé quay số (10 lượng)", "Mua 2 vé quay số (10 triệu xu)", "Mua 2 vé quay số đặc biệt (10 lượng)","Mua 1 vé quay Moltres (50 lượng)", "Quà 35 ma", "Quà 35 vật"};
                            }
                        } else {
                            menu = new String[]{"Mua vé số (1 triệu xu)", "Số đã mua", "Quay số", "Mua 2 vé quay số (10 lượng)", "Mua 2 vé quay số (10 triệu xu)", "Mua 2 vé quay số đặc biệt (10 lượng)","Mua 2 vé quay Moltres (50 lượng)", "Nhập mã quà tặng", "Kim cương", "Nạp sms", "Tặng lượng(" + p.vetangluong + " vé)"};
                            if (p.canReceiveGif35()) {
                                menu = new String[]{"Mua vé số (1 triệu xu)", "Số đã mua", "Quay số", "Mua 2 vé quay số (10 lượng)", "Mua 2 vé quay số (10 triệu xu)", "Mua 2 vé quay số đặc biệt (10 lượng)", "Mua 2 vé quay Moltres (50 lượng)", "Quà 35 ma", "Quà 35 vật", "Nhập mã quà tặng", "Kim cương", "Nạp sms", "Tặng lượng(" + p.vetangluong + " vé)"};
                            }
                        }
                        msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                        session.sendMessage(msg);
                    }
                }
                return;
            }
            case -23: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.myCountry == -1) {
                    if (p.idClan > -1) {
                        final NewClan clan = NewClan.getClan(p.idClan);
                        if (clan != null) {
                            if (clan.country == -1) {
                                menu = Map.nameCountry;
                                msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                                session.sendMessage(msg);
                            } else {
                                session.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể chọn vùng cho đội.", ""));
                            }
                        }
                    } else {
                        menu = Map.nameCountry;
                        msg = MessageCreator.createMsgMenu(menu, npcType, 1);
                        session.sendMessage(msg);
                    }
                }
                return;
            }
            case -25: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && !(p.myCountry > -1 && p.idClan > -1 && Map.idClanTown[p.myCountry] == p.idClan
                        && NewClan.getClan(p.idClan) != null && NewClan.getClan(p.idClan).master.equals(p.charname))) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(
                            "Lễ quan chỉ phục vụ đội trưởng của đội đang giữ Gym (bảo hộ Gym, đăng ký tấn công vùng).", ""));
                    return;
                }
                if (p != null && p.myCountry > -1 && p.idClan > -1 && Map.idClanTown[p.myCountry] == p.idClan) {
                    final NewClan cl = NewClan.getClan(p.idClan);
                    if (cl != null && cl.master.equals(p.charname)) {
                        final String[] hour = {"Bảo hộ 6h-10h (15 triệu xu đội)", "Bảo hộ 11h-15h (15 triệu xu đội)", "Bảo hộ 16h-20h (15 triệu xu đội)", "Bảo hộ 20h-24h (15 triệu xu đội)"};
                        menu = new String[]{hour[Char.getIdOpenShield(Calendar.getInstance().get(11))], "Bảo hộ toàn bộ (50 triệu xu đội)", "Đăng ký tấn công vùng"};
                        p.sendMessage(MessageCreator.createMsgMenu(menu, -25, 0));
                    }
                }
                return;
            }
            case -22: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    // Cong dich chuyen (Duong Victory 118): sang lanh tho vung kia / xuong Cinnabar (map 9).
                    // Vung Hoenn khong co bien gioi Kanto-Johto o day - bao thay vi im lang.
                    if (p.myCountry == 2 || p.inCountry < 0 || p.inCountry > 1) {
                        session.sendMessage(MessageCreator.createServerAlertMessage(
                                "Cổng dịch chuyển này chỉ nối biên giới Kanto và Johto.", ""));
                        return;
                    }
                    final String cinnabar = real.NpcDichVu.tenBanDo(9)[0];
                    String[][] vilage = {{"Đến biên giới " + Map.nameCountry[1]}, {"Đến biên giới " + Map.nameCountry[0]}};
                    if (!Map.pkAuto) {
                        vilage = new String[][]{{"Đến biên giới " + Map.nameCountry[1], cinnabar}, {"Đến biên giới " + Map.nameCountry[0], cinnabar}};
                    }
                    msg = MessageCreator.createMsgMenu(vilage[p.inCountry], npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -7: {
                return;
            }
            case -21: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = real.NpcDichVu.tenBanDo(114, 2581);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -20: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = real.NpcDichVu.tenBanDo(114, 2581);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -19: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = real.NpcDichVu.tenBanDo(21, 22);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -18: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Nâng cấp kho"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -17: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Nâng cấp nhà"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -16: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Đăng ký"};
                    if (p.petUsing != null) {
                        menu = new String[]{"Đăng ký", "Cho pet ăn", "Nâng cấp", "Nâng cấp dùng Luyện kim dược 30", "Nâng cấp dùng Luyện kim dược 35", "Nâng cấp dùng Luyện kim dược 40"};
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -15: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Đổi hệ", "Tháo ngọc khảm", "Đặt mật khẩu rương", "Vòng quay", "Đồ đã bán"};
                    if (Char.isOpenDauGiaChoiBlackFriday()) {
                        menu = new String[]{"Đổi hệ", "Tháo ngọc khảm", "Đặt mật khẩu rương", "Vòng quay", "Đồ đã bán", "Đấu giá"};
                    } else if (Char.isOpenSellBlackFriday()) {
                        menu = new String[]{"Đổi hệ", "Tháo ngọc khảm", "Đặt mật khẩu rương", "Vòng quay", "Đồ đã bán", "Mua đồ thời trang", "Mua lồng đèn", "Nhận lại tiền cọc"};
                        if (!p.passWord.equals("")) {
                            menu[2] = "Xóa mật khẩu rương";
                        }
                        session.sendMessage(MessageCreator.createMsgMenu(menu, npcType, 12));
                        return;
                    }
                    if (!p.passWord.equals("")) {
                        menu[2] = "Xóa mật khẩu rương";
                    }
                    if (Char.isSuKienBlackFriday()) {
                        menu = new String[]{"Đổi hệ", "Tháo ngọc khảm", "Đặt mật khẩu rương", "Vòng quay", "Đồ đã bán", "Đấu giá", "Mua Chổi Băng(30 ngày)", "Mua Chổi Điện(30 ngày)", "Mua Chổi Độc(30 ngày)", "Mua Chổi Lửa(30 ngày)", "Mua pet Sandshrew(60 ngày)"};
                        session.sendMessage(MessageCreator.createMsgMenu(menu, npcType, 10));
                        return;
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -9: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    final int[][] lang = {{70, 1701}, {0, 301}, {80, 1901}};
                    menu = real.NpcDichVu.tenBanDo(lang[Math.max(0, Math.min(2, p.inCountry))]);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -10: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Hợp đục", "Mở rộng hành trang (150 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh"};
                    if (p.canReceiveGiftOffline > -1) {
                        menu = new String[]{"Hợp đục", "Mở rộng hành trang (150 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh", "Nhận quà top"};
                    }
                    if (p.canReceiveGiftEvent()) {
                        menu = new String[]{"Hợp đục", "Mở rộng hành trang (150 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh", "Nhận quà"};
                    }
                    if (p.maxBag > 1) {
                        menu = new String[]{"Hợp đục", "Mở rộng hành trang (500 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh"};
                        if (p.canReceiveGiftOffline > -1) {
                            menu = new String[]{"Hợp đục", "Mở rộng hành trang (500 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh", "Nhận quà top"};
                        }
                        if (p.canReceiveGiftEvent()) {
                            menu = new String[]{"Hợp đục", "Mở rộng hành trang (500 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Đăng ký giải Liên minh", "Nhận quà"};
                        }
                    }
                    if (!TeamServer.isServerLienDau()) {
                        final boolean isReg = Database.instance.isCharRegisterLiendau(p.charname);
                        if (isReg) {
                            menu[5] = "Cập nhật thông tin";
                        }
                    }
                    if (TeamServer.isServerLienDau()) {
                        menu = new String[]{"Hợp đục", "Mở rộng hành trang (500 lượng)", "Thử vận may", "Chuyển vùng (300 lượng)", "Nhận quà Giftcode", "Vào chiến trường", "Kết quả"};
                    }
                    // "Dang ky giai Lien minh" (vi tri 5) chi chay khi co may chu Lien minh (sv.me=1 / may chu
                    // Lien minh) - con lai chi bao "Chuc nang dang phat trien" -> go khoi menu, MapDoMenu doi vi tri.
                    p.boMucTongQuan = -1;
                    if (!TeamServer.isServerLienDau() && !Map.openLog && menu.length > 5) {
                        final String[] bo = new String[menu.length - 1];
                        System.arraycopy(menu, 0, bo, 0, 5);
                        System.arraycopy(menu, 6, bo, 5, menu.length - 6);
                        menu = bo;
                        p.boMucTongQuan = 5;
                    }
                    // Nhan moc nap (49k/99k/299k): luon la muc cuoi menu, ghi nho vi tri
                    // that su cua no vao Char de Map.doMenu nhan dung du menu dai/ngan khac nhau.
                    // Che do Pokemon (LauDai): qua moc nap da ngung (Map.doChonMocNap) -> khong hien muc.
                    p.idxMocNapMenu = -1;
                    if (!com.htbb.pokemon.LauDai.bat()) {
                        String[] menuMocNap = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuMocNap, 0, menu.length);
                        menuMocNap[menu.length] = "Nhận mốc nạp";
                        p.idxMocNapMenu = menu.length;
                        menu = menuMocNap;
                    }
                    // Su kien World Cup: cung noi vao CUOI menu chu khong chen vao giua,
                    // de moi vi tri phia truoc giu nguyen. Xem real/SuKienWorldCup.
                    p.idxSuKienWorldCup = -1;
                    if (real.SuKienWorldCup.hienTrongMenu()) {
                        String[] menuSuKien = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuSuKien, 0, menu.length);
                        menuSuKien[menu.length] = "Sự kiện World Cup";
                        p.idxSuKienWorldCup = menu.length;
                        menu = menuSuKien;
                    }
                    // Thuong dua top (trang quan tri web): cung noi vao CUOI menu, CHI hien
                    // voi nhan vat co ten trong bang tob_top_thuong va chua nhan. Xem
                    // real/SuKienDuaTop.
                    p.idxThuongDuaTop = -1;
                    if (real.SuKienDuaTop.coThuongChoNhan(p.charname)) {
                        String[] menuThuongTop = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuThuongTop, 0, menu.length);
                        menuThuongTop[menu.length] = "Nhận thưởng đua top";
                        p.idxThuongDuaTop = menu.length;
                        menu = menuThuongTop;
                    }
                    // Chuyen luong (them 19/09/2026, theo yeu cau nguoi dung): cung noi vao
                    // CUOI menu. Xem real/CharChuyenLuong.
                    {
                        String[] menuChuyenLuong = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuChuyenLuong, 0, menu.length);
                        menuChuyenLuong[menu.length] = "Chuyển lượng";
                        p.idxChuyenLuong = menu.length;
                        menu = menuChuyenLuong;
                    }
                    // Kho xu/luong (them 21/09/2026, theo yeu cau nguoi dung): cung noi vao
                    // CUOI menu. Xem real/KhoXuLuong.
                    {
                        String[] menuKho = new String[menu.length + 1];
                        System.arraycopy(menu, 0, menuKho, 0, menu.length);
                        menuKho[menu.length] = "Kho xu/lượng";
                        p.idxKhoXuLuong = menu.length;
                        menu = menuKho;
                    }
                    // nap tien: muc "Cửa hàng & Nạp" noi CUOI menu (tat co: menu y nguyen).
                    menu = com.htbb.pokemon.NapTienGame.noiMenuTongQuan(p, menu);
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -90: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    // "Go skin linh thu" bi bo khoi menu nay theo yeu cau nguoi choi (giu
                    // lai hai duong go khac: lenh chat "boskin" va goi packet 29 chung cho
                    // moi trang bi thu — xem SkinThuCuoi.thaoSkin / Map.thaoTrangBiThu).
                    menu = new String[]{
                            "Điểm hiện tại: " + p.diemDoiThuong + " điểm - Xem đổi thưởng"
                    };
                    // Pokemon (G7.D3): noi muc "Pokédex" vao CUOI menu, dung vi tri ma
                    // MapDoMenu cho (PokedexPokemon.VI_TRI_MUC_NPC_DOI_THUONG). Menu doi so muc
                    // thi phai doi hang so do - khong khop thi khong hien, tranh chon nham muc.
                    if (menu.length == real.PokedexPokemon.VI_TRI_MUC_NPC_DOI_THUONG) {
                        final String[] menuPokedex = java.util.Arrays.copyOf(menu, menu.length + 1);
                        menuPokedex[menu.length] = real.PokedexPokemon.TEN_MUC;
                        menu = menuPokedex;
                    }
                    // GD2: "So tay huan luyen" (thiet ke dai han) noi vao cuoi, dung vi tri MapDoMenu cho.
                    if (com.htbb.pokemon.LauDai.bat() && menu.length == real.SoTayLauDai.viTri()) {
                        final String[] menuSoTay = java.util.Arrays.copyOf(menu, menu.length + 1);
                        menuSoTay[menu.length] = real.SoTayLauDai.TEN_MUC;
                        menu = menuSoTay;
                    }
                    // he sinh thai: muc "He sinh thai" noi vao cuoi, dung vi tri MapDoMenu cho.
                    if (com.htbb.pokemon.HeSinhThai.bat() && menu.length == com.htbb.pokemon.HeSinhThaiMenu.viTri()) {
                        final String[] menuHst = java.util.Arrays.copyOf(menu, menu.length + 1);
                        menuHst[menu.length] = com.htbb.pokemon.HeSinhThaiGame.TEN_MUC;
                        menu = menuHst;
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -11: {
                p = CharManager.instance.getByUserID(session.userID);
                msg = MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Ngọc hoàng hôn", "Ngọc hoàng hôn khoá", "Bột thường", "Bột khoá"}, npcType, 0);
                if (p.hlt_buy > 0) {
                    msg = MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Ngọc hoàng hôn", "Ngọc hoàng hôn khoá", "Bột thường", "Bột khoá", "Kẹo thú cưng(" + p.hlt_buy + ")"}, npcType, 0);
                }
                session.sendMessage(msg);
                return;
            }
            case -12: {
                msg = MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Xương không khóa", "Xương khóa"}, npcType, 0);
                session.sendMessage(msg);
                return;
            }
            case -8: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    p.infoItemCreate = null;
                    menu = new String[]{"Đồ cầm tay", "Trang bị", "Trang bị thú",
                        "Nhận thành tích(ĐN " + p.diemNapVip + " , ĐT " + p.diemxaiVip + ", MN: " + (p.luongNap * 2) + ")",
                        // "Hoat dong gio" (chi bao "Sap ra mat") da go 19/10/2026 - RealController case -8
                        // doi vi tri hien thi >= 4 len 1 bac cho khop case cu.
                        (p.isTatPhiPhong() ? "Bật" : "Tắt") + " phi phong", 
                        (p.isTatMatNa() ? "Bật" : "Tắt") + " mặt nạ",
                        (p.isTatCanh() ? "Bật" : "Tắt") + " cánh",
                        // "Than Khi" doi xuong cuoi menu (05/09/2026) - moi muc tu day tro xuong
                        // deu dich len 1 bac, doi case trong RealController.doCreateItem cho khop.
                        // "Doi ten nhan vat" chen giua (19/09/2026, yeu cau nguoi dung dat dung
                        // giua menu) - MOI muc TU DAY TRO XUONG lai dich len 1 bac NUA so voi
                        // ghi chu cu o tren, da doi lai toan bo case trong RealController.doCreateItem.
                        "Đổi tên Pokémon",
                        "Tinh luyện bảo vật", "Gia hạn đồ cầm tay thời trang (từ 5.000 lượng / 7 ngày)", "Cải tạo cải trang", "Bộ sưu tập",
                        "Hoá Hình Pet" + (p.petUsing != null && p.petUsing.dangHoaHinh() ? " (Lân cấp " + p.petUsing.capHoaHinh + ")" : ""),
                        (p.isTatSkinLinhThu() ? "Bật" : "Tắt") + " hiển thị skin thú",
                        // "Go skin thu" (11/09/2026) - muc rieng ngang hang Than Khi, dat TRUOC
                        // no theo yeu cau, bam la go luon (khong mo submenu). Xem case trong
                        // RealController.doCreateItem - da doi cheo cho khop lai vi tri.
                        "Gỡ skin thú", "Bảo Vật Huyền Thoại"};
                    if (TeamServer.isServerLienDau()) {
                        menu = new String[]{"Đồ cầm tay", "Trang bị", "Trang bị thú"};
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -14: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Quay bằng lượng", "Quay bằng xu"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -6: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Tạo thú", "Luyện thú"};
                    if (p.animalRide != null) {
                        final String tenMucHoaHinh = "Hóa Hình" + (p.animalRide.capHoaHinh > 0 ? " (cấp " + p.animalRide.capHoaHinh + "/5)" : "");
                        menu = new String[]{"Tạo thú", "Luyện thú", "Nâng cấp thú (" + Animal.price[3][p.animalRide.level] + " lượng)", tenMucHoaHinh};
                        if ((p.animalRide.isPhuongHoangBang() && p.animalRide.level >= 4 && p.animalRide.KHANG_BI_CHAY == 0) || (p.animalRide.isPhuongHoangLua() && p.animalRide.level >= 4 && p.animalRide.KHANG_DONG_BANG == 0) || (p.animalRide.isLan() && p.animalRide.level >= 4 && p.animalRide.KHANG_HOA_TUYET == 0)
                                // Rong bang: mo ca hai khang (bi chay + dong bang) trong mot lan
                                || (p.animalRide.isRongLon() && p.animalRide.level >= 4
                                    && (p.animalRide.KHANG_BI_CHAY == 0 || p.animalRide.KHANG_DONG_BANG == 0))) {
                            menu = new String[]{"Tạo thú", "Luyện thú", "Nâng cấp thú (" + Animal.price[3][p.animalRide.level] + " lượng)", tenMucHoaHinh, "Nâng cấp kháng (200 lượng)"};
                        }
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -4: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.idClan <= -1 && !p.map.isMapLienDau()) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(CHUA_CO_DOI, ""));
                    return;
                }
                if (p != null && (p.idClan > -1 || p.map.isMapLienDau())) {
                    menu = new String[]{"Giao thẻ"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -5: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.idClan <= -1 && !p.map.isMapLienDau()) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(CHUA_CO_DOI, ""));
                    return;
                }
                if (p != null && (p.idClan > -1 || p.map.isMapLienDau())) {
                    menu = new String[]{"Giao thẻ"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -13: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.idClan <= -1) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(CHUA_CO_DOI, ""));
                    return;
                }
                if (p != null && p.idClan > -1) {
                    menu = new String[]{"Kỹ năng đội", "Kỹ năng thành viên", "Mua 500 điểm cống hiến (100 lượng)"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
            case -3: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null && p.idClan <= -1) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(CHUA_CO_DOI, ""));
                    return;
                }
                if (p != null && p.idClan > -1) {
                    menu = new String[]{"Nâng cấp đội", "Danh sách quản lý đội", "Đặt thuế"};
                    if (p.rankClan == 0) {
                        final NewClan clan = NewClan.getClan(p.idClan);
                        final long money = clan.getMoneyDistributeSalary(p.idClan);
                        menu = new String[]{"Nâng cấp đội", "Danh sách quản lý đội", "Đặt thuế", "Phong chức", "Giáng chức", "Phát lương (tối đa " + money + " xu)", "Lương thành viên đội", "Phát lương nhanh (tối đa " + money + " xu)"};
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                }
                return;
            }
           
            case 1:
            case 26: {
                m.dos.writeByte(0);
                m.dos.writeByte(real.NpcDichVu.THUOC_BAN.length);
                for (final int idThuoc : real.NpcDichVu.THUOC_BAN) {
                    m.dos.writeByte(idThuoc);
                }
                break;
            }
            case 24: {
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeShort(50);
                for (int i = 214; i <= 263; ++i) {
                    m.dos.writeShort(i);
                }
                break;
            }
            case 28: {
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                m.dos.writeShort(40);
                for (int i = 174; i <= 213; ++i) {
                    m.dos.writeShort(i);
                }
                break;
            }
            case 2: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    menu = new String[]{"Mua đồ cầm tay", "Mua trang bị thú"};
                    if (TeamServer.isServerIndo()) {
                        menu = new String[]{"Membeli senjata"};
                    }
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                    return;
                }
                if (p == null) {
                    return;
                }
                m.dos.writeByte(1);
                m.dos.writeByte(-1);
                if (p.mapID < 200) {
                    m.dos.writeShort(97);
                    for (int i = 79; i <= 113; ++i) {
                        m.dos.writeShort(i);
                    }
                    for (int i = 465; i <= 466; ++i) {
                        m.dos.writeShort(i);
                    }
                    for (int i = 509; i <= 568; ++i) {
                        m.dos.writeShort(i);
                    }
                    break;
                }
                if (p.mapID > 200) {
                    m.dos.writeShort(40);
                    for (int i = 174; i <= 213; ++i) {
                        m.dos.writeShort(i);
                    }
                    break;
                }
                break;
            }
            case 27: {
                final int giap1 = message.dis.readByte();
                p = CharManager.instance.getByUserID(session.userID);
                if (p != null) {
                    p.classBuy = (byte) giap1;
                    menu = new String[]{"Trang bị đặc biệt", "Trang bị vật lý"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                    return;
                }
                m.dos.writeByte(1);
                m.dos.writeByte(giap1);
                m.dos.writeShort(164);
                for (int j = 21; j <= 26; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 47; j <= 52; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 73; j <= 78; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 123; j <= 125; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 135; j <= 137; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 147; j <= 149; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 159; j <= 161; ++j) {
                    m.dos.writeShort(j);
                }
                for (int j = 171; j <= 173; ++j) {
                    m.dos.writeShort(j);
                }
                break;
            }
            case 3: {
                final int giap2 = message.dis.readByte();
                p = CharManager.instance.getByUserID(session.userID);
                if (p == null) {
                    return;
                }
                if (p != null) {
                    p.classBuy = (byte) giap2;
                    menu = new String[]{"Trang bị đặc biệt", "Trang bị vật lý"};
                    msg = MessageCreator.createMsgMenu(menu, npcType, 0);
                    session.sendMessage(msg);
                    return;
                }
                m.dos.writeByte(1);
                m.dos.writeByte(giap2);
                if (p.mapID < 200) {
                    m.dos.writeShort(103);
                    for (int k = 3; k <= 20; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 27; k <= 46; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 53; k <= 72; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 114; k <= 122; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 126; k <= 134; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 138; k <= 146; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 150; k <= 158; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 162; k <= 170; ++k) {
                        m.dos.writeShort(k);
                    }
                    break;
                }
                if (p.mapID > 200) {
                    m.dos.writeShort(33);
                    for (int k = 21; k <= 26; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 47; k <= 52; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 73; k <= 78; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 123; k <= 125; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 135; k <= 137; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 147; k <= 149; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 159; k <= 161; ++k) {
                        m.dos.writeShort(k);
                    }
                    for (int k = 171; k <= 173; ++k) {
                        m.dos.writeShort(k);
                    }
                    break;
                }
                break;
            }
            case 9:
            case 30: {
                m.dos.writeByte(2);
                p = CharManager.instance.getByUserID(session.userID);
                if (p == null) {
                    return;
                }
                // Kho do: trong nha Kangaskhan (78/88/108/262) hoac dung canh Kangaskhan o thi tran (0/70/80/201).
                if (!real.NpcTinhBanDo.oNoiDichVu(p, real.NpcTinhBanDo.NPC_GIU_DO)) {
                    session.sendMessage(MessageCreator.createServerAlertMessage("Hãy đến gần Kangaskhan thủ kho để mở kho đồ.", ""));
                    return;
                }
                p.isPutItem2Bag = true;
                m.dos.writeByte(p.bItem.size());
                for (int k = 0; k < p.bItem.size(); ++k) {
                    m.dos.writeByte(p.bItem.get(k).clazz);
                    m.dos.writeShort(p.bItem.get(k).id);
                    final int template = p.bItem.get(k).getTemplate().id;
                    m.dos.writeShort(template);
                    m.dos.writeByte(p.bItem.get(k).plus);
                    m.dos.writeByte(p.bItem.get(k).level);
                    m.dos.writeShort(p.bItem.get(k).mDurable);
                    m.dos.writeShort(p.bItem.get(k).durable);
                }
                break;
            }
            case 6:
            case 29: {
                m.dos.writeByte(3);
                break;
            }
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20: {
                p = CharManager.instance.getByUserID(session.userID);
                if (p == null) {
                    return;
                }
                // Gian hang ky gui theo vung: nhan vat chua chon vung (myCountry -1) dung gian hang vung dang dung.
                final int vungShop = (p.myCountry >= 0 && p.myCountry < Map.npcSell.length) ? p.myCountry
                        : Math.max(0, Math.min(Map.npcSell.length - 1, p.inCountry));
                m.dos.writeByte(4);
                m.dos.writeByte(Map.npcSell[vungShop][npcType - 11].shop.length);
                for (int k = 0; k < Map.npcSell[vungShop][npcType - 11].shop.length; ++k) {
                    m.dos.writeByte(Map.npcSell[vungShop][npcType - 11].shop[k].id);
                }
                m.dos.writeByte(npcType);
                m.dos.writeByte(message.dis.readByte());
                break;
            }
        }
        session.sendMessage(m);
    }
}
