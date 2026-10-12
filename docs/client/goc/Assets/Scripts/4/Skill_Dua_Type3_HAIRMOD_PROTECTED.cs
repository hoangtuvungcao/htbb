public class Skill_Dua_Type3_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private class Eff_DauRong_Down : Effect_HAIRMOD_PROTECTED
	{
		public new int x;

		public new int y;

		public int xTo;

		public int yTo;

		public int vy = 5;

		public int power;

		public LiveActor_HAIRMOD_PROTECTED to;

		private int[] wImg = new int[4] { 0, 0, 24, 24 };

		public Eff_DauRong_Down(int xF, int yF, LiveActor_HAIRMOD_PROTECTED to)
		{
			x = Canvas_HAIRMOD_PROTECTED.random(xF - 15, xF + 15);
			y = Canvas_HAIRMOD_PROTECTED.random(yF - 20, yF + 20);
			this.to = to;
		}

		public override void update()
		{
			y += vy;
			vy += 2;
			if (y > to.y - 15)
			{
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y - 5, 30);
			}
			if (power != 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + power, 0, x, y - 15, 1, -2);
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("MISS", 0, x, y - 15, 1, -2);
			}
		}

		public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
		{
			if (Res_HAIRMOD_PROTECTED.getImgArrow(4) != null)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[4], wImg[0], wImg[1], wImg[2], wImg[3], 5, x, y, 3);
			}
		}
	}

	private int pmonster;

	private int lvSkill;

	private int ID;

	private sbyte loop;

	private sbyte lap;

	private sbyte nDragon = 1;

	public Skill_Dua_Type3_HAIRMOD_PROTECTED(int id)
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
		if (c.p1 == 8)
		{
			loop = 0;
			lap = 0;
		}
		updateSkillDua(c);
		if (c.p1 == 3 && ID == 0)
		{
			short[][] array = new short[4][]
			{
				new short[11]
				{
					90, 315, 225, 270, 315, 225, 90, 315, 225, 315,
					225
				},
				new short[11]
				{
					270, 135, 45, 90, 135, 45, 270, 135, 45, 135,
					45
				},
				new short[11]
				{
					180, 45, 315, 180, 45, 315, 180, 45, 315, 45,
					315
				},
				new short[11]
				{
					0, 135, 225, 0, 135, 225, 0, 135, 225, 135,
					225
				}
			};
			if (c.dir <= SkillAnimate_HAIRMOD_PROTECTED.splashDuaX.Length - 1 && c.dir <= SkillAnimate_HAIRMOD_PROTECTED.splashDuaY.Length - 1 && c.dir <= array.Length - 1 && loop <= array[c.dir].Length - 1)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(3, c, c.attackTarget, c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], (c.attkPower == 2000000) ? c.attkPower : (c.attkPower / nDragon), c.attkEffect, array[c.dir][loop]);
			}
			loop++;
			if (loop < nDragon)
			{
				c.p1 = 0;
			}
		}
		if (lap % 2 == 0 && ID == 1)
		{
			int num = 1;
			if (lvSkill < 7 && lvSkill > 3)
			{
				num = 2;
			}
			else if (lvSkill >= 7)
			{
				num = 4;
			}
			for (int i = 0; i < num; i++)
			{
				Eff_DauRong_Down eff_DauRong_Down = new Eff_DauRong_Down(c.attackTarget.x, c.attackTarget.y - 200, c.attackTarget);
				if (c.attkPower != 2000000)
				{
					eff_DauRong_Down.power = c.attkPower / nDragon;
				}
				else
				{
					eff_DauRong_Down.power = c.attkPower;
				}
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(eff_DauRong_Down);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.x, c.y - 15, 23);
			}
		}
		c.p1++;
		if (ID == 1)
		{
			lap++;
		}
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
