public class Skill_Dua_TypeAEO_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private bool isSkill5;

	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Dua_TypeAEO_HAIRMOD_PROTECTED(bool isSkill5)
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
		if (c == null)
		{
			return;
		}
		c.frame = 4;
		if (c.p1 == 18)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
		}
		else if (c.p1 >= 6 && c.p1 < 18)
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
		if (c.p1 == 3)
		{
			if (!isSkill5)
			{
				for (int i = 0; i < mst.size(); i++)
				{
					LiveActor_HAIRMOD_PROTECTED to = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
					Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(5, c, to, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c.attkPower, c.attkEffect);
				}
			}
			else
			{
				for (int j = 0; j < mst.size(); j++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(j);
					Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED = new Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED(c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c, liveActor_HAIRMOD_PROTECTED, 4);
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED.textAttack[0] = "-" + c.attkPower;
					}
					if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
					{
						skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED.textAttack[1] = AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect];
					}
					if (liveActor_HAIRMOD_PROTECTED.catagory == 1)
					{
						((Monster_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).isTarget = true;
					}
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED);
				}
			}
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
