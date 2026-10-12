public class Skill_Cung_Type4_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Cung_Type4_HAIRMOD_PROTECTED(int skillWeapon)
		: base(skillWeapon)
	{
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
	}

	public override void setActorter(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
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
		updateSkillCung_2_3(c);
		if (c.p1 == 10)
		{
			if (mst.size() > 0)
			{
				for (int i = 0; i < mst.size(); i++)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)mst.elementAt(i);
					Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, (LiveActor_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
				}
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
			}
		}
		if (c.p1 >= 13)
		{
			if (mst.size() > 0)
			{
				for (int j = 0; j < mst.size(); j++)
				{
					Actor_HAIRMOD_PROTECTED ac = (Actor_HAIRMOD_PROTECTED)mst.elementAt(j);
					attack(c, ac);
				}
			}
			else
			{
				attack(c, c.attackTarget);
			}
		}
		c.p1++;
	}

	private void attack(Char_HAIRMOD_PROTECTED c, Actor_HAIRMOD_PROTECTED ac)
	{
		int num = ac.x;
		int num2 = ac.y - 20;
		int num3 = Res_HAIRMOD_PROTECTED.rnd(3) + 1;
		for (int i = 0; i < num3; i++)
		{
			int num4 = Res_HAIRMOD_PROTECTED.rnd(50) + 50;
			int num5 = num4 * Util_HAIRMOD_PROTECTED.Cos(i * 30) >> 10;
			int num6 = -(num4 * Util_HAIRMOD_PROTECTED.Sin(i * 30)) >> 10;
			Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, (LiveActor_HAIRMOD_PROTECTED)ac, num + num5, num2 + num6, c.attkPower, c.attkEffect, Res_HAIRMOD_PROTECTED.rnd(2));
		}
	}

	public new void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
