/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.NewClan;

public class Skill_Clan {
    public static String[] SKILL_NAME_CLAN = new String[]{"Thăng cấp hiệu quả", "Tấn công hiệu quả", "Thuật cường thân", "Bộc phát tiềm năng"};
    public static String[] SKILL_DECRIPTION_CLAN = new String[]{
        "Tăng %xp nhận được khi đánh quái ", "Tăng % chí mạng khi đánh quái ", "Tăng HP cho nhân vật ", "Bộc phát tiềm năng "
    };
    public static String[] SKILL_DECRIPTION_CLAN_MEMBER = new String[]{
        "Tăng %xu nhặt được khi đánh quái ", "Tăng điểm phòng thủ vật khi đánh quái ", "Tăng điểm phòng thủ ma khi đánh quái ", "Tăng % khi sử dụng HP "
    };
    public static String[] SKILL_NAME_CLAN_MEMBER = new String[]{
        "Thần tài hỗ trợ", "Phòng thủ vật lý hiệu quả", "Phòng thủ ma pháp hiệu quả", "Trị liệu thần thánh"
    };
    public static byte[][] ID_IMAGE_SKILL_CLAN = new byte[][]{{0, 1, 2, 3, 0, 0, 0, 0, 0, 0, 0}, {4, 5, 6, 7, 4, 4, 4, 4, 4, 4, 4}};
    public static short[][] SKILL_CLAN = new short[][]{{2, 4, 6, 8}, {1, 2, 3, 4}, {1000, 2000, 3000, 4000}, {1, 2, 3, 4}};
    public static short[][] SKILL_CLAN_MEMBER = new short[][]{{5, 7, 10, 15}, {5, 10, 15, 20}, {5, 10, 15, 20}, {3, 5, 7, 10}};
    public static byte[][] LEVEL_SKILL_CLAN = new byte[][]{{10, 15, 30, 50}, {15, 20, 35, 45}, {20, 25, 35, 40}, {25, 30, 40, 45}};
    public static byte[][] LEVEL_SKILL_CLAN_MEMBER = new byte[][]{{15, 20, 25, 45}, {25, 30, 35, 45}, {30, 35, 40, 50}, {10, 15, 20, 40}};
    public static short[][] TIME_SKILL_CLAN = new short[][]{
        {330, 360, 390, 420, 450, 480, 510, 540, 570, 600},
        {330, 360, 390, 420, 450, 480, 510, 540, 570, 600},
        {330, 360, 390, 420, 450, 480, 510, 540, 570, 600},
        {330, 360, 390, 420, 450, 480, 510, 540, 570, 600}
    };
    public static short[][] TIME_SKILL_CLAN_MEMBER = new short[][]{
        {120, 120, 120, 120, 120, 120, 120, 120, 120, 120},
        {120, 120, 120, 120, 120, 120, 120, 120, 120, 120},
        {120, 120, 120, 120, 120, 120, 120, 120, 120, 120},
        {120, 120, 120, 120, 120, 120, 120, 120, 120, 120}
    };
    public static short[][] DEVOTE_BUY_SKILL_CLAN = new short[][]{
        {1000, 3000, 5000, 7000}, {1000, 3000, 5000, 7000}, {1000, 3000, 5000, 7000}, {1000, 3000, 5000, 7000}
    };
    public static short[][] DEVOTE_BUY_SKILL_CLAN_MEMBER = new short[][]{{200, 300, 400, 500}, {200, 300, 400, 500}, {200, 300, 400, 500}, {200, 300, 400, 500}};

    public static int getEffectSkill(byte idSkill, byte lvSkill, byte type) {
        return type == 0 ? SKILL_CLAN[idSkill][lvSkill] : SKILL_CLAN_MEMBER[idSkill][lvSkill];
    }

    public static String[] getListSkillClan(int type, int idSkill) {
        String[] var10000 = new String[]{""};
        String[] st;
        if (type == 0) {
            st = new String[SKILL_CLAN[idSkill].length];

            for (int i = 0; i < st.length; i++) {
                st[i] = SKILL_NAME_CLAN[idSkill]
                    + " lv"
                    + LEVEL_SKILL_CLAN[idSkill][i]
                    + " ch:"
                    + DEVOTE_BUY_SKILL_CLAN[idSkill][i]
                    + " tg:"
                    + TIME_SKILL_CLAN[idSkill][i]
                    + "p";
            }
        } else {
            st = new String[SKILL_CLAN[idSkill].length];

            for (int i = 0; i < st.length; i++) {
                st[i] = SKILL_NAME_CLAN_MEMBER[idSkill]
                    + " lv"
                    + LEVEL_SKILL_CLAN_MEMBER[idSkill][i]
                    + " ch:"
                    + DEVOTE_BUY_SKILL_CLAN_MEMBER[idSkill][i]
                    + " tg:"
                    + TIME_SKILL_CLAN_MEMBER[idSkill][i]
                    + "p";
            }
        }

        return st;
    }

    public static boolean canLearnSkill(Char p, int idSkill, int levelSkill, int type) {
        if (type == 0) {
            NewClan clan = NewClan.getClan(p.idClan);
            if (clan != null && clan.pDevote >= DEVOTE_BUY_SKILL_CLAN[idSkill][levelSkill]) {
                clan.setSkillClan(idSkill, TIME_SKILL_CLAN[idSkill][levelSkill], DEVOTE_BUY_SKILL_CLAN[idSkill][levelSkill], levelSkill);
                return true;
            }
        } else if (p.pointCongHienClan >= DEVOTE_BUY_SKILL_CLAN_MEMBER[idSkill][levelSkill]) {
            p.setSkillClan(idSkill + 10, TIME_SKILL_CLAN_MEMBER[idSkill][levelSkill], DEVOTE_BUY_SKILL_CLAN_MEMBER[idSkill][levelSkill], (byte)levelSkill);
            return true;
        }

        return false;
    }
}
