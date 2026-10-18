package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_DanhThuong extends SkillAnimate {
    public Skill_DanhThuong(int cooldown) {
        super(0);
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
        updateSkillKiem(c);
        if (c.p1 == 3 && c.attackTarget != null) {
            if (c.attackTarget.catagory == 1) {
                if (c.attkEffect == 0) {
                    EffectManager.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 11);
                    ((Monster) c.attackTarget).jump();
                } else if (c.attkEffect == 2) {
                    EffectManager.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 12);
                    ((Monster) c.attackTarget).doublejump();
                }
            }
            c.attackTarget.realHPSyncTime = 2;
            if (c.attkPower != 0 && c.attkPower != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower).toString(), 0, c.attackTarget.x + 0, c.attackTarget.y - 15, -1, -2);
            }
            if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, c.attackTarget.x + 0, c.attackTarget.y - 15, 2, -2);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
