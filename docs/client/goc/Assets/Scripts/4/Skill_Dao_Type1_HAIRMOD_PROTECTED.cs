public class Skill_Dao_Type1_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	public Skill_Dao_Type1_HAIRMOD_PROTECTED()
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
		if (c.p1 == 8)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
		}
		else if (c.p1 == 7 || c.p1 == 6)
		{
			c.frame = 5;
			c.weapon_frame = 7;
		}
		else if (c.p1 == 5 || c.p1 == 4)
		{
			c.frame = 5;
			c.weapon_frame = 6;
		}
		else if (c.p1 == 3 || c.p1 == 2)
		{
			c.frame = 4;
			c.weapon_frame = 5;
		}
		else
		{
			c.frame = 4;
			c.weapon_frame = 4;
		}
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
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 20, 15);
			}
			if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect], 0, c.attackTarget.x, c.attackTarget.y - 15, 2, -2);
			}
		}
		c.p1++;
	}
}
