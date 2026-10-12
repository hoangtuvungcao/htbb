public class Skill_Cung_Type3_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private int lvSkill;

	private int ID;

	private sbyte nDragon = 1;

	private int loop;

	public Skill_Cung_Type3_HAIRMOD_PROTECTED(int id)
		: base(0)
	{
		ID = id;
	}

	public override void setLvSkill(int lv)
	{
		lvSkill = lv;
	}

	public override int getX2()
	{
		if (lvSkill < SkillAnimate_HAIRMOD_PROTECTED.nDra.Length)
		{
			nDragon = (sbyte)SkillAnimate_HAIRMOD_PROTECTED.nDra[lvSkill];
			return SkillAnimate_HAIRMOD_PROTECTED.nDra[lvSkill];
		}
		return 1;
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
		if (c.p1 == 15)
		{
			loop = 0;
		}
		updateSkillCung_2_3(c);
		if (c.p1 == 10)
		{
			if (ID == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, (c.attkPower == 2000000) ? c.attkPower : (c.attkPower / nDragon), c.attkEffect, 2);
				loop++;
				if (loop < nDragon)
				{
					c.p1 = 7;
				}
			}
			else
			{
				int num = 0;
				int num2 = 0;
				if (c.dir == 3)
				{
					num2 = -22;
					num = 10;
				}
				else
				{
					num2 = -22;
					num = -10;
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.startWeaponsLazer(c, c.attackTarget, num, num2, c.dir, (c.attkPower == 2000000) ? c.attkPower : (c.attkPower / nDragon), 1);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 15, 32);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 9);
			}
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
