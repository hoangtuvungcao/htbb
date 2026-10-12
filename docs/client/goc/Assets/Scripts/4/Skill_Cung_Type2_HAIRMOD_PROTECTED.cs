public class Skill_Cung_Type2_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Cung_Type2_HAIRMOD_PROTECTED()
		: base(0)
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
		if (c.state != 0)
		{
			updateSkillCung_2_3(c);
			if (c.p1 == 10)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower, c.attkEffect, 2);
			}
			c.p1++;
		}
	}
}
