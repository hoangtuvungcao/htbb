public class HelpScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	private static HelpScr_HAIRMOD_PROTECTED me;

	public int cmtoY;

	public int cmy;

	public int cmdy;

	public int cmvy;

	public int cmyLim = 10000;

	private string[] stInfo;

	private int[] font;

	private short x;

	private short y;

	private short w;

	private short h;

	public int xTr;

	public int yTr;

	private bool trans;

	private int pa;

	public bool transYShowText;

	private long timePointYt;

	private long timeOpent;

	private long timeDelayt;

	private int pyLastt;

	private int dyTrant;

	private int dxTrant;

	private int xBegint;

	private int yBegint;

	private int wToucht;

	private int hToucht;

	private int countShowText;

	public float vYT;

	public HelpScr_HAIRMOD_PROTECTED()
	{
		int num = Canvas_HAIRMOD_PROTECTED.w;
		if (num > 480)
		{
			num = 480;
		}
		int num2 = Canvas_HAIRMOD_PROTECTED.h;
		if (num2 > 320)
		{
			num2 = 320;
		}
		x = (y = 20);
		w = (short)(num - x * 2);
		h = (short)(num2 - y * 2 - MyScreen_HAIRMOD_PROTECTED.deltaY);
		if (Canvas_HAIRMOD_PROTECTED.h * Canvas_HAIRMOD_PROTECTED.w > 153600)
		{
			xTr = (Canvas_HAIRMOD_PROTECTED.w - w) / 2;
			yTr = (Canvas_HAIRMOD_PROTECTED.h - h) / 2;
		}
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
			me = null;
			stInfo = null;
		};
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		right.action = action;
		GameService_HAIRMOD_PROTECTED.gI().requestString(0);
		Canvas_HAIRMOD_PROTECTED.startWaitDlg();
	}

	public static HelpScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new HelpScr_HAIRMOD_PROTECTED());
	}

	public void setInfo(string st)
	{
		stInfo = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(st, w - 100);
		int num = 0;
		font = new int[stInfo.Length];
		for (int i = 0; i < stInfo.Length; i++)
		{
			if (stInfo[i].StartsWith("x0"))
			{
				stInfo[i] = stInfo[i].Substring(3);
				num = 0;
			}
			else if (stInfo[i].StartsWith("x1"))
			{
				stInfo[i] = stInfo[i].Substring(3);
				num = 1;
			}
			font[i] = num;
		}
		cmyLim = stInfo.Length * 13 - (h - 35);
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public override bool keyPress(int keyCode)
	{
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
		g.translate(x, y - 10);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xTr, yTr, w, h);
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, "Hướng dẫn", Canvas_HAIRMOD_PROTECTED.hw, 4 + yTr, 2);
		g.translate(0, 30);
		g.setClip(xTr, -2 + yTr, w - 4, h - 32);
		g.translate(0, -cmy);
		if (stInfo != null)
		{
			int num = cmy / 13;
			if (num <= 0)
			{
				num = 0;
			}
			int num2 = num + (h - 35) / 13 + 2;
			if (num2 > stInfo.Length)
			{
				num2 = stInfo.Length;
			}
			for (int i = num; i < num2; i++)
			{
				MFont_HAIRMOD_PROTECTED mFont_HAIRMOD_PROTECTED = ((font[i] != 0) ? MFont_HAIRMOD_PROTECTED.normalFont[0] : MFont_HAIRMOD_PROTECTED.arialFontW);
				mFont_HAIRMOD_PROTECTED.drawString(g, stInfo[i], 7 + xTr, i * 13 + yTr, 0);
			}
		}
		base.paint(g);
	}

	public override void switchToMe()
	{
		base.switchToMe();
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.update();
		xBegint = 20 + xTr;
		yBegint = 20 + yTr;
		int num = Canvas_HAIRMOD_PROTECTED.w;
		if (num > 480)
		{
			num = 480;
		}
		int num2 = Canvas_HAIRMOD_PROTECTED.h;
		if (num2 > 320)
		{
			num2 = 320;
		}
		wToucht = num - 40;
		hToucht = num2 - 60;
		updateKeyShowText();
		base.update();
	}

	public void updateKeyShowText()
	{
		countShowText++;
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownText && Canvas_HAIRMOD_PROTECTED.isPointer(xBegint, yBegint, wToucht, hToucht))
		{
			pyLastt = Canvas_HAIRMOD_PROTECTED.pyLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownText = false;
			timeDelayt = countShowText;
			pa = cmy;
			transYShowText = true;
			vYT = 0f;
		}
		if (transYShowText)
		{
			long num = countShowText - timeDelayt;
			int num2 = pyLastt - Canvas_HAIRMOD_PROTECTED.py;
			pyLastt = Canvas_HAIRMOD_PROTECTED.py;
			if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
			{
				if (countShowText % 2 == 0)
				{
					dyTrant = Canvas_HAIRMOD_PROTECTED.py;
					timePointYt = countShowText;
				}
				vYT = 0f;
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
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerRelease)
			{
				transYShowText = false;
				int num3 = (int)(countShowText - timePointYt);
				float num4 = dyTrant - Canvas_HAIRMOD_PROTECTED.py;
				float num5 = dxTrant - Canvas_HAIRMOD_PROTECTED.px;
				vYT = num4 / (float)num3 * 10f;
				timePointYt = -1L;
			}
		}
		moveCameraShowText();
	}

	public void moveCameraShowText()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (vYT != 0f)
		{
			if (cmy < 0 || cmy > cmyLim)
			{
				if (vYT > 500f)
				{
					vYT = 500f;
				}
				else if (vYT < -500f)
				{
					vYT = -500f;
				}
				vYT -= vYT / 5f;
				if (Math_HAIRMOD_PROTECTED.abs((int)(vYT / 10f)) <= 10)
				{
					vYT = 0f;
				}
			}
			cmy += (int)(vYT / 15f);
			cmtoY = cmy;
			vYT -= vYT / 20f;
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
