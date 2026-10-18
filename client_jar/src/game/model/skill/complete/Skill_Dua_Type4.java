package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.FilePack;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_Dua_Type4 extends SkillAnimate {
    Image img;
    Vector mst;

    public Skill_Dua_Type4(int skillWeapon) {
        super(skillWeapon);
        this.mst = new Vector();
        FilePack.init("/skillpublic.sh");
        this.img = FilePack.getImg("muabang");
        FilePack.reset();
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void setActorter(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void updateSkill(Monster c) {
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillDua(c);
        if (c.p1 % 3 == 0) {
            if (this.mst.size() > 0) {
                for (int i = 0; i < this.mst.size(); i++) {
                    Actor ac = (Actor) this.mst.elementAt(i);
                    for (int a = 0; a < 6; a++) {
                        Skill_Dua_4 skill = new Skill_Dua_4((ac.x - 25) + Res.rnd(50), (ac.y - 25) + Res.rnd(50));
                        EffectManager.hiEffects.addElement(skill);
                    }
                }
            } else {
                for (int a2 = 0; a2 < 6; a2++) {
                    Skill_Dua_4 skill2 = new Skill_Dua_4((c.attackTarget.x - 25) + Res.rnd(50), (c.attackTarget.y - 25) + Res.rnd(50));
                    EffectManager.hiEffects.addElement(skill2);
                }
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    class Skill_Dua_4 extends Effect {
        int angle;
        int xGo;
        int yGo;
        byte index = 0;
        byte g;
        boolean isExplo;

        public Skill_Dua_4(int x, int y) {
            this.angle = 0;
            this.g = (byte) 10;
            int y2 = y - 10;
            this.xGo = x;
            this.yGo = y2;
            this.x = x + 50;
            this.y = y2 - 150;
            this.g = (byte) (5 + Res.rnd(10));
            this.angle = Util.angle(x - this.x, -(y2 - this.y));
        }

        @Override
        public void update() {
            if (!this.isExplo) {
                int xa = (this.g * Util.cos(this.angle)) >> 10;
                int ya = (-(this.g * Util.sin(this.angle))) >> 10;
                this.g = (byte) (this.g + 2);
                this.x += xa;
                this.y += ya;
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
            this.index = (byte) (this.index + 1);
            if (this.index >= 12) {
                EffectManager.lowEffects.removeElement(this);
            }
        }

        @Override
        public void paint(Graphics g) {
            g.drawRegion(Skill_Dua_Type4.this.img, 0, 15 * (this.index / 3), 13, 15, 0, this.x, this.y, 3);
        }
    }
}
