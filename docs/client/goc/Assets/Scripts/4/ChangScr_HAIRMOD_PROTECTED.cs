using System;

public class ChangScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	private static ChangScr_HAIRMOD_PROTECTED me;

	private int x;

	private int y;

	private int index;

	private int trans;

	private int ang;

	private int dis;

	private int lastTime;

	private mVector_HAIRMOD_PROTECTED list = new mVector_HAIRMOD_PROTECTED();

	private int map = -1;

	private int xMap;

	private int yMap;

	public static ChangScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ChangScr_HAIRMOD_PROTECTED());
	}

	public void setMap(int map, int xMap, int yMap)
	{
		this.map = map;
		this.xMap = xMap;
		this.yMap = yMap;
	}

	public override void update()
	{
		if (map > -1 && Environment.TickCount / 1000 - lastTime > 30)
		{
			lastTime = Environment.TickCount / 1000;
			GameService_HAIRMOD_PROTECTED.gI().changeMap(map, xMap, yMap);
		}
		updateWaiting();
	}

	public void disConnectWhenTimeLimit()
	{
		Canvas_HAIRMOD_PROTECTED.instance.timeRemove = -1L;
		Canvas_HAIRMOD_PROTECTED.infoDisConnect = "Mất kết nối.";
		Canvas_HAIRMOD_PROTECTED.xInfoDisConnect = Canvas_HAIRMOD_PROTECTED.w;
		Main_HAIRMOD_PROTECTED.isDisConnect = true;
		Main_HAIRMOD_PROTECTED.isStartSaveIp = false;
		Canvas_HAIRMOD_PROTECTED.endDlg();
		Canvas_HAIRMOD_PROTECTED.instance.init();
		Session_ME_HAIRMOD_PROTECTED.gI().close();
		Tilemap_HAIRMOD_PROTECTED.loadMap(0, null);
		ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	public override bool keyPress(int keyCode)
	{
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(0);
		g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		for (int i = 0; i < list.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)list.elementAt(i);
			if (Res_HAIRMOD_PROTECTED.imgEffect[8] == null)
			{
				Res_HAIRMOD_PROTECTED.getImgEffect(8);
			}
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgEffect[8], 0, position_HAIRMOD_PROTECTED.index / 2 * 10, 10, 10, 0, position_HAIRMOD_PROTECTED.x, position_HAIRMOD_PROTECTED.y, 3);
		}
		if (Res_HAIRMOD_PROTECTED.imgArrow[3] == null)
		{
			Res_HAIRMOD_PROTECTED.loadImgArrow(3);
		}
		if (Res_HAIRMOD_PROTECTED.imgArrow[3] != null)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[3], 0, index * 24, 24, 24, trans, x, y, 3);
		}
	}

	public override void switchToMe()
	{
		ang = 0;
		dis = 5;
		list.removeAllElements();
		lastTime = Environment.TickCount / 1000;
		base.switchToMe();
	}

	private void updateWaiting()
	{
		ang += 15;
		if (ang >= 360)
		{
			ang -= 360;
		}
		int num = ang - 15;
		if (num < 0)
		{
			num = 360 + num;
		}
		int num2 = dis * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(ang)) >> 10;
		int num3 = -(dis * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(ang))) >> 10;
		int num4 = dis * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(num)) >> 10;
		int num5 = -(dis * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(num))) >> 10;
		if (dis < 30)
		{
			dis++;
		}
		x = Canvas_HAIRMOD_PROTECTED.hw + num2;
		y = Canvas_HAIRMOD_PROTECTED.hh + num3;
		int num6 = Arrow_HAIRMOD_PROTECTED.findDirIndexFromAngle(Util_HAIRMOD_PROTECTED.angle(x - (Canvas_HAIRMOD_PROTECTED.hw + num4), -(y - (Canvas_HAIRMOD_PROTECTED.hh + num5))));
		index = Arrow_HAIRMOD_PROTECTED.FRAME[num6];
		trans = Arrow_HAIRMOD_PROTECTED.TRANSFORM[num6];
		list.addElement(new Position_HAIRMOD_PROTECTED(x, y));
		for (int i = 0; i < list.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)list.elementAt(i);
			position_HAIRMOD_PROTECTED.index++;
			if (position_HAIRMOD_PROTECTED.index >= 20)
			{
				list.removeElement(position_HAIRMOD_PROTECTED);
			}
		}
	}
}
