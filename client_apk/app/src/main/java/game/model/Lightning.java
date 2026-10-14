package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Lightning extends Effect {
    private static int[] color = {-197379, -5588996};
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

    public void setInfo(Vector vector, Position position, boolean z) {
        int size = vector.size();
        if (size == 0) {
            return;
        }
        this.isContinue = z;
        if (!z) {
            orderVector(vector);
        }
        this.listPos = vector;
        this.posangle = position;
        this.list = new Vector[size];
        int i = 0;
        int i2 = 0;
        while (true) {
            Vector[] vectorArr = this.list;
            if (i2 >= vectorArr.length) {
                break;
            }
            vectorArr[i2] = new Vector();
            i2++;
        }
        int i3 = -1;
        position.follow = (byte) -1;
        this.list[0].addElement(position);
        int i4 = 0;
        int iRndIndexList = -1;
        while (true) {
            int i5 = 10;
            if (i4 >= size) {
                break;
            }
            int i6 = position.x;
            int i7 = position.y;
            if (z && iRndIndexList != i3) {
                Position position2 = (Position) vector.elementAt(iRndIndexList);
                int i8 = position2.x;
                i7 = position2.y;
                i6 = i8;
            }
            iRndIndexList = !z ? rndIndexList(vector) : iRndIndexList + 1;
            rndIndex(vector, 1);
            int size2 = this.list[iRndIndexList].size() - 1;
            Position position3 = (Position) vector.elementAt(iRndIndexList);
            int iAngle = Util.angle(position3.x - i6, -(position3.y - i7));
            int iRnd = Res.rnd(15) + 10;
            int i9 = i;
            while (true) {
                int iFixangle = Util.fixangle(i9 != 0 ? (iAngle - 5) + Res.rnd(i5) : i);
                int i10 = iRnd * i9;
                int iCos = i6 + ((Util.cos(iFixangle) * i10) >> 10);
                int i11 = ((-(i10 * Util.sin(iFixangle))) >> i5) + i7;
                int i12 = size2 + 1;
                this.list[iRndIndexList].addElement(new Position(iCos, i11, size2));
                if (Util.distance(i6, i7, iCos, i11) >= Util.distance(i6, i7, position3.x, position3.y) - 20) {
                    break;
                }
                i9++;
                size2 = i12;
                i = 0;
                i5 = 10;
            }
            i4++;
            i = 0;
            i3 = -1;
        }
        int length = this.list.length;
        for (int i13 = 0; i13 < length; i13++) {
            int size3 = this.list[i13].size();
            Position position4 = (Position) vector.elementAt(i13);
            position4.follow = (byte) (this.list[i13].size() - 1);
            position4.index = (short) -1;
            Position position5 = new Position(position4.x, position4.y, position4.follow);
            position5.index = (short) -1;
            this.list[i13].addElement(position5);
            for (int i14 = 1; i14 < size3; i14++) {
                Position position6 = (Position) this.list[i13].elementAt(i14);
                int iRnd2 = Res.rnd(2);
                for (int i15 = 0; i15 < iRnd2; i15++) {
                    int iRnd3 = Res.rnd(180) + 180;
                    int iRnd4 = Res.rnd(10) + 5;
                    Position position7 = new Position(position6.x + ((Util.cos(Util.fixangle(iRnd3)) * iRnd4) >> 10), position6.y + ((-(iRnd4 * Util.sin(Util.fixangle(iRnd3)))) >> 10), i14);
                    position7.index = (short) 0;
                    this.list[i13].addElement(position7);
                }
            }
        }
    }

    public static Vector orderVector(Vector vector) {
        int size = vector.size();
        int i = 0;
        while (i < size - 1) {
            Position position = (Position) vector.elementAt(i);
            int i2 = i + 1;
            for (int i3 = i2; i3 < size; i3++) {
                Position position2 = (Position) vector.elementAt(i3);
                if (position.x > position2.x) {
                    vector.setElementAt(position, i3);
                    vector.setElementAt(position2, i);
                    position = position2;
                }
            }
            i = i2;
        }
        return vector;
    }

    private int rndIndex(Vector vector, int i) {
        int size = vector.size();
        int i2 = 0;
        for (int i3 = 0; i3 < size; i3++) {
            if (((Position) vector.elementAt(i3)).index != -1) {
                i2++;
            }
        }
        if (i2 != 0) {
            int iRnd = Res.rnd(i2);
            int i4 = 0;
            for (int i5 = 0; i5 < size; i5++) {
                if (((Position) vector.elementAt(i5)).index != -1 && iRnd == (i4 = i4 + 1)) {
                    return i5;
                }
            }
        }
        return -1;
    }

    private int rndIndexList(Vector vector) {
        int size = vector.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            if (((Position) vector.elementAt(i2)).index == -1) {
                i++;
            }
        }
        if (i != 0) {
            int iRnd = Res.rnd(i);
            int i3 = 0;
            for (int i4 = 0; i4 < size; i4++) {
                Position position = (Position) vector.elementAt(i4);
                if (position.index == -1) {
                    if (iRnd == i3) {
                        position.index = (short) 0;
                        return i4;
                    }
                    i3++;
                }
            }
        }
        return -1;
    }

    @Override
    public void update() {
        if (GCanvas.gameTick % 2 == 1) {
            this.posangle.follow = (byte) -1;
            this.posangle.index = (short) -1;
            int size = this.listPos.size();
            for (int i = 0; i < size; i++) {
                Position position = (Position) this.listPos.elementAt(i);
                position.index = (short) -1;
                position.follow = (byte) -1;
            }
            if (this.isContinue && this.isRemove && size > 1 && (System.currentTimeMillis() / 10) - this.timeDel > 30) {
                this.timeDel = System.currentTimeMillis() / 10;
                this.posangle = (Position) this.listPos.elementAt(0);
                this.listPos.removeElementAt(0);
            }
            setInfo(this.listPos, this.posangle, this.isContinue);
            if ((System.currentTimeMillis() / 10) - this.countDown > (this.isContinue ? 20 : 0) + 60) {
                this.aa = 7;
                EffectManager.hiEffects.removeElement(this);
            }
            this.countDown++;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        this.dem = 0;
        Res.imgSmoke.drawFrame(this.cou / 3, this.posangle.x, this.posangle.y, 0, 3, graphics);
        int i = this.cou + 1;
        this.cou = i;
        if (i >= 12) {
            this.cou = 0;
        }
        if (this.list != null) {
            for (int i2 = 0; i2 < this.list.length; i2++) {
                for (int i3 = 0; i3 < this.list[i2].size(); i3++) {
                    Position position = (Position) this.list[i2].elementAt(i3);
                    if (position.follow >= 0 && position.follow < this.list[i2].size()) {
                        paintLine(graphics, position, (Position) this.list[i2].elementAt(position.follow));
                        if (this.isContinue && this.isRemove) {
                            int i4 = this.dem + 1;
                            this.dem = i4;
                            int i5 = this.aa;
                            if (i4 >= i5) {
                                this.aa = i5 + 7;
                                return;
                            }
                        }
                    }
                }
                Position position2 = (Position) this.listPos.elementAt(i2);
                Res.backSmoke.drawFrame((position2.count / 4) + 1, position2.x, position2.y, 0, 3, graphics);
                position2.count = (byte) (position2.count + 1);
                if (position2.count >= 12) {
                    position2.count = (byte) 0;
                }
            }
        }
    }

    private void paintLine(Graphics graphics, Position position, Position position2) {
        graphics.setColor(color[0]);
        graphics.drawLine(position.x, position.y, position2.x, position2.y);
        if (position.index == -1) {
            graphics.setColor(color[1]);
            graphics.drawLine(position.x - 1, position.y, position2.x - 1, position2.y);
            if (this.isContinue && this.isRemove) {
                graphics.drawLine(position.x + 1, position.y, position2.x + 1, position2.y);
            }
        }
    }
}
