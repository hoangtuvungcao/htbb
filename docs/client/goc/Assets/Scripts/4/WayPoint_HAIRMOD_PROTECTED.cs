public class WayPoint_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private int index;

	public sbyte clazz;

	private static int[] color = new int[6] { 15006715, 11598072, 7994363, 4062203, 16383610, 16682106 };

	private mVector_HAIRMOD_PROTECTED list = new mVector_HAIRMOD_PROTECTED();

	private bool isWayPoint;

	private bool isJoin;

	public WayPoint_HAIRMOD_PROTECTED(int x, int y, short type, sbyte cla, bool isWay)
	{
		isWayPoint = isWay;
		base.x = x;
		base.y = y;
		base.type = type;
		clazz = cla;
		isJoin = false;
	}

	public override void update()
	{
		if (type != 0 && x + 20 > GameScr_HAIRMOD_PROTECTED.cmx && x - 20 < GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w && y > GameScr_HAIRMOD_PROTECTED.cmy && y < GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h)
		{
			for (int i = 0; i < 2; i++)
			{
				int angle = Res_HAIRMOD_PROTECTED.rnd(360);
				int num = 18 * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
				int num2 = -(18 * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
				num2 += -(num2 / 3);
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = new Position_HAIRMOD_PROTECTED(x + num, y + num2, (sbyte)(15 + Res_HAIRMOD_PROTECTED.rnd(20)));
				position_HAIRMOD_PROTECTED.dir = (sbyte)Res_HAIRMOD_PROTECTED.rnd(20);
				list.addElement(position_HAIRMOD_PROTECTED);
			}
		}
		if (!isWayPoint)
		{
			return;
		}
		if (type != Canvas_HAIRMOD_PROTECTED.gameScr.map)
		{
			if (clazz == 1)
			{
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeAllElements();
			}
			else if (clazz == 0)
			{
				EffectManager_HAIRMOD_PROTECTED.lowEffects.removeAllElements();
			}
		}
		else if (!isJoin && Util_HAIRMOD_PROTECTED.distance(x, y, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.x, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.y) < 17)
		{
			GameService_HAIRMOD_PROTECTED.gI().doWayPoint(x, y, type);
			Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor = null;
			ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
			isJoin = true;
		}
	}

	public void paintEff(MyGraphics_HAIRMOD_PROTECTED g)
	{
		paint(g);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (x + 20 <= GameScr_HAIRMOD_PROTECTED.cmx || x - 20 >= GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w || y <= GameScr_HAIRMOD_PROTECTED.cmy || y - 40 >= GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h)
		{
			return;
		}
		if (clazz == 0)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.getWayPoint(), 0, index / 6 * 24, 39, 24, 0, x, y, 3);
			index++;
			if (index >= 12)
			{
				index = 0;
			}
			return;
		}
		for (int i = 0; i < list.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)list.elementAt(i);
			if (position_HAIRMOD_PROTECTED.dir == 5)
			{
				g.setColor(color[4]);
			}
			else if (position_HAIRMOD_PROTECTED.dir == 15)
			{
				g.setColor(color[5]);
			}
			else
			{
				g.setColor(color[position_HAIRMOD_PROTECTED.follow / 10]);
			}
			g.fillRect(position_HAIRMOD_PROTECTED.x, position_HAIRMOD_PROTECTED.y - (1 + position_HAIRMOD_PROTECTED.follow / 3), 1, 1 + position_HAIRMOD_PROTECTED.follow / 3);
			position_HAIRMOD_PROTECTED.y -= 2;
			position_HAIRMOD_PROTECTED.follow--;
			if (position_HAIRMOD_PROTECTED.follow < 0)
			{
				list.removeElementAt(i);
			}
		}
	}
}
