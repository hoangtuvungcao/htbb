package game.model.skill;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.Effect;
import game.model.EffectManager;
import game.model.FilePack;
import game.model.FrameImage;
import game.render.GCanvas;
import game.render.Util;

public class Skill_Kiem_Down extends Effect {
    private static Bitmap imgCut;
    private static FrameImage imgSword;
    byte countPop;
    int count = 120;
    byte frame = 0;
    byte g = 5;
    byte delay = 10;

    static {
        FilePack.init("/other");
        imgSword = new FrameImage(FilePack.getImg("swordskill_kiem01"), 12, 39);
        imgCut = FilePack.getImg("swordskill_all1");
        FilePack.reset();
    }

    public Skill_Kiem_Down(int i, int i2) {
        this.x = i;
        this.y = i2;
        Effect effect2 = new Effect(i, i2, 34);
        effect2.loop = (byte) 3;
        EffectManager.lowEffects.addElement(effect2);
    }

    @Override
    public void update() {
        int i;
        try {
            byte b = this.delay;
            if (b > 0) {
                this.delay = (byte) (b - 1);
            }
            if (this.delay <= 0 && (i = this.count) > 0) {
                byte b2 = this.g;
                int i2 = i - b2;
                this.count = i2;
                this.g = (byte) (b2 + 5);
                if (i2 < 0) {
                    try {
                        this.count = 0;
                        EffectManager.hiEffects.removeElement(this);
                        EffectManager.lowEffects.addElement(this);
                    } catch (Exception unused) {
                    }
                }
            }
            if (this.count == 0 && GCanvas.gameTick % 2 == 1) {
                for (int i3 = 0; i3 < 10; i3++) {
                    try {
                        int i4 = i3 * 36;
                        int iCos = (this.countPop * Util.cos(i4)) >> 10;
                        int i5 = (-(this.countPop * Util.sin(i4))) >> 10;
                        EffectManager.addLowEffect(this.x + iCos, (this.y + (i5 + ((-(i5 / 3)) * 2))) - 10, 15);
                    } catch (Exception unused2) {
                    }
                }
                byte b3 = (byte) (this.countPop + 10);
                this.countPop = b3;
                if (b3 == 30) {
                    try {
                        EffectManager.lowEffects.removeElement(this);
                    } catch (Exception unused3) {
                    }
                }
            }
        } catch (Exception unused4) {
            Cout.println("update skill kiem down");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            if (this.count > 0) {
                imgSword.drawFrame(this.frame / 3, this.x, this.y - this.count, 0, Graphics.BOTTOM | Graphics.HCENTER, graphics);
            } else {
                graphics.drawImage(imgCut, this.x, this.y, Graphics.BOTTOM | Graphics.HCENTER);
            }
            byte b = (byte) (this.frame + 1);
            this.frame = b;
            if (b >= 6) {
                this.frame = (byte) 0;
            }
        } catch (Exception unused) {
            Cout.println("Loi skill kiem down");
        }
    }
}
