public class Skill_Kiem_Type0_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Kiem_Type0_HAIRMOD_PROTECTED(int cooldown)
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
		if (c.p1 == 3 && c.attackTarget != null)
		{
			if (c.attackTarget.catagory == 1)
			{
				if (c.attkEffect == 0)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 11);
					((Monster_HAIRMOD_PROTECTED)c.attackTarget).jump();
				}
				else if (c.attkEffect == 2)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 12);
					((Monster_HAIRMOD_PROTECTED)c.attackTarget).doublejump();
				}
			}
			c.attackTarget.realHPSyncTime = 2;
			if (c.attkPower != 0 && c.attkPower != 2000000)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + c.attkPower, 0, c.attackTarget.x, c.attackTarget.y - 15, -1, -2);
			}
			if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect], 0, c.attackTarget.x, c.attackTarget.y - 15, 2, -2);
			}
		}
		c.p1++;
	}
}
