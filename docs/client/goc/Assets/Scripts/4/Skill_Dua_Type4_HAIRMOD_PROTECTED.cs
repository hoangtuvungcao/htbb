public class Skill_Dua_Type4_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private class Skill_Dua_4 : Effect_HAIRMOD_PROTECTED
	{
		private int angle;

		private int xGo;

		private int yGo;

		private byte index;

		private byte g = 10;

		private bool isExplo;

		public Skill_Dua_4(int x, int y)
		{
			y -= 10;
			xGo = x;
			yGo = y;
			base.x = x + 50;
			base.y = y - 150;
			g = (byte)(5 + Res_HAIRMOD_PROTECTED.rnd(10));
			angle = Util_HAIRMOD_PROTECTED.angle(x - base.x, -(y - base.y));
		}

		public override void update()
		{
			if (!isExplo)
			{
				int num = g * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10;
				int num2 = -(g * Util_HAIRMOD_PROTECTED.Sin(angle)) >> 10;
				g += 2;
				x += num;
				y += num2;
				if (Util_HAIRMOD_PROTECTED.distance(x, y, xGo, yGo) <= 20)
				{
					y = yGo;
					index = 4;
					isExplo = true;
					EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(this);
					EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				}
			}
			else
			{
				index++;
				if (index >= 12)
				{
					EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
				}
			}
		}

		public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
		{
			g.drawRegion(img, 0, 15 * (index / 3), 13, 15, 0, x, y, 3);
		}
	}

	public static Image_HAIRMOD_PROTECTED img;

	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	public Skill_Dua_Type4_HAIRMOD_PROTECTED(int skillWeapon)
		: base(skillWeapon)
	{
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.effPublic);
		img = FilePack_HAIRMOD_PROTECTED.getImg("muabang");
		FilePack_HAIRMOD_PROTECTED.reset();
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
	}

	public override void setActorter(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst = mst;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
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
		updateSkillDua(c);
		if (c.p1 % 3 == 0)
		{
			if (mst.size() > 0)
			{
				for (int i = 0; i < mst.size(); i++)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)mst.elementAt(i);
					for (int j = 0; j < 6; j++)
					{
						Skill_Dua_4 o = new Skill_Dua_4(actor_HAIRMOD_PROTECTED.x - 25 + Res_HAIRMOD_PROTECTED.rnd(50), actor_HAIRMOD_PROTECTED.y - 25 + Res_HAIRMOD_PROTECTED.rnd(50));
						EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
					}
				}
			}
			else
			{
				for (int k = 0; k < 6; k++)
				{
					Skill_Dua_4 o2 = new Skill_Dua_4(c.attackTarget.x - 25 + Res_HAIRMOD_PROTECTED.rnd(50), c.attackTarget.y - 25 + Res_HAIRMOD_PROTECTED.rnd(50));
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o2);
				}
			}
		}
		c.p1++;
	}
}
