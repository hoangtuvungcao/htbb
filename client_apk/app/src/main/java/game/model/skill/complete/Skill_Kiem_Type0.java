package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Kiem_Type0 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Kiem_Type0(int i) {
        super(0);
    }

    @Override
    public void updateSkill(Char r12) {
        try {
            super.updateSkill(r12);
            if (r12.state == 0) {
                return;
            }
            try {
                updateSkillKiem(r12);
            } catch (Exception unused) {
            }
            try {
                if (r12.p1 == 3 && r12.attackTarget != null) {
                    if (r12.attackTarget.catagory == 1) {
                        try {
                            if (r12.attkEffect == 0) {
                                EffectManager.addHiEffect(r12.attackTarget.x, r12.attackTarget.y - 10, 11);
                                ((Monster) r12.attackTarget).jump();
                            } else if (r12.attkEffect == 2) {
                                EffectManager.addHiEffect(r12.attackTarget.x, r12.attackTarget.y - 10, 12);
                                ((Monster) r12.attackTarget).doublejump();
                            }
                        } catch (Exception unused2) {
                        }
                    }
                    r12.attackTarget.realHPSyncTime = 2;
                    if (r12.attkPower != 0 && r12.attkPower != 2000000) {
                        GCanvas.gameScr.startFlyText("-" + r12.attkPower, 0, r12.attackTarget.x, r12.attackTarget.y - 15, -1, -2);
                    }
                    if (r12.attkEffect != 0 && r12.attkEffect < AttackResult.EFF_NAME.length) {
                        GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r12.attkEffect], 0, r12.attackTarget.x, r12.attackTarget.y - 15, 2, -2);
                    }
                }
            } catch (Exception unused3) {
            }
            r12.p1 = (short) (r12.p1 + 1);
        } catch (Exception unused4) {
            Cout.println("skill kiem type 0");
        }
    }
}
