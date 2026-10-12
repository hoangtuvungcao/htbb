public class Skill_Dao_Type2_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Dao_Type2_HAIRMOD_PROTECTED()
		: base(-1)
	{
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(4, c, c.attackTarget, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c.targethplost, (sbyte)c.eff);
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
		if (c == null)
		{
			return;
		}
		base.updateSkill(c);
		if (c.state != 0)
		{
			updateSkillDao(c);
			if (c.p1 == 9)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(4, c, c.attackTarget, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c.attkPower, c.attkEffect);
			}
			c.p1++;
		}
	}
}
