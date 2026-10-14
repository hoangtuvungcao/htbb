package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;

public class QuickSlot {
    public static final byte TYPE_POTION = 2;
    public static final byte TYPE_SKILL = 1;
    public static final byte[] WEAPON_OF_CLAZZ = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};
    private static Bitmap img = null;
    private byte indexPotion;
    public int potionType;
    private byte skillType;
    public int quickslotType = -1;
    public boolean isBuff = false;

    public static void getImg() {
        FilePack.init("/other");
        img = FilePack.getImg("cooldown");
        FilePack.reset();
    }

    public void setIsSkill(int i, int i2, boolean z) {
        this.quickslotType = 1;
        this.skillType = (byte) i;
        this.isBuff = z;
    }

    public void paint(Graphics graphics, int i, int i2) {
        try {
            int i3 = this.quickslotType;
            if (i3 == -1) {
                return;
            }
            if (i3 == 1) {
                if (GameData.imgSkillIcon != null) {
                    byte b = GCanvas.gameScr.mainChar.clazz;
                    if (b == 0) {
                        graphics.drawRegion(Res.imgIconSkillKiem, 0, this.skillType * 20, 20, 20, 0, i, i2, Graphics.VCENTER | Graphics.HCENTER);
                    } else if (b == 1) {
                        graphics.drawRegion(Res.imgIconSkillDao, 0, this.skillType * 20, 20, 20, 0, i, i2, Graphics.VCENTER | Graphics.HCENTER);
                    } else if (b == 2) {
                        graphics.drawRegion(Res.imgIconSkillPs, 0, this.skillType * 20, 20, 20, 0, i, i2, Graphics.VCENTER | Graphics.HCENTER);
                    } else if (b == 3) {
                        graphics.drawRegion(Res.imgIconSkillBua, 0, this.skillType * 20, 20, 20, 0, i, i2, Graphics.VCENTER | Graphics.HCENTER);
                    } else if (b == 4) {
                        graphics.drawRegion(Res.imgIconSkillCung, 0, this.skillType * 20, 20, 20, 0, i, i2, Graphics.VCENTER | Graphics.HCENTER);
                    }
                }
                long jCurrentTimeMillis = System.currentTimeMillis() - GCanvas.gameScr.mainChar.timeLastUseSkills[this.skillType];
                if (jCurrentTimeMillis < GCanvas.gameScr.mainChar.coolDownSkill[this.skillType]) {
                    int i4 = (int) ((jCurrentTimeMillis * 26) / GCanvas.gameScr.mainChar.coolDownSkill[this.skillType]);
                    graphics.drawRegion(img, 0, i4, 26, 26 - i4, 0, i, i2 + 1 + (i4 / 2), Graphics.VCENTER | Graphics.HCENTER);
                    return;
                }
                return;
            }
            Res.paintPotion(graphics, MainChar.listPotion[this.potionType].index, i, i2, Graphics.VCENTER | Graphics.HCENTER);
            Font.verySmallFont.drawString(graphics, GCanvas.gameScr.mainChar.potions[this.potionType] + "", i + 3, i2 + 5, 2);
            long jCurrentTimeMillis2 = System.currentTimeMillis() - GCanvas.gameScr.mainChar.potionLastTimeUse[this.potionType];
            if (jCurrentTimeMillis2 < MainChar.listPotion[this.potionType].delay) {
                int i5 = (int) ((jCurrentTimeMillis2 * 26) / MainChar.listPotion[this.potionType].delay);
                graphics.drawRegion(img, 0, i5, 26, 26 - i5, 0, i, i2 + 1 + (i5 / 2), Graphics.VCENTER | Graphics.HCENTER);
            }
        } catch (Exception unused) {
            Cout.println("ERROR QUICKSLOT");
        }
    }

    public byte getSkillType() {
        return this.skillType;
    }

    public int getPotionType() {
        return this.potionType;
    }

    public void setIsPotion(int i) {
        this.quickslotType = 2;
        this.potionType = i;
    }

    public void setIsNothing() {
        this.quickslotType = -1;
    }
}
