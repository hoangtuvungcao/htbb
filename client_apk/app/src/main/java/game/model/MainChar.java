package game.model;

import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Util;
import game.render.screen.GameScr;
import game.render.screen.MenuWindows;
import game.render.screen.RMS;
import java.lang.reflect.Array;
import java.util.Vector;

public class MainChar extends Char {
    public static short[] itemQuest;
    public static Potion[] listPotion;
    public static QuickSlot[][] quickSlot;
    public boolean canMove;
    public boolean check;
    public short day;
    public int dedicationPoint;
    public boolean isFirstTimeGetItem;
    public boolean isFirstTimeGetMoney;
    public boolean isFirstTimeGetPotion;
    public short lastLv;
    public byte limItemBag;
    public Quest mainQuest;
    public int pArena;
    public short[] posTransRoad;
    public short xBegin;
    public short yBegin;
    public static Vector gemitemImbue = new Vector();
    public static Vector gemItem = new Vector();
    public static Vector gemItemKhoa = new Vector();
    public static Vector shopItem = new Vector();
    public boolean autoFight = false;
    public byte idSkill = 0;
    public int idMonster = 0;
    public Vector tItems = new Vector();
    public Vector rItems = new Vector();
    public boolean first = false;
    public int countRoad = 0;
    public String nameHusband_wife = "";
    public boolean stop = false;

    @Override
    public boolean isMainchar() {
        return true;
    }

    public static void remove() {
        gemitemImbue.removeAllElements();
        gemItem.removeAllElements();
        gemItemKhoa.removeAllElements();
        shopItem.removeAllElements();
        quickSlot = null;
        listPotion = null;
    }

    public void loadQuickSlot() {
        if (quickSlot != null) {
            return;
        }
        quickSlot = (QuickSlot[][]) Array.newInstance((Class<?>) QuickSlot.class, 2, 5);
        for (int i = 0; i < 5; i++) {
            quickSlot[0][i] = new QuickSlot();
            quickSlot[1][i] = new QuickSlot();
        }
        if (skillLevelLearnt[1] > 0) {
            quickSlot[0][0].setIsSkill(1, this.clazz, false);
        }
        if (skillLevelLearnt[2] > 0) {
            quickSlot[0][1].setIsSkill(2, this.clazz, false);
        }
        quickSlot[0][2].setIsSkill(0, this.clazz, false);
        quickSlot[0][3].setIsPotion(1);
        quickSlot[0][4].setIsPotion(4);
        quickSlot[1][3].setIsPotion(1);
        quickSlot[1][4].setIsPotion(4);
        RMS.loadQuickSlot();
    }

    public void resetAuto() {
        this.autoFight = false;
        this.idSkill = (byte) 0;
    }

    public String getPercent() {
        return (this.lvpercent / 10) + "." + (this.lvpercent % 10);
    }

    public void add2Party(PartyInfo partyInfo) {
        int size = party.size();
        for (int i = 0; i < size; i++) {
            if (((PartyInfo) party.elementAt(i)).name.equals(partyInfo.name)) {
                return;
            }
        }
        party.addElement(partyInfo);
    }

    public void add2Party(Char r2) {
        if (party.contains(r2)) {
            return;
        }
        party.addElement(r2);
    }

    public String removeUserParty(short s) {
        int size = party.size();
        for (int i = 0; i < size; i++) {
            if (((PartyInfo) party.elementAt(i)).id == s) {
                String str = ((PartyInfo) party.elementAt(i)).name;
                party.removeElementAt(i);
                return str;
            }
        }
        return "";
    }

    public String removeCharParty(short s) {
        int size = party.size();
        for (int i = 0; i < size; i++) {
            if (((Char) party.elementAt(i)).ID == s) {
                String str = ((Char) party.elementAt(i)).name;
                party.removeElementAt(i);
                return str;
            }
        }
        return "";
    }

    @Override
    public void update() {
        super.update();
        if (!this.isFreeze && this.posTransRoad != null) {
            updateWayPointer();
            this.check = false;
        }
        if (!this.stop && System.currentTimeMillis() - GameScr.timeMove >= 0) {
            GameScr.timeMove = System.currentTimeMillis() + ((long) GameScr.dtmove);
            this.stop = true;
            GameService.gI().moveChar(this.x, this.y);
        }
        if (GameScr.isNhanPhim) {
            return;
        }
        checkChangemapTouch();
    }

