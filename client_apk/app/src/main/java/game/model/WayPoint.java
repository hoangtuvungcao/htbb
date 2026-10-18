package game.model;

import game.core.j2me.Graphics;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.ChangScr;
import game.render.screen.GameScr;
import java.util.Vector;

public class WayPoint extends Effect {
    private static int[] color = {-1770501, -5179144, -8782853, -12715013, -393606, -95110};
    public byte clazz;
    boolean isJoin;
    boolean isWayPoint;
    private int index = 0;
    Vector list = new Vector();

    public WayPoint(int i, int i2, short s, byte b, boolean z) {
        this.clazz = (byte) 0;
        this.isJoin = false;
        this.isWayPoint = z;
        this.x = i;
        this.y = i2;
        this.type = s;
        this.clazz = b;
        this.isJoin = false;
    }

    @Override
    public void update() {
        if (this.type != 0 && this.x + 20 > GameScr.cmx && this.x - 20 < GameScr.cmx + GCanvas.w && this.y > GameScr.cmy && this.y < GameScr.cmy + GCanvas.h) {
            for (int i = 0; i < 2; i++) {
                int iRnd = Res.rnd(360);
                int iCos = (Util.cos(Util.fixangle(iRnd)) * 18) >> 10;
                int i2 = (-(Util.sin(Util.fixangle(iRnd)) * 18)) >> 10;
                Position position = new Position(this.x + iCos, this.y + i2 + (-(i2 / 3)), (byte) (Res.rnd(20) + 15));
                position.dir = (byte) Res.rnd(20);
                this.list.addElement(position);
            }
        }
        if (this.isWayPoint) {
            if (this.type != GCanvas.gameScr.map) {
                byte b = this.clazz;
                if (b == 1) {
                    EffectManager.hiEffects.removeAllElements();
                    return;
                } else {
                    if (b == 0) {
                        EffectManager.lowEffects.removeAllElements();
                        return;
                    }
                    return;
                }
            }
            if (this.isJoin || Util.distance(this.x, this.y, GCanvas.gameScr.mainChar.x, GCanvas.gameScr.mainChar.y) >= 17) {
                return;
            }
            GameService.gI().doWayPoint(this.x, this.y, this.type);
            GCanvas.gameScr.focusedActor = null;
            ChangScr.gI().switchToMe();
            this.isJoin = true;
        }
    }

    public void paintEff(Graphics graphics) {
        paint(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.x + 20 <= GameScr.cmx || this.x - 20 >= GameScr.cmx + GCanvas.w || this.y <= GameScr.cmy || this.y - 40 >= GameScr.cmy + GCanvas.h) {
            return;
        }
        if (this.clazz == 0) {
            graphics.drawRegion(Res.getWayPoint(), 0, (this.index / 6) * 24, 39, 24, 0, this.x, this.y, 3);
            int i = this.index + 1;
            this.index = i;
            if (i >= 12) {
                this.index = 0;
                return;
            }
            return;
        }
        int size = this.list.size();
        for (int i2 = 0; i2 < size; i2++) {
            Position position = (Position) this.list.elementAt(i2);
            if (position.dir == 5) {
                graphics.setColor(color[4]);
            } else if (position.dir == 15) {
                graphics.setColor(color[5]);
            } else {
                graphics.setColor(color[position.follow / 10]);
            }
            graphics.fillRect(position.x, position.y - ((position.follow / 3) + 1), 1, (position.follow / 3) + 1);
            position.y -= 2;
            position.follow = (byte) (position.follow - 1);
            if (position.follow < 0) {
                this.list.removeElementAt(i2);
            }
        }
    }
}
