public class Skill_Kiem_Type2_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Kiem_Type2_HAIRMOD_PROTECTED()
		: base(-1)
	{
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
		if (c == null)
		{
			return;
		}
		base.updateSkill(c);
		if (c.state == 0)
		{
			return;
		}
		if (c.p1 == 16)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
		}
		else if (c.p1 >= 14 && c.p1 < 16)
		{
			c.frame = 5;
			c.weapon_frame = 7;
		}
		else if (c.p1 == 13 || c.p1 == 12)
		{
			c.frame = 5;
			c.weapon_frame = 6;
		}
		else if (c.p1 == 11 || c.p1 == 10)
		{
			c.frame = 4;
			c.weapon_frame = 5;
		}
		else
		{
			if (c.p1 % 2 == 0)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.x + SkillAnimate_HAIRMOD_PROTECTED.splashKiemX[c.dir], c.y - 80, 10);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.x + SkillAnimate_HAIRMOD_PROTECTED.splashKiemX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashKiemY[c.dir], 3);
			}
			c.frame = 4;
			c.weapon_frame = 4;
		}
		if (c.p1 == 9)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(1, c, c.attackTarget, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashKiemX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashKiemY[c.dir], c.attkPower, c.attkEffect);
		}
		c.p1++;
	}
}