    private void checkChangemapTouch() {
        short s;
        GameScr gameScr = GCanvas.gameScr;
        if (GameScr.iTaskAuto != 2) {
            if (this.canMove && !this.check && this.state == 1) {
                short s2 = 32;
                if (this.dir == 2) {
                    s = -32;
                } else {
                    s = this.dir == 3 ? (short) 32 : (short) 0;
                }
                if (this.dir != 0) {
                    s2 = this.dir == 1 ? (short) -32 : (short) 0;
                }
                if ((Math.abs(this.x - this.xBegin) >= 16 || Math.abs(this.y - this.yBegin) >= 16) && GCanvas.gameScr.checkCanChangeMap(this.x + s, this.y + s2, this.dir)) {
                    this.posTransRoad = null;
                    this.check = true;
                    return;
                }
            }
            if (this.state == 1) {
                this.canMove = true;
            } else {
                this.canMove = false;
            }
        }
    }

    private void updateWayPointer() {
        if (Util.abs(this.x - this.xTo) > 3 || Util.abs(this.y - this.yTo) > 3) {
            return;
        }
        for (int length = (this.posTransRoad.length - 1) - this.countRoad; length >= 0; length--) {
            if (this.posTransRoad[length] > 0) {
                byte b = (byte) (GameScr.xStart + (this.posTransRoad[length] >> 8));
                byte b2 = (byte) (GameScr.yStart + (this.posTransRoad[length] & 255));
                if (GameScr.iTaskAuto != 2 && ((this.dir == 1 || Tilemap.isOfflineMap) && GCanvas.gameScr.checkCanChangeMap((b * 16) + this.speed, (b2 * 16) + this.speed, this.dir))) {
                    this.posTransRoad = null;
                    this.countRoad = 0;
                    return;
                }
                int i = b * 16;
                int i2 = b2 * 16;
                if (GCanvas.gameScr.checkMoveLimit(this.speed + i, this.speed + i2)) {
                    this.posTransRoad = null;
                    return;
                }
                moveTo((short) (this.speed + i), (short) (this.speed + i2));
                GCanvas.gameScr.sendActorMove((short) (i + this.speed), (short) (i2 + this.speed));
                this.posTransRoad[length] = -1;
                this.countRoad++;
                return;
            }
            if (length == 0) {
                this.posTransRoad = null;
                this.countRoad = 0;
                return;
            }
        }
    }

