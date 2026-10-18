package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.arrow.Arrow;
import game.model.skill.SkillAnimate;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_Squirtle_8x extends SkillAnimate {
    Vector mst;

    public Skill_Squirtle_8x(int skillWeapon) {
        super(skillWeapon);
        this.mst = new Vector();
    }

    public Skill_Squirtle_8x() {
        super(0);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void updateSkill(Char c) {
        c.frame = (byte) 4;
        updateStateBua(c, 4, 0);
        if (c.p1 == 13) {
            Random r = new Random(System.currentTimeMillis());
            int iNextInt = r.nextInt() % 20;
            if (c.dir == 0 || c.dir == 1) {
            }
            if (this.mst != null) {
                Effect_Loc_Xoay eff = new Effect_Loc_Xoay(c.x, c.y, this.mst, c);
                EffectManager.addLowEffect(eff);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    public class Effect_Loc_Xoay extends Effect {
        public byte frame;
        public int count;
        public int xto;
        public int yto;

        public int effect;
        public int id1;
        public LiveActor from;
        public int[] arr_radian = {0, 180};
        public int[] eff = {9};
        Vector mvector = new Vector();

        public Effect_Loc_Xoay(int x, int y, Vector mst, LiveActor from) {
            this.x = x;
            this.y = y;
            this.from = from;
            add(mst);
            this.id1 = 16;
        }

        private void add(Vector mst) {
            this.mvector.removeAllElements();
            for (int i = 0; i < mst.size(); i++) {
                Actor live = (Actor) mst.elementAt(i);
                Actor ac = Canvas.gameScr.findActorByID(live.ID);
                if (live != null) {
                    this.mvector.addElement(ac);
                }
            }
        }

        @Override
        public void paint(Graphics g) {
            Image img = Res.getImgArrow(this.id1);
            if (img != null) {
                for (int i = 0; i < this.arr_radian.length; i++) {
                    g.drawRegion(img, 0, this.frame * Arrow.ARROWSIZE[1][this.id1], Arrow.ARROWSIZE[0][this.id1], Arrow.ARROWSIZE[1][this.id1], 0, ((Util.cos(this.arr_radian[i]) * 40) / 1024) + this.x, ((Util.sin(this.arr_radian[i]) * 40) / 1024) + this.y, 3);
                }
            }
        }

        @Override
        public void update() {
            this.frame = (byte) (this.frame + 1);
            if (this.frame >= 3) {
                this.frame = (byte) 0;
            }
            if (Canvas.gameTick % 30 == 0 && this.mvector != null) {
                this.count++;
                for (int i = 0; i < this.mvector.size(); i++) {
                    LiveActor t = (LiveActor) this.mvector.elementAt(i);
                    Canvas.gameScr.startNewArrow(7, this.from, t, this.x, this.y, t.damAttack, (byte) 0, this.id1);
                }
            }
            if (this.count == 2) {
                this.wantDetroy = true;
            }
            for (int i2 = 0; i2 < this.arr_radian.length; i2++) {
                int[] iArr = this.arr_radian;
                int i3 = i2;
                iArr[i3] = iArr[i3] + 30;
                if (this.arr_radian[i2] > 360) {
                    this.arr_radian[i2] = 0;
                }
            }
        }
    }
}
