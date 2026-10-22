package real;


import data.BossMatQuy;
import data.CharWeaponData;
import data.Text;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;

/**
 * Than cac method nap tai nguyen cua Map: nap bang ngoc, NPC, ban do,
 * tile map, boss, vi tri, anh vu khi.
 *
 * Tach tu real/Map.java. Cac method VAN CON trong Map, giu NGUYEN chu ky cu,
 * chi than chuyen sang day - nen lop loi cu (phuc hoi tu htbb-core.jar o P3) va cac lop
 * con (OfflineMap override loadTileMap) khong bi anh huong.
 * Tham so "Map map" thay cho "this" truoc day.
 */
class MapNapTaiNguyen {

    static void loadGemTemplateIndo() {
        Map.gemTemplate = new GemTemplate[]{
            new GemTemplate(0, 0, Text.BAO_HIEM_20, Text.BAO_HIEM_20_DCR, 20, 0, 100, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(1, 0, Text.BAO_HIEM_30, Text.BAO_HIEM_30_DCR, 30, 2, 200, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(2, 1, Text.BAO_HIEM_40, Text.BAO_HIEM_40_DCR, 40, 3, 300, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(3, 1, Text.BAO_HIEM_50, Text.BAO_HIEM_50_DCR, 50, 1, 400, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(4, 2, Text.BAO_HIEM_90, Text.BAO_HIEM_90_DCR, 90, 4, 500, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(5, 3, Text.DA_MAY_MAN_1, Text.DA_MAY_MAN_1_DCR, 20, -1, 500, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(6, 4, Text.DA_MAY_MAN_2, Text.DA_MAY_MAN_2_DCR, 40, -1, 600, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(7, 5, Text.DA_MAY_MAN_3, Text.DA_MAY_MAN_3_DCR, 60, -1, 800, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(8, 6, Text.LKD, Text.LKD_2_DCR, 20, -1, 600, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(9, 7, Text.LKD, Text.LKD_3_DCR, 30, -1, 700, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(10, 8, Text.LKD, Text.LKD_4_DCR, 40, -1, 800, GemTemplate.TYPE_IMBUE, 1, 0),
            new GemTemplate(11, 17, Text.LKD, Text.LKD_30_DCR, 30, -1, 1, GemTemplate.TYPE_IMBUE, 0, 1),
            new GemTemplate(12, 10, Text.DA_KHAM_1SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(13, 13, Text.DA_KHAM_1NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(14, 13, Text.DA_KHAM_1TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(15, 10, Text.DA_KHAM_1SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(16, 10, Text.DA_KHAM_2SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
            new GemTemplate(17, 13, Text.DA_KHAM_2NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
            new GemTemplate(18, 13, Text.DA_KHAM_2TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
            new GemTemplate(19, 10, Text.DA_KHAM_2SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
            new GemTemplate(20, 10, Text.DA_KHAM_3SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
            new GemTemplate(21, 13, Text.DA_KHAM_3NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
            new GemTemplate(22, 13, Text.DA_KHAM_3TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
            new GemTemplate(23, 10, Text.DA_KHAM_3SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
            new GemTemplate(24, 10, Text.DA_KHAM_4SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
            new GemTemplate(25, 13, Text.DA_KHAM_4NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
            new GemTemplate(26, 13, Text.DA_KHAM_4TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
            new GemTemplate(27, 10, Text.DA_KHAM_4SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
            new GemTemplate(28, 9, Text.GAY_MU_1, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(29, 12, Text.DONG_BANG_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(30, 11, Text.TRUNG_DOC_1, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(31, 9, Text.GAY_CHOANG_1, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(32, 13, Text.HOA_THACH_1, Text.KHAM_NHAN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(33, 11, Text.GIAM_TOC_1, Text.KHAM_NON_QUAN_DCR, new byte[]{1, 2}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(34, 11, Text.KHANG_GIAM_TOC_10, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(35, 11, Text.KHANG_TRUNG_DOC_10, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(36, 9, Text.KHANG_GAY_MU_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(37, 12, Text.KHANG_DONG_BANG_10, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(38, 9, Text.KHANG_GAY_CHOANG_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(39, 10, Text.KHANG_HOA_THACH_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
            new GemTemplate(40, 11, Text.KHANG_GIAM_TOC_20, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(41, 11, Text.KHANG_TRUNG_DOC_20, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(42, 9, Text.KHANG_GAY_MU_20, "Khảm vào găng tay và giày", new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(43, 12, Text.KHANG_DONG_BANG_20, "Khảm vào Áo và đồ cầm tay", new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(44, 9, Text.KHANG_GAY_CHOANG_20, "Khảm vào găng tay và giày", new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(45, 10, Text.KHANG_HOA_THACH_20, "Khảm vào dây chuyền và ngọc", new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
            new GemTemplate(46, 11, Text.KHANG_GIAM_TOC_30, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(47, 11, Text.KHANG_TRUNG_DOC_30, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(48, 9, Text.KHANG_GAY_MU_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(49, 12, Text.KHANG_DONG_BANG_30, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(50, 9, Text.KHANG_GAY_CHOANG_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(51, 10, Text.KHANG_HOA_THACH_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
            new GemTemplate(52, 11, Text.KHANG_GIAM_TOC_40, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(53, 11, Text.KHANG_TRUNG_DOC_40, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(54, 9, Text.KHANG_GAY_MU_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(55, 12, Text.KHANG_DONG_BANG_40, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(56, 9, Text.KHANG_GAY_CHOANG_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(57, 10, Text.KHANG_HOA_THACH_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
            new GemTemplate(58, 12, Text.TANG_X2_BY_HIT_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(59, 12, Text.TANG_YTLE_ROT_BAO_VAT_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
            new GemTemplate(60, 14, Text.DUC_TUONG_KHAM + " 1", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(61, 14, Text.DUC_TUONG_KHAM + " 2", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(62, 14, Text.DUC_TUONG_KHAM + " 3", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(63, 14, Text.DUC_TUONG_KHAM + " 4", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(64, 14, Text.DUC_TUONG_KHAM + " 5", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(65, 14, Text.DUC_TUONG_KHAM + " 6", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
            new GemTemplate(66, 17, Text.LKD_35, "Tỷ lệ thành công 35%", 35, -1, 1, GemTemplate.TYPE_IMBUE, 0, 0)
        };
    }

    static void loadGemTemplate() {
        if (TeamServer.isServerIndo()) {
            loadGemTemplateIndo();
        } else {
            Map.gemTemplate = new GemTemplate[]{
                new GemTemplate(0, 0, Text.BAO_HIEM_20, Text.BAO_HIEM_20_DCR, 20, 0, 100, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(1, 0, Text.BAO_HIEM_30, Text.BAO_HIEM_30_DCR, 30, 2, 200, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(2, 1, Text.BAO_HIEM_40, Text.BAO_HIEM_40_DCR, 40, 3, 300, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(3, 1, Text.BAO_HIEM_50, Text.BAO_HIEM_50_DCR, 50, 1, 400, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(4, 2, Text.BAO_HIEM_90, Text.BAO_HIEM_90_DCR, 90, 4, 500, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(5, 3, Text.DA_MAY_MAN_1, Text.DA_MAY_MAN_1_DCR, 20, -1, 500, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(6, 4, Text.DA_MAY_MAN_2, Text.DA_MAY_MAN_2_DCR, 40, -1, 600, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(7, 5, Text.DA_MAY_MAN_3, Text.DA_MAY_MAN_3_DCR, 60, -1, 800, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(8, 6, Text.LKD, Text.LKD_2_DCR, 20, -1, 600, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(9, 7, Text.LKD, Text.LKD_3_DCR, 30, -1, 700, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(10, 8, Text.LKD, Text.LKD_4_DCR, 40, -1, 800, GemTemplate.TYPE_IMBUE, 1, 0),
                new GemTemplate(11, 17, Text.LKD, Text.LKD_30_DCR, 30, -1, 1, GemTemplate.TYPE_IMBUE, 0, 1),
                new GemTemplate(12, 10, Text.DA_KHAM_1SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(13, 13, Text.DA_KHAM_1NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(14, 13, Text.DA_KHAM_1TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(15, 10, Text.DA_KHAM_1SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(16, 10, Text.DA_KHAM_2SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
                new GemTemplate(17, 13, Text.DA_KHAM_2NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
                new GemTemplate(18, 13, Text.DA_KHAM_2TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
                new GemTemplate(19, 10, Text.DA_KHAM_2SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 2, 0, 0, 10),
                new GemTemplate(20, 10, Text.DA_KHAM_3SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
                new GemTemplate(21, 13, Text.DA_KHAM_3NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
                new GemTemplate(22, 13, Text.DA_KHAM_3TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
                new GemTemplate(23, 10, Text.DA_KHAM_3SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 3, 0, 0, 10),
                new GemTemplate(24, 10, Text.DA_KHAM_4SM, Text.DA_KHAM_SM_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
                new GemTemplate(25, 13, Text.DA_KHAM_4NN, Text.DA_KHAM_NN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
                new GemTemplate(26, 13, Text.DA_KHAM_4TT, Text.DA_KHAM_TT_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
                new GemTemplate(27, 10, Text.DA_KHAM_4SK, Text.DA_KHAM_SK_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 4, 0, 0, 10),
                new GemTemplate(28, 9, Text.GAY_MU_1, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(29, 12, Text.DONG_BANG_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(30, 11, Text.TRUNG_DOC_1, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(31, 9, Text.GAY_CHOANG_1, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(32, 13, Text.HOA_THACH_1, Text.KHAM_NHAN_DCR, new byte[]{8}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(33, 11, Text.GIAM_TOC_1, Text.KHAM_NON_QUAN_DCR, new byte[]{1, 2}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(34, 11, Text.KHANG_GIAM_TOC_10, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(35, 11, Text.KHANG_TRUNG_DOC_10, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(36, 9, Text.KHANG_GAY_MU_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(37, 12, Text.KHANG_DONG_BANG_10, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(38, 9, Text.KHANG_GAY_CHOANG_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(39, 10, Text.KHANG_HOA_THACH_10, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 10, 0, 0, 10),
                new GemTemplate(40, 11, Text.KHANG_GIAM_TOC_20, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(41, 11, Text.KHANG_TRUNG_DOC_20, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(42, 9, Text.KHANG_GAY_MU_20, "Khảm vào găng tay và giày", new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(43, 12, Text.KHANG_DONG_BANG_20, "Khảm vào Áo và đồ cầm tay", new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(44, 9, Text.KHANG_GAY_CHOANG_20, "Khảm vào găng tay và giày", new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(45, 10, Text.KHANG_HOA_THACH_20, "Khảm vào dây chuyền và ngọc", new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 20, 0, 0, 10),
                new GemTemplate(46, 11, Text.KHANG_GIAM_TOC_30, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(47, 11, Text.KHANG_TRUNG_DOC_30, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(48, 9, Text.KHANG_GAY_MU_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(49, 12, Text.KHANG_DONG_BANG_30, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(50, 9, Text.KHANG_GAY_CHOANG_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(51, 10, Text.KHANG_HOA_THACH_30, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 30, 0, 0, 10),
                new GemTemplate(52, 11, Text.KHANG_GIAM_TOC_40, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(53, 11, Text.KHANG_TRUNG_DOC_40, Text.KHAM_NON_QUAN_DCR, new byte[]{2, 1}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(54, 9, Text.KHANG_GAY_MU_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(55, 12, Text.KHANG_DONG_BANG_40, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(56, 9, Text.KHANG_GAY_CHOANG_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{10, 11}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(57, 10, Text.KHANG_HOA_THACH_40, Text.KHAM_GANG_GIAY_DCR, new byte[]{9, 12}, -1, GemTemplate.TYPE_KHAM, 40, 0, 0, 10),
                new GemTemplate(58, 12, Text.TANG_X2_BY_HIT_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(59, 12, Text.TANG_YTLE_ROT_BAO_VAT_1, Text.KHAM_AO_VK_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_KHAM, 1, 0, 0, 10),
                new GemTemplate(60, 14, Text.DUC_TUONG_KHAM + " 1", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(61, 14, Text.DUC_TUONG_KHAM + " 2", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(62, 14, Text.DUC_TUONG_KHAM + " 3", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(63, 14, Text.DUC_TUONG_KHAM + " 4", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(64, 14, Text.DUC_TUONG_KHAM + " 5", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(65, 14, Text.DUC_TUONG_KHAM + " 6", Text.DUC_TUONG_KHAM_DCR, 1, -1, 100, GemTemplate.TYPE_DUC_LO, 0, 0),
                new GemTemplate(66, 17, Text.LKD_35, "Tỷ lệ thành công 35%", 35, -1, 1, GemTemplate.TYPE_IMBUE, 0, 0),
                new GemTemplate(67, 18, Text.SOI_BONG, Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(68, 28, Text.VAI, Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(69, 28, Text.VAI + " 2", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(70, 28, Text.VAI + " 3", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(71, 28, Text.VAI + " 4", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 0, 5),
                new GemTemplate(72, 28, Text.VAI + " 5", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 0, 15),
                new GemTemplate(73, 28, Text.VAI + " 6", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(74, 19, "Quặng sắt", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(75, 29, "Sắt cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(76, 29, "Sắt cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(77, 29, "Sắt cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(78, 29, "Sắt cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 5),
                new GemTemplate(79, 29, "Sắt cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 15),
                new GemTemplate(80, 29, "Sắt cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(81, 20, "Ngọc thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(82, 30, "Ngọc cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(83, 30, "Ngọc cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(84, 30, "Ngọc cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(85, 30, "Ngọc cấp 4", "Dùng chế tạo trang bịc", new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 5),
                new GemTemplate(86, 30, "Ngọc cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 15),
                new GemTemplate(87, 30, "Ngọc cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(88, 21, "Gỗ thường thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(89, 31, "Gỗ thường cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(90, 31, "Gỗ thường cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(91, 31, "Gỗ thường cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(92, 31, "Gỗ thường cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 5),
                new GemTemplate(93, 31, "Gỗ thường cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 15),
                new GemTemplate(94, 31, "Gỗ thường cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{3, 4, 5, 6, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(95, 22, "Da mềm thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(96, 32, "Da mềm cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(97, 32, "Da mềm cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(98, 32, "Da mềm cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(99, 32, "Da mềm cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 5),
                new GemTemplate(100, 32, "Da mềm cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 15),
                new GemTemplate(101, 32, "Da mềm cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(102, 23, "Sợi tơ", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(103, 33, "Tơ lụa cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(104, 33, "Tơ lụa cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(105, 33, "Tơ lụa cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(106, 33, "Tơ lụa cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 5 * Map.gg1 / Map.gg2),
                new GemTemplate(107, 33, "Tơ lụa cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 15 * Map.gg1 / Map.gg2),
                new GemTemplate(108, 33, "Tơ lụa cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{1, 11}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(109, 24, "Quặng bạc", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(110, 34, "Bạc cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(111, 34, "Bạc cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(112, 34, "Bạc cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(113, 34, "Bạc cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 5 * Map.gg1 / Map.gg2),
                new GemTemplate(114, 34, "Bạc cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 15 * Map.gg1 / Map.gg2),
                new GemTemplate(115, 34, "Bạc cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{0, 3, 4, 6}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(116, 25, "Thuỷ tinh thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(117, 35, "Thuỷ tinh cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(118, 35, "Thuỷ tinh cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(119, 35, "Thuỷ tinh cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(120, 35, "Thuỷ tinh cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 5 * Map.gg1 / Map.gg2),
                new GemTemplate(121, 35, "Thuỷ tinh cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 15 * Map.gg1 / Map.gg2),
                new GemTemplate(122, 35, "Thuỷ tinh cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{8, 9, 12}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(123, 26, "Gỗ sưa thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(124, 36, "Gỗ sưa cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(125, 36, "Gỗ sưa cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(126, 36, "Gỗ sưa cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(127, 36, "Gỗ sưa cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 5 * Map.gg1 / Map.gg2),
                new GemTemplate(128, 36, "Gỗ sưa cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 15 * Map.gg1 / Map.gg2),
                new GemTemplate(129, 36, "Gỗ sưa cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{5, 7}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(130, 27, "Da cứng thô", Text.NGUYEN_LIEU_DCR, 1, -1, 100, GemTemplate.TYPE_CREATE_ITEM, 0, 1),
                new GemTemplate(131, 37, "Da cứng cấp 1", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(132, 37, "Da cứng cấp 2", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(133, 37, "Da cứng cấp 3", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(134, 37, "Da cứng cấp 4", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 5 * Map.gg1 / Map.gg2),
                new GemTemplate(135, 37, "Da cứng cấp 5", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 1, 1, 15 * Map.gg1 / Map.gg2),
                new GemTemplate(136, 37, "Da cứng cấp 6", Text.NGUYEN_LIEU_DCR, new byte[]{0, 2, 10}, -1, GemTemplate.TYPE_CREATE_ITEM, 40, 0, 1, 10),
                new GemTemplate(137, 38, Text.DA_NGU_HOP_THUONG + " lv 1", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 110000),
                new GemTemplate(138, 38, Text.DA_NGU_HOP_THUONG + " lv 2", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 150000),
                new GemTemplate(139, 38, Text.DA_NGU_HOP_THUONG + " lv 3", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 210000),
                new GemTemplate(140, 38, Text.DA_NGU_HOP_THUONG + " lv 4", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 240000),
                new GemTemplate(141, 38, Text.DA_NGU_HOP_THUONG + " lv 5", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 300000),
                new GemTemplate(142, 38, Text.DA_NGU_HOP_THUONG + " lv 6", Text.DA_NGU_HOP_THUONG_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 450000),
                new GemTemplate(
                143, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 1", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 210000
                ),
                new GemTemplate(
                144, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 2", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 300000
                ),
                new GemTemplate(
                145, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 3", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 375000
                ),
                new GemTemplate(
                146, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 4", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 450000
                ),
                new GemTemplate(
                147, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 5", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 600000
                ),
                new GemTemplate(
                148, 15, Text.DA_NGU_HOP_CAO_CAP + " lv 6", Text.DA_NGU_HOP_CAO_CAP_DCR, null, -1, GemTemplate.TYPE_HOP_ITEM_ANIMAL, 40, 1, 0, 750000
                ),
                new GemTemplate(
                149,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 1",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                15 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(
                150,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 2",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                20 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(
                151,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 3",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                25 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(
                152,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 4",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                30 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(
                153,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 5",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                40 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(
                154,
                16,
                Text.DA_NGU_HOP_TINH_KHIET + " lv 6",
                Text.DA_NGU_HOP_TINH_KHIET_DCR,
                null,
                -1,
                GemTemplate.TYPE_HOP_ITEM_ANIMAL,
                40,
                1,
                1,
                50 * Map.gg1 / Map.gg2
                ),
                new GemTemplate(155, 41, "Đá may mắn cấp 4", "Tăng 70% tỷ lệ luyện đồ thành công", 40, -1, 600, GemTemplate.TYPE_IMBUE, 0, 0),
                new GemTemplate(156, 42, "Đá may mắn cấp 5", "Tăng 80% tỷ lệ luyện đồ thành công", 60, -1, 800, GemTemplate.TYPE_IMBUE, 0, 0),
                new GemTemplate(157, 39, "Luyện kim dược cao cấp", "Tỷ lệ thành công tăng 50%", 30, -1, 700, GemTemplate.TYPE_IMBUE, 0, 0),
                new GemTemplate(158, 40, "Luyện kim dược cao cấp", "Tỷ lệ thành công tăng 60%", 40, -1, 800, GemTemplate.TYPE_IMBUE, 0, 0),
                new GemTemplate(159, 43, "Mảnh mai 1", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(160, 43, "Mảnh mai 2", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(161, 43, "Mảnh mai 3", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(162, 43, "Mảnh mai 4", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(163, 44, "Mảnh Berry Pecha 1", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(164, 44, "Mảnh Berry Pecha 2", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(165, 44, "Mảnh Berry Pecha 3", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(166, 44, "Mảnh Berry Pecha 4", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(167, 45, "Mảnh Berry Cheri 1", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(168, 45, "Mảnh Berry Cheri 2", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(169, 45, "Mảnh Berry Cheri 3", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(170, 45, "Mảnh Berry Cheri 4", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(171, 46, "Mảnh Berry Rawst 1", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(172, 46, "Mảnh Berry Rawst 2", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(173, 46, "Mảnh Berry Rawst 3", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(174, 46, "Mảnh Berry Rawst 4", "Mảnh bộ Berry", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(175, 47, "Đá tách nguyên liệu 5x", "Tách nguyên liệu từ trang bị 5x", 1, -1, 15 * Map.gg1 / Map.gg2, 5, 1, 1),
                new GemTemplate(176, 48, "Đá tách nguyên liệu 6x", "Tách nguyên liệu từ trang bị 6x", 1, -1, 20 * Map.gg1 / Map.gg2, 5, 1, 1),
                new GemTemplate(177, 49, "Đá tách nguyên liệu 7x", "Tách nguyên liệu từ trang bị 7x", 1, -1, 25 * Map.gg1 / Map.gg2, 5, 1, 1),
                new GemTemplate(178, 50, "Đá tách nguyên liệu đặc biệt", "Tách nguyên liệu từ trang bị 5x trở lên", 1, -1, 100, 5, 0, 1),
                new GemTemplate(179, 51, "Mảnh Blue Flute 1", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(180, 51, "Mảnh Blue Flute 2", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(181, 51, "Mảnh Blue Flute 3", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(182, 51, "Mảnh Blue Flute 4", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(183, 52, "Mảnh Red Flute 1", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(184, 52, "Mảnh Red Flute 2", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(185, 52, "Mảnh Red Flute 3", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(186, 52, "Mảnh Red Flute 4", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(187, 53, "Mảnh Yellow Flute 1", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(188, 53, "Mảnh Yellow Flute 2", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(189, 53, "Mảnh Yellow Flute 3", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(190, 53, "Mảnh Yellow Flute 4", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(191, 54, "Mảnh White Flute 1", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(192, 54, "Mảnh White Flute 2", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(193, 54, "Mảnh White Flute 3", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(194, 54, "Mảnh White Flute 4", "Mảnh bộ Flute", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(195, 57, "Mảnh huy hiệu Cascade 1", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(196, 57, "Mảnh huy hiệu Cascade 2", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(197, 57, "Mảnh huy hiệu Cascade 3", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(198, 57, "Mảnh huy hiệu Cascade 4", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(199, 58, "Mảnh huy hiệu Rainbow 1", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(200, 58, "Mảnh huy hiệu Rainbow 2", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(201, 58, "Mảnh huy hiệu Rainbow 3", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(202, 58, "Mảnh huy hiệu Rainbow 4", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(203, 59, "Mảnh huy hiệu Boulder 1", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(204, 59, "Mảnh huy hiệu Boulder 2", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(205, 59, "Mảnh huy hiệu Boulder 3", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(206, 59, "Mảnh huy hiệu Boulder 4", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(207, 60, "Mảnh huy hiệu Marsh 1", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(208, 60, "Mảnh huy hiệu Marsh 2", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(209, 60, "Mảnh huy hiệu Marsh 3", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(210, 60, "Mảnh huy hiệu Marsh 4", "Mảnh bộ huy hiệu", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(211, 61, "Mảnh Rayquaza 1", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(212, 61, "Mảnh Rayquaza 2", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(213, 61, "Mảnh Rayquaza 3", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(214, 61, "Mảnh Rayquaza 4", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(215, 62, "Mảnh Suicune 1", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(216, 62, "Mảnh Suicune 2", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(217, 62, "Mảnh Suicune 3", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(218, 62, "Mảnh Suicune 4", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(219, 63, "Mảnh Kyogre 1", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(220, 63, "Mảnh Kyogre 2", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(221, 63, "Mảnh Kyogre 3", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(222, 63, "Mảnh Kyogre 4", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(223, 64, "Mảnh Ho-Oh 1", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(224, 64, "Mảnh Ho-Oh 2", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(225, 64, "Mảnh Ho-Oh 3", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(226, 64, "Mảnh Ho-Oh 4", "Mảnh bộ huyền thoại", 40, -1, 800, GemTemplate.TYPE_TU_BINH, 0, 0),
                new GemTemplate(
                227,
                55,
                "Đá thuộc tính thường ",
                "Tạo hoặc thay đổi thuộc tính mới cho trang bị max chỉ số 0.3%",
                1,
                -1,
                10,
                GemTemplate.TYPE_ADD_NEW_ATTRIBUTE,
                1,
                1
                ),
                new GemTemplate(
                228,
                56,
                "Đá thuộc tính cao cấp",
                "Tạo hoặc thay đổi thuộc tính mới cho trang bị max chỉ số 0.5%",
                1,
                -1,
                20,
                GemTemplate.TYPE_ADD_NEW_ATTRIBUTE,
                1,
                1
                ),
                new GemTemplate(229, 65, "Xương cấp 1", "Khóa đồ thú", 1, -1, 20, GemTemplate.TYPE_LOCK_ANIMAL, 0, 1),
                new GemTemplate(230, 65, "Xương cấp 2", "Khóa đồ thú", 1, -1, 20, GemTemplate.TYPE_LOCK_ANIMAL, 0, 1),
                new GemTemplate(231, 65, "Xương cấp 3", "Khóa đồ thú", 1, -1, 20, GemTemplate.TYPE_LOCK_ANIMAL, 0, 1),
                new GemTemplate(232, 65, "Xương cấp 4", "Khóa đồ thú", 1, -1, 20, GemTemplate.TYPE_LOCK_ANIMAL, 0, 1),
                new GemTemplate(233, 65, "Xương cấp 5", "Khóa đồ thú", 1, -1, 20, GemTemplate.TYPE_LOCK_ANIMAL, 0, 1),
                new GemTemplate(234, 65, "Xương cấp 6", "Khóa đồ thú", 1, -1, 25, GemTemplate.TYPE_LOCK_ANIMAL, 1, 1),
                new GemTemplate(
                235, 66, "Ngọc hoàng hôn cấp 1", "Tăng 5% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 25, GemTemplate.TYPE_LOCK_ITEM, Map.NOT_SELL, 1
                ),
                new GemTemplate(
                236, 66, "Ngọc hoàng hôn cấp 2", "Tăng 10% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 25, GemTemplate.TYPE_LOCK_ITEM, Map.NOT_SELL, 1
                ),
                new GemTemplate(
                237, 66, "Ngọc hoàng hôn cấp 3", "Tăng 15% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 25, GemTemplate.TYPE_LOCK_ITEM, Map.NOT_SELL, 1
                ),
                new GemTemplate(
                238, 66, "Ngọc hoàng hôn cấp 4", "Tăng 20% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 25, GemTemplate.TYPE_LOCK_ITEM, Map.NOT_SELL, 1
                ),
                new GemTemplate(
                239, 66, "Ngọc hoàng hôn cấp 5", "Tăng 25% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 25, GemTemplate.TYPE_LOCK_ITEM, Map.NOT_SELL, 1
                ),
                new GemTemplate(
                240, 66, "Ngọc hoàng hôn cấp 6", "Tăng 30% tỷ lệ khoá thành công vật phẩm lên 25%", 1, -1, 250, GemTemplate.TYPE_LOCK_ITEM, Map.SELL, 1
                ),
                new GemTemplate(241, 67, "Thẻ Rayquaza", "Dùng nâng cấp đội", 1, -1, 100, 0, Map.NOT_SELL, 1),
                new GemTemplate(242, 68, "Thẻ Suicune", "Dùng nâng cấp đội", 1, -1, 100, 0, Map.NOT_SELL, 0),
                new GemTemplate(243, 69, "Thẻ Kyogre", "Dùng nâng cấp đội", 1, -1, 100, 0, Map.NOT_SELL, 0),
                new GemTemplate(244, 70, "Thẻ Ho-Oh", "Dùng nâng cấp đội", 1, -1, 100, 0, Map.NOT_SELL, 0),
                new GemTemplate(245, 71, "Luyện kim dược 40", "Tỷ lệ thành công 40%", 40, -1, 1, GemTemplate.TYPE_IMBUE, Map.NOT_SELL, 0),
                new GemTemplate(246, 72, "Bột trắng", "Nguyên liệu thăng bậc", 1, -1, 100, GemTemplate.TYPE_UNKNOW, Map.NOT_SELL, 0),
                new GemTemplate(247, 73, "Bột xanh", "Nguyên liệu thăng bậc", 1, -1, 100, GemTemplate.TYPE_UNKNOW, Map.NOT_SELL, 0),
                new GemTemplate(248, 74, "Bột đỏ", "Nguyên liệu thăng bậc", 1, -1, 100, GemTemplate.TYPE_UNKNOW, Map.NOT_SELL, 0),
                new GemTemplate(249, 75, "Bột xanh lá", "Nguyên liệu thăng bậc", 1, -1, 100, GemTemplate.TYPE_BOT, Map.NOT_SELL, 0),
                new GemTemplate(250, 76, "Đá thăng bậc", "Nguyên liệu thăng bậc", 1, -1, 10, GemTemplate.TYPE_DA_TIEN_GIAI, Map.SELL, 1),
                new GemTemplate(251, 77, "Giấy", "Nguyên liệu làm pháo hoa", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(252, 78, "Thuốc", "Nguyên liệu làm pháo hoa", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(253, 79, "Mãng cầu", "Nguyên liệu làm mâm ngũ quả", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(254, 80, "Sung", "Nguyên liệu làm mâm ngũ quả", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(255, 81, "Dừa", "Nguyên liệu làm mâm ngũ quả", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(256, 82, "Đu đủ", "Nguyên liệu làm mâm ngũ quả", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(257, 83, "Xoài", "Nguyên liệu làm mâm ngũ quả", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.NOT_SELL, 1),
                new GemTemplate(258, 84, "Nham thạch cấp 1", "1% nâng cấp thành công Ponyta", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, Map.SELL, 1),
                new GemTemplate(259, 85, "Nham thạch cấp 2", "10% nâng cấp thành công Ponyta", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(260, 86, "Ngọc 1 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(261, 87, "Ngọc 2 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(262, 88, "Ngọc 3 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(263, 89, "Ngọc 4 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(264, 90, "Ngọc 5 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(265, 91, "Ngọc 6 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(266, 92, "Ngọc 7 sao", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(267, 93, "Bản đồ núi châu báu", "Vật phẩm sự kiện", 1, -1, 10, GemTemplate.TYPE_UNKNOW1, 0, 1),
                new GemTemplate(268, 94, "Thuốc hóa hình", "Dùng để hóa hình thú cưỡi", 1, -1, 100, GemTemplate.TYPE_UNKNOW, Map.NOT_SELL, 0),
                // DA NGHI HUU: sach ky nang pet gio la potion 111. Moi stack gem 269 da
                // duoc doi sang potion, khong con cho nao phat hay dung gem nay nua.
                // VAN GIU o day vi listGemitem duoc cap phat theo Map.gemTemplate.length va
                // cot tob_gem_new luu du 270 truong - bo di la lech dinh dang dang luu.
                new GemTemplate(Map.SACH_SKILL_PET_GEM, 95, "Sách kỹ năng pet (cũ)", "Vật phẩm đã ngừng dùng", 1, -1, 100, GemTemplate.TYPE_TU_BINH, Map.NOT_SELL, 0),
                // 19/10/2026: Ve mo rong hanh trang (real.MoRongTuiGame). Kieu TU_BINH (4) de MOI client hien nut
                // "Dung" (goi -67 douseGemItem); Map.doTuBinh chuyen id nay sang MoRongTuiGame. NOT_SELL: Persian
                // khong ban (mua o cua hang luong / mo bang xu tai Kangaskhan). Anh 52 = icon tam (giay vang).
                MoRongTuiGame.taoMauVe(),
            };
        }
    }

    static void loadAllLocaltionMap() {
        System.out.println("LOAD ALL LOCATION MAP");
        int[][] lc = new int[][]{{13, 0, 11, 10, 1, 1, 61}, {20, 50, 10, 9, 2, 15, 1}};
        Vector<LocationMap> dsLocation = new Vector();

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(0, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{17, 0, 10, 9, 1, 1, 61}, {17, 52, 10, 7, 2, 15, 1}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(70, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{0, 55, 10, 10, 0, 17, 2}, {0, 55, 10, 10, 0, 22, 22}, {69, 5, 10, 10, 4, 1, 12}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(1, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{12, 0, 10, 10, 0, 23, 50}, {12, 0, 10, 10, 0, 17, 2}, {120, 15, 10, 10, 3, 1, 22}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(2, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{0, 18, 10, 10, 2, 126, 21}, {87, 0, 10, 10, 4, 93, 98}, {87, 90, 10, 10, 5, 71, 1}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(3, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{0, 8, 10, 10, 1, 78, 11}, {85, 90, 10, 10, 3, 91, 3}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(4, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{67, 0, 10, 10, 3, 91, 97}, {61, 70, 10, 10, 6, 40, 2}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(5, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{35, 0, 10, 10, 5, 65, 77}, {39, 39, 10, 10, 7, 2, 91}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(6, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{27, 0, 10, 10, 8, 27, 77}, {0, 82, 10, 10, 6, 45, 44}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(7, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{42, 0, 10, 10, 9, 28, 98}, {23, 70, 10, 10, 7, 32, 2}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(8, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{24, 89, 10, 10, 8, 48, 2}, {29, 0, 10, 10, 201, 39, 90}, {29, 0, 10, 10, 118, 10, 20}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(9, dsLocation);
        dsLocation = new Vector();
        lc = new int[][]{{29, 90, 21, 10, 9, 33, 1}};

        for (int i = 0; i < lc.length; i++) {
            LocationMap l = new LocationMap(lc[i][0], lc[i][1], lc[i][2], lc[i][3], lc[i][4], lc[i][5], lc[i][6]);
            dsLocation.add(l);
        }

        Map.ALL_LOCALTION_MAP.put(201, dsLocation);
    }

    static void loadImageCharWeaponeData() {
        if (Map.weapone_data.size() == 0) {
            FileInputStream fis = null;
            DataInputStream dis = null;
            FileInputStream fisdata = null;
            DataInputStream disdata = null;
            FileInputStream fiswpa = null;
            DataInputStream diswpa = null;

            try {
                for (byte i = 0; i < 5; i++) {
                    Vector<Vector<CharWeaponData>> v = new Vector();

                    for (int j = 0; j < 7; j++) {
                        Vector<CharWeaponData> data = new Vector();
                        String urlData = "map/wpa/" + (i + 3) + j + "/data";
                        String urlWpa = "map/wpa/" + (i + 3) + j + "/wpa_data";
                        fisdata = new FileInputStream(urlData);
                        disdata = new DataInputStream(fisdata);
                        byte[] wdata = new byte[disdata.available()];
                        disdata.read(wdata, 0, wdata.length);
                        fiswpa = new FileInputStream(urlWpa);
                        diswpa = new DataInputStream(fiswpa);
                        byte[] wpa = new byte[diswpa.available()];
                        diswpa.read(wpa, 0, wpa.length);

                        for (byte k = 0; k < (j < 5 ? 7 : 5); k++) {
                            String url = "map/wpa/" + (i + 3) + j + "/" + k + "_h";
                            CharWeaponData cwpdata = new CharWeaponData();
                            fis = new FileInputStream(url);
                            dis = new DataInputStream(fis);
                            cwpdata.headerImg = new byte[dis.available()];
                            dis.read(cwpdata.headerImg, 0, cwpdata.headerImg.length);
                            cwpdata.dataImg = wdata;
                            cwpdata.wpa_xy = wpa;
                            data.add(cwpdata);

                            try {
                                fis.close();
                            } catch (Exception var23) {
                            }

                            try {
                                dis.close();
                            } catch (Exception var22) {
                            }
                        }

                        v.add(data);

                        try {
                            fisdata.close();
                        } catch (Exception var21) {
                        }

                        try {
                            disdata.close();
                        } catch (Exception var20) {
                        }

                        try {
                            fiswpa.close();
                        } catch (Exception var19) {
                        }

                        try {
                            diswpa.close();
                        } catch (Exception var18) {
                        }
                    }

                    Map.weapone_data.add(v);
                }
            } catch (Exception var24) {
                var24.printStackTrace();
            }
        }
    }

    static void loadNpcServerIndo() {
        Map m = new Map();
        Char bot = new Char(null);
        bot.setInfoChar("phat lo", -2, 2, 0, m, 208, 160, -2000, 25, 51, 71);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{208, 160}, {208, 160}, {368, 192}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        String a = "abcdefghijklmn0pqrst09347";
        String name = "";

        for (int i = 0; i < 8; i++) {
            name = name + a.charAt(Map.r.nextInt(a.length()));
        }

        bot = new Char(null);
        bot.setInfoChar(name, -35, 1, 2, new Map(), 208, 160, -3061, 1, 27, 53);
        bot.headStyle = 1;
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{320, 400}, {320, 400}, {320, 400}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -19, 2, 0, m, 1920, 1904, -2028, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -20, 2, 0, m, 608, 48, -2030, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -21, 2, 0, m, 32, 736, -2032, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -38, 2, 0, m, 1088, 1008, -3063, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -39, 2, 0, m, 192, 768, -3064, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -40, 2, 0, m, 480, 96, -3065, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -41, 2, 0, m, 144, 304, -3066, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -42, 2, 0, m, 704, 864, -3067, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -43, 2, 0, m, 880, 848, -3068, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -44, 2, 0, m, 128, 672, -3069, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -45, 2, 0, m, 752, 80, -3070, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -46, 2, 0, m, 336, 80, -3071, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -47, 2, 0, m, 1136, 1968, -3072, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -48, 2, 0, m, 1024, 128, -3073, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("doi tieu", -52, 2, 0, m, 704, 656, -32077, 25, 51, 77);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        // hoa tieu moi cho "Rung gia" (lo #4 loi.xlsx) - toa do TAM, can chinh lai trong game
        bot.setInfoChar("hoa tieu", -100, 2, 0, m, 160, 160, -3080, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        // hoa tieu moi cho "Ham toi" (lo #4 loi.xlsx) - toa do TAM, can chinh lai trong game
        bot.setInfoChar("hoa tieu", -101, 2, 0, m, 160, 160, -3081, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        if (!Char.isFinishAllSuKienSnNgude()) {
            bot = new Char(null);
            bot.setInfoChar("co nang tiec tung", -49, 2, 0, m, 800, 672, -3074, 269, 293, 317);
            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
            bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
            Map.npcVilage.add(bot);
            CharManager.instance.put(bot);
            bot.setInfoModelCharBot(0, 264);
        }

        bot = new Char(null);
        bot.setInfoChar("tong tieu dau", -50, 2, 0, m, 800, 672, -3076, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{400, 176}, {464, 320}, {320, 672}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
    }

    static void loadNpcServer() {
        // Da bo khoi shop dac biet: 728 (Gay Giang Sinh) va 746/747/748
        // (Mat na nga quy / nga khuyen / thi khien).
        int[] idItemShop = {751, 752, 753, 723, 724};
        int[] array = new int[10];
        array[0] = 100000;
        int[] priceluong = {100, 50, 50, 500, 500};
        int[] soLuong = {10, 1, 1, 1, 1};
        int[] time = {0, 0, 0, 10800, 10800};
        for (int i = 0; i < idItemShop.length; ++i) {
            Item newItem = new ItemShop(Map.itemTemplates.get(5).get(idItemShop[i]), false, 0, 0, idItemShop[i]);
            newItem.id = (short) i;
            newItem.owner = i;
            newItem.level = newItem.getTemplate().level;
            newItem.identify = i;
            newItem.clazz = -1;
            newItem.setXuSell(array[i]);
            newItem.setLuongSell(priceluong[i]);
            newItem.setSoluong(soLuong[i]);
            newItem.setTimeExpire(time[i]);
            // Hai khoi duoi day khong con id nao chay vao, giu lai de neu sau
            // nay them lai gay giang sinh / mat na vao idItemShop thi chi so
            // van duoc gan dung nhu cu.
            if (idItemShop[i] == 728) {
                newItem.lockAtb[0] = 10;
            }
            if (idItemShop[i] >= 745 && idItemShop[i] <= 748) {
                int option, valueOption;
                switch (idItemShop[i]) {
                    case 745: {
                        option = 0;
                        valueOption = 5;
                        break;
                    }
                    case 746: {
                        option = 1;
                        valueOption = 5;
                        break;
                    }
                    case 747: {
                        option = 2;
                        valueOption = 10;
                        break;
                    }
                    case 748: {
                        option = 3;
                        valueOption = 30;
                        break;
                    }
                    default: {
                        option = 0;
                        valueOption = 5;
                        break;
                    }
                }
                newItem.createOptionMatNa(option, valueOption);
            }
            if (time[i] > 0) {
                newItem.minuteExist = time[i];
                newItem.timeLoan = System.currentTimeMillis();
            }
            Map.all_item_shop_special.add(newItem);
        }
        // "Shop Trung Thu" (moi 19/09/2026, yeu cau nguoi dung - RIENG voi Shop Dac Biet o
        // tren, khong bat buoc Vip, xem Map.java TYPE_SHOP_NEW_TRUNG_THU va doBuyItemShopNew).
        // Danh sach nay nap MOI LAN khoi dong bat ke su kien dang bat/tat - client chi thay
        // duoc menu "Shop Trung Thu" khi Char.isSuKienTrungThul2016() (xem Map.java dong
        // ~8174), nen khong can loc rieng o day.
        {
            final Item matNaOngDia = new ItemShop(Map.itemTemplates.get(5).get(872), false, 0, 0, 872);
            matNaOngDia.id = 0;
            matNaOngDia.owner = 0;
            matNaOngDia.level = matNaOngDia.getTemplate().level;
            matNaOngDia.identify = 0;
            matNaOngDia.clazz = -1;
            matNaOngDia.setXuSell(0);
            matNaOngDia.setLuongSell(800);
            matNaOngDia.setSoluong(1);
            matNaOngDia.setTimeExpire(10080);
            Map.all_item_shop_trungthu.add(matNaOngDia);
        }
        Map m = new Map();
        Char bot = new Char(null);
        // NPC "admin" chi admin thay (xem CharCapNhat.updateNearChar / Map.laNpcAnCuaAdmin).
        // Van tao va add vao Map.npcVilage vi startSendInfoAdv tham chieu NPC theo chi so
        // (Map.npcVilage.get(0), get(1), get(3), get(6), get(8), get(9)) - xoa han se lech het.
        bot.setInfoChar(Map.NAME_NPC_AN, -2, 2, 0, m, 208, 160, -2000, 25, 51,  -1);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{208, 160}, {208, 160}, {368, 192}};
        bot.setInfoModelCharBot(0, 741);


        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        // NPC Trung Thu "Hang Nga" (moi 19/09/2026, yeu cau nguoi dung): truoc la 1 sprite
        // tinh gui rieng qua addNPC.java (createMsgNpc, chi ve 1 anh don theo idImg), doi
        // sang bot DANG KY that su de dung setInfoModelCharBot(0, 574) - CHINH co che
        // "model clothes" nhieu manh (chan/than/dau/non) ma nguoi choi dung khi mac Ao Hang
        // Nga that, xem Char.doInitCharCopy() dong ~5315 la vi du da chay dung truoc do.
        // Cung npcType -49 nhu cu de RequestNPCInfoHandler.java (case -49) tiep tuc xu ly
        // dung menu Trung Thu khi bam vao. Chi tao luc khoi dong NEU su kien dang bat -
        // giong cac gian hang tu bat/tat khac, can KHOI DONG LAI server khi bat/tat su kien
        // Trung Thu thi NPC nay moi xuat hien/bien mat dung (khong tu dong theo doi runtime).
        if (Char.isSuKienTrungThul2016()) {
            bot = new Char(null);
            // Ten "Hang Nga" KHONG DAU (thay vi "Hằng Nga" co dau) - font rieng cua nhan
            // (label noi tren dau NPC ngoai ban do) khong co ky tu "ằ" (a + dau moc + dau
            // huyen), hien vo ra "OONG OGA" (nguoi dung xac nhan qua anh chup 19/09/2026).
            // Cac cho khac (ten vat pham "Áo Hằng Nga", hop thoai...) dung font khac, van
            // giu nguyen dau day du, KHONG doi.
            bot.setInfoChar("hang nga", -49, 2, 0, m, 880, 672, -3090, 269, 293, 317);
            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
            bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
            bot.setInfoModelCharBot(0, 574);
            Map.npcVilage.add(bot);
            CharManager.instance.put(bot);
        }
//        bot = new Char(null);
//        bot.setInfoChar("chucmungnammoi", -39, 1, 2, new Map(), 800, 672, -3074, 269, 293, 317);
//        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
//        bot.posNPCInVilage = new short[][]{{320, 400}, {320, 400}, {320, 400}};
//        Map.npcVilage.add(bot);
//        CharManager.instance.put(bot);
        final String a = "abcdefghijklmn0pqrst09347";
        StringBuilder name = new StringBuilder();
        for (int j = 0; j < 8; ++j) {
            name.append(a.charAt(Map.r.nextInt(a.length())));
        }
//        bot = new Char(null);
//        bot.setInfoChar("ong gia noel", -35, 1, 2, new Map(), 208, 160, -3061, 25, 51, 71);
//        bot.headStyle = 1;
//        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
//        bot.posNPCInVilage = new short[][]{{320, 400}, {320, 400}, {320, 400}};
//        Map.npcVilage.add(bot);
//        CharManager.instance.put(bot);

        bot = new Char(null);
        bot.setInfoChar("tho ren than bi", -8, 2, 0, m, 400, 400, -2002, 277, 301, 325);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{464, 224}, {400, 400}, {672, 656}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("tong quan", -10, 2, 0, m, 400, 480, -2026, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{416, 416}, {400, 480}, {736, 576}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        // NPC doi thuong: dung diem tich tu van tieu/cuop tieu/danh quai de doi vat pham.
        // Toa do dat lech ben canh "tong quan" ~40px moi huong - CHUA kiem chung truc quan
        // trong game, can vao thu 3 lang xac nhan khong de chong len NPC khac roi chinh
        // lai x,y neu can (xem ghi chu build.bat / thong bao cua Claude khi giao viec nay).
        bot.setInfoChar("doi thuong", -90, 2, 0, m, 440, 480, -2090, 277, 301, 325);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{456, 416}, {440, 480}, {776, 576}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("thohopthanhsocap", -11, 2, 0, m, 112, 448, -2010, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{112, 480}, {112, 448}, {192, 528}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("thohopthanhcaocap", -12, 2, 0, m, 112, 544, -2012, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{112, 640}, {112, 544}, {224, 656}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("thay ngu hanh", -15, 2, 0, m, 176, 608, -2016, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{432, 624}, {176, 608}, {368, 656}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("dau truong", -31, 2, 0, m, 640, 816, -3056, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{800, 672}, {496, 816}, {496, 704}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -19, 2, 0, m, 1920, 1904, -2028, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -20, 2, 0, m, 608, 48, -2030, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -21, 2, 0, m, 32, 736, -2032, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -38, 2, 0, m, 1088, 1008, -3063, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -39, 2, 0, m, 192, 768, -3064, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -40, 2, 0, m, 480, 96, -3065, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -41, 2, 0, m, 144, 304, -3066, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -42, 2, 0, m, 704, 864, -3067, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -43, 2, 0, m, 880, 848, -3068, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -44, 2, 0, m, 128, 672, -3069, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -45, 2, 0, m, 752, 80, -3070, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -46, 2, 0, m, 336, 80, -3071, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -47, 2, 0, m, 1136, 1968, -3072, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("hoa tieu", -48, 2, 0, m, 1024, 128, -3073, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        bot.setInfoChar("doi tieu", -52, 2, 0, m, 704, 656, -32077, 25, 51, 77);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        // hoa tieu moi cho "Rung gia" (lo #4 loi.xlsx) - toa do TAM, can chinh lai trong game
        bot.setInfoChar("hoa tieu", -100, 2, 0, m, 160, 160, -3080, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);
        bot = new Char(null);
        // hoa tieu moi cho "Ham toi" (lo #4 loi.xlsx) - toa do TAM, can chinh lai trong game
        bot.setInfoChar("hoa tieu", -101, 2, 0, m, 160, 160, -3081, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.hienThiMoiRegion = true;
        Map.npcHoatieu.add(bot);
        CharManager.instance.put(bot);

        bot = new Char(null);
        bot.setInfoChar("tong tieu dau", -50, 2, 0, m, 800, 672, -3076, 269, 293, 317);
        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
        bot.posNPCInVilage = new short[][]{{400, 176}, {464, 320}, {320, 672}};
        Map.npcVilage.add(bot);
        CharManager.instance.put(bot);

//        if (!Char.isFinishAllSuKienHaloween2016()) {
//            bot = new Char(null);
//            bot.setInfoChar("Phu Thuy De Thuong", -49, 2, 0, m, 800, 672, -3074, 269, 293, 578);
//            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
//            Map.idNPC49 = bot.id;
//            bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
//            Map.npcVilage.add(bot);
//            CharManager.instance.put(bot);
//            bot.setInfoModelCharBot(0, 671);
//        }



        
        
        // todo thả npc wordcup ra map ở đây
        if (Char.isSuKienWordcup2017()) {
            bot = new Char(null);
            bot.setInfoChar("trong tai", -49, 2, 0, m, 800, 672, -3074, 266, 293, 317);
            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
            Map.idNPC49 = bot.id;
            bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
            Map.npcVilage.add(bot);
            CharManager.instance.put(bot);
            bot.setInfoModelCharBot(0, 265);
        }
        
        if (Char.isSuKienMiniChucNu()) {
            bot = new Char(null);
            bot.setInfoChar("Nguu Lang", -49, 2, 0, m, 800, 672, -3074, 277, 301, 325);
            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
            Map.idNPC49 = bot.id;
            bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
            Map.npcVilage.add(bot);
            CharManager.instance.put(bot);
            //bot.setInfoModelCharBot(0, 265);
        }      

        

//        bot = new Char(null);
//        bot.setInfoChar("ong gia noel hoho", -35, 1, 2, m,  800, 672, -3061, 25, 51, 71);
//        bot.headStyle = 1;
//        bot.id = RealController.intance.idGen.getID(0, "Tao bot");
//        bot.posNPCInVilage = new short[][]{{880, 672}, {560, 816}, {560, 704}};
//        Map.npcVilage.add(bot);
//        CharManager.instance.put(bot);

        // Pokemon: gan loai cho tung bot NPC trong lang theo chi so (Chansey,
        // Kecleon, Xatu...) de goi 5 cua bot kem -90. Tat co thi khong lam gi.
        TienHoaPokemon.ganLoaiBot(Map.npcVilage);
    }

    static void loadBoss(int type) {
        if (!TeamServer.isServerIndo() && !TeamServer.isServerLienDau()) {
            Map.boss.size();
            if (type == 0) {
                Boss bo = new BossDusknoir(null, (MonsterTemplate) Map.monsterTemplates.get(94), 432, 112, 0);
                bo.isDead = true;
                bo.id = RealController.intance.idGen.getID(1, "new monster");
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                bo.he = bo.heKhiSinh(he[Map.r.nextInt(5)]);
                bo.level = bo.getMonsterTemplate().level;
                Map.boss.add(bo);
                if (Map.event.timeBossAppear[1] > 0L) {
                    bo.bornTime = Map.event.timeBossAppear[1];
                }
            } else if (type == 1) {
                Boss bo = new BossMatQuy(null, (MonsterTemplate) Map.monsterTemplates.get(93), 432, 112, 0);
                bo.isDead = true;
                bo.id = RealController.intance.idGen.getID(1, "new monster");
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                bo.he = bo.heKhiSinh(he[Map.r.nextInt(5)]);
                bo.level = bo.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[0] > 0L) {
                    bo.bornTime = Map.event.timeBossAppear[0];
                }

                Map.boss.add(bo);
                bo = new BossDotKich(null, (MonsterTemplate) Map.monsterTemplates.get(82), 432, 112, 0);
                bo.isDead = true;
                bo.id = RealController.intance.idGen.getID(1, "new monster");
                bo.he = bo.heKhiSinh(he[Map.r.nextInt(5)]);
                bo.level = bo.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[3] > 0L) {
                    bo.bornTime = Map.event.timeBossAppear[3];
                }

                Map.boss.add(bo);
                Boss var7 = new BossDotKich(null, (MonsterTemplate) Map.monsterTemplates.get(39), 432, 112, 0);
                var7.isDead = true;
                var7.id = RealController.intance.idGen.getID(1, "new monster");
                var7.he = var7.heKhiSinh(he[Map.r.nextInt(5)]);
                var7.level = var7.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[4] > 0L) {
                    var7.bornTime = Map.event.timeBossAppear[4];
                }

                Map.boss.add(var7);
                Boss var8 = new BossSerperior(null, (MonsterTemplate) Map.monsterTemplates.get(91), 432, 112, 0);
                var8.isDead = true;
                var8.id = RealController.intance.idGen.getID(1, "new monster");
                var8.he = var8.heKhiSinh(he[Map.r.nextInt(5)]);
                var8.level = var8.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[2] > 0L) {
                    var8.bornTime = Map.event.timeBossAppear[2];
                }

                Map.boss.add(var8);

                try {
                    Boss var9 = new BossServine(null, (MonsterTemplate) Map.monsterTemplates.get(92), 432, 112, 0);
                    var9.isDead = true;
                    var9.id = RealController.intance.idGen.getID(1, "new monster");
                    var9.he = var9.heKhiSinh(he[Map.r.nextInt(5)]);
                    var9.level = var9.getMonsterTemplate().level;
                    if (Map.event.timeBossAppear[2] > 0L) {
                        var9.bornTime = Map.event.timeBossAppear[2];
                    }

                    Map.boss.add(var9);
                } catch (Exception var4) {
                }

                Boss var10 = new BossDusknoir(null, (MonsterTemplate) Map.monsterTemplates.get(113), 432, 112, 0);
                var10.isDead = true;
                var10.id = RealController.intance.idGen.getID(1, "new monster");
                var10.he = var10.heKhiSinh(he[Map.r.nextInt(5)]);
                var10.level = var10.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[5] > 0L) {
                    var10.bornTime = Map.event.timeBossAppear[5];
                }

                var10.bornTime = 1000L;
                Map.boss.add(var10);
                Boss var11 = new BossDusknoir(null, (MonsterTemplate) Map.monsterTemplates.get(115), 432, 112, 0);
                var11.isDead = true;
                var11.id = RealController.intance.idGen.getID(1, "new monster");
                var11.he = var11.heKhiSinh(he[Map.r.nextInt(5)]);
                var11.level = var11.getMonsterTemplate().level;
                if (Map.event.timeBossAppear[6] > 0L) {
                    var11.bornTime = Map.event.timeBossAppear[6];
                }

                Map.boss.add(var11);
            } else if (type == 2) {
                // Trung Thu 2026: 1 Boss rieng cho MOI quoc gia (inCountry 0/1/2), cung tren
                // map Truong Giang (id 9) - xem real/BossTrungThu.java. Mau "Su Tu" (id 48)
                // la do hoa TAM DUNG theo yeu cau nguoi dung, khong phai boss goc.
                for (byte country = 0; country <= 2; country++) {
                    final Boss boTrungThu = new BossTrungThu(null, (MonsterTemplate) Map.monsterTemplates.get(48), 432, 112, country);
                    boTrungThu.isDead = true;
                    boTrungThu.id = RealController.intance.idGen.getID(1, "new monster");
                    boTrungThu.he = boTrungThu.heKhiSinh(new byte[]{0, 1, 2, 3, 4}[Map.r.nextInt(5)]);
                    boTrungThu.level = boTrungThu.getMonsterTemplate().level;
                    Map.boss.add(boTrungThu);
                }
            }
        }
    }

    static void loadTileMap(Map map, InputStream iss, DataInputStream is, int loadmap) {
        try {
            label69:
            while (true) {
                int x = is.read();
                if (x != 255) {
                    int y = is.read();
                    int ww = is.read();
                    int hh = is.read();
                    int yy = y;

                    while (true) {
                        if (yy >= y + hh) {
                            continue label69;
                        }

                        for (int xx = x; xx < x + ww; xx++) {
                            short var28 = (short) is.read();
                        }

                        yy++;
                    }
                }

                while (true) {
                    x = is.read();
                    if (x == 255) {
                        while (true) {
                            x = is.read();
                            if (x == 254) {
                                x = 255;
                            }

                            if (x == 255) {
                                boolean isOfflineMap = is.read() == 1;
                                int nLocation = is.read();

                                for (int i = 0; i < nLocation; i++) {
                                    int xx = is.read();
                                    int y = is.read();
                                    map.type[y * map.w + xx] = 2000000000 + i;
                                    CMLocation cmLocation = null;
                                    int[] aa1 = new int[]{is.read(), is.read()};
                                    short value = 0;

                                    for (int kk = 1; kk >= 0; kk--) {
                                        value = (short) (value << 8);
                                        value = (short) (value | 0xFF & aa1[kk]);
                                    }

                                    int xxx = is.read();
                                    int yy = is.read();
                                    cmLocation = new CMLocation(value, xx, y, xxx, yy);
                                    if (value > 10) {
                                        ((Map) RealController.mapList.get(-1)).loadTileMap(null, null, value);
                                    }

                                    map.mapchangeLocations.addElement(cmLocation);
                                }
                                break label69;
                            }

                            int y = is.read();
                            int var21 = is.read();
                        }
                    }

                    int y = is.read();
                    short i = (short) is.read();
                }
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }
    }

    static void loadMap(Map map, String filename, int magic_physic) {
        short idMonster = 0;

        try {
            FileInputStream fis = new FileInputStream(filename);
            DataInputStream dis = new DataInputStream(fis);
            int w = dis.readUnsignedByte();
            int h = dis.readUnsignedByte();
            map.mapWidth = w * 16;
            map.mapHeight = h * 16;
            Monster m0 = null;

            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int value = dis.readUnsignedByte();
                    if (value > 0 && value != 255) {
                        if (map.mapId == 17 && value > 81) {
                            value += 3;
                        }
 
                        
                        // Normal monster loading logic
                        for (int k = 0; k < Map.MAX_COUNTRY; k++) {
                            if (map.nRegion == 0) {
                                m0 = new Monster(map, (MonsterTemplate) Map.monsterTemplates.get(value), j * 16, i * 16, (byte) k);
                                m0.level = m0.getMonsterTemplate().level;
                                m0.id = idMonster;
                                byte[] he = new byte[]{0, 1, 2, 3, 4};
                                m0.he = m0.heKhiSinh(he[Map.r.nextInt(5)]);
                                byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                                m0.typeAttack = t[Map.r.nextInt(10)];
                                m0.inCountry = (byte) k;
                                m0.magic_physic = (byte) magic_physic;
                                ((Hashtable) map.monsters.get(k)).put(m0.id, m0);
                            } else {
                                for (int l = 0; l < map.nRegion; l++) {
                                    m0 = new Monster(map, (MonsterTemplate) Map.monsterTemplates.get(value), j * 16, i * 16, (byte) k);
                                    m0.idregion = (byte) l;
                                    m0.inCountry = (byte) k;
                                    m0.level = m0.getMonsterTemplate().level;
                                    m0.id = idMonster;
                                    byte[] he = new byte[]{0, 1, 2, 3, 4};
                                    m0.he = m0.heKhiSinh(he[Map.r.nextInt(5)]);
                                    byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                                    m0.typeAttack = t[Map.r.nextInt(10)];
                                    m0.magic_physic = (byte) magic_physic;
                                    m0.region = l;
                                    ((Region) map.ALL_REGION.get((short) l)).addMonster(m0.id, m0, k);
                                }
                            }
                        }

                        idMonster++;
                    }
                }
            }
            

            dis.close();
            fis.close();
            DataInputStream var22 = null;
            FileInputStream var19 = null;
        } catch (FileNotFoundException var16) {
            System.out.println("File not found: " + filename);
            var16.printStackTrace();
        } catch (IOException var17) {
            System.out.println("Load map error: " + filename);
            var17.printStackTrace();
        }

        if (map.mapIDLoadMap == 27) {
            short id = 0;

            for (int kx = 0; kx < Map.MAX_COUNTRY; kx++) 
            {
                int xs = 74;
                int ys = 50;
                BossMeCung bo = new BossMeCung(map, (MonsterTemplate) MapVanTienTran.monsterTemplates.get(116), xs * 16, ys * 16, 0);
                bo.id = id++;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                bo.he = bo.heKhiSinh(he[Map.r.nextInt(5)]);
                bo.level = 1;
                ((Hashtable) map.monsters.get(kx)).put(bo.id, bo);
            }
        }

        if (map.mapIDLoadMap == 201) {
            for (int kx = 0; kx < Map.MAX_COUNTRY; kx++) {
                Monster m0 = new Monster(map, (MonsterTemplate) Map.monsterTemplates.get(83), 640, 1206, (byte) kx);
                m0.level = m0.getMonsterTemplate().level;
                m0.id = idMonster;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                m0.he = m0.heKhiSinh(he[Map.r.nextInt(5)]);
                byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                m0.typeAttack = t[Map.r.nextInt(10)];
                m0.magic_physic = (byte) magic_physic;
                m0.isDead = true;
                m0.isBoss = true;
                MapTown.congthanh.add(m0);
            }

            idMonster++;
        }
        
        if (map.mapIDLoadMap == 28) 
        {
            int[][] customMonsters = 
            {
                {136, 64, 7},
                {136, 68, 11},
                {136, 64, 15},
                {136, 71, 18},
                {136, 64, 22},
                {136, 72, 26},
                {136, 64, 28},
                {136, 63, 35}
            };
            for (int[] data : customMonsters) 
            {
                int templateId = data[0];
                int param1     = data[1] * 16;
                int param2     = data[2] * 16;
                for (int country = 0; country < Map.MAX_COUNTRY; country++) 
                {
                    if (map.nRegion == 0) 
                    {
                        Monster m0 = new Monster(map,(MonsterTemplate) Map.monsterTemplates.get(templateId), param1, param2, country);
                        m0.level        = m0.getMonsterTemplate().level;
                        m0.id           = idMonster;
                        byte[] heads     = {0, 1, 2, 3, 4};
                        m0.he           = m0.heKhiSinh(heads[Map.r.nextInt(heads.length)]);
                        byte[] attacks   = {0, 1, 0, 1, 0, 0, 1, 0};
                        m0.typeAttack   = attacks[Map.r.nextInt(attacks.length)];
                        m0.inCountry    = (byte) country;
                        m0.magic_physic = (byte) magic_physic;
                        ((Hashtable) map.monsters.get(country)).put(m0.id, m0);
                    } 
                    else 
                    {
                        for (int regionIndex = 0; regionIndex < map.nRegion; regionIndex++) 
                        {
                            Monster m0 = new Monster(map, (MonsterTemplate) Map.monsterTemplates.get(templateId), param1, param2, country);
                            m0.level        = m0.getMonsterTemplate().level;
                            m0.id           = idMonster;
                            m0.idregion     = (byte) regionIndex;
                            m0.inCountry    = (byte) country;
                            byte[] heads     = {0, 1, 2, 3, 4};
                            m0.he           = m0.heKhiSinh(heads[Map.r.nextInt(heads.length)]);
                            byte[] attacks   = {0, 1, 0, 1, 0, 0, 1, 0};
                            m0.typeAttack   = attacks[Map.r.nextInt(attacks.length)];
                            m0.region       = regionIndex;
                            ((Region) map.ALL_REGION.get((short) regionIndex)).addMonster(m0.id, m0, country);
                        }
                    }
                    idMonster++;
                }
            }
        }

        if (map.mapIDLoadMap != 0 && map.mapIDLoadMap != 70) 
        {
        }
    }

    static void loadGhost() {
        Map.loadBossSonTinhThuyTinh();

        try {
            if (!Map.loadGhost) {
                return;
            }

            int[] mapct = new int[]{10, 11, 13, 14, 15, 18, 19, 20, 21, 22};
            int[][] xxx = new int[][]{{98}, {77}, {30}, {23}, {55}, {38}, {43}, {23}, {9}, {30}};
            int[][] yyy = new int[][]{{16}, {13}, {30}, {57}, {40}, {26}, {16}, {24}, {22}, {29}};
            int[] lv = new int[]{32, 36, 39, 42, 45, 54, 58, 62, 66, 70};

            for (int i = 0; i < mapct.length; i++) {
                int rd = 0;
                int c = 0;
                int mapid = rd == 0 ? mapct[i] : mapct[i] * 20 + 300 + rd;
                System.out.println("MAP GHOST " + mapid + " >> " + mapct[i]);
                Map m = (Map) RealController.mapList.get(mapid);
                MonsterTemplate mons = null;
                Enumeration<Integer> en = Map.monsterTemplates.keys();

                while (en.hasMoreElements()) {
                    MonsterTemplate mt = (MonsterTemplate) Map.monsterTemplates.get(en.nextElement());
                    if (mt.level == lv[i]) {
                        mons = mt;
                        break;
                    }
                }

                for (int j = 0; j < xxx[i].length; j++) {
                    for (int k = 0; k < Map.MAX_COUNTRY; k++) {
                        CuongThi ct = new CuongThi(m, (MonsterTemplate) Map.monsterTemplates.get(Map.idGhost), xxx[i][j] * 16, yyy[i][j] * 16, (byte) k);
                        ct.setInfo(mons.level, mons.maxhp * 10, mons.rcvXp * 2);
                        ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                        ct.attack = mons.attack + 800;
                        ct.defend_physic = mons.defend + 500;
                        ct.defend_magic = mons.defend + 500;
                        ct.isDead = true;
                        ct.bornTime = System.currentTimeMillis() + 21600000L;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        ct.he = ct.heKhiSinh(he[Map.r.nextInt(5)]);
                        m.addMonster(ct, k, 0);
                    }
                }
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        }
    }

}
