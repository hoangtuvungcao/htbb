package game.model.skill;

import game.model.MainChar;
import game.model.skill.complete.Skill_Boss_Xmas;
import game.model.skill.complete.Skill_Bua_Type1;
import game.model.skill.complete.Skill_Bua_Type2;
import game.model.skill.complete.Skill_Bua_Type3;
import game.model.skill.complete.Skill_Bua_Type4;
import game.model.skill.complete.Skill_Bua_TypeAEO;
import game.model.skill.complete.Skill_Cung_Type0;
import game.model.skill.complete.Skill_Cung_Type1;
import game.model.skill.complete.Skill_Cung_Type2;
import game.model.skill.complete.Skill_Cung_Type3;
import game.model.skill.complete.Skill_Cung_Type4;
import game.model.skill.complete.Skill_Cung_TypeAEO;
import game.model.skill.complete.Skill_Dao_Type1;
import game.model.skill.complete.Skill_Dao_Type2;
import game.model.skill.complete.Skill_Dao_Type3;
import game.model.skill.complete.Skill_Dao_Type4;
import game.model.skill.complete.Skill_Dao_TypeAEO;
import game.model.skill.complete.Skill_Dua_Type1;
import game.model.skill.complete.Skill_Dua_Type2;
import game.model.skill.complete.Skill_Dua_Type3;
import game.model.skill.complete.Skill_Dua_Type4;
import game.model.skill.complete.Skill_Dua_TypeAEO;
import game.model.skill.complete.Skill_Kiem_Type0;
import game.model.skill.complete.Skill_Kiem_Type1;
import game.model.skill.complete.Skill_Kiem_Type2;
import game.model.skill.complete.Skill_Kiem_Type3;
import game.model.skill.complete.Skill_Kiem_Type4;
import game.model.skill.complete.Skill_Kiem_TypeAEO;
import java.util.Hashtable;

