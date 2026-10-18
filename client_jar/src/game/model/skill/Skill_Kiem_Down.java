package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.FrameImage;
import game.render.Canvas;
import game.render.Util;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_Kiem_Down extends Effect {
    private static FrameImage imgSword;
    private static Image imgCut;
    int count = 120;
    byte frame = 0;
    byte g = 5;
    byte delay = 10;
    byte countPop;

    static {
        try {
            imgSword = new FrameImage(Image.createImage("/sword skill/kiem01.png"), 12, 39);
            imgCut = Image.createImage("/sword skill/all1.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Skill_Kiem_Down(int x, int y) {
        this.x = x;
        this.y = y;
        Effect eff = new Effect(x, y, 34);
        eff.loop = (byte) 3;
        EffectManager.lowEffects.addElement(eff);
    }

    @Override
    public void update() {
        if (this.delay > 0) {
            this.delay = (byte) (this.delay - 1);
        }
        if (this.delay <= 0 && this.count > 0) {
            this.count -= this.g;
            this.g = (byte) (this.g + 5);
            if (this.count < 0) {
                this.count = 0;
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.addElement(this);
            }
        }
        if (this.count == 0 && Canvas.gameTick % 2 == 1) {
            for (int i = 0; i < 10; i++) {
                int xa = (this.countPop * Util.cos(i * 36)) >> 10;
                int ya = (-(this.countPop * Util.sin(i * 36))) >> 10;
                EffectManager.addLowEffect(this.x + xa, (this.y + (ya + ((-(ya / 3)) * 2))) - 10, 15);
            }
            this.countPop = (byte) (this.countPop + 10);
            if (this.countPop == 30) {
                EffectManager.lowEffects.removeElement(this);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.count > 0) {
            imgSword.drawFrame(this.frame / 3, this.x, this.y - this.count, 0, 33, g);
        } else {
            g.drawImage(imgCut, this.x, this.y, 33);
        }
        this.frame = (byte) (this.frame + 1);
        if (this.frame >= 6) {
            this.frame = (byte) 0;
        }
    }
}
