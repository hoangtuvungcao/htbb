public class InputDlg_HAIRMOD_PROTECTED : Dialog_HAIRMOD_PROTECTED
{
	protected string title;

	public TField_HAIRMOD_PROTECTED tfInput;

	public ActionPerform_HAIRMOD_PROTECTED okAction;

	public ActionPerform_HAIRMOD_PROTECTED backAction;

	private int w;

	public bool isMax;

	public static InputDlg_HAIRMOD_PROTECTED me = new InputDlg_HAIRMOD_PROTECTED();

	private int hitem;

	private int wDia;

	private int hDia;

	private int xDia;

	private int yDia;

	private int numw;

	private int hTouch;

	private int istouchMore;

	private int numh;

	private string[] strinfo;

	private string[] name;

	private bool iscroll;

	private bool isShow;

	public TField_HAIRMOD_PROTECTED[] mtfInput;

	public static Scroll_HAIRMOD_PROTECTED scr = new Scroll_HAIRMOD_PROTECTED();

	private int select;

	public InputDlg_HAIRMOD_PROTECTED()
	{
		tfInput = new TField_HAIRMOD_PROTECTED();
		tfInput.height = 21;
		tfInput.y = Canvas_HAIRMOD_PROTECTED.h - 76;
		tfInput.isFocus = true;
	}

	public static InputDlg_HAIRMOD_PROTECTED gI()
	{
		if (me == null)
		{
			me = new InputDlg_HAIRMOD_PROTECTED();
		}
		return me;
	}

	public void init()
	{
		tfInput.y = Canvas_HAIRMOD_PROTECTED.h - 76;
		w = MFont_HAIRMOD_PROTECTED.borderFont.getWidth(title) + 30;
		if (w > Canvas_HAIRMOD_PROTECTED.w)
		{
			string text = string.Empty;
			for (int i = 0; i < title.Length - 1; i++)
			{
				text += title.Substring(i, 1);
				if (MFont_HAIRMOD_PROTECTED.borderFont.getWidth(text) >= Canvas_HAIRMOD_PROTECTED.w - 50)
				{
					title = text + "...";
					w = MFont_HAIRMOD_PROTECTED.borderFont.getWidth(title) + 30;
					break;
				}
			}
		}
		else if (!isMax)
		{
			w = Canvas_HAIRMOD_PROTECTED.w - 30;
		}
		if (w < 110)
		{
			w = 110;
		}
		tfInput.width = w - 40;
		tfInput.x = Canvas_HAIRMOD_PROTECTED.hw - tfInput.width / 2;
	}

	public void setInfo(string[] strinfo, string title, ActionPerform_HAIRMOD_PROTECTED ok, int type, int num, bool isMaxW)
	{
		mtfInput = null;
		isMax = isMaxW;
		tfInput.setText(string.Empty);
		tfInput.setIputType(type);
		tfInput.setMaxTextLenght(num);
		this.title = title;
		isMax = isMaxW;
		tfInput.setText(string.Empty);
		tfInput.setIputType(type);
		tfInput.setMaxTextLenght(num);
		ActionPerform_HAIRMOD_PROTECTED actionPerform_HAIRMOD_PROTECTED = delegate
		{
		};
		this.title = title;
		okAction = ok;
		isShow = true;
		backAction = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.chatMode = false;
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		left = new Command_HAIRMOD_PROTECTED("Đóng");
		left.action = backAction;
		center = new Command_HAIRMOD_PROTECTED("Ok");
		center.action = okAction;
		right = tfInput.cmdClear;
		init();
		init();
		hitem = 50;
		wDia = Canvas_HAIRMOD_PROTECTED.w - 30;
		if (wDia > 140)
		{
			wDia = 140;
		}
		mtfInput = new TField_HAIRMOD_PROTECTED[strinfo.Length];
		this.strinfo = strinfo;
		name = name;
		if (mtfInput.Length > 3)
		{
			iscroll = true;
		}
		if (iscroll)
		{
			wDia = Canvas_HAIRMOD_PROTECTED.w / 4 * 3;
			hDia = Canvas_HAIRMOD_PROTECTED.h / 5 * 4;
			wDia = Canvas_HAIRMOD_PROTECTED.w - 30;
			if (wDia > 140)
			{
				wDia = 140;
			}
			if (hDia < 210)
			{
				hDia = 210;
			}
			else if (hDia > 280)
			{
				hDia = 280;
			}
			if (hDia < 210)
			{
				hDia = 210;
			}
			else if (hDia > 280)
			{
				hDia = 280;
			}
			xDia = Canvas_HAIRMOD_PROTECTED.hw - wDia / 2;
			yDia = Canvas_HAIRMOD_PROTECTED.hh - hDia / 2;
			if (yDia < 20)
			{
				yDia = 20;
			}
			if (Canvas_HAIRMOD_PROTECTED.isSmallScreen)
			{
				yDia = 0;
			}
		}
		else
		{
			hDia = (TField_HAIRMOD_PROTECTED.getHeight() + 18) * strinfo.Length + 6 + Canvas_HAIRMOD_PROTECTED.hCommand;
			hDia += hTouch + istouchMore;
			xDia = Canvas_HAIRMOD_PROTECTED.hw - wDia / 2;
			yDia = Canvas_HAIRMOD_PROTECTED.h - Canvas_HAIRMOD_PROTECTED.hCommand * 2 - hDia + 15;
		}
		numw = (wDia - 6) / 32;
		numh = (hDia - 6) / 32;
		for (int i = 0; i < mtfInput.Length; i++)
		{
			mtfInput[i] = new TField_HAIRMOD_PROTECTED(xDia + 10, yDia + 15 + (TField_HAIRMOD_PROTECTED.getHeight() + 18) * i + istouchMore + Canvas_HAIRMOD_PROTECTED.hCommand, wDia - 20, 21);
			mtfInput[i].setText(string.Empty);
		}
	}

	public void setInfo(string title, ActionPerform_HAIRMOD_PROTECTED ok, int type, int num, bool isMaxW)
	{
		isMax = isMaxW;
		tfInput.setText(string.Empty);
		isShow = false;
		tfInput.setIputType(type);
		tfInput.setMaxTextLenght(num);
		this.title = title;
		okAction = ok;
		backAction = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.chatMode = false;
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		left = new Command_HAIRMOD_PROTECTED("Đóng");
		left.action = backAction;
		center = new Command_HAIRMOD_PROTECTED("Ok");
		center.action = okAction;
		right = tfInput.cmdClear;
		init();
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isShow)
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			Res_HAIRMOD_PROTECTED.paintDlgFull(g, Canvas_HAIRMOD_PROTECTED.hw - w / 2, Canvas_HAIRMOD_PROTECTED.h - 110, w, 69);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, title, Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.h - 104, 2);
			tfInput.paint(g);
		}
		else
		{
			paintList(g);
		}
		base.paint(g);
	}

	public void paintList(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDia, yDia, wDia, hDia);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, title, xDia + wDia / 2, yDia + 3, 2);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		scr.setStyle(mtfInput.Length + 1, 50, xDia, yDia, wDia, hDia, true, 1);
		scr.setClip(g, xDia + 10, yDia + Canvas_HAIRMOD_PROTECTED.hCommand, wDia, hDia - hitem - 10);
		int num = yDia + istouchMore + Canvas_HAIRMOD_PROTECTED.hCommand;
		for (int i = 0; i < mtfInput.Length; i++)
		{
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, strinfo[i], Canvas_HAIRMOD_PROTECTED.w / 2, num + i * (TField_HAIRMOD_PROTECTED.getHeight() + 18), 2);
			mtfInput[i].paintByList(g);
		}
	}

	public override void keyPress(int keyCode)
	{
		if (isShow)
		{
			for (int i = 0; i < mtfInput.Length; i++)
			{
				if (mtfInput[i].isFocus)
				{
					mtfInput[i].keyPressed(keyCode);
				}
			}
		}
		if (tfInput.keyPressed(keyCode))
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
		}
	}

	public override void update()
	{
		if (isShow)
		{
			for (int i = 0; i < mtfInput.Length; i++)
			{
				mtfInput[i].updatelist();
			}
			ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = scr.updateKey();
			select = scrollResult_HAIRMOD_PROTECTED.selected;
			scr.updatecm();
		}
		tfInput.update();
		base.updateKey();
	}

	public override void updateKey()
	{
	}
}
