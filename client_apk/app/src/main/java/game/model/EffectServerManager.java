package game.model;

import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Vector;

public class EffectServerManager {
    public static Vector effectList = new Vector();
    public short ID;
    public short count = 0;
    public short idPlayer;
    public short indexLoop;
    public short indexPos;
    public short loop;
    public short loopLimit;
    public byte loopType;
    public short radius;
    public byte style;
    public short x;
    public short[] xLoop;
    public short y;
    public short[] yLoop;

    public void update() {
        if (getEffect(this.ID) == null) {
            return;
        }
        if (this.style == 0) {
            Actor actorFindActorByID = GCanvas.gameScr.findActorByID(this.idPlayer);
            this.x = actorFindActorByID.x;
            this.y = actorFindActorByID.y;
        }
        if (this.count == this.loopLimit) {
            this.count = (short) 0;
            EffectObj effectObj = new EffectObj();
            effectObj.ID = this.ID;
            effectObj.idPlayer = this.idPlayer;
            effectObj.style = this.style;
            byte b = this.loopType;
            if (b == 0) {
                effectObj.x = this.x;
                effectObj.y = this.y;
            } else if (b == 1) {
                int iRnd = Res.rnd(this.radius);
                int iRnd2 = Res.rnd(360);
                int iCos = (Util.cos(Util.fixangle(iRnd2)) * iRnd) >> 10;
                int i = (-(iRnd * Util.sin(Util.fixangle(iRnd2)))) >> 10;
                effectObj.x = this.x;
                effectObj.y = this.y;
                effectObj.dx = (short) iCos;
                effectObj.dy = (short) i;
            } else if (b == 2) {
                effectObj.x = this.x;
                effectObj.y = this.y;
                if (this.style == 0) {
                    effectObj.dx = this.xLoop[this.indexPos];
                    effectObj.dy = this.yLoop[this.indexPos];
                } else {
                    effectObj.x = (short) (effectObj.x + this.xLoop[this.indexPos]);
                    effectObj.y = (short) (effectObj.y + this.yLoop[this.indexPos]);
                }
            }
            short s = (short) (this.indexLoop + 1);
            this.indexLoop = s;
            short s2 = (short) (this.indexPos + 1);
            this.indexPos = s2;
            short[] sArr = this.xLoop;
            if (sArr != null && s2 >= sArr.length) {
                this.indexPos = (short) 0;
            }
            short s3 = this.loop;
            if (s3 != -1 && s >= s3) {
                GameScr.effManager.removeElement(this);
            }
            byte b2 = this.style;
            if (b2 == 0 || b2 == 1) {
                GCanvas.gameScr.actors.addElement(effectObj);
            }
        }
        this.count = (short) (this.count + 1);
    }

    public static EffectData getEffect(short s) {
        int size = effectList.size();
        for (int i = 0; i < size; i++) {
            EffectData effectData = (EffectData) effectList.elementAt(i);
            if (effectData.ID == s) {
                return effectData;
            }
        }
        return null;
    }
}
