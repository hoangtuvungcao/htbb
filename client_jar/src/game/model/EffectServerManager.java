package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Vector;

public class EffectServerManager {
    public short ID;
    public short loop;
    public short loopLimit;
    public short radius;
    public short x;
    public short y;
    public short count = 0;
    public short indexLoop;
    public short indexPos;
    public short idPlayer;
    public byte style;
    public byte loopType;
    public short[] xLoop;
    public short[] yLoop;
    public static Vector effectList = new Vector();

    public void update() {
        EffectData effDa = getEffect(this.ID);
        if (effDa == null) {
            return;
        }
        if (this.style == 0) {
            Actor ava = Canvas.gameScr.findActorByID(this.idPlayer);
            this.x = ava.x;
            this.y = ava.y;
        }
        if (this.count == this.loopLimit) {
            this.count = (short) 0;
            EffectObj eff = new EffectObj();
            eff.ID = this.ID;
            eff.idPlayer = this.idPlayer;
            eff.style = this.style;
            switch (this.loopType) {
                case 0:
                    eff.x = this.x;
                    eff.y = this.y;
                    break;
                case 1:
                    int dis = Res.rnd(this.radius);
                    int angle = Res.rnd(360);
                    int xa = (dis * Util.cos(Util.fixangle(angle))) >> 10;
                    int ya = (-(dis * Util.sin(Util.fixangle(angle)))) >> 10;
                    eff.x = this.x;
                    eff.y = this.y;
                    eff.dx = (short) xa;
                    eff.dy = (short) ya;
                    break;
                case 2:
                    eff.x = this.x;
                    eff.y = this.y;
                    if (this.style == 0) {
                        eff.dx = this.xLoop[this.indexPos];
                        eff.dy = this.yLoop[this.indexPos];
                    } else {
                        eff.x = (short) (eff.x + this.xLoop[this.indexPos]);
                        eff.y = (short) (eff.y + this.yLoop[this.indexPos]);
                    }
                    break;
            }
            this.indexLoop = (short) (this.indexLoop + 1);
            this.indexPos = (short) (this.indexPos + 1);
            if (this.xLoop != null && this.indexPos >= this.xLoop.length) {
                this.indexPos = (short) 0;
            }
            if (this.loop != -1 && this.indexLoop >= this.loop) {
                GameScr.effManager.removeElement(this);
            }
            switch (this.style) {
                case 0:
                case 1:
                    Canvas.gameScr.actors.addElement(eff);
                    break;
            }
        }
        this.count = (short) (this.count + 1);
    }

    public static EffectData getEffect(short id) {
        for (int i = 0; i < effectList.size(); i++) {
            EffectData eff = (EffectData) effectList.elementAt(i);
            if (eff.ID == id) {
                return eff;
            }
        }
        return null;
    }
}