    public String[] getInfoSkill(int i) {
        String[] strArr = null;
        if (skillLevelLearnt[i] == -1) {
            return new String[]{"Chưa học kỹ năng này", "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][1])};
        }
        if (skillLevelLearnt[i] == 0) {
            return new String[]{"Chưa học kỹ năng này", getLvRequest(i)};
        }
        if (!MenuWindows.gI().isBuffSkill()) {
            String[] strArr2 = new String[5];
            byte b = (byte) i;
            strArr2[0] = "Thời gian đánh: " + SkillManager.getSkillCooldown(b, this.clazz, skillLevelLearnt[i]) + " ms";
            strArr2[1] = SkillManager.infoString[this.clazz][i] + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
            strArr2[2] = "Phạm vi: " + SkillManager.getSkillRange(b, this.clazz);
            strArr2[3] = "MP mất: " + SkillManager.getSkillMP(i, skillLevelLearnt[i], this.clazz);
            strArr2[4] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
            return strArr2;
        }
        byte[][] bArr = {new byte[]{1, 5}, new byte[]{1, 2}, new byte[]{3, 4, 5, 6}, new byte[]{0, 1}, new byte[]{0, 3}};
        String str = "MP mất: " + SkillManager.getSkillMP(i, skillLevelLearnt[i], this.clazz);
        byte b2 = this.clazz;
        if (b2 == 0) {
            if (i == 4) {
                String[] strArr3 = new String[4];
                strArr3[0] = "Đánh xuyên giáp";
                strArr3[1] = "Tỷ lệ thành công: " + ((int) SkillManager.SKILL_DAM_PERCENT[this.clazz][i][skillLevelLearnt[i]]) + "%";
                strArr3[2] = str;
                strArr3[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                return strArr3;
            }
            if (i == 5) {
                strArr = new String[6];
                strArr[0] = "Gây tác hại lại 1 phần";
                strArr[1] = "sức công cho đối thủ";
                strArr[2] = "Thời gian: " + ((int) SkillManager.TIME_LIFE_BUFF_SKILL[bArr[this.clazz][i - 4]][skillLevelLearnt[i]]) + "s";
                strArr[3] = "Tỷ lệ: " + ((int) SkillManager.SKILL_DAM_PERCENT[this.clazz][i][skillLevelLearnt[i]]) + "%";
                strArr[4] = str;
                strArr[5] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                return strArr;
            }
            return null;
        }
        if (b2 == 1) {
            if (i == 5) {
                String[] strArr4 = new String[4];
                strArr4[0] = "Tăng sức tấn công";
                strArr4[1] = "của bản thân";
                strArr4[2] = "Tỷ lệ tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
                strArr4[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                return strArr4;
            }
            if (i == 4) {
                strArr = new String[4];
                strArr[0] = "Phòng thủ tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
                strArr[1] = "Thời gian: " + ((int) SkillManager.TIME_LIFE_BUFF_SKILL[bArr[this.clazz][i - 4]][skillLevelLearnt[i]]) + "s";
                strArr[2] = str;
                strArr[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                return strArr;
            }
            return null;
        }
        if (b2 == 2) {
            if (i == 5) {
                String[] strArr5 = new String[4];
                strArr5[0] = "Tăng tinh thần";
                strArr5[1] = "của bản thân";
                strArr5[2] = "Tỷ lệ tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
                strArr5[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                return strArr5;
            }
            if (i == 4) {
                strArr = new String[4];
                strArr[0] = "MP + HP tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + " %";
                strArr[1] = "Thời gian: " + ((int) SkillManager.TIME_LIFE_BUFF_SKILL[bArr[this.clazz][i - 4]][skillLevelLearnt[i]]) + "s";
                strArr[2] = str;
                strArr[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
            } else {
                if (i == 6) {
                    String[] strArr6 = new String[4];
                    strArr6[0] = "Hồi sinh";
                    strArr6[1] = "HP hồi phục: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
                    strArr6[2] = str;
                    strArr6[3] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                    return strArr6;
                }
                if (i == 7) {
                    strArr = new String[7];
                    strArr[0] = "Khiên MP";
                    strArr[1] = "Chuyển 1 lượng hao tổn";
                    strArr[2] = "HP sang MP";
                    strArr[3] = "Tỷ lệ chuyển: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%";
                    strArr[4] = "Thời gian: " + ((int) SkillManager.TIME_LIFE_BUFF_SKILL[bArr[this.clazz][i - 4]][skillLevelLearnt[i]]) + "s";
                    strArr[5] = str;
                    strArr[6] = skillLevelLearnt[i] < 9 ? "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]) : "";
                }
            }
            return strArr;
        }
        if (b2 != 3) {
            if (b2 == 4) {
                if (i == 5) {
                    return new String[]{"Tăng độc tính sử dụng", "Tỷ lệ tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%", getLvRequest(i)};
                }
                if (i == 4) {
                    return new String[]{"Tẩm độc vào tên: ", "Độc tính: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz), "Thời gian: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "s", str, getLvRequest(i)};
                }
            }
        } else {
            if (i == 5) {
                return new String[]{"Tăng sức phòng thủ", "của bản thân", "Tỷ lệ tăng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%", getLvRequest(i)};
            }
            if (i == 4) {
                return new String[]{"Gây choáng cho", "đối phương", "Thời gian: 5s", "Tỷ lệ gây choáng: " + SkillManager.getSkillDamPercent(i, skillLevelLearnt[i], this.clazz) + "%", str, getLvRequest(i)};
            }
        }
        return null;
    }

    public String getLvRequest(int i) {
        return "Lv yêu cầu: " + ((int) SkillManager.LEVEL_ADD_SKILL[i][skillLevelLearnt[i]]);
    }

    public boolean setWay(int i, int i2) {
        short s = this.x;
        if (i != 0) {
            if (!Tilemap.tileTypeAtPixel(this.x + i, this.y - 16, 2) && !Tilemap.tileTypeAtPixel(this.x, this.y - 16, 2)) {
                moveTo(s, (short) (this.y - 16));
                return true;
            }
            if (Tilemap.tileTypeAtPixel(this.x + i, this.y + 16, 2) || Tilemap.tileTypeAtPixel(this.x, this.y + 16, 2)) {
                return false;
            }
            moveTo(s, (short) (this.y + 16));
            return true;
        }
        if (i2 == 0) {
            return false;
        }
        if (!Tilemap.tileTypeAtPixel(this.x - 16, this.y + i2, 2) && !Tilemap.tileTypeAtPixel(this.x - 16, this.y, 2)) {
            moveTo((short) (this.x - 16), this.y);
            return true;
        }
        if (Tilemap.tileTypeAtPixel(this.x + 16, this.y + i2, 2) || Tilemap.tileTypeAtPixel(this.x + 16, this.y, 2)) {
            return false;
        }
        moveTo((short) (this.x + 16), this.y);
        return true;
    }
}
