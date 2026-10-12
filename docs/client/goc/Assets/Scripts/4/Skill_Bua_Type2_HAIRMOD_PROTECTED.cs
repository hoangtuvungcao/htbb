public class Skill_Bua_Type2_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Bua_Type2_HAIRMOD_PROTECTED()
		: base(0)
	{
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
		if (c != null)
		{
			base.updateSkill(c);
			if (c.state != 0)
			{
				updateSkillBua_2_3(c, false, 1, 0, 0);
				c.p1++;
			}
		}
	}
}
