package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class AnimateEffect extends Effect {
    private static final byte RAIN = 0;
    private static final byte FALLING_LEAVES = 1;
    private static final byte SNOW = 2;
    private static final byte FALLING_FLOWER = 3;
    private static final byte FIREWORK = 4;
    private byte type;
    private int number;
    private int timeLimit;
    public boolean isStop;
    private static int countWind;
    private short idImg;
    private static int wind = 5;
    private static int dirWind = Res.rnd(1, -1);
    Vector list = new Vector();
    private int curTime = (int) (System.currentTimeMillis() / 1000);

    public void close() {
        GameScr.effAnimate.removeElement(this);
    }

    public void stop() {
        this.isStop = true;
    }

    public AnimateEffect(byte type, boolean isStart, int number, int timeLimit) {
        Point pos;
        this.type = (byte) 0;
        this.number = 0;
        this.timeLimit = timeLimit;
        this.type = type;
        this.number = number;
        if (type == game.pokemon.SinhThai.SUONG_MU || type == game.pokemon.SinhThai.NANG_GAT) {
            // pokemon (hệ sinh thái): sương mù / nắng gắt = lớp phủ vẽ bằng code, không có hạt
            this.number = 0;
            return;
        }
        switch (type) {
            case 1:
                this.idImg = (short) 801;
                break;
            case 2:
                this.idImg = (short) 805;
                break;
            case 3:
                this.idImg = (short) 802;
                this.type = (byte) 3;
                break;
            case 4:
                this.idImg = (short) 803;
                this.type = (byte) 3;
                break;
            case 5:
                this.idImg = (short) 804;
                this.type = (byte) 3;
                break;
        }
        for (int i = 0; i < number; i++) {
            if (isStart) {
                pos = new Point(((GameScr.cmx - Canvas.hw) + Res.rnd(Canvas.w * 2)) * 10, ((GameScr.cmy - (Canvas.h * 2)) + Res.rnd(Canvas.h * 2)) * 10);
            } else {
                pos = new Point();
                rndPos(pos);
            }
            if (type == 2 || this.type == 3) {
                pos.h = Res.rnd(3);
            } else {
                pos.h = Res.rnd(4);
            }
            pos.limitY = 16 + (Res.rnd(3) * 4);
            pos.v = Res.rnd(-1, 1);
            pos.color = Res.rnd(pos.limitY);
            pos.dis = (byte) Res.rnd(20);
            this.list.addElement(pos);
        }
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        g.translate(-GameScr.cmx, -GameScr.cmy);
        switch (this.type) {
            case 0:
                paintRain(g);
                break;
            case 1:
                paintFallingLeaves(g);
                break;
            case 2:
                paintSnow(g);
                break;
            case 3:
                paintFallingFlower(g);
                break;
            case 6:
                game.pokemon.SinhThai.veSuongMu(g, this.doDam);
                break;
            case 7:
                game.pokemon.SinhThai.veNangGat(g, this.doDam);
                break;
        }
    }

    /** Độ đậm lớp phủ 6/7 (0..256): hiện dần khi bắt đầu, mờ dần rồi đóng khi dừng. */
    private int doDam;

    private void paintSnow(Graphics g) {
        Image img = null;
        if (GameData.getImgIcon(this.idImg) != null) {
            img = GameData.getImgIcon(this.idImg).img;
        }
        if (img != null) {
            for (int i = 0; i < this.number; i++) {
                Point pos = (Point) this.list.elementAt(i);
                if (pos.x / 10 > GameScr.cmx && pos.x / 10 < GameScr.cmx + Canvas.w && pos.y / 10 > GameScr.cmy && GameData.getImgIcon(this.idImg) != null) {
                    g.drawRegion(img, 0, (2 - pos.h) * 5, 5, 5, 0, pos.x / 10, pos.y / 10, 3);
                }
            }
        }
    }

    private void paintFallingFlower(Graphics g) {
        Image img = null;
        if (GameData.getImgIcon(this.idImg) != null) {
            img = GameData.getImgIcon(this.idImg).img;
        }
        if (img != null) {
            for (int i = 0; i < this.number; i++) {
                Point pos = (Point) this.list.elementAt(i);
                if (pos.x / 10 > GameScr.cmx && pos.x / 10 < GameScr.cmx + Canvas.w && pos.y / 10 > GameScr.cmy) {
                    int aa = (2 - pos.h) + 1;
                    if (aa < 2) {
                        aa = pos.dis / 10;
                    }
                    g.drawRegion(img, 0, aa * 10, 10, 10, 0, pos.x / 10, pos.y / 10, 3);
                    pos.dis = (byte) (pos.dis + 1);
                    if (pos.dis >= 20) {
                        pos.dis = (byte) 0;
                    }
                }
            }
        }
    }

    private void paintFallingLeaves(Graphics g) {
        Image img = null;
        if (GameData.getImgIcon(this.idImg) != null) {
            img = GameData.getImgIcon(this.idImg).img;
        }
        if (img != null) {
            for (int i = 0; i < this.number; i++) {
                Point p = (Point) this.list.elementAt(i);
                if (p.x / 10 > GameScr.cmx && p.x / 10 < GameScr.cmx + Canvas.w && p.y / 10 > GameScr.cmy) {
                    g.drawRegion(img, 0, (p.color / (p.limitY / 4)) * 10, 16, 10, 0, p.x / 10, p.y / 10, 3);
                }
            }
        }
    }

    private void paintRain(Graphics g) {
        g.setColor(14540253);
        for (int i = 0; i < this.number; i++) {
            Point pos = (Point) this.list.elementAt(i);
            if (pos.x / 10 > GameScr.cmx && pos.x / 10 < GameScr.cmx + Canvas.w && pos.y / 10 > GameScr.cmy) {
                g.fillRect(pos.x / 10, pos.y / 10, 1, pos.h + 2);
            }
        }
    }

    public static void updateWind() {
        int aa = 1;
        if (Canvas.gameTick % 6 == 3) {
            aa = Res.rnd(15);
        }
        if (aa == 0 && wind == 5) {
            wind = 5 + Res.rnd(20);
            countWind = 50 + Res.rnd(100);
        }
        if (countWind > 0) {
            countWind--;
        }
        if (countWind == 0 && wind > 5 && Canvas.gameTick % 4 == 2) {
            wind--;
        }
    }

    @Override
    public void update() {
        if (this.timeLimit > 0 && (System.currentTimeMillis() / 1000) - ((long) this.curTime) > this.timeLimit) {
            this.isStop = true;
        }
        switch (this.type) {
            case 0:
                updateRain();
                break;
            case 1:
                updateFallingLeaves();
                break;
            case 2:
                updateSnow();
                break;
            case 3:
                updateFlower();
                break;
            case 6:
            case 7:
                if (this.isStop) {
                    this.doDam -= 16;
                    if (this.doDam <= 0) {
                        close();
                    }
                } else if (this.doDam < 256) {
                    this.doDam += 10;
                    if (this.doDam > 256) {
                        this.doDam = 256;
                    }
                }
                break;
        }
    }

    private void updateSnow() {
        for (int i = 0; i < this.number; i++) {
            Point pos = (Point) this.list.elementAt(i);
            pos.y += (pos.h + 4) * 3;
            pos.x += ((pos.h + 1) * 2) + (wind * dirWind);
            if (pos.y / 10 < GameScr.cmy - Canvas.hw || pos.y / 10 > (GameScr.cmy + (Canvas.h + Canvas.hh)) - ((4 - pos.h) * 50) || pos.x / 10 < GameScr.cmx - Canvas.hw || pos.x / 10 > GameScr.cmx + Canvas.w + Canvas.hw) {
                rndPos(pos);
            }
        }
    }

    private void updateFlower() {
        for (int i = 0; i < this.number; i++) {
            Point pos = (Point) this.list.elementAt(i);
            pos.y += (pos.h + 2) * 5;
            pos.x += ((pos.h + 1) * 2) + (wind * dirWind);
            if (pos.y / 10 < GameScr.cmy - Canvas.hw || pos.y / 10 > (GameScr.cmy + (Canvas.h + Canvas.hh)) - ((4 - pos.h) * 50) || pos.x / 10 < GameScr.cmx - Canvas.hw || pos.x / 10 > GameScr.cmx + Canvas.w + Canvas.hw) {
                rndPos(pos);
            }
        }
    }

    private void updateFallingLeaves() {
        for (int i = 0; i < this.number; i++) {
            Point pos = (Point) this.list.elementAt(i);
            pos.y += 10;
            pos.x += (pos.v * 10) + (wind * dirWind);
            pos.color++;
            if (pos.color >= pos.limitY) {
                pos.color = 0;
            }
            if (pos.y / 10 < GameScr.cmy - Canvas.hw || pos.y / 10 > (GameScr.cmy + (Canvas.h + Canvas.hh)) - ((4 - pos.h) * 50) || pos.x / 10 < GameScr.cmx - Canvas.hw || pos.x / 10 > GameScr.cmx + Canvas.w + Canvas.hw) {
                rndPos(pos);
            }
        }
    }

    private void updateRain() {
        for (int i = 0; i < this.number; i++) {
            Point pos = (Point) this.list.elementAt(i);
            pos.y += ((pos.h + 1) * 15) + ((3 - pos.h) * 3);
            pos.g++;
            pos.x += (((3 - pos.h) + 1) * 2) + (wind * dirWind);
            if (pos.y / 10 < GameScr.cmy - Canvas.hw || pos.y / 10 > (GameScr.cmy + (Canvas.h + Canvas.hh)) - ((4 - pos.h) * 50) || pos.x / 10 < GameScr.cmx - Canvas.hw || pos.x / 10 > GameScr.cmx + Canvas.w + Canvas.hw) {
                rndPos(pos);
            }
        }
    }

    private void rndPos(Point pos) {
        if (this.isStop) {
            this.list.removeElement(pos);
            this.number = this.list.size();
            if (this.list.size() == 0) {
                close();
                return;
            }
            return;
        }
        pos.y = ((GameScr.cmy - Canvas.hh) + Res.rnd(Canvas.h * 2)) * 10;
        pos.x = ((GameScr.cmx - Canvas.hw) + Res.rnd(Canvas.w * 2)) * 10;
        if (this.type == 2 || this.type == 3) {
            pos.h = Res.rnd(3);
        } else {
            pos.h = Res.rnd(4);
        }
    }
}
