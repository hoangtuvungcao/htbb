package game.model.skill;

import game.model.Char;
import game.model.skill.complete.Skill_Boss_Xmas;
import game.model.skill.complete.Skill_Squirtle_Type1;
import game.model.skill.complete.Skill_Squirtle_Type2;
import game.model.skill.complete.Skill_Squirtle_Type3;
import game.model.skill.complete.Skill_Squirtle_Type4;
import game.model.skill.complete.Skill_Squirtle_TypeAEO;
import game.model.skill.complete.Skill_Pichu_Type0;
import game.model.skill.complete.Skill_Pichu_Type1;
import game.model.skill.complete.Skill_Pichu_Type2;
import game.model.skill.complete.Skill_Pichu_Type3;
import game.model.skill.complete.Skill_Pichu_Type4;
import game.model.skill.complete.Skill_Pichu_TypeAEO;
import game.model.skill.complete.Skill_Bulbasaur_Type1;
import game.model.skill.complete.Skill_Bulbasaur_Type2;
import game.model.skill.complete.Skill_Bulbasaur_Type3;
import game.model.skill.complete.Skill_Bulbasaur_Type4;
import game.model.skill.complete.Skill_Bulbasaur_TypeAEO;
import game.model.skill.complete.Skill_Eevee_Type1;
import game.model.skill.complete.Skill_Eevee_Type2;
import game.model.skill.complete.Skill_Eevee_Type3;
import game.model.skill.complete.Skill_Eevee_Type4;
import game.model.skill.complete.Skill_Eevee_TypeAEO;
import game.model.skill.complete.Skill_DanhThuong;
import game.model.skill.complete.Skill_Charmander_Type1;
import game.model.skill.complete.Skill_Charmander_Type2;
import game.model.skill.complete.Skill_Charmander_Type3;
import game.model.skill.complete.Skill_Charmander_Type4;
import game.model.skill.complete.Skill_Charmander_TypeAEO;
import java.util.Hashtable;

