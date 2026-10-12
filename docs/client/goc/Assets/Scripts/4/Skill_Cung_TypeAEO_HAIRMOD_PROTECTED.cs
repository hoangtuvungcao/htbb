public class Skill_Cung_TypeAEO_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private bool isAEO;

	private sbyte idSkill;

	private sbyte imgIndex;

	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	private bool isMonster;

	public Skill_Cung_TypeAEO_HAIRMOD_PROTECTED(sbyte idSkill2, bool iss, sbyte imgIndex)
		: base(0)
	{
		isAEO = iss;
		idSkill = idSkill2;
		this.imgIndex = imgIndex;
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
		isMonster = true;
	}

	public override void setActorter(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
		isMonster = false;
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
			for (int i = 0; i < mst.size(); i++)
			{
				LiveActor_HAIRMOD_PROTECTED to = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, to, c.x, c.y - 15, c.attkPower, c.attkEffect, imgIndex);
			}
			isMonster = false;
		}
		if (isAEO && c.p1 == 14)
		{
			if (mst.size() > 0)
			{
				Skill_AEO_CUNG_5_HAIRMOD_PROTECTED o = new Skill_AEO_CUNG_5_HAIRMOD_PROTECTED(mst, c);
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
			}
			else
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				mVector_HAIRMOD_PROTECTED2.addElement(c.attackTarget);
				Skill_AEO_CUNG_5_HAIRMOD_PROTECTED o2 = new Skill_AEO_CUNG_5_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED2, c);
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o2);
			}
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
		if (mst != null && mst.size() > 0)
		{
			for (int i = 0; i < mst.size(); i++)
			{
				LiveActor_HAIRMOD_PROTECTED to = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, to, c.x, c.y - 15, 0, 0, imgIndex);
			}
			isMonster = false;
		}
		else
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, 0, 0, imgIndex);
		}
		c.p1++;
	}
}
