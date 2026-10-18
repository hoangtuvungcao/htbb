package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.render.GCanvas;
import game.render.screen.GameScr;
import java.util.Vector;

public class Skill_AEO_CUNG_5 extends Effect {
    private Char c;
    private int count;
    private int dem;
    private int dir;
    Vector list;
    private int v;

    @Override
    public void paint(Graphics graphics) {
    }

    public Skill_AEO_CUNG_5(Vector vector, Char r3) {
        new Vector();
        this.dir = 1;
        this.list = vector;
        this.v = 12;
        this.c = r3;
        for (int i = 0; i < this.list.size(); i++) {
            LiveActor liveActor = (LiveActor) this.list.elementAt(i);
            if (liveActor != null) {
                if (liveActor instanceof Monster) {
                    ((Monster) liveActor).state = (byte) 3;
                } else if (liveActor instanceof Char) {
                    ((Char) liveActor).state = (byte) 2;
                }
            }
        }
    }

    @Override
    public void update() {
        int i;
        this.count++;
        if (this.dem > 6 && this.v == 0) {
            this.dir = -1;
        }
        int i2 = this.dir;
        if (i2 == -1 && (i = this.v) != 11) {
            this.v = i + 1;
        }
        int i3 = this.v;
        if (i3 > 0 && i2 == 1) {
            this.v = i3 - 1;
        }
        if (i2 == -1 && this.v == 11) {
            for (int i4 = 0; i4 < this.list.size(); i4++) {
                LiveActor liveActor = (LiveActor) this.list.elementAt(i4);
                if (liveActor != null) {
                    if (liveActor instanceof Monster) {
                        ((Monster) liveActor).state = (byte) 0;
                    } else if (liveActor instanceof Char) {
                        ((Char) liveActor).state = (byte) 0;
                    }
                }
            }
            EffectManager.hiEffects.removeElement(this);
        }
        for (int i5 = 0; i5 < this.list.size(); i5++) {
            LiveActor liveActor2 = (LiveActor) this.list.elementAt(i5);
            liveActor2.dy = (short) (liveActor2.dy - ((short) (this.v * this.dir)));
        }
        if (this.v == 0 && this.dir == 1 && this.count % 3 == 0) {
            if (this.dem <= 3) {
                for (int i6 = 0; i6 < this.list.size(); i6++) {
                    LiveActor liveActor3 = (LiveActor) this.list.elementAt(i6);
                    GameScr gameScr = GCanvas.gameScr;
                    Char r7 = this.c;
                    gameScr.startNewArrow(0, r7, liveActor3, r7.x, this.c.y - 15, this.c.attkPower, this.c.attkEffect, 1);
                }
            }
            this.dem++;
        }
    }
}
