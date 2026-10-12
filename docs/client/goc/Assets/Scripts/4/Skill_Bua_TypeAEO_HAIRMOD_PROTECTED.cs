public class Skill_Bua_TypeAEO_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private bool isSkill5;

	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Bua_TypeAEO_HAIRMOD_PROTECTED(bool isSkill5)
		: base(0)
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
		updateStateBua(c, 4, 0);
		if (c.p1 == 13)
		{
			MyRandom_HAIRMOD_PROTECTED myRandom_HAIRMOD_PROTECTED = new MyRandom_HAIRMOD_PROTECTED();
			int num = myRandom_HAIRMOD_PROTECTED.nextInt() % 20;
			int num2 = num;
			if (c.dir == 0 || c.dir == 1)
			{
				num2 = 0;
			}
			else
			{
				num = 0;
			}
			if (c.attackTarget != null)
			{
				if (c.attackTarget.catagory == 1)
				{
					if (c.attkEffect == 0)
					{
						EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x + num, c.attackTarget.y - 10 + num2, 11);
						c.attackTarget.jump();
					}
					else if (c.attkEffect == 2)
					{
						EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.attackTarget.x + num, c.attackTarget.y - 10 + num2, 12);
						c.attackTarget.doublejump();
					}
				}
				c.attackTarget.realHPSyncTime = 2;
			}
			if (!isSkill5)
			{
				for (int i = 0; i < mst.size(); i++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
					EffectManager_HAIRMOD_PROTECTED.addLowEffect(liveActor_HAIRMOD_PROTECTED.x - 10 + num, liveActor_HAIRMOD_PROTECTED.y - 25 + num2, 14);
					EffectManager_HAIRMOD_PROTECTED.addLowEffect(liveActor_HAIRMOD_PROTECTED.x + 10 + num, liveActor_HAIRMOD_PROTECTED.y - 25 + num2, 14);
					EffectManager_HAIRMOD_PROTECTED.addLowEffect(liveActor_HAIRMOD_PROTECTED.x + num, liveActor_HAIRMOD_PROTECTED.y - 25 - 10 + num2, 14);
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + c.attkPower, 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y - 15, -1, -2);
					}
					if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect], 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y - 15, 2, -2);
					}
				}
			}
			else
			{
				for (int j = 0; j < mst.size(); j++)
				{
					LiveActor_HAIRMOD_PROTECTED c2 = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(j);
					Skill_AEO_Dao_5_HAIRMOD_PROTECTED skill_AEO_Dao_5_HAIRMOD_PROTECTED = new Skill_AEO_Dao_5_HAIRMOD_PROTECTED(c.x, c.y + 37, c2, 1, true);
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[0] = "-" + c.attkPower;
					}
					if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[1] = AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect];
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
