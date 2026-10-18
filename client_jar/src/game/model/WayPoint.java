package game.model;

import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.ChangScr;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class WayPoint extends Effect {
    public byte clazz;
    private static int[] color = {15006715, 11598072, 7994363, 4062203, 16383610, 16682106};
    boolean isWayPoint;
    boolean isJoin;
    private int index = 0;
    Vector list = new Vector();

    public WayPoint(int x, int y, short type, byte cla, boolean isWay) {
        this.clazz = (byte) 0;
        this.isWayPoint = false;
        this.isJoin = false;
        this.isWayPoint = isWay;
        this.x = x;
        this.y = y;
        this.type = type;
        this.clazz = cla;
        this.isJoin = false;
    }

    @Override
    public void update() {
        if (this.type != 0 && this.x + 20 > GameScr.cmx && this.x - 20 < GameScr.cmx + Canvas.w && this.y > GameScr.cmy && this.y < GameScr.cmy + Canvas.h) {
            for (int i = 0; i < 2; i++) {
                int angle = Res.rnd(360);
                int xa = (18 * Util.cos(Util.fixangle(angle))) >> 10;
                int ya = (-(18 * Util.sin(Util.fixangle(angle)))) >> 10;
                Position p = new Position(this.x + xa, this.y + ya + (-(ya / 3)), (byte) (15 + Res.rnd(20)));
                p.dir = (byte) Res.rnd(20);
                this.list.addElement(p);
            }
        }
        if (this.isWayPoint) {
            if (this.type != Canvas.gameScr.map) {
                if (this.clazz == 1) {
                    EffectManager.hiEffects.removeAllElements();
                    return;
                } else {
                    if (this.clazz == 0) {
                        EffectManager.lowEffects.removeAllElements();
                        return;
                    }
                    return;
                }
            }
            if (!this.isJoin && Util.distance(this.x, this.y, Canvas.gameScr.mainChar.x, Canvas.gameScr.mainChar.y) < 17) {
                GameService.gI().doWayPoint(this.x, this.y, this.type);
                Canvas.gameScr.focusedActor = null;
                ChangScr.gI().switchToMe();
                this.isJoin = true;
            }
        }
    }

    public void paintEff(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        if (this.x + 20 > GameScr.cmx && this.x - 20 < GameScr.cmx + Canvas.w && this.y > GameScr.cmy && this.y - 40 < GameScr.cmy + Canvas.h) {
            if (this.clazz == 0) {
                g.drawRegion(Res.getWayPoint(), 0, (this.index / 6) * 24, 39, 24, 0, this.x, this.y, 3);
                this.index++;
                if (this.index >= 12) {
                    this.index = 0;
                    return;
                }
                return;
            }
            for (int i = 0; i < this.list.size(); i++) {
                Position p = (Position) this.list.elementAt(i);
                if (p.dir == 5) {
                    g.setColor(color[4]);
                } else if (p.dir == 15) {
                    g.setColor(color[5]);
                } else {
                    g.setColor(color[p.follow / 10]);
                }
                g.fillRect(p.x, p.y - (1 + (p.follow / 3)), 1, 1 + (p.follow / 3));
                p.y -= 2;
                p.follow = (byte) (p.follow - 1);
                if (p.follow < 0) {
                    this.list.removeElementAt(i);
                }
            }
        }
    }
}
