public class Skill_Bua_Type4_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Bua_Type4_HAIRMOD_PROTECTED(int skillWeapon)
		: base(skillWeapon)
	{
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
		base.updateSkill(c);
		if (c.state == 0)
		{
			return;
		}
		updateStateBua(c, 35, 0);
		if (c.p1 == 13)
		{
			if (mst.size() > 0)
			{
				for (int i = 0; i < mst.size(); i++)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)mst.elementAt(i);
					EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(new Skill_Bua_4_HAIRMOD_PROTECTED((LiveActor_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED, c.x, c.y, false));
				}
			}
			else
			{
				EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(new Skill_Bua_4_HAIRMOD_PROTECTED(c.attackTarget, c.x, c.y, false));
			}
		}
		c.p1++;
	}
}
