package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Dao_Type1 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dao_Type1() {
        super(0);
    }

    @Override
    public void updateSkill(Char r13) {
        try {
            super.updateSkill(r13);
            if (r13.state == 0) {
                return;
            }
            if (r13.p1 == 8) {
                r13.state = (byte) 0;
                r13.p3 = (short) 0;
                r13.p2 = (short) 0;
                r13.p1 = (short) 0;
                r13.weapon_frame = (short) 0;
            } else if (r13.p1 == 7 || r13.p1 == 6) {
                r13.frame = (byte) 5;
                r13.weapon_frame = (short) 7;
            } else if (r13.p1 == 5 || r13.p1 == 4) {
                r13.frame = (byte) 5;
                r13.weapon_frame = (short) 6;
            } else if (r13.p1 == 3 || r13.p1 == 2) {
                r13.frame = (byte) 4;
                r13.weapon_frame = (short) 5;
            } else {
                r13.frame = (byte) 4;
                r13.weapon_frame = (short) 4;
            }
            if (r13.p1 == 3 && r13.attackTarget != null) {
                if (r13.attackTarget.catagory == 1) {
                    try {
                        if (r13.attkEffect == 0) {
                            EffectManager.addHiEffect(r13.attackTarget.x, r13.attackTarget.y - 10, 11);
                            ((Monster) r13.attackTarget).jump();
                        } else if (r13.attkEffect == 2) {
                            EffectManager.addHiEffect(r13.attackTarget.x, r13.attackTarget.y - 10, 12);
                            ((Monster) r13.attackTarget).doublejump();
                        }
                    } catch (Exception unused) {
                    }
                }
                r13.attackTarget.realHPSyncTime = 2;
                if (r13.attkPower != 0 && r13.attkPower != 2000000) {
                    try {
                        GCanvas.gameScr.startFlyText("-" + r13.attkPower, 0, r13.attackTarget.x, r13.attackTarget.y - 15, -1, -2);
                        EffectManager.addHiEffect(r13.attackTarget.x, r13.attackTarget.y - 20, 15);
                    } catch (Exception unused2) {
                    }
                }
                if (r13.attkEffect != 0 && r13.attkEffect < AttackResult.EFF_NAME.length) {
                    GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r13.attkEffect], 0, r13.attackTarget.x, r13.attackTarget.y - 15, 2, -2);
                }
            }
            r13.p1 = (short) (r13.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("Loi dao type 1");
        }
    }
}
