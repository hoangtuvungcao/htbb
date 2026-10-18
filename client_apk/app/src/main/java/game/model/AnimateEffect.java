package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.util.Vector;

public class AnimateEffect extends Effect {
    private static final byte FALLING_FLOWER_DAO = 3;
    private static final byte FALLING_FLOWER_MAI = 4;
    private static final byte FALLING_FLOWER_ROSE = 5;
    private static final byte FALLING_LEAVES = 1;
    private static final byte RAIN = 0;
    private static final byte SNOW = 2;
    private static int countWind = 0;
    private static int dirWind = Res.rnd(1, -1);
    private static int wind = 5;
    private short idImg;
    public boolean isStop;
    private int number;
    private int timeLimit;
    private byte type;
    Vector list = new Vector();
    private int curTime = (int) (System.currentTimeMillis() / 1000);

    public void close() {
        GameScr.effAnimate.removeElement(this);
    }

    public void stop() {
        this.isStop = true;
    }

    public AnimateEffect(byte b, boolean z, int i, int i2) {
        Point point;
        byte b2;
        this.type = (byte) 0;
        this.number = 0;
        this.timeLimit = i2;
        this.type = b;
        this.number = i;
        if (b == 1) {
            this.idImg = (short) 801;
        } else if (b == 2) {
            this.idImg = (short) 805;
        } else if (b == 3) {
            this.idImg = (short) 802;
            this.type = (byte) 3;
        } else if (b == 4) {
            this.idImg = (short) 803;
            this.type = (byte) 4;
        } else if (b == 5) {
            this.idImg = (short) 804;
            this.type = (byte) 5;
        }
        for (int i3 = 0; i3 < i; i3++) {
            if (z) {
                point = new Point(((GameScr.cmx - GCanvas.hw) + Res.rnd(GCanvas.w * 2)) * 10, ((GameScr.cmy - (GCanvas.h * 2)) + Res.rnd(GCanvas.h * 2)) * 10);
            } else {
                point = new Point();
                rndPos(point);
            }
            if (b == 2 || (b2 = this.type) == 3 || b2 == 4 || b2 == 5) {
                point.h = Res.rnd(3);
            } else {
                point.h = Res.rnd(4);
            }
            point.limitY = (Res.rnd(3) * 4) + 16;
            point.v = Res.rnd(-1, 1);
            point.color = Res.rnd(point.limitY);
            point.dis = (byte) Res.rnd(20);
            this.list.addElement(point);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        graphics.translate(-GameScr.cmx, -GameScr.cmy);
        byte b = this.type;
        if (b == 0) {
            paintRain(graphics);
            return;
        }
        if (b == 1) {
            paintFallingLeaves(graphics);
            return;
        }
        if (b == 2) {
            paintSnow(graphics);
        } else if (b == 3 || b == 4 || b == 5) {
            paintFallingFlower(graphics);
        }
    }

    private void paintSnow(Graphics graphics) {
        Bitmap bitmap = GameData.getImgIcon(this.idImg) != null ? GameData.getImgIcon(this.idImg).img : null;
        if (bitmap != null) {
            for (int i = 0; i < this.number; i++) {
                Point point = (Point) this.list.elementAt(i);
                if (point.x / 10 > GameScr.cmx && point.x / 10 < GameScr.cmx + GCanvas.w && point.y / 10 > GameScr.cmy && GameData.getImgIcon(this.idImg) != null) {
                    graphics.drawRegion(bitmap, 0, (2 - point.h) * 5, 5, 5, 0, point.x / 10, point.y / 10, 3);
                }
            }
        }
    }

    private void paintFallingFlower(Graphics graphics) {
        Bitmap bitmap = GameData.getImgIcon(this.idImg) != null ? GameData.getImgIcon(this.idImg).img : null;
        if (bitmap != null) {
            for (int i = 0; i < this.number; i++) {
                Point point = (Point) this.list.elementAt(i);
                if (point.x / 10 > GameScr.cmx && point.x / 10 < GameScr.cmx + GCanvas.w && point.y / 10 > GameScr.cmy) {
                    int i2 = 3 - point.h;
                    if (i2 < 2) {
                        i2 = point.dis / 10;
                    }
                    graphics.drawRegion(bitmap, 0, i2 * 10, 10, 10, 0, point.x / 10, point.y / 10, 3);
                    point.dis = (byte) (point.dis + 1);
                    if (point.dis >= 20) {
                        point.dis = (byte) 0;
                    }
                }
            }
        }
    }

    private void paintFallingLeaves(Graphics graphics) {
        Bitmap bitmap = GameData.getImgIcon(this.idImg) != null ? GameData.getImgIcon(this.idImg).img : null;
        if (bitmap != null) {
            for (int i = 0; i < this.number; i++) {
                Point point = (Point) this.list.elementAt(i);
                if (point.x / 10 > GameScr.cmx && point.x / 10 < GameScr.cmx + GCanvas.w && point.y / 10 > GameScr.cmy) {
                    graphics.drawRegion(bitmap, 0, (point.color / (point.limitY / 4)) * 10, 16, 10, 0, point.x / 10, point.y / 10, 3);
                }
            }
        }
    }

    private void paintRain(Graphics graphics) {
        graphics.setColor(-2236963);
        for (int i = 0; i < this.number; i++) {
            Point point = (Point) this.list.elementAt(i);
            if (point.x / 10 > GameScr.cmx && point.x / 10 < GameScr.cmx + GCanvas.w && point.y / 10 > GameScr.cmy) {
                graphics.fillRect(point.x / 10, point.y / 10, 1, point.h + 2);
            }
        }
    }

    public static void updateWind() {
        if ((GCanvas.gameTick % 6 == 3 ? Res.rnd(15) : 1) == 0 && wind == 5) {
            wind = Res.rnd(20) + 5;
            countWind = Res.rnd(100) + 50;
        }
        int i = countWind;
        if (i > 0) {
            countWind = i - 1;
        }
        if (countWind == 0 && wind > 5 && GCanvas.gameTick % 4 == 2) {
            wind--;
        }
    }

    @Override
    public void update() {
        if (this.timeLimit > 0 && (System.currentTimeMillis() / 1000) - ((long) this.curTime) > this.timeLimit) {
            this.isStop = true;
        }
        byte b = this.type;
        if (b == 0) {
            updateRain();
            return;
        }
        if (b == 1) {
            updateFallingLeaves();
            return;
        }
        if (b == 2) {
            updateSnow();
        } else if (b == 3 || b == 4 || b == 5) {
            updateFlower();
        }
    }

    private void updateSnow() {
        for (int i = 0; i < this.number; i++) {
            Point point = (Point) this.list.elementAt(i);
            point.y += (point.h + 4) * 3;
            point.x += ((point.h + 1) * 2) + (wind * dirWind);
            if (point.y / 10 < GameScr.cmy - GCanvas.hw || point.y / 10 > (GameScr.cmy + (GCanvas.h + GCanvas.hh)) - ((4 - point.h) * 50) || point.x / 10 < GameScr.cmx - GCanvas.hw || point.x / 10 > GameScr.cmx + GCanvas.w + GCanvas.hw) {
                rndPos(point);
            }
        }
    }

    private void updateFlower() {
        for (int i = 0; i < this.number; i++) {
            Point point = (Point) this.list.elementAt(i);
            point.y += (point.h + 2) * 5;
            point.x += ((point.h + 1) * 2) + (wind * dirWind);
            if (point.y / 10 < GameScr.cmy - GCanvas.hw || point.y / 10 > (GameScr.cmy + (GCanvas.h + GCanvas.hh)) - ((4 - point.h) * 50) || point.x / 10 < GameScr.cmx - GCanvas.hw || point.x / 10 > GameScr.cmx + GCanvas.w + GCanvas.hw) {
                rndPos(point);
            }
        }
    }

    private void updateFallingLeaves() {
        for (int i = 0; i < this.number; i++) {
            Point point = (Point) this.list.elementAt(i);
            point.y += 10;
            point.x += (point.v * 10) + (wind * dirWind);
            point.color++;
            if (point.color >= point.limitY) {
                point.color = 0;
            }
            if (point.y / 10 < GameScr.cmy - GCanvas.hw || point.y / 10 > (GameScr.cmy + (GCanvas.h + GCanvas.hh)) - ((4 - point.h) * 50) || point.x / 10 < GameScr.cmx - GCanvas.hw || point.x / 10 > GameScr.cmx + GCanvas.w + GCanvas.hw) {
                rndPos(point);
            }
        }
    }

    private void updateRain() {
        for (int i = 0; i < this.number; i++) {
            Point point = (Point) this.list.elementAt(i);
            point.y += ((point.h + 1) * 15) + ((3 - point.h) * 3);
            point.g++;
            point.x += ((4 - point.h) * 2) + (wind * dirWind);
            if (point.y / 10 < GameScr.cmy - GCanvas.hw || point.y / 10 > (GameScr.cmy + (GCanvas.h + GCanvas.hh)) - ((4 - point.h) * 50) || point.x / 10 < GameScr.cmx - GCanvas.hw || point.x / 10 > GameScr.cmx + GCanvas.w + GCanvas.hw) {
                rndPos(point);
            }
        }
    }

    private void rndPos(Point point) {
        if (this.isStop) {
            this.list.removeElement(point);
            this.number = this.list.size();
            if (this.list.size() == 0) {
                close();
                return;
            }
            return;
        }
        point.y = ((GameScr.cmy - GCanvas.hh) + Res.rnd(GCanvas.h * 2)) * 10;
        point.x = ((GameScr.cmx - GCanvas.hw) + Res.rnd(GCanvas.w * 2)) * 10;
        byte b = this.type;
        if (b == 2 || b == 3 || b == 4 || b == 5) {
            point.h = Res.rnd(3);
        } else {
            point.h = Res.rnd(4);
        }
    }
}