public class SkillManager extends Hashtable {
    public static byte[][] LEVEL_ADD_SKILL = null;
    public static byte[][] SKILL_AEO = null;
    public static int[][][] SKILL_COOLDOWN = null;
    public static short[][][] SKILL_DAM_PERCENT = null;
    public static short[][][] SKILL_MP = null;
    public static short[][] SKILL_RANGE = null;
    public static short[][] TIME_LIFE_BUFF_SKILL = null;
    private static final String strPhongThu = "Phòng thủ tăng: ";
    private static final String strTancong = "Tấn công tăng: ";
    public static SkillManager instance = new SkillManager();
    public static String[][] infoString = {new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Phòng thủ tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Phòng thủ tăng: ", "Tăng sức công: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "HP+MP tăng: ", "MP tăng: ", "Hồi sinh: ", "Chia sẻ thiệt hại: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tốc độ tăng: ", "Phòng thủ tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tốc độ tăng: ", "HP tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}};
    public static final byte[][] EFF_BUFF_SKILL = {new byte[]{20, 24}, new byte[]{20, 21}, new byte[]{25, 30, 27, 23}, new byte[]{19, 20}, new byte[]{22, 19}};
    public static final byte[][] SKILL_CAN_BUFF_TO_USER = {new byte[]{-1, 0}, new byte[]{0, -1}, new byte[]{0, -1, 1, 0}, new byte[]{0, -1}, new byte[]{0, -1}};
    public static final byte[][][] RANGE_SKILL_AEO = {new byte[][]{new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}, new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}, new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}}};
    public static final String[][] SKILL_NAME = {new String[]{"Cào", "Than hồng", "Nanh lửa", "Xoáy lửa", "Màn khói", "Múa kiếm", "Súng phun lửa", "Vuốt rồng", "Đại hỏa"}, new String[]{"Húc", "Roi dây leo", "Lá dao", "Bom bùn", "Tự vệ", "Sinh trưởng", "Bom hạt", "Bão lá", "Tia mặt trời"}, new String[]{"Húc", "Tấn công nhanh", "Cắn", "Sao băng", "Mong ước", "Nghỉ ngơi", "Phước lành hồi sinh", "Chia sẻ nỗi đau", "Đuôi sắt", "Bứt phá cuối cùng", "Tia phá hủy"}, new String[]{"Húc", "Súng nước", "Bong bóng", "Xoáy nước", "Phá mai", "Rút vào mai", "Đuôi nước", "Bơm nước", "Đại pháo nước"}, new String[]{"Tia lửa điện", "Sóng điện", "Cầu điện", "Tia chớp", "Nhanh nhẹn", "Nạp điện", "Lưới điện", "Sấm sét", "Đâm sầm điện"}};
    public static final SkillAnimate[][] SKILL_ANIMATE = {new SkillAnimate[]{new Skill_Kiem_Type0(700), new Skill_Kiem_Type1(), new Skill_Kiem_Type2(), new Skill_Kiem_Type3(0), null, null, new Skill_Kiem_TypeAEO(4), new Skill_Kiem_TypeAEO(5), new Skill_Kiem_Type4()}, new SkillAnimate[]{new Skill_Kiem_Type0(1200), new Skill_Dao_Type1(), new Skill_Dao_Type2(), new Skill_Dao_Type3(0), null, null, new Skill_Dao_TypeAEO(false), new Skill_Dao_TypeAEO(true), new Skill_Dao_Type4()}, new SkillAnimate[]{new Skill_Kiem_Type0(1000), new Skill_Dua_Type1(), new Skill_Dua_Type2(), new Skill_Dua_Type3(0), null, null, null, null, new Skill_Dua_TypeAEO(false), new Skill_Dua_TypeAEO(true), new Skill_Dua_Type4(0)}, new SkillAnimate[]{new Skill_Kiem_Type0(1500), new Skill_Bua_Type1(), new Skill_Bua_Type2(), new Skill_Bua_Type3(0), null, null, new Skill_Bua_TypeAEO(false), new Skill_Bua_TypeAEO(true), new Skill_Bua_Type4(0)}, new SkillAnimate[]{new Skill_Cung_Type0(), new Skill_Cung_Type1(), new Skill_Cung_Type2(), new Skill_Cung_Type3(0), null, null, new Skill_Cung_TypeAEO((byte) 4, false, (byte) 2), new Skill_Cung_TypeAEO((byte) 5, true, (byte) 2), new Skill_Cung_Type4(0), new Skill_Cung_TypeAEO((byte) 4, false, (byte) 5)}};

    public static int getSkillDamPercent(int i, int i2, int i3) {
        if (i2 > -1) {
            return SKILL_DAM_PERCENT[0][i][i2];
        }
        return 0;
    }

    public static int getSkillMP(int i, int i2, int i3) {
        if (i2 > -1) {
            return SKILL_MP[0][i][i2];
        }
        return 0;
    }

    public static long getSkillCooldown(byte b, byte b2, int i) {
        if (i > -1) {
            return SKILL_COOLDOWN[0][b][i];
        }
        return 0L;
    }

    public static int getSkillRange(byte b, byte b2) {
        return SKILL_RANGE[0][b];
    }

    public static short getLvAddSkill(int i, int i2, int i3) {
        if (i3 > -1) {
            return LEVEL_ADD_SKILL[i2][i3];
        }
        return (short) 0;
    }

    public static byte getBuffToUser(int i, int i2) {
        return SKILL_CAN_BUFF_TO_USER[i][i2];
    }

    public static boolean isSkillAeo(int i, int i2) {
        int i3 = 0;
        while (true) {
            byte[] bArr = SKILL_AEO[i];
            if (i3 >= bArr.length) {
                return false;
            }
            if (i2 == bArr[i3]) {
                return true;
            }
            i3++;
        }
    }

    public static int getRangeSkillAeo(int i, int i2) {
        try {
            byte b = MainChar.skillLevelLearnt[i2];
            if (b <= -1) {
                return 1984;
            }
            int i3 = 0;
            while (true) {
                byte[] bArr = SKILL_AEO[i];
                if (i3 >= bArr.length) {
                    return 1984;
                }
                if (i2 == bArr[i3]) {
                    return RANGE_SKILL_AEO[0][i3][b];
                }
                i3++;
            }
        } catch (Exception unused) {
            return 1984;
        }
    }

    public static SkillAnimate getSkillAnimate(int i, int i2) {
        return SKILL_ANIMATE[i2][i];
    }

    public static SkillAnimate setSkillBoss_Xmas() {
        return new Skill_Boss_Xmas(false);
    }
}
