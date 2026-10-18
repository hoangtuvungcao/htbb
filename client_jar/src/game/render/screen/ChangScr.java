package game.render.screen;

import game.model.Position;
import game.model.arrow.Arrow;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class ChangScr extends Screen {
    private static ChangScr me;
    int x;
    int y;
    int index;
    int trans;
    int ang;
    int dis;
    int lastTime;
    Vector list = new Vector();
    private int map = -1;
    private int xMap;
    private int yMap;

    public static ChangScr gI() {
        if (me != null) {
            return me;
        }
        ChangScr changScr = new ChangScr();
        me = changScr;
        return changScr;
    }

    public void setMap(int map, int xMap, int yMap) {
        this.map = map;
        this.xMap = xMap;
        this.yMap = yMap;
    }

    @Override
    public void update() {
        if (this.map > -1 && (System.currentTimeMillis() / 1000) - ((long) this.lastTime) > 30) {
            this.lastTime = (int) (System.currentTimeMillis() / 1000);
            GameService.gI().changeMap(this.map, this.xMap, this.yMap);
        }
        updateWaiting();
    }

    private void updateWaiting() {
        this.ang += 15;
        if (this.ang >= 360) {
            this.ang -= 360;
        }
        int ang0 = this.ang - 15;
        if (ang0 < 0) {
            ang0 += 360;
        }
        int xa = (this.dis * Util.cos(Util.fixangle(this.ang))) >> 10;
        int ya = (-(this.dis * Util.sin(Util.fixangle(this.ang)))) >> 10;
        int xb = (this.dis * Util.cos(Util.fixangle(ang0))) >> 10;
        int yb = (-(this.dis * Util.sin(Util.fixangle(ang0)))) >> 10;
        if (this.dis < 30) {
            this.dis++;
        }
        this.x = Canvas.hw + xa;
        this.y = Canvas.hh + ya;
        int d = Arrow.findDirIndexFromAngle(Util.angle(this.x - (Canvas.hw + xb), -(this.y - (Canvas.hh + yb))));
        this.index = Arrow.FRAME[d];
        this.trans = Arrow.TRANSFORM[d];
        this.list.addElement(new Position(this.x, this.y));
        for (int i = 0; i < this.list.size(); i++) {
            Position p = (Position) this.list.elementAt(i);
            p.index = (short) (p.index + 1);
            if (p.index >= 20) {
                this.list.removeElement(p);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(0);
        g.fillRect(0, 0, Canvas.w, Canvas.h);
        for (int i = 0; i < this.list.size(); i++) {
            Position p = (Position) this.list.elementAt(i);
            if (Res.getImageEffect(8) != null) {
                g.drawRegion(Res.getImageEffect(8), 0, (p.index / 2) * 10, 10, 10, 0, p.x, p.y, 3);
            }
        }
        if (Res.getImgArrow(3) != null) {
            g.drawRegion(Res.getImgArrow(3), 0, this.index * 24, 24, 24, this.trans, this.x, this.y, 3);
        }
    }

    @Override
    public void switchToMe() {
        this.ang = 0;
        this.dis = 5;
        this.list.removeAllElements();
        this.lastTime = (int) (System.currentTimeMillis() / 1000);
        super.switchToMe();
    }
}