public class SkillManager extends Hashtable {
    private static final String strTancong = "Tấn công tăng: ";
    private static final String strPhongThu = "Phòng thủ tăng: ";
    public static byte[][] LEVEL_ADD_SKILL;
    public static byte[][] SKILL_AEO;
    public static short[][][] SKILL_DAM_PERCENT;
    public static short[][] TIME_LIFE_BUFF_SKILL;
    public static int[][][] SKILL_COOLDOWN;
    public static short[][][] SKILL_MP;
    public static short[][] SKILL_RANGE;
    public static final SkillAnimate[][] SKILL_ANIMATE;
    public static SkillManager instance = new SkillManager();
    public static String[][] infoString = {new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Phòng thủ tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Phòng thủ tăng: ", "Tăng sức công: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "HP+MP tăng: ", "MP tăng: ", "Hồi sinh: ", "Chia sẻ thiệt hại: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tốc độ tăng: ", "Phòng thủ tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}, new String[]{"Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tốc độ tăng: ", "HP tăng: ", "Tấn công tăng: ", "Tấn công tăng: ", "Tấn công tăng: "}};
    public static final byte[][] EFF_BUFF_SKILL = {new byte[]{20, 24}, new byte[]{20, 21}, new byte[]{25, 30, 27, 23}, new byte[]{19, 20}, new byte[]{22, 19}};
    public static final byte[][] SKILL_CAN_BUFF_TO_USER = {new byte[]{-1, 0}, new byte[]{0, -1}, new byte[]{0, -1, 1, 0}, new byte[]{0, -1}, new byte[]{0, -1}};
    public static final byte[][][] RANGE_SKILL_AEO = {new byte[][]{new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}, new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}, new byte[]{0, 64, 64, 80, 80, 96, 96, 102, 118, 124, 124}}};
    public static final String[][] SKILL_NAME = {new String[]{"Cào", "Than hồng", "Nanh lửa", "Xoáy lửa", "Màn khói", "Múa kiếm", "Súng phun lửa", "Vuốt rồng", "Đại hỏa"}, new String[]{"Húc", "Roi dây leo", "Lá dao", "Bom bùn", "Tự vệ", "Sinh trưởng", "Bom hạt", "Bão lá", "Tia mặt trời"}, new String[]{"Húc", "Tấn công nhanh", "Cắn", "Sao băng", "Mong ước", "Nghỉ ngơi", "Phước lành hồi sinh", "Chia sẻ nỗi đau", "Đuôi sắt", "Bứt phá cuối cùng", "Tia phá hủy"}, new String[]{"Húc", "Súng nước", "Bong bóng", "Xoáy nước", "Phá mai", "Rút vào mai", "Đuôi nước", "Bơm nước", "Đại pháo nước", "Bua 8x"}, new String[]{"Tia lửa điện", "Sóng điện", "Cầu điện", "Tia chớp", "Nhanh nhẹn", "Nạp điện", "Lưới điện", "Sấm sét", "Đâm sầm điện"}};

    static {
        SkillAnimate[] skillAnimateArr = new SkillAnimate[9];
        skillAnimateArr[0] = new Skill_DanhThuong(700);
        skillAnimateArr[1] = new Skill_Charmander_Type1();
        skillAnimateArr[2] = new Skill_Charmander_Type2();
        skillAnimateArr[3] = new Skill_Charmander_Type3(0);
        skillAnimateArr[6] = new Skill_Charmander_TypeAEO(4);
        skillAnimateArr[7] = new Skill_Charmander_TypeAEO(5);
        skillAnimateArr[8] = new Skill_Charmander_Type4();
        SkillAnimate[] skillAnimateArr2 = new SkillAnimate[9];
        skillAnimateArr2[0] = new Skill_DanhThuong(1200);
        skillAnimateArr2[1] = new Skill_Bulbasaur_Type1();
        skillAnimateArr2[2] = new Skill_Bulbasaur_Type2();
        skillAnimateArr2[3] = new Skill_Bulbasaur_Type3(0);
        skillAnimateArr2[6] = new Skill_Bulbasaur_TypeAEO(false);
        skillAnimateArr2[7] = new Skill_Bulbasaur_TypeAEO(true);
        skillAnimateArr2[8] = new Skill_Bulbasaur_Type4();
        SkillAnimate[] skillAnimateArr3 = new SkillAnimate[11];
        skillAnimateArr3[0] = new Skill_DanhThuong(1000);
        skillAnimateArr3[1] = new Skill_Eevee_Type1();
        skillAnimateArr3[2] = new Skill_Eevee_Type2();
        skillAnimateArr3[3] = new Skill_Eevee_Type3(0);
        skillAnimateArr3[8] = new Skill_Eevee_TypeAEO(false);
        skillAnimateArr3[9] = new Skill_Eevee_TypeAEO(true);
        skillAnimateArr3[10] = new Skill_Eevee_Type4(0);
        SkillAnimate[] skillAnimateArr4 = new SkillAnimate[9];
        skillAnimateArr4[0] = new Skill_DanhThuong(1500);
        skillAnimateArr4[1] = new Skill_Squirtle_Type1();
        skillAnimateArr4[2] = new Skill_Squirtle_Type2();
        skillAnimateArr4[3] = new Skill_Squirtle_Type3(0);
        skillAnimateArr4[6] = new Skill_Squirtle_TypeAEO(false);
        skillAnimateArr4[7] = new Skill_Squirtle_TypeAEO(true);
        skillAnimateArr4[8] = new Skill_Squirtle_Type4(0);
        SkillAnimate[] skillAnimateArr5 = new SkillAnimate[10];
        skillAnimateArr5[0] = new Skill_Pichu_Type0();
        skillAnimateArr5[1] = new Skill_Pichu_Type1();
        skillAnimateArr5[2] = new Skill_Pichu_Type2();
        skillAnimateArr5[3] = new Skill_Pichu_Type3(0);
        skillAnimateArr5[6] = new Skill_Pichu_TypeAEO((byte) 4, false, (byte) 2);
        skillAnimateArr5[7] = new Skill_Pichu_TypeAEO((byte) 5, true, (byte) 2);
        skillAnimateArr5[8] = new Skill_Pichu_Type4(0);
        skillAnimateArr5[9] = new Skill_Pichu_TypeAEO((byte) 4, false, (byte) 5);
        SKILL_ANIMATE = new SkillAnimate[][]{skillAnimateArr, skillAnimateArr2, skillAnimateArr3, skillAnimateArr4, skillAnimateArr5};
    }

    public static int getSkillDamPercent(int skillType, int skillLevel, int clazz) {
        if (skillLevel > -1) {
            return SKILL_DAM_PERCENT[0][skillType][skillLevel];
        }
        return 0;
    }

    public static int getSkillMP(int skillType, int skillLevel, int clazz) {
        if (skillLevel > -1) {
            return SKILL_MP[0][skillType][skillLevel];
        }
        return 0;
    }

    public static long getSkillCooldown(byte skillType, byte clazz, int level) {
        return level > -1 ? SKILL_COOLDOWN[0][skillType][level] : 0;
    }

    public static int getSkillRange(byte skillType, byte clazz) {
        return SKILL_RANGE[0][skillType];
    }

    public static short getLvAddSkill(int clazz, int idSkill, int lvSkill) {
        if (lvSkill > -1) {
            return LEVEL_ADD_SKILL[idSkill][lvSkill];
        }
        return (short) 0;
    }

    public static byte getBuffToUser(int clazz, int id) {
        return SKILL_CAN_BUFF_TO_USER[clazz][id];
    }

    public static SkillAnimate setSkillBoss_Xmas() {
        return new Skill_Boss_Xmas(false);
    }

    public static boolean isSkillAeo(int cClass, int skill) {
        for (int i = 0; i < SKILL_AEO[cClass].length; i++) {
            if (skill == SKILL_AEO[cClass][i]) {
                return true;
            }
        }
        return false;
    }

    public static int getRangeSkillAeo(int charClass, int skill) {
        byte b = Char.skillLevelLearnt[skill];
        if (b > -1) {
            for (int i = 0; i < SKILL_AEO[charClass].length; i++) {
                if (skill == SKILL_AEO[charClass][i]) {
                    return RANGE_SKILL_AEO[0][i][b];
                }
            }
            return 1984;
        }
        return 1984;
    }

    public static SkillAnimate getSkillAnimate(int skillType, int clazz) {
        return SKILL_ANIMATE[clazz][skillType];
    }
}
