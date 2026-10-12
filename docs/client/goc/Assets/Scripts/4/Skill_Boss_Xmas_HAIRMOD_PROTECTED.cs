public class Skill_Boss_Xmas_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private bool isSkill5;

	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Boss_Xmas_HAIRMOD_PROTECTED(bool isSkill5)
		: base(-1)
	{
		this.isSkill5 = isSkill5;
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
		if (c == null)
		{
			return;
		}
		if (c.p1 == 3 && !isSkill5)
		{
			for (int i = 0; i < mst.size(); i++)
			{
				LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(7, c, liveActor_HAIRMOD_PROTECTED, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], liveActor_HAIRMOD_PROTECTED.damAttack, 0);
			}
		}
		c.p1++;
	}
}
