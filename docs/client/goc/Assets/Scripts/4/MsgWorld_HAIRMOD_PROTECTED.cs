public class MsgWorld_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static MsgWorld_HAIRMOD_PROTECTED me;

	public int cmtoY;

	public int cmy;

	public int cmdy;

	public int cmvy;

	public int cmyLim;

	public int selected;

	public int limSelect;

	public mVector_HAIRMOD_PROTECTED data = new mVector_HAIRMOD_PROTECTED();

	private bool trans;

	private int pa;

	public MsgWorld_HAIRMOD_PROTECTED()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
			selected = (limSelect = 0);
		};
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		right.action = action;
	}

	public static MsgWorld_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new MsgWorld_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		base.switchToMe();
		init();
	}

	public override void init()
	{
		data = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < GameScr_HAIRMOD_PROTECTED.savemsgWorld.size(); i++)
		{
			MsgInfo_HAIRMOD_PROTECTED msgInfo_HAIRMOD_PROTECTED = (MsgInfo_HAIRMOD_PROTECTED)GameScr_HAIRMOD_PROTECTED.savemsgWorld.elementAt(i);
			msgInfo_HAIRMOD_PROTECTED.arr = MFont_HAIRMOD_PROTECTED.arialFontW.splitFontBStrInLine(msgInfo_HAIRMOD_PROTECTED.message, Canvas_HAIRMOD_PROTECTED.w - 44);
			data.addElement(msgInfo_HAIRMOD_PROTECTED);
			limSelect += msgInfo_HAIRMOD_PROTECTED.arr.Length;
		}
		cmyLim = limSelect * 15 - (Canvas_HAIRMOD_PROTECTED.h - 60) + 35;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		cmy = (cmtoY = 0);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 20, 20, Canvas_HAIRMOD_PROTECTED.w - 40, Canvas_HAIRMOD_PROTECTED.h - 60);
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, "Kênh thế giới", Canvas_HAIRMOD_PROTECTED.w / 2, 26, 2);
		g.setClip(20, 46, Canvas_HAIRMOD_PROTECTED.w - 40, Canvas_HAIRMOD_PROTECTED.h - 90);
		g.translate(20, 47);
		g.translate(0, -cmy);
		int num = 2;
		for (int i = 0; i < data.size(); i++)
		{
			MsgInfo_HAIRMOD_PROTECTED msgInfo_HAIRMOD_PROTECTED = (MsgInfo_HAIRMOD_PROTECTED)data.elementAt(i);
			for (int j = 0; j < msgInfo_HAIRMOD_PROTECTED.arr.Length; j++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, msgInfo_HAIRMOD_PROTECTED.arr[j], 8, num, 0);
				num += 15;
			}
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		base.paint(g);
	}

	public override void updateKey()
	{
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.isPointer(20, 20, Canvas_HAIRMOD_PROTECTED.w - 40, Canvas_HAIRMOD_PROTECTED.h - 60))
		{
			if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
			{
				if (!trans)
				{
					pa = cmy;
					trans = true;
				}
				if (Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pyLast - Canvas_HAIRMOD_PROTECTED.py) != 0)
				{
					cmtoY = pa + (Canvas_HAIRMOD_PROTECTED.pyLast - Canvas_HAIRMOD_PROTECTED.py);
					if (cmtoY < 0)
					{
						cmtoY = 0;
					}
					if (cmtoY > cmyLim)
					{
						cmtoY = cmyLim;
					}
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				selected = (cmtoY + Canvas_HAIRMOD_PROTECTED.py - 47) / 15;
				if (selected > limSelect - 1)
				{
					selected = limSelect - 1;
				}
				if (selected < 0)
				{
					selected = 0;
				}
				trans = false;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
			selected++;
			if (selected > limSelect - 1)
			{
				selected = limSelect - 1;
			}
			flag = true;
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
			selected--;
			if (selected < 0)
			{
				selected = 0;
			}
			flag = true;
		}
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
			if (cmy < 0)
			{
				cmy = 0;
			}
			if (cmy > cmyLim)
			{
				cmy = cmyLim;
			}
		}
		if (flag)
		{
			cmtoY = selected * 15 - (Canvas_HAIRMOD_PROTECTED.h - 60) / 5;
		}
		base.updateKey();
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.update();
		base.update();
	}
}
