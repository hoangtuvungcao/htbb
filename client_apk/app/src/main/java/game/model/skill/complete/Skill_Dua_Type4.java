package game.model.skill.complete;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Actor;
import game.model.Char;
import game.model.Cout;
import game.model.Effect;
import game.model.EffectManager;
import game.model.FilePack;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Dua_Type4 extends SkillAnimate {
    Bitmap img;
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dua_Type4(int i) {
        super(i);
        this.mst = new Vector();
        FilePack.init("/skillpublic.sh");
        this.img = FilePack.getImg("muabang");
        FilePack.reset();
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void setActorter(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r12) {
        try {
            super.updateSkill(r12);
            if (r12.state == 0) {
                return;
            }
            try {
                updateSkillDua(r12);
            } catch (Exception unused) {
            }
            if (r12.p1 % 3 == 0) {
                try {
                    if (this.mst.size() > 0) {
                        int size = this.mst.size();
                        for (int i = 0; i < size; i++) {
                            Actor actor = (Actor) this.mst.elementAt(i);
                            for (int i2 = 0; i2 < 6; i2++) {
                                EffectManager.hiEffects.addElement(new Skill_Dua_4((actor.x - 25) + Res.rnd(50), (actor.y - 25) + Res.rnd(50)));
                            }
                        }
                    } else {
                        for (int i3 = 0; i3 < 6; i3++) {
                            EffectManager.hiEffects.addElement(new Skill_Dua_4((r12.attackTarget.x - 25) + Res.rnd(50), (r12.attackTarget.y - 25) + Res.rnd(50)));
                        }
                    }
                } catch (Exception unused2) {
                }
            }
            r12.p1 = (short) (r12.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("Loi skill dua type 4");
        }
    }

    class Skill_Dua_4 extends Effect {
        int angle;
        byte g;
        byte index = 0;
        boolean isExplo;
        int xGo;
        int yGo;

        public Skill_Dua_4(int i, int i2) {
            this.angle = 0;
            this.g = (byte) 10;
            int i3 = i2 - 10;
            this.xGo = i;
            this.yGo = i3;
            this.x = i + 50;
            this.y = i2 - 160;
            this.g = (byte) (Res.rnd(10) + 5);
            this.angle = Util.angle(i - this.x, -(i3 - this.y));
        }

        @Override
        public void update() {
            if (!this.isExplo) {
                int iCos = (this.g * Util.cos(this.angle)) >> 10;
                int i = (-(this.g * Util.sin(this.angle))) >> 10;
                this.g = (byte) (this.g + 2);
                this.x += iCos;
                this.y += i;
                if (Util.distance(this.x, this.y, this.xGo, this.yGo) <= 20) {
                    this.y = this.yGo;
                    this.index = (byte) 4;
                    this.isExplo = true;
                    EffectManager.lowEffects.addElement(this);
                    EffectManager.hiEffects.removeElement(this);
                    return;
                }
                return;
            }
            byte b = (byte) (this.index + 1);
            this.index = b;
            if (b >= 12) {
                EffectManager.lowEffects.removeElement(this);
            }
        }

        @Override
        public void paint(Graphics graphics) {
            graphics.drawRegion(Skill_Dua_Type4.this.img, 0, (this.index / 3) * 15, 13, 15, 0, this.x, this.y, 3);
        }
    }
}
