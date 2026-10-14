package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class QuickSlot {
    public byte skillType;
    public byte indexPotion;
    public static final byte TYPE_SKILL = 1;
    public int potionType;
    public static final byte TYPE_POTION = 2;
    public static final byte[] WEAPON_OF_CLAZZ = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};
    private static Image img = null;
    public int quickslotType = -1;
    public boolean isBuff = false;

    public static void getImg() {
        img = FilePack.getImg("cooldown");
    }

    public void setIsSkill(int skillType, int clazz, boolean isBuff) {
        this.quickslotType = 1;
        this.skillType = (byte) skillType;
        this.isBuff = isBuff;
    }

    public void paint(Graphics g, int x, int y) {
        try {
            if (this.quickslotType == -1) {
                return;
            }
            if (this.quickslotType == 1) {
                if (GameData.imgSkillIcon != null) {
                    GameData.imgSkillIcon.drawFrame(this.skillType, x - 2, y - 1, 0, 0, g);
                }
                long timeSinceUseThisSkill = System.currentTimeMillis() - Canvas.gameScr.mainChar.timeLastUseSkills[this.skillType];
                if (timeSinceUseThisSkill < Canvas.gameScr.mainChar.coolDownSkill[this.skillType]) {
                    int skillCooldownHeight = (int) ((timeSinceUseThisSkill * 20) / Canvas.gameScr.mainChar.coolDownSkill[this.skillType]);
                    g.drawRegion(img, 0, skillCooldownHeight, 20, 20 - skillCooldownHeight, 0, x - 2, (y - 2) + skillCooldownHeight, 20);
                    return;
                }
                return;
            }
            Res.paintPotion(g, MainChar.listPotion[this.potionType].index, x, y, 0);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(Canvas.gameScr.mainChar.potions[this.potionType])).toString(), x + 16, y + 9, 1);
            long timeSinceUseThisPotion = System.currentTimeMillis() - Canvas.gameScr.mainChar.potionLastTimeUse[this.potionType];
            if (timeSinceUseThisPotion < MainChar.listPotion[this.potionType].delay) {
                int potionCooldownHeight = (int) ((timeSinceUseThisPotion * 20) / MainChar.listPotion[this.potionType].delay);
                g.drawRegion(img, 0, potionCooldownHeight, 20, 20 - potionCooldownHeight, 0, x - 2, (y - 2) + potionCooldownHeight, 20);
            }
        } catch (Exception e) {
            Cout.println(new StringBuffer("ERROR QUICKSLOT ").append(e.toString()).toString());
        }
    }

    public byte getSkillType() {
        return this.skillType;
    }

    public void update() {
    }

    public int getPotionType() {
        return this.potionType;
    }

    public void setIsPotion(int potionType) {
        this.quickslotType = 2;
        this.potionType = potionType;
    }

    public void setIsNothing() {
        this.quickslotType = -1;
    }
}
