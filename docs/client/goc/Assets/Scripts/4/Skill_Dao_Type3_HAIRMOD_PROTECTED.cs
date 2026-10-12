using System;

public class Skill_Dao_Type3_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private static sbyte[] x2 = new sbyte[9] { 0, 20, 20, 0, 0, 20, 20, 20, 20 };

	private static sbyte[] y2 = new sbyte[9] { 0, 0, 0, 20, 20, 20, 20, 20, 20 };

	private int lvSkill;

	private int ID;

	private sbyte nDragon = 1;

	private sbyte loop;

	public static short[][] goc = new short[4][]
	{
		new short[12]
		{
			90, 315, 225, 270, 315, 225, 90, 315, 225, 315,
			225, 225
		},
		new short[12]
		{
			270, 135, 45, 90, 135, 45, 270, 135, 45, 135,
			45, 45
		},
		new short[12]
		{
			180, 45, 315, 180, 45, 315, 180, 45, 315, 45,
			315, 315
		},
		new short[12]
		{
			0, 135, 225, 0, 135, 225, 0, 135, 225, 135,
			225, 225
		}
	};

	public Skill_Dao_Type3_HAIRMOD_PROTECTED(int id)
		: base(-1)
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
		if (c.p1 == 16)
		{
			loop = 0;
		}
		updateSkillDao(c);
		if (c.p1 == 9)
		{
			try
			{
				if (ID == 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(4, c, c.attackTarget, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir] + x2[loop], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir] + y2[loop], (c.attkPower == 2000000) ? c.attkPower : (c.attkPower / nDragon), c.attkEffect, goc[c.dir][loop]);
					loop++;
					if (loop < nDragon)
					{
						c.p1 = 8;
					}
				}
				else
				{
					Skill_AEO_Dao_5_HAIRMOD_PROTECTED skill_AEO_Dao_5_HAIRMOD_PROTECTED = new Skill_AEO_Dao_5_HAIRMOD_PROTECTED(c.x, c.y + 30, c.attackTarget, 2, true);
					if (lvSkill > 0 && lvSkill < 7)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.isDoubleDregon = false;
					}
					else
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.isDoubleDregon = true;
					}
					if (c.attkPower != 0 && c.attkPower != 2000000)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[0] = "-" + c.attkPower;
					}
					if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
					{
						skill_AEO_Dao_5_HAIRMOD_PROTECTED.textAttack[1] = AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect];
					}
					if (c.attackTarget.catagory == 1)
					{
						((Monster_HAIRMOD_PROTECTED)c.attackTarget).isTarget = true;
					}
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_AEO_Dao_5_HAIRMOD_PROTECTED);
				}
			}
			catch (Exception)
			{
			}
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
