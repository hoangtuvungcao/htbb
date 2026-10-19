using System;

public class ChangScr : MyScreen
{
	private static ChangScr me;

	private int x;

	private int y;

	private int index;

	private int trans;

	private int ang;

	private int dis;

	private int lastTime;

	private long batDau;

	private mVector list = new mVector();

	private int map = -1;

	private int xMap;

	private int yMap;

	public static ChangScr gI()
	{
		return (me != null) ? me : (me = new ChangScr());
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
			GameService.gI().changeMap(map, xMap, yMap);
		}
	}

	public void disConnectWhenTimeLimit()
	{
		Canvas.instance.timeRemove = -1L;
		Canvas.infoDisConnect = "Mất kết nối.";
		Canvas.xInfoDisConnect = Canvas.w;
		Main.isDisConnect = true;
		Main.isStartSaveIp = false;
		Canvas.endDlg();
		Canvas.instance.init();
		Session_ME.gI().close();
		Tilemap.loadMap(0, null);
		ServerListScr.gI().switchToMe();
	}

	public override bool keyPress(int keyCode)
	{
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics g)
	{
		// HTBB: màn chuyển bản đồ kiểu Pokémon (= client_jar ChangScr): nền key art, thẻ chờ có Poké Ball quay,
		// "Đang tải bản đồ..." + thanh tiến trình ước lượng theo thời gian (chậm dần tới ~95%)
		Canvas.resetTrans(g);
		g.setColor(0);
		g.fillRect(0, 0, Canvas.w, Canvas.h);
		BannerParallax.ve(g, false);
		Canvas.resetTrans(g);
		int cw = System.Math.Min(Canvas.w - 24, 220);
		int ch = 46;
		int cx = (Canvas.w - cw) / 2;
		int cy = System.Math.Min(Canvas.h * 2 / 3 - ch / 2, Canvas.h - 8 - ch);
		GiaoDien.hop(g, cx, cy, cw, ch);
		int r = 12;
		int bx = cx + 10 + r;
		GiaoDien.dangTai(g, bx, cy + ch / 2, r);
		int tx = bx + r + 10;
		int tw = cx + cw - 12 - tx;
		string chu = "Đang tải bản đồ" + new string('.', (Environment.TickCount / 400) & 3);
		MFont f = MFont.arialFont != null ? MFont.arialFont : MFont.normalFont[0];
		if (f != null)
		{
			f.drawString(g, chu, tx, cy + 8, 0);
		}
		long t = System.Math.Max(0L, (long)Environment.TickCount - batDau);
		int hy = cy + ch - 15;
		GiaoDien.khoi(g, tx, hy, tw, 7, 3, GiaoDien.VIEN);
		GiaoDien.khoi(g, tx + 1, hy + 1, tw - 2, 5, 2, 0x3A4258);
		int dai = System.Math.Max(3, (int)((tw - 2) * (950L * t / (t + 1200L)) / 1000));
		GiaoDien.khoiDoc(g, tx + 1, hy + 1, dai, 5, 2, GiaoDien.VANG_NHAT, 0xFF9F1C);
	}

	public override void switchToMe()
	{
		lastTime = Environment.TickCount / 1000;
		batDau = Environment.TickCount;
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
		int num2 = dis * Util.Cos(Util.fixangle(ang)) >> 10;
		int num3 = -(dis * Util.Sin(Util.fixangle(ang))) >> 10;
		int num4 = dis * Util.Cos(Util.fixangle(num)) >> 10;
		int num5 = -(dis * Util.Sin(Util.fixangle(num))) >> 10;
		if (dis < 30)
		{
			dis++;
		}
		x = Canvas.hw + num2;
		y = Canvas.hh + num3;
		int num6 = Arrow.findDirIndexFromAngle(Util.angle(x - (Canvas.hw + num4), -(y - (Canvas.hh + num5))));
		index = Arrow.FRAME[num6];
		trans = Arrow.TRANSFORM[num6];
		list.addElement(new Position(x, y));
		for (int i = 0; i < list.size(); i++)
		{
			Position position = (Position)list.elementAt(i);
			position.index++;
			if (position.index >= 20)
			{
				list.removeElement(position);
			}
		}
	}
}
