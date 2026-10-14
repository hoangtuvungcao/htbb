package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Position;
import game.model.arrow.Arrow;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class ChangScr extends Screen {
    private static ChangScr me;
    int ang;
    int dis;
    int index;
    int lastTime;
    Vector list = new Vector();
    private int map = -1;
    int trans;
    int x;
    private int xMap;
    int y;
    private int yMap;

    public static ChangScr gI() {
        ChangScr changScr = me;
        if (changScr != null) {
            return changScr;
        }
        ChangScr changScr2 = new ChangScr();
        me = changScr2;
        return changScr2;
    }

    public void setMap(int i, int i2, int i3) {
        this.map = i;
        this.xMap = i2;
        this.yMap = i3;
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
        int i = this.ang;
        int i2 = i + 15;
        this.ang = i2;
        if (i2 >= 360) {
            this.ang = i - 345;
        }
        int i3 = this.ang;
        int i4 = i3 - 15;
        if (i4 < 0) {
            i4 = i3 + 345;
        }
        int iCos = (this.dis * Util.cos(Util.fixangle(i3))) >> 10;
        int i5 = (-(this.dis * Util.sin(Util.fixangle(this.ang)))) >> 10;
        int iCos2 = (this.dis * Util.cos(Util.fixangle(i4))) >> 10;
        int i6 = (-(this.dis * Util.sin(Util.fixangle(i4)))) >> 10;
        int i7 = this.dis;
        if (i7 < 30) {
            this.dis = i7 + 1;
        }
        this.x = GCanvas.hw + iCos;
        this.y = GCanvas.hh + i5;
        int iFindDirIndexFromAngle = Arrow.findDirIndexFromAngle(Util.angle(this.x - (GCanvas.hw + iCos2), -(this.y - (GCanvas.hh + i6))));
        this.index = Arrow.FRAME[iFindDirIndexFromAngle];
        this.trans = Arrow.TRANSFORM[iFindDirIndexFromAngle];
        this.list.addElement(new Position(this.x, this.y));
        for (int i8 = 0; i8 < this.list.size(); i8++) {
            Position position = (Position) this.list.elementAt(i8);
            position.index = (short) (position.index + 1);
            if (position.index >= 20) {
                this.list.removeElement(position);
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.setColor(0);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        int size = this.list.size();
        for (int i = 0; i < size; i++) {
            Position position = (Position) this.list.elementAt(i);
            if (Res.imgEffect[8] == null) {
                Res.getImgEffect(8);
            }
            graphics.drawRegion(Res.imgEffect[8], 0, (position.index / 2) * 10, 10, 10, 0, position.x, position.y, 3);
        }
        if (Res.imgArrow[3] == null) {
            Res.loadImgArrow(3);
        }
        if (Res.imgArrow[3] != null) {
            graphics.drawRegion(Res.imgArrow[3], 0, this.index * 24, 24, 24, this.trans, this.x, this.y, 3);
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
