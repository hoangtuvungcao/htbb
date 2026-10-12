public class Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private MagicLogic_HAIRMOD_PROTECTED magic;

	private int countDau = 7;

	private int dir = -1;

	private int xDau_1;

	private int yDau_1;

	private int xDau_2;

	private int yDau_2;

	private int xlast;

	private int yLast;

	private int xDau_3;

	private int yDau_3;

	private int xDau_4;

	private int yDau_4;

	private mVector_HAIRMOD_PROTECTED listPos = new mVector_HAIRMOD_PROTECTED();

	public string[] textAttack;

	private LiveActor_HAIRMOD_PROTECTED to;

	public bool isDoubleDragon;

	public Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED(int xFrom, int yFrom, LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, sbyte type)
	{
		base.type = type;
		this.to = to;
		magic = new MagicLogic_HAIRMOD_PROTECTED();
		xlast = xFrom;
		yLast = yFrom;
		if (type == 5)
		{
			xlast = to.x;
			yLast = to.y;
			to.x = (short)xlast;
			to.y = (short)(yLast - 200);
			this.to = to;
			magic.set(type, xlast, yLast, to.dir, to);
			int num = Util_HAIRMOD_PROTECTED.angle(xlast - x, -(yLast - y));
			magic.setAngle(Util_HAIRMOD_PROTECTED.fixangle(num - 45));
		}
		else
		{
			magic.set(type, xFrom, yFrom, from.dir, to);
			int num2 = Util_HAIRMOD_PROTECTED.angle(xFrom - x, -(yFrom - y));
			magic.setAngle(Util_HAIRMOD_PROTECTED.fixangle(num2 - 180));
		}
		textAttack = new string[2];
	}

	public override void update()
	{
		if (magic == null)
		{
			return;
		}
		magic.updateAngle();
		if (magic.isEnd)
		{
			if (type != 5)
			{
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(magic.x, magic.y, (type != 4) ? 28 : 30);
			}
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(magic.x, magic.y, (type != 4) ? 28 : 30);
			EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
			if (to.catagory == 1)
			{
				((Monster_HAIRMOD_PROTECTED)to).isTarget = false;
			}
			to.jump();
			if (textAttack[0] != null)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(textAttack[0], 0, x, y - 15, 1, -2);
			}
			if (textAttack[1] != null)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(textAttack[1], 0, x, y - 15, 2, -2);
			}
			return;
		}
		x = magic.x;
		y = magic.y;
		int num = Util_HAIRMOD_PROTECTED.angle(xlast - x, -(yLast - y));
		int a = Util_HAIRMOD_PROTECTED.fixangle(num + 90);
		int num2 = countDau * Util_HAIRMOD_PROTECTED.Cos(a) >> 10;
		int num3 = -(countDau * Util_HAIRMOD_PROTECTED.Sin(a)) >> 10;
		xDau_1 = x + num2;
		yDau_1 = y + num3;
		int a2 = Util_HAIRMOD_PROTECTED.fixangle(num - 90);
		int num4 = countDau * Util_HAIRMOD_PROTECTED.Cos(a2) >> 10;
		int num5 = -(countDau * Util_HAIRMOD_PROTECTED.Sin(a2)) >> 10;
		xDau_2 = x + num4;
		yDau_2 = y + num5;
		if (type == 5 && isDoubleDragon)
		{
			int a3 = Util_HAIRMOD_PROTECTED.fixangle(num + 135);
			int num6 = countDau * Util_HAIRMOD_PROTECTED.Cos(a3) >> 10;
			int num7 = -(countDau * Util_HAIRMOD_PROTECTED.Sin(a3)) >> 10;
			xDau_3 = x + num6 + 10;
			yDau_3 = y + num7 - 10;
			int a4 = Util_HAIRMOD_PROTECTED.fixangle(num - 135);
			int num8 = countDau * Util_HAIRMOD_PROTECTED.Cos(a4) >> 10;
			int num9 = -(countDau * Util_HAIRMOD_PROTECTED.Sin(a4)) >> 10;
			xDau_4 = x + num8 + 10;
			yDau_4 = y + num9 - 10;
		}
		countDau += dir * 3;
		if (Util_HAIRMOD_PROTECTED.abs(countDau) > 9)
		{
			dir *= -1;
		}
		Position_HAIRMOD_PROTECTED o = new Position_HAIRMOD_PROTECTED(xDau_1, yDau_1, 0);
		listPos.addElement(o);
		Position_HAIRMOD_PROTECTED o2 = new Position_HAIRMOD_PROTECTED(xDau_2, yDau_2, 0);
		listPos.addElement(o2);
		if (type == 5 && isDoubleDragon)
		{
			Position_HAIRMOD_PROTECTED o3 = new Position_HAIRMOD_PROTECTED(xDau_3, yDau_3, 0);
			listPos.addElement(o3);
			Position_HAIRMOD_PROTECTED o4 = new Position_HAIRMOD_PROTECTED(xDau_4, yDau_4, 0);
			listPos.addElement(o4);
		}
		xlast = x;
		yLast = y;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (type == 4)
		{
			for (int i = 0; i < listPos.size(); i++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)listPos.elementAt(i);
				Res_HAIRMOD_PROTECTED.paintImgEff(g, 29, 0, (17 - position_HAIRMOD_PROTECTED.follow) * 11, 11, 11, position_HAIRMOD_PROTECTED.x, position_HAIRMOD_PROTECTED.y, 3);
				position_HAIRMOD_PROTECTED.follow++;
				if (position_HAIRMOD_PROTECTED.follow >= 18)
				{
					listPos.removeElement(position_HAIRMOD_PROTECTED);
				}
			}
		}
		else
		{
			for (int j = 0; j < listPos.size(); j++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)listPos.elementAt(j);
				Res_HAIRMOD_PROTECTED.paintImgEff(g, 8, 0, position_HAIRMOD_PROTECTED2.follow * 10, 10, 10, position_HAIRMOD_PROTECTED2.x, position_HAIRMOD_PROTECTED2.y, 3);
				position_HAIRMOD_PROTECTED2.follow++;
				if (position_HAIRMOD_PROTECTED2.follow >= 10)
				{
					listPos.removeElement(position_HAIRMOD_PROTECTED2);
				}
			}
		}
		if (Res_HAIRMOD_PROTECTED.getImgArrow(type) == null || magic.isEnd)
		{
			return;
		}
		if (type == 5)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[3], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_1, yDau_1, 3);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[3], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_2, yDau_2, 3);
			if (isDoubleDragon)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[3], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_3, yDau_3, 3);
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[3], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_4, yDau_4, 3);
			}
		}
		else
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[type], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_1, yDau_1, 3);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[type], 0, magic.headArrowFrame * 24, 24, 24, magic.headTransform, xDau_2, yDau_2, 3);
		}
	}
}
