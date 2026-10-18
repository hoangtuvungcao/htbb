package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Bua_Type1 extends SkillAnimate {
    public Skill_Bua_Type1() {
        super(0);
    }

    @Override
    public void updateSkill(Char r18) {
        try {
            super.updateSkill(r18);
            if (r18.state == 0) {
                return;
            }
            if (r18.p1 == 15) {
                r18.state = (byte) 0;
                r18.p3 = (short) 0;
                r18.p2 = (short) 0;
                r18.p1 = (short) 0;
                r18.weapon_frame = (short) 0;
            } else if (r18.p1 == 12 || r18.p1 == 11 || r18.p1 == 13 || r18.p1 == 14) {
                r18.frame = (byte) 5;
                r18.weapon_frame = (short) 7;
            } else if (r18.p1 == 10 || r18.p1 == 9) {
                r18.frame = (byte) 5;
                r18.weapon_frame = (short) 6;
            } else if (r18.p1 == 8 || r18.p1 == 7) {
                r18.frame = (byte) 4;
                r18.weapon_frame = (short) 5;
            } else {
                r18.frame = (byte) 4;
                r18.weapon_frame = (short) 4;
            }
            if (r18.p1 == 8) {
                if (r18.attackTarget != null) {
                    if (r18.attackTarget.catagory == 1) {
                        if (r18.attkEffect == 0) {
                            EffectManager.addHiEffect(r18.attackTarget.x, r18.attackTarget.y - 10, 11);
                            ((Monster) r18.attackTarget).jumpVang(r18);
                        } else if (r18.attkEffect == 2) {
                            EffectManager.addHiEffect(r18.attackTarget.x, r18.attackTarget.y - 10, 12);
                            ((Monster) r18.attackTarget).doublejump();
                        }
                    }
                    r18.attackTarget.realHPSyncTime = 2;
                }
                if (r18.attkEffect != 1) {
                    EffectManager.addLowEffect(r18.attackTarget.x, r18.attackTarget.y - 10, 13);
                }
                if (r18.attkPower != 0 && r18.attkPower != 2000000) {
                    GCanvas.gameScr.startFlyText("-" + r18.attkPower, 0, r18.attackTarget.x, r18.attackTarget.y - 15, -1, -2);
                }
                if (r18.attkEffect != 0 && r18.attkEffect < AttackResult.EFF_NAME.length) {
                    GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r18.attkEffect], 0, r18.attackTarget.x, r18.attackTarget.y - 15, 2, -2);
                }
            }
            r18.p1 = (short) (r18.p1 + 1);
        } catch (Exception unused) {
            Cout.println("Loi skill bua type 1");
        }
    }
}
