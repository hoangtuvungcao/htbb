package game.model.skill;

import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Skill_AEO_CUNG_5 extends Effect {
    Vector list;
    private Char c;
    private int v;
    private int dir = 1;
    private int count;
    private int dem;

    public Skill_AEO_CUNG_5(Vector mst, Char c) {
        this.v = 12;
        this.list = mst;
        this.v = 12;
        this.c = c;
        for (int i = 0; i < this.list.size(); i++) {
            LiveActor live = (LiveActor) this.list.elementAt(i);
            if (live instanceof Monster) {
                ((Monster) live).state = (byte) 3;
            } else if (live instanceof Char) {
                ((Char) live).state = (byte) 2;
            }
        }
    }

    @Override
    public void update() {
        this.count++;
        if (this.dem > 6 && this.v == 0) {
            this.dir = -1;
        }
        if (this.dir == -1 && this.v != 11) {
            this.v++;
        }
        if (this.v > 0 && this.dir == 1) {
            this.v--;
        }
        if (this.dir == -1 && this.v == 11) {
            for (int i = 0; i < this.list.size(); i++) {
                LiveActor live = (LiveActor) this.list.elementAt(i);
                if (live instanceof Monster) {
                    ((Monster) live).state = (byte) 0;
                } else if (live instanceof Char) {
                    ((Char) live).state = (byte) 0;
                }
            }
            EffectManager.hiEffects.removeElement(this);
        }
        for (int i2 = 0; i2 < this.list.size(); i2++) {
            LiveActor live2 = (LiveActor) this.list.elementAt(i2);
            live2.dy = (short) (live2.dy - (this.v * this.dir));
        }
        if (this.v == 0 && this.dir == 1 && this.count % 3 == 0) {
            if (this.dem <= 3) {
                for (int i3 = 0; i3 < this.list.size(); i3++) {
                    Canvas.gameScr.startNewArrow(0, this.c, (LiveActor) this.list.elementAt(i3), this.c.x, this.c.y - 15, this.c.attkPower, this.c.attkEffect, 1);
                }
            }
            this.dem++;
        }
    }

    @Override
    public void paint(Graphics g) {
    }
}
