public class PaintPopup_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	private static PaintPopup_HAIRMOD_PROTECTED me;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	private int x;

	private int y;

	private int x0;

	private int y0;

	private int w;

	private int h;

	private int numW;

	private int wOne;

	public int selected;

	public static MyScreen_HAIRMOD_PROTECTED lastScr;

	public mVector_HAIRMOD_PROTECTED cmd;

	public string title;

	public bool transY;

	public bool transXB;

	public bool canSelecet;

	public bool isTabIp;

	public bool isPaintFc;

	private long timePointY;

	private long timePointX;

	private long count;

	private long timeOpen;

	private long timeDelay;

	private int pxLast;

	private int pyLast;

	private int pa;

	private int dyTran;

	private int dxTran;

	private int sizeW;

	private int sizeH;

	private int disX;

	private int xBegin;

	private int yBegin;

	private int wTouch;

	private int hTouch;

	private int curentTabUpdate;

	private int timeHold;

	private int countPutKey;

	public float vY;

	public float vX;

	public PaintPopup_HAIRMOD_PROTECTED()
	{
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			lastScr.switchToMe();
			gI().cmd.removeAllElements();
			gI().title = null;
			lastScr = null;
		};
		right.action = action;
		center = new Command_HAIRMOD_PROTECTED("Chọn");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = (Command_HAIRMOD_PROTECTED)gI().cmd.elementAt(gI().selected);
			command_HAIRMOD_PROTECTED.perform();
		};
		center.action = action2;
	}

	public static PaintPopup_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new PaintPopup_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		lastScr = Canvas_HAIRMOD_PROTECTED.currentScreen;
		base.switchToMe();
	}

	public void setPos(int x0, int y0, int w0, int h0, int numW)
	{
		x = x0;
		y = y0;
		w = w0;
		h = h0;
		this.numW = numW;
		wOne = (w - 6) / numW;
		this.x0 = (this.y0 = (w - 6 - wOne * numW) / 2);
	}

	public void setInfo(mVector_HAIRMOD_PROTECTED cmd, string title)
	{
		this.title = title;
		this.cmd = cmd;
		int num = 0;
		if (cmd.size() % numW != 0)
		{
			num = 1;
		}
		cmyLim = (cmd.size() / numW + num) * wOne - (h - 32);
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
	}

	public override void update()
	{
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
	}

	public override void updateKey()
	{
		xBegin = x;
		yBegin = y;
		wTouch = w;
		hTouch = h;
		updateKeyDong();
		moveCamera();
		base.updateKey();
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (lastScr != null)
		{
			lastScr.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, x, y, w, h);
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, title, x + w / 2, y + 5, 2);
		g.translate(x + 3 + x0, y + 26 + y0);
		g.setClip(0, 0, w - x0 * 2, h - 30 - y0);
		g.translate(0, -cmy);
		if (isPaintFc)
		{
			Res_HAIRMOD_PROTECTED.paintSelected(g, selected % numW * wOne, selected / numW * wOne, wOne, wOne);
		}
		int num = cmy / wOne * numW;
		if (num <= 0)
		{
			num = 0;
		}
		int num2 = num + ((h - 31) / wOne + 2) * numW;
		if (num2 > cmd.size())
		{
			num2 = cmd.size();
		}
		for (int i = num; i < num2; i++)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = (Command_HAIRMOD_PROTECTED)cmd.elementAt(i);
			if (command_HAIRMOD_PROTECTED != null)
			{
				g.setColor(Res_HAIRMOD_PROTECTED.color[0]);
				g.drawRect(i % numW * wOne, i / numW * wOne, wOne, wOne);
				command_HAIRMOD_PROTECTED.paint(g, i % numW * wOne + wOne / 2, i / numW * wOne + wOne / 2);
			}
		}
		base.paint(g);
	}

	public void checkSelect()
	{
		int num = x + 3 + x0;
		int num2 = y + 26 + y0;
		int num3 = (cmtoY + Canvas_HAIRMOD_PROTECTED.py - num2) / wOne;
		int num4 = (Canvas_HAIRMOD_PROTECTED.px - num) / wOne;
		selected = num3 * numW + num4;
	}

	public void updateKeyDong()
	{
		count++;
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (timeOpen > 0)
		{
			timeOpen--;
			isPaintFc = true;
			if (timeOpen == 0L && canSelecet)
			{
				checkSelect();
				canSelecet = false;
			}
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownItem && Canvas_HAIRMOD_PROTECTED.isPointer(xBegin, yBegin, wTouch, hTouch))
		{
			pyLast = Canvas_HAIRMOD_PROTECTED.pyLast;
			pxLast = Canvas_HAIRMOD_PROTECTED.pxLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownItem = false;
			timeDelay = count;
			pa = cmy;
			transY = true;
			vY = 0f;
			vX = 0f;
		}
		if (!transY)
		{
			return;
		}
		long num = count - timeDelay;
		int num2 = pyLast - Canvas_HAIRMOD_PROTECTED.py;
		pyLast = Canvas_HAIRMOD_PROTECTED.py;
		int num3 = pxLast - Canvas_HAIRMOD_PROTECTED.px;
		pxLast = Canvas_HAIRMOD_PROTECTED.px;
		if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
		{
			if (count % 2 == 0)
			{
				dyTran = Canvas_HAIRMOD_PROTECTED.py;
				dxTran = Canvas_HAIRMOD_PROTECTED.px;
				timePointY = count;
				timePointX = count;
			}
			vY = 0f;
			vX = 0f;
			if (cmtoY <= 0 || cmtoY >= cmyLim)
			{
				if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
				{
					cmtoY = pa + Canvas_HAIRMOD_PROTECTED.dy() / 2;
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
			{
				cmtoY = pa + num2;
				pa = cmtoY;
			}
			cmy = cmtoY;
			if (Main_HAIRMOD_PROTECTED.canvas.canPutKey())
			{
				timeHold++;
				if (timeHold >= 5)
				{
					timeHold = 5;
					isPaintFc = true;
					checkSelect();
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.canNotPutKey())
			{
				timeHold = 0;
				countPutKey = 0;
				isPaintFc = false;
			}
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointerRelease)
		{
			return;
		}
		transY = false;
		int num4 = (int)(count - timePointY);
		float num5 = dyTran - Canvas_HAIRMOD_PROTECTED.py;
		float num6 = dxTran - Canvas_HAIRMOD_PROTECTED.px;
		if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
		{
			vY = num5 / (float)num4 * 10f;
		}
		int num7 = (int)(count - timePointX);
		if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrX())
		{
			vX = num6 / (float)num7 * 10f;
		}
		timePointY = -1L;
		timePointX = -1L;
		if (Main_HAIRMOD_PROTECTED.canvas.canPutKey() && timeHold != 5)
		{
			if (num <= 4)
			{
				timeHold = 0;
				timeOpen = 5L;
				canSelecet = true;
				checkSelect();
				countPutKey++;
			}
		}
		else if (timeHold >= 5)
		{
			timeHold = 0;
			timeOpen = 5L;
			canSelecet = true;
			checkSelect();
			countPutKey++;
		}
		transXB = false;
	}

	public void moveCamera()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (vY != 0f)
		{
			if (cmy < 0 || cmy > cmyLim)
			{
				if (vY > 500f)
				{
					vY = 500f;
				}
				else if (vY < -500f)
				{
					vY = -500f;
				}
				vY -= vY / 5f;
				if (Math_HAIRMOD_PROTECTED.abs((int)(vY / 10f)) <= 10)
				{
					vY = 0f;
				}
			}
			cmy += (int)(vY / 15f);
			cmtoY = cmy;
			vY -= vY / 20f;
		}
		else if (cmy < 0)
		{
			cmtoY = 0;
		}
		else if (cmy > cmyLim)
		{
			cmtoY = cmyLim;
		}
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
	}
}
