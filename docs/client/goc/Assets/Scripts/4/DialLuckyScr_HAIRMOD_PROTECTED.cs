public class DialLuckyScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public class Gift
	{
		public short idPart;

		public sbyte type;

		public int xu;

		public int x;

		public int y;

		public int xp;

		public int luong;

		public string expire = string.Empty;
	}

	private static DialLuckyScr_HAIRMOD_PROTECTED me;

	private Image_HAIRMOD_PROTECTED imgCau_back;

	private Image_HAIRMOD_PROTECTED imgDo;

	private Image_HAIRMOD_PROTECTED imgDauHoi;

	private FrameImage_HAIRMOD_PROTECTED imgFireWork;

	public int radius;

	public int degree;

	public int part;

	public int g;

	public int degreeKim;

	public int num;

	public int selectedNumber;

	private Position_HAIRMOD_PROTECTED posCenter;

	private bool isTurn;

	private bool isPaint;

	private bool isable;

	public MyScreen_HAIRMOD_PROTECTED lastScr;

	private Command_HAIRMOD_PROTECTED cmdDial;

	private Command_HAIRMOD_PROTECTED cmdWait;

	private Command_HAIRMOD_PROTECTED cmdClose;

	private Command_HAIRMOD_PROTECTED cmdErr;

	private Command_HAIRMOD_PROTECTED cmdContinue;

	private Command_HAIRMOD_PROTECTED cmdReChose;

	private mVector_HAIRMOD_PROTECTED listGift = new mVector_HAIRMOD_PROTECTED();

	private long timePaint;

	private bool[] isFireWork;

	private mVector_HAIRMOD_PROTECTED listFireWork;

	public bool isStart;

	private MyRandom_HAIRMOD_PROTECTED r = new MyRandom_HAIRMOD_PROTECTED();

	private int hBorder;

	private static int[] posPaintX = new int[3];

	private static int[] posPaintY = new int[3];

	private sbyte cout = -1;

	public static sbyte[] arrayIndex = null;

	public DialLuckyScr_HAIRMOD_PROTECTED()
	{
		FilePack_HAIRMOD_PROTECTED.init(Main_HAIRMOD_PROTECTED.res + "/qs/qs");
		imgDo = FilePack_HAIRMOD_PROTECTED.getImg("8Chars");
		imgDauHoi = FilePack_HAIRMOD_PROTECTED.getImg("q");
		imgFireWork = FrameImage_HAIRMOD_PROTECTED.init("st", 11, 11);
		imgCau_back = FilePack_HAIRMOD_PROTECTED.getImg("wood21");
		FilePack_HAIRMOD_PROTECTED.reset();
		if (Canvas_HAIRMOD_PROTECTED.w < 200)
		{
			radius = 80;
		}
		else
		{
			radius = 90;
		}
		posCenter = new Position_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.hh + 1);
		part = 30;
		num = 360 / part;
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			isable = true;
		};
		cmdDial = new Command_HAIRMOD_PROTECTED("Bắt đầu");
		cmdDial.action = action;
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
		};
		cmdWait = new Command_HAIRMOD_PROTECTED("Xin chờ");
		cmdWait.action = action2;
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			lastScr.switchToMe();
			doContinue();
			isStart = false;
		};
		cmdClose = new Command_HAIRMOD_PROTECTED("Đóng");
		cmdClose.action = action3;
		ActionPerform_HAIRMOD_PROTECTED action4 = delegate
		{
		};
		cmdReChose = new Command_HAIRMOD_PROTECTED("Chọn lại");
		cmdReChose.action = delegate
		{
			doContinue();
			ItemQsScreen_HAIRMOD_PROTECTED.gI().switchToMe();
			isStart = false;
		};
		cmdContinue = new Command_HAIRMOD_PROTECTED("Tiếp tục");
		cmdContinue.action = delegate
		{
			doContinue();
			isStart = false;
			center = cmdDial;
			left = cmdErr;
			right = cmdClose;
		};
		cmdErr = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdErr.action = action4;
		center = cmdDial;
		right = cmdClose;
		degreeKim = 90;
		isFireWork = new bool[3];
		listFireWork = new mVector_HAIRMOD_PROTECTED();
		hBorder = MFont_HAIRMOD_PROTECTED.borderFont.getHeight();
	}

	public static DialLuckyScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new DialLuckyScr_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		Canvas_HAIRMOD_PROTECTED.keyHold[5] = false;
		base.switchToMe();
		doContinue();
		left = cmdErr;
	}

	protected void doContinue()
	{
		isPaint = false;
		isTurn = false;
		center = cmdDial;
		right = cmdClose;
		timePaint = 0L;
		for (int i = 0; i < 3; i++)
		{
			isFireWork[i] = false;
		}
		listFireWork.removeAllElements();
		listGift.removeAllElements();
		cout = -1;
	}

	public override void update()
	{
		if (lastScr != null)
		{
			lastScr.update();
		}
		if (g > 0)
		{
			degree -= g;
			if (degree < 0)
			{
				degree = 7200 + degree;
			}
			if (g < 10)
			{
				if (degree / 20 % 30 == 0)
				{
					g = 0;
				}
			}
			else
			{
				g--;
			}
		}
		else if (isTurn)
		{
			stop();
		}
		if (cout == 2 && listFireWork.size() == 0)
		{
			getResult();
			cout = 3;
		}
		if (!isPaint)
		{
			return;
		}
		if (center == cmdWait)
		{
			int num = 0;
			for (int i = 0; i < isFireWork.Length; i++)
			{
				if (isFireWork[i])
				{
					num++;
				}
			}
			if (num == 3)
			{
				center = cmdClose;
				left = cmdContinue;
				right = cmdReChose;
			}
		}
		for (int j = 0; j < listFireWork.size(); j++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)listFireWork.elementAt(j);
			point_HAIRMOD_PROTECTED.x += point_HAIRMOD_PROTECTED.g;
			if (point_HAIRMOD_PROTECTED.g > 1 || point_HAIRMOD_PROTECTED.g < -1)
			{
				point_HAIRMOD_PROTECTED.g -= point_HAIRMOD_PROTECTED.g / abs(point_HAIRMOD_PROTECTED.g);
			}
			point_HAIRMOD_PROTECTED.y += point_HAIRMOD_PROTECTED.h;
			point_HAIRMOD_PROTECTED.h++;
			point_HAIRMOD_PROTECTED.color++;
			if (point_HAIRMOD_PROTECTED.color > 20)
			{
				listFireWork.removeElement(point_HAIRMOD_PROTECTED);
			}
		}
		if (arrayIndex == null)
		{
			return;
		}
		for (int k = 0; k < arrayIndex.Length; k++)
		{
			if (!isFireWork[k] && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timePaint > (k + 1) * 5)
			{
				isFireWork[k] = true;
				addFire(posPaintX[k], posPaintY[k]);
			}
		}
	}

	private int abs(int a)
	{
		return (a >= 0) ? a : (-a);
	}

	private void addFire(int x, int y)
	{
		for (int i = 0; i < 10; i++)
		{
			int num = 1;
			if (i % 2 == 0)
			{
				num = -1;
			}
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = new Point_HAIRMOD_PROTECTED(x, y);
			point_HAIRMOD_PROTECTED.color = 0;
			point_HAIRMOD_PROTECTED.g = num * (r.nextInt(80) / 10);
			point_HAIRMOD_PROTECTED.h = -r.nextInt(70) / 10;
			listFireWork.addElement(point_HAIRMOD_PROTECTED);
		}
	}

	private void stop()
	{
		posPaintX = new int[arrayIndex.Length];
		posPaintY = new int[arrayIndex.Length];
		isTurn = false;
		isPaint = true;
		isable = false;
		timePaint = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100;
		for (int i = 0; i < arrayIndex.Length; i++)
		{
			int num = 0;
			switch (i)
			{
			case 0:
				num = 150;
				break;
			case 1:
				num = 180;
				break;
			default:
				num = 210;
				break;
			}
			num = Util_HAIRMOD_PROTECTED.fixangle(num);
			int num2 = radius * Util_HAIRMOD_PROTECTED.Cos(num) >> 10;
			int num3 = -(radius * Util_HAIRMOD_PROTECTED.Sin(num)) >> 10;
			posPaintX[i] = posCenter.x + num2;
			posPaintY[i] = posCenter.y + num3;
		}
	}

	public override void updateKey()
	{
		if (!isPaint)
		{
			if (Canvas_HAIRMOD_PROTECTED.collisionCmdBar() == 1)
			{
				if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
				{
					isable = true;
					Canvas_HAIRMOD_PROTECTED.keyHold[5] = true;
				}
				if (Canvas_HAIRMOD_PROTECTED.isPointerRelease)
				{
					Canvas_HAIRMOD_PROTECTED.keyReleased[5] = true;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyHold[5] && !isTurn && isable)
			{
				if (degreeKim < 270)
				{
					degreeKim += 3;
				}
			}
			else if (degreeKim > 90 && isStart)
			{
				degreeKim -= 3;
			}
			if (Canvas_HAIRMOD_PROTECTED.keyReleased[5])
			{
				if (degreeKim > 90)
				{
					start();
				}
				Canvas_HAIRMOD_PROTECTED.keyReleased[5] = false;
			}
		}
		base.updateKey();
	}

	private void start()
	{
		if (!isTurn && isable)
		{
			selectedNumber = degreeKim;
			GameService_HAIRMOD_PROTECTED.gI().onStartLuckyDraw(ItemQsScreen_HAIRMOD_PROTECTED.index);
			center = cmdWait;
		}
	}

	public void onStart()
	{
		center = cmdWait;
		g = 100 + (selectedNumber - 90);
		isTurn = true;
		right = cmdErr;
		isStart = true;
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (lastScr != null)
		{
			lastScr.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = degree / 20;
		for (int i = 0; i < this.num; i++)
		{
			int num2 = num + i * part;
			if (num2 > 360)
			{
				num2 -= 360;
			}
			if (num2 >= 82 && num2 <= 278)
			{
				int a = Util_HAIRMOD_PROTECTED.fixangle(num2);
				int num3 = radius * Util_HAIRMOD_PROTECTED.Cos(a) >> 10;
				int num4 = -(radius * Util_HAIRMOD_PROTECTED.Sin(a)) >> 10;
				g.drawImage(imgCau_back, posCenter.x + num3, posCenter.y + num4, 3);
			}
		}
		if (isPaint)
		{
			paintGift(g);
		}
		int num5 = 0;
		for (int j = 0; j < this.num; j++)
		{
			int num6 = num + j * part;
			if (num6 > 360)
			{
				num6 -= 360;
			}
			if (num6 >= 82 && num6 <= 278)
			{
				int a2 = Util_HAIRMOD_PROTECTED.fixangle(num6);
				int num7 = radius * Util_HAIRMOD_PROTECTED.Cos(a2) >> 10;
				int num8 = -(radius * Util_HAIRMOD_PROTECTED.Sin(a2)) >> 10;
				long num9 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timePaint;
				if (!isPaint || num6 < 150 || num6 > 210 || (num9 <= (num5 + 1) * 5 && num9 > (num5 + 1) * 5 - 5))
				{
					g.drawImage(imgDauHoi, posCenter.x + num7, posCenter.y + num8, 3);
				}
				else
				{
					num5++;
				}
			}
		}
		g.setColor(9013127);
		g.fillRect(posCenter.x - 18, posCenter.y - 18, 18, 36);
		paintKim(g);
		g.drawRegion(imgDo, 0, 0, imgDo.getWidth(), imgDo.getHeight(), 0, posCenter.x, posCenter.y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
		g.drawRegion(imgDo, 0, 0, imgDo.getWidth(), imgDo.getHeight(), 1, posCenter.x, posCenter.y, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
		if (ItemQsScreen_HAIRMOD_PROTECTED.index >= 0 && ItemQsScreen_HAIRMOD_PROTECTED.index < ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.size())
		{
			((ItemLuckyDraw_HAIRMOD_PROTECTED)ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.elementAt(ItemQsScreen_HAIRMOD_PROTECTED.index))?.paint(g, posCenter.x - imgDo.getWidth() + 28, posCenter.y);
		}
		if (isPaint || this.g > 0)
		{
			paintFireWork(g);
		}
		base.paint(g);
	}

	private void paintFireWork(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < listFireWork.size(); i++)
		{
			Point_HAIRMOD_PROTECTED point_HAIRMOD_PROTECTED = (Point_HAIRMOD_PROTECTED)listFireWork.elementAt(i);
			int num = point_HAIRMOD_PROTECTED.color / 5;
			if (num < 4)
			{
				imgFireWork.drawFrame(num, point_HAIRMOD_PROTECTED.x, point_HAIRMOD_PROTECTED.y, 0, 3, g);
			}
			cout++;
			if (cout >= 2)
			{
				cout = 2;
			}
		}
	}

	private void paintKim(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int a = Util_HAIRMOD_PROTECTED.fixangle(degreeKim);
		int num = (radius / 3 + 2) * Util_HAIRMOD_PROTECTED.Cos(a) >> 10;
		int num2 = -((radius / 3 + 2) * Util_HAIRMOD_PROTECTED.Sin(a)) >> 10;
		int num3 = degreeKim + 90;
		if (num3 > 360)
		{
			num3 -= 360;
		}
		int a2 = Util_HAIRMOD_PROTECTED.fixangle(num3);
		int num4 = 6 * Util_HAIRMOD_PROTECTED.Cos(a2) >> 10;
		int num5 = -(6 * Util_HAIRMOD_PROTECTED.Sin(a2)) >> 10;
		int num6 = degreeKim - 90;
		if (num6 < 0)
		{
			num6 = 360 + num6;
		}
		num6 = Util_HAIRMOD_PROTECTED.fixangle(num6);
		int num7 = 6 * Util_HAIRMOD_PROTECTED.Cos(num6) >> 10;
		int num8 = -(6 * Util_HAIRMOD_PROTECTED.Sin(num6)) >> 10;
		g.setColor(13935404);
		int h = (degreeKim - 90) * 36 / 180;
		if (degreeKim - 90 <= 0)
		{
			h = 0;
		}
		g.fillRect(posCenter.x - 18, posCenter.y - 18, 18, h);
	}

	public void getResult()
	{
		string text = "Bạn nhận được: ";
		if (arrayIndex != null)
		{
			for (int i = 0; i < arrayIndex.Length; i++)
			{
				if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timePaint > (i + 1) * 5)
				{
					ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = (ItemLuckyDraw_HAIRMOD_PROTECTED)ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.elementAt(arrayIndex[i]);
					text = text + "\n" + itemLuckyDraw_HAIRMOD_PROTECTED.name;
				}
			}
		}
		Canvas_HAIRMOD_PROTECTED.startOKDlg(text);
	}

	private void paintGift(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (arrayIndex == null)
		{
			return;
		}
		for (int i = 0; i < arrayIndex.Length; i++)
		{
			if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timePaint > (i + 1) * 5)
			{
				ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = (ItemLuckyDraw_HAIRMOD_PROTECTED)ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.elementAt(arrayIndex[i]);
				itemLuckyDraw_HAIRMOD_PROTECTED.paint(g, posPaintX[i], posPaintY[i]);
			}
		}
	}
}
