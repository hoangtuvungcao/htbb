package game.model;

import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class MonsterVanTieu extends Monster {
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{0, 0, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6}, new byte[]{7, 7, 7, 7, 7, 7, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13}, new byte[]{14, 14, 14, 14, 14, 14, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20}, new byte[]{14, 14, 14, 14, 14, 14, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20}};
    byte[][] INJURE_FRAME = {new byte[3], new byte[]{7, 7, 7}, new byte[]{14, 14, 14}, new byte[]{14, 14, 14}};
    byte[][] STAND_FRAME = {new byte[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3}, new byte[]{7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10}, new byte[]{14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17}, new byte[]{14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17}};
    byte totalExp = 0;
    private byte skillID = 0;

    @Override
    public void paint(Graphics g) {
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y - 20, 3);
            }
            paintExp(g);
        }
        super.paintEffSkill(g);
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        try {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 8 * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, 35, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 25, 0);
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
        } catch (Exception e) {
            Cout.println(" loi tai paaintCorner monter");
        }
    }

    @Override
    public void update() {
        updateExp();
        switch (this.state) {
            case 0:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.STAND_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.STAND_FRAME[this.dir][this.p1];
                break;
            case 2:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.MOVE_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.MOVE_FRAME[this.dir][this.p1];
                if (!this.isFreeze) {
                    move();
                }
                break;
            case 4:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.INJURE_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.INJURE_FRAME[this.dir][this.p1];
                break;
            case 5:
                this.frame = (byte) 0;
                createExp();
                Canvas.gameScr.actors.removeElement(this);
                break;
        }
        updateEffSkill();
    }

    @Override
    public void jump() {
    }

    @Override
    public void jumpVang(Actor causeBy) {
        super.jumpVang(causeBy);
    }

    private void paintExp(Graphics g) {
        int size = this.exp.size();
        for (int i = 0; i < size; i++) {
            Explode ex = (Explode) this.exp.elementAt(i);
            if (ex != null) {
                ex.paint(g);
            }
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Explode ex = (Explode) this.exp.elementAt(i);
            if (ex != null) {
                ex.update();
                if (ex.destroy) {
                    this.exp.removeElement(ex);
                    if (this.exp.size() == 0) {
                        this.wantDestroy = true;
                    }
                }
            }
        }
    }

    @Override
    public void startDeadFly(int dx, int dy) {
        this.state = (byte) 5;
    }

    private void createExp() {
    }

    @Override
    public void startAttack(Vector list, byte skillID) {
        this.listAttack = list;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.skillID = skillID;
        if (skillID == 1) {
            this.attackTarget = (LiveActor) list.elementAt(0);
        }
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }
}
