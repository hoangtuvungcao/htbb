package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Lightning extends Effect {
    private static int[] color = {16579837, 11188220};
    public Vector[] list;
    private Position posangle;
    public Vector listPos = new Vector();
    long countDown = 0;
    long timeDel = 0;
    public boolean isContinue = false;
    public boolean isRemove = true;
    int cou = 0;
    int dem = 0;
    int aa = 7;

    public void reset() {
        this.countDown = System.currentTimeMillis() / 10;
        this.timeDel = System.currentTimeMillis() / 10;
        this.listPos.removeAllElements();
    }

    public void setInfo(Vector position, Position angle, boolean isContinue) {
        if (position.size() == 0) {
            return;
        }
        this.isContinue = isContinue;
        if (!isContinue) {
            orderVector(position);
        }
        this.listPos = position;
        this.posangle = angle;
        this.list = new Vector[position.size()];
        for (int i = 0; i < this.list.length; i++) {
            this.list[i] = new Vector();
        }
        angle.follow = (byte) -1;
        this.list[0].addElement(angle);
        int index = -1;
        for (int i2 = 0; i2 < position.size(); i2++) {
            int x = angle.x;
            int y = angle.y;
            if (isContinue && index != -1) {
                Position pCon = (Position) position.elementAt(index);
                x = pCon.x;
                y = pCon.y;
            }
            if (!isContinue) {
                index = rndIndexList(position);
            } else {
                index++;
            }
            rndIndex(position, 1);
            int follow = this.list[index].size() - 1;
            Position posTo = (Position) position.elementAt(index);
            int angle1 = Util.angle(posTo.x - x, -(posTo.y - y));
            int disPart = Res.rnd(15) + 10;
            int a = 0;
            while (true) {
                int ang = 0;
                if (a != 0) {
                    ang = (angle1 - 5) + Res.rnd(10);
                }
                int ang2 = Util.fixangle(ang);
                int xa = ((disPart * a) * Util.cos(ang2)) >> 10;
                int ya = (-((disPart * a) * Util.sin(ang2))) >> 10;
                int i3 = follow;
                follow++;
                Position posNext = new Position(x + xa, y + ya, i3);
                this.list[index].addElement(posNext);
                if (Util.distance(x, y, x + xa, y + ya) >= Util.distance(x, y, posTo.x, posTo.y) - 20) {
                    break;
                } else {
                    a++;
                }
            }
        }
        for (int i4 = 0; i4 < this.list.length; i4++) {
            int limit = this.list[i4].size();
            Position pa = (Position) position.elementAt(i4);
            pa.follow = (byte) (this.list[i4].size() - 1);
            pa.index = (short) -1;
            Position pz = new Position(pa.x, pa.y, pa.follow);
            pz.index = (short) -1;
            this.list[i4].addElement(pz);
            for (int a2 = 1; a2 < limit; a2++) {
                Position p = (Position) this.list[i4].elementAt(a2);
                int rnd = Res.rnd(2);
                for (int b = 0; b < rnd; b++) {
                    int ang3 = 180 + Res.rnd(180);
                    int dis = 5 + Res.rnd(10);
                    Position po = new Position(p.x + ((dis * Util.cos(Util.fixangle(ang3))) >> 10), p.y + ((-(dis * Util.sin(Util.fixangle(ang3)))) >> 10), a2);
                    po.index = (short) 0;
                    this.list[i4].addElement(po);
                }
            }
        }
    }

    public static Vector orderVector(Vector obj) {
        int a = obj.size();
        for (int i = 0; i < a - 1; i++) {
            Position o = (Position) obj.elementAt(i);
            for (int j = i + 1; j < a; j++) {
                Position o1 = (Position) obj.elementAt(j);
                if (o.x > o1.x) {
                    obj.setElementAt(o, j);
                    obj.setElementAt(o1, i);
                    o = o1;
                }
            }
        }
        return obj;
    }

    private int rndIndex(Vector pos, int chieu) {
        int num = 0;
        for (int i = 0; i < pos.size(); i++) {
            Position p = (Position) pos.elementAt(i);
            if (p.index != -1) {
                num++;
            }
        }
        if (num != 0) {
            int num2 = Res.rnd(num);
            int num1 = 0;
            for (int i2 = 0; i2 < pos.size(); i2++) {
                Position p2 = (Position) pos.elementAt(i2);
                if (p2.index != -1) {
                    num1++;
                    if (num2 == num1) {
                        return i2;
                    }
                }
            }
            return -1;
        }
        return -1;
    }

    private int rndIndexList(Vector pos) {
        int num = 0;
        for (int i = 0; i < pos.size(); i++) {
            if (((Position) pos.elementAt(i)).index == -1) {
                num++;
            }
        }
        if (num != 0) {
            int num2 = Res.rnd(num);
            int num1 = 0;
            for (int i2 = 0; i2 < pos.size(); i2++) {
                Position p = (Position) pos.elementAt(i2);
                if (p.index == -1) {
                    if (num2 == num1) {
                        p.index = (short) 0;
                        return i2;
                    }
                    num1++;
                }
            }
            return -1;
        }
        return -1;
    }

    @Override
    public void update() {
        if (Canvas.gameTick % 2 == 1) {
            this.posangle.follow = (byte) -1;
            this.posangle.index = (short) -1;
            for (int i = 0; i < this.listPos.size(); i++) {
                Position pos = (Position) this.listPos.elementAt(i);
                pos.index = (short) -1;
                pos.follow = (byte) -1;
            }
            if (this.isContinue && this.isRemove && this.listPos.size() > 1 && (System.currentTimeMillis() / 10) - this.timeDel > 30) {
                this.timeDel = System.currentTimeMillis() / 10;
                this.posangle = (Position) this.listPos.elementAt(0);
                this.listPos.removeElementAt(0);
            }
            setInfo(this.listPos, this.posangle, this.isContinue);
            if ((System.currentTimeMillis() / 10) - this.countDown > 60 + (this.isContinue ? 20 : 0)) {
                this.aa = 7;
                EffectManager.hiEffects.removeElement(this);
            }
            this.countDown++;
        }
    }

    @Override
    public void paint(Graphics g) {
        this.dem = 0;
        Res.imgSmoke.drawFrame(this.cou / 3, this.posangle.x, this.posangle.y, 0, 3, g);
        this.cou++;
        if (this.cou >= 12) {
            this.cou = 0;
        }
        if (this.list != null) {
            for (int i = 0; i < this.list.length; i++) {
                for (int a = 0; a < this.list[i].size(); a++) {
                    Position pos0 = (Position) this.list[i].elementAt(a);
                    if (pos0.follow >= 0 && pos0.follow < this.list[i].size()) {
                        Position pos1 = (Position) this.list[i].elementAt(pos0.follow);
                        paintLine(g, pos0, pos1);
                        if (this.isContinue && this.isRemove) {
                            this.dem++;
                            if (this.dem >= this.aa) {
                                this.aa += 7;
                                return;
                            }
                        }
                    }
                }
                Position p = (Position) this.listPos.elementAt(i);
                Res.loadImgEff(32);
                if (Res.backSmoke != null) {
                    Res.backSmoke.drawFrame(1 + (p.count / 4), p.x, p.y, 0, 3, g);
                }
                p.count = (byte) (p.count + 1);
                if (p.count >= 12) {
                    p.count = (byte) 0;
                }
            }
        }
    }

    private void paintLine(Graphics g, Position pos1, Position pos2) {
        g.setColor(color[0]);
        g.drawLine(pos1.x, pos1.y, pos2.x, pos2.y);
        if (pos1.index == -1) {
            g.setColor(color[1]);
            g.drawLine(pos1.x - 1, pos1.y, pos2.x - 1, pos2.y);
            if (this.isContinue && this.isRemove) {
                g.drawLine(pos1.x + 1, pos1.y, pos2.x + 1, pos2.y);
            }
        }
    }
}
