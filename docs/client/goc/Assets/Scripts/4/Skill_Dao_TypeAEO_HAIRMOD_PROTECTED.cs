public class Skill_Dao_TypeAEO_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED mst;

	private bool isSkill5;

	private bool isMonster;

	public Skill_Dao_TypeAEO_HAIRMOD_PROTECTED(bool isSkill5)
		: base(-1)
	{
		this.isSkill5 = isSkill5;
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
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
		c.frame = 4;
		if (c.p1 == 16)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
		}
		else if (c.p1 >= 14 && c.p1 < 16)
		{
			c.frame = 5;
			c.weapon_frame = 7;
		}
		else if (c.p1 == 13 || c.p1 == 12)
		{
			c.frame = 5;
			c.weapon_frame = 6;
		}
		else if (c.p1 == 11 || c.p1 == 10)
		{
			c.frame = 4;
			c.weapon_frame = 5;
		}
		else
		{
			c.frame = 4;
			c.weapon_frame = 4;
		}
		if (c.p1 == 9 && mst != null)
		{
			if (!isSkill5)
			{
				if (c.attkPower == 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("MISS", 0, c.attackTarget.x, c.attackTarget.y - 15, 1, -2);
				}
				for (int i = 0; i < mst.size(); i++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
					Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED = new Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED(c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c, liveActor_HAIRMOD_PROTECTED, 3);
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED.textAttack[0] = "-" + c.attkPower;
					}
					if (c.attkEffect != 0)
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
			else
			{
				for (int j = 0; j < mst.size(); j++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(j);
					Skill_AEO_Dao_5_HAIRMOD_PROTECTED skill_AEO_Dao_5_HAIRMOD_PROTECTED = new Skill_AEO_Dao_5_HAIRMOD_PROTECTED(c.x, c.y + 30, liveActor_HAIRMOD_PROTECTED2, 0, true);
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[0] = "-" + c.attkPower;
					}
					if (c.attkEffect != 0)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[1] = AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect];
					}
					if (liveActor_HAIRMOD_PROTECTED2.catagory == 1)
					{
						((Monster_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED2).isTarget = true;
					}
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_AEO_Dao_5_HAIRMOD_PROTECTED);
				}
			}
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
