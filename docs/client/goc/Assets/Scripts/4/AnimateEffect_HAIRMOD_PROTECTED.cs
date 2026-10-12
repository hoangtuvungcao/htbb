public class AnimateEffect_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	public const sbyte RAIN = 0;

	public const sbyte FALLING_LEAVES = 1;

	public const sbyte SNOW = 2;

	public const sbyte FALLING_FLOWER = 3;

	public const sbyte FIREWORK = 4;

	public new sbyte type;

	public long number;

	public long timeLimit;

	public long curTime;

	private mVector_HAIRMOD_PROTECTED list = new mVector_HAIRMOD_PROTECTED();

	public bool isStop;

	public static int wind = 5;

	public static int countWind;

	public static int dirWind = Res_HAIRMOD_PROTECTED.rnd(1, -1);

	public short idImg;

	public AnimateEffect_HAIRMOD_PROTECTED(sbyte type, bool isStart, int number, int timeLimit)
	{
		this.timeLimit = timeLimit;
		curTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000;
		this.type = type;
		this.number = number;
		switch (type)
		{
		case 1:
			idImg = 801;
			break;
		case 2:
			idImg = 805;
			break;
		case 3:
			idImg = 802;
			this.type = 3;
			break;
		case 4:
			idImg = 803;
			this.type = 3;
			break;
		case 5:
			idImg = 804;
			this.type = 3;
			break;
		}
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = null;
			if (isStart)
			{
				point_HAIRMOD_PROTECTED = new Point_HAIRMOD_PROTECTED((GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw + Res_HAIRMOD_PROTECTED.rnd(Canvas_HAIRMOD_PROTECTED.w * 2)) * 10, (GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.h * 2 + Res_HAIRMOD_PROTECTED.rnd(Canvas_HAIRMOD_PROTECTED.h * 2)) * 10);
			}
			else
			{
				point_HAIRMOD_PROTECTED = new Point_HAIRMOD_PROTECTED();
				rndPos(point_HAIRMOD_PROTECTED);
			}
			if (type == 2 || this.type == 3)
			{
				point_HAIRMOD_PROTECTED.h = Res_HAIRMOD_PROTECTED.rnd(3);
			}
			else
			{
				point_HAIRMOD_PROTECTED.h = Res_HAIRMOD_PROTECTED.rnd(4);
			}
			point_HAIRMOD_PROTECTED.limitY = 16 + Res_HAIRMOD_PROTECTED.rnd(3) * 4;
			point_HAIRMOD_PROTECTED.v = Res_HAIRMOD_PROTECTED.rnd(-1, 1);
			point_HAIRMOD_PROTECTED.color = Res_HAIRMOD_PROTECTED.rnd(point_HAIRMOD_PROTECTED.limitY);
			point_HAIRMOD_PROTECTED.dis = (sbyte)Res_HAIRMOD_PROTECTED.rnd(20);
			list.addElement(point_HAIRMOD_PROTECTED);
		}
	}

	public void close()
	{
		GameScr_HAIRMOD_PROTECTED.effAnimate.removeElement(this);
	}

	public void stop()
	{
		isStop = true;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.translate(-GameScr_HAIRMOD_PROTECTED.cmx, -GameScr_HAIRMOD_PROTECTED.cmy);
		switch (type)
		{
		case 0:
			paintRain(g);
			break;
		case 1:
			paintFallingLeaves(g);
			break;
		case 3:
			paintFallingFlower(g);
			break;
		case 2:
			paintSnow(g);
			break;
		}
	}

	private void paintSnow(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = null;
		if (GameData_HAIRMOD_PROTECTED.getImgIcon(idImg) != null)
		{
			image_HAIRMOD_PROTECTED = GameData_HAIRMOD_PROTECTED.getImgIcon(idImg).img;
		}
		if (image_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null && point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx && point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w && point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy && GameData_HAIRMOD_PROTECTED.getImgIcon(idImg) != null)
			{
				g.drawRegion(image_HAIRMOD_PROTECTED, 0, (2 - point_HAIRMOD_PROTECTED.h) * 5, 5, 5, 0, point_HAIRMOD_PROTECTED.x / 10, point_HAIRMOD_PROTECTED.y / 10, 3);
			}
		}
	}

	private void paintFallingFlower(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = null;
		if (GameData_HAIRMOD_PROTECTED.getImgIcon(idImg) != null)
		{
			image_HAIRMOD_PROTECTED = GameData_HAIRMOD_PROTECTED.getImgIcon(idImg).img;
		}
		int num = 0;
		if (image_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null && point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx && point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w && point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy)
			{
				num = 2 - point_HAIRMOD_PROTECTED.h + 1;
				if (num < 2)
				{
					num = point_HAIRMOD_PROTECTED.dis / 10;
				}
				g.drawRegion(image_HAIRMOD_PROTECTED, 0, num * 10, 10, 10, 0, point_HAIRMOD_PROTECTED.x / 10, point_HAIRMOD_PROTECTED.y / 10, 3);
				point_HAIRMOD_PROTECTED.dis++;
				if (point_HAIRMOD_PROTECTED.dis >= 20)
				{
					point_HAIRMOD_PROTECTED.dis = 0;
				}
			}
		}
	}

	private void paintFallingLeaves(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = null;
		if (GameData_HAIRMOD_PROTECTED.getImgIcon(idImg) != null)
		{
			image_HAIRMOD_PROTECTED = GameData_HAIRMOD_PROTECTED.getImgIcon(idImg).img;
		}
		if (image_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null && point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx && point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w && point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy)
			{
				g.drawRegion(image_HAIRMOD_PROTECTED, 0, point_HAIRMOD_PROTECTED.color / (point_HAIRMOD_PROTECTED.limitY / 4) * 10, 16, 10, 0, point_HAIRMOD_PROTECTED.x / 10, point_HAIRMOD_PROTECTED.y / 10, 3);
			}
		}
	}

	private void paintRain(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(14540253);
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null && point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx && point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w && point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy)
			{
				g.fillRect(point_HAIRMOD_PROTECTED.x / 10, point_HAIRMOD_PROTECTED.y / 10, 1, point_HAIRMOD_PROTECTED.h + 2);
			}
		}
	}

	public static void updateWind()
	{
		int num = 1;
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 6 == 3)
		{
			num = Res_HAIRMOD_PROTECTED.rnd(15);
		}
		if (num == 0 && wind == 5)
		{
			wind = 5 + Res_HAIRMOD_PROTECTED.rnd(20);
			countWind = 50 + Res_HAIRMOD_PROTECTED.rnd(100);
		}
		if (countWind > 0)
		{
			countWind--;
		}
		if (countWind == 0 && wind > 5 && Canvas_HAIRMOD_PROTECTED.gameTick % 4 == 2)
		{
			wind--;
		}
	}

	public override void update()
	{
		if (timeLimit > 0 && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - curTime > timeLimit)
		{
			isStop = true;
		}
		switch (type)
		{
		case 0:
			updateRain();
			break;
		case 1:
			updateFallingLeaves();
			break;
		case 3:
			updateFlower();
			break;
		case 2:
			updateSnow();
			break;
		}
	}

	private void updateSnow()
	{
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null)
			{
				point_HAIRMOD_PROTECTED.y += (point_HAIRMOD_PROTECTED.h + 4) * 3;
				point_HAIRMOD_PROTECTED.x += (point_HAIRMOD_PROTECTED.h + 1) * 2 + wind * dirWind;
				if (point_HAIRMOD_PROTECTED.y / 10 < GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy + (Canvas_HAIRMOD_PROTECTED.h + Canvas_HAIRMOD_PROTECTED.hh) - (4 - point_HAIRMOD_PROTECTED.h) * 50 || point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w + Canvas_HAIRMOD_PROTECTED.hw)
				{
					rndPos(point_HAIRMOD_PROTECTED);
				}
			}
		}
	}

	private void updateFlower()
	{
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null)
			{
				point_HAIRMOD_PROTECTED.y += (point_HAIRMOD_PROTECTED.h + 2) * 5;
				point_HAIRMOD_PROTECTED.x += (point_HAIRMOD_PROTECTED.h + 1) * 2 + wind * dirWind;
				if (point_HAIRMOD_PROTECTED.y / 10 < GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy + (Canvas_HAIRMOD_PROTECTED.h + Canvas_HAIRMOD_PROTECTED.hh) - (4 - point_HAIRMOD_PROTECTED.h) * 50 || point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w + Canvas_HAIRMOD_PROTECTED.hw)
				{
					rndPos(point_HAIRMOD_PROTECTED);
				}
			}
		}
	}

	private void updateFallingLeaves()
	{
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null)
			{
				point_HAIRMOD_PROTECTED.y += 10;
				point_HAIRMOD_PROTECTED.x += point_HAIRMOD_PROTECTED.v * 10 + wind * dirWind;
				point_HAIRMOD_PROTECTED.color++;
				if (point_HAIRMOD_PROTECTED.color >= point_HAIRMOD_PROTECTED.limitY)
				{
					point_HAIRMOD_PROTECTED.color = 0;
				}
				if (point_HAIRMOD_PROTECTED.y / 10 < GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy + (Canvas_HAIRMOD_PROTECTED.h + Canvas_HAIRMOD_PROTECTED.hh) - (4 - point_HAIRMOD_PROTECTED.h) * 50 || point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w + Canvas_HAIRMOD_PROTECTED.hw)
				{
					rndPos(point_HAIRMOD_PROTECTED);
				}
			}
		}
	}

	private void updateRain()
	{
		for (int i = 0; i < number; i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)list.elementAt(i);
			if (point_HAIRMOD_PROTECTED != null)
			{
				point_HAIRMOD_PROTECTED.y += (point_HAIRMOD_PROTECTED.h + 1) * 15 + (3 - point_HAIRMOD_PROTECTED.h) * 3;
				point_HAIRMOD_PROTECTED.g++;
				point_HAIRMOD_PROTECTED.x += (3 - point_HAIRMOD_PROTECTED.h + 1) * 2 + wind * dirWind;
				if (point_HAIRMOD_PROTECTED.y / 10 < GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.y / 10 > GameScr_HAIRMOD_PROTECTED.cmy + (Canvas_HAIRMOD_PROTECTED.h + Canvas_HAIRMOD_PROTECTED.hh) - (4 - point_HAIRMOD_PROTECTED.h) * 50 || point_HAIRMOD_PROTECTED.x / 10 < GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw || point_HAIRMOD_PROTECTED.x / 10 > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w + Canvas_HAIRMOD_PROTECTED.hw)
				{
					rndPos(point_HAIRMOD_PROTECTED);
				}
			}
		}
	}

	private void rndPos(Point_HAIRMOD_PROTECTED pos)
	{
		if (isStop)
		{
			list.removeElement(pos);
			number = list.size();
			if (list.size() == 0)
			{
				close();
			}
		}
		else
		{
			pos.y = (GameScr_HAIRMOD_PROTECTED.cmy - Canvas_HAIRMOD_PROTECTED.hh + Res_HAIRMOD_PROTECTED.rnd(Canvas_HAIRMOD_PROTECTED.h * 2)) * 10;
			pos.x = (GameScr_HAIRMOD_PROTECTED.cmx - Canvas_HAIRMOD_PROTECTED.hw + Res_HAIRMOD_PROTECTED.rnd(Canvas_HAIRMOD_PROTECTED.w * 2)) * 10;
			if (type == 2 || type == 3)
			{
				pos.h = Res_HAIRMOD_PROTECTED.rnd(3);
			}
			else
			{
				pos.h = Res_HAIRMOD_PROTECTED.rnd(4);
			}
		}
	}
}
