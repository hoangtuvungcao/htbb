public class Skill_Kiem_Type1_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Kiem_Type1_HAIRMOD_PROTECTED()
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
		if (c.state == 0)
		{
			return;
		}
		updateSkillKiem(c);
		if (c.p1 == 3)
		{
			if (c.attackTarget != null)
			{
				if (c.attackTarget.catagory == 1)
				{
					((Monster_HAIRMOD_PROTECTED)c.attackTarget).jump();
				}
				c.attackTarget.realHPSyncTime = 2;
			}
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 3);
			if (c.attkPower != 2000000)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + c.attkPower, 0, c.attackTarget.x, c.attackTarget.y - 15, -1, -2);
			}
		}
		c.p1++;
	}
}
