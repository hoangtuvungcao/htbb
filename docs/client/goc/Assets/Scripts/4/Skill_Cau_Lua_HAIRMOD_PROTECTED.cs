public class Skill_Cau_Lua_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private class Pos
	{
		private int x;

		private int y;

		private int xTo;

		private int yTo;

		public bool isRemove;

		public Pos(int x, int y, int angle)
		{
			this.x = x;
			this.y = y;
			xTo = x + Util_HAIRMOD_PROTECTED.Cos(angle) * 100 / 1024;
			yTo = y + Util_HAIRMOD_PROTECTED.Sin(angle) * 100 / 1024;
		}

		public void update()
		{
			if (x != xTo)
			{
				if (xTo - x >> 1 == 0)
				{
					x = xTo;
				}
				else
				{
					x += xTo - x >> 1;
				}
			}
			if (y != yTo)
			{
				if (yTo - y >> 1 == 0)
				{
					y = yTo;
				}
				else
				{
					y += yTo - y >> 1;
				}
			}
			if (x == xTo && y == yTo)
			{
				isRemove = true;
			}
			if (isRemove)
			{
				for (int i = 0; i < 5; i++)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(x + Res_HAIRMOD_PROTECTED.rnd(10) - 5, y + Res_HAIRMOD_PROTECTED.rnd(10) - 10, 28);
				}
			}
			else
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 15);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 49);
			}
		}
	}

	private mVector_HAIRMOD_PROTECTED total = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED list;

	private int[] angle = new int[12]
	{
		0, 30, 60, 90, 120, 150, 180, 210, 240, 270,
		300, 330
	};

	public Skill_Cau_Lua_HAIRMOD_PROTECTED(int xC, int yC, mVector_HAIRMOD_PROTECTED list)
	{
		total = new mVector_HAIRMOD_PROTECTED();
		this.list = new mVector_HAIRMOD_PROTECTED();
		this.list = list;
		for (int i = 0; i < angle.Length; i++)
		{
			total.addElement(new Pos(xC, yC, angle[i]));
		}
	}

	public override void update()
	{
		for (int i = 0; i < total.size(); i++)
		{
			Pos pos = (Pos)total.elementAt(i);
			if (pos != null)
			{
				pos.update();
				if (pos.isRemove)
				{
					total.removeElement(pos);
				}
			}
		}
		if (total.size() != 0)
		{
			return;
		}
		wantDetroy = true;
		for (int j = 0; j < list.size(); j++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(j);
			if (liveActor_HAIRMOD_PROTECTED != null)
			{
				int damAttack = liveActor_HAIRMOD_PROTECTED.damAttack;
				liveActor_HAIRMOD_PROTECTED.doublejump();
				if (damAttack > 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + damAttack, 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y + liveActor_HAIRMOD_PROTECTED.dy - 15, 1, -2);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("MISS", 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y + liveActor_HAIRMOD_PROTECTED.dy - 15, 1, -2);
				}
			}
		}
	}
}
