public class PageScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static PageScr_HAIRMOD_PROTECTED me;

	public Layer_HAIRMOD_PROTECTED layer;

	private MyScreen_HAIRMOD_PROTECTED lastScr;

	private int x;

	private int y;

	private int w;

	private int h;

	public int indexFont;

	private string title;

	public bool isBigMap;

	public static PageScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new PageScr_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen != this)
		{
			lastScr = Canvas_HAIRMOD_PROTECTED.currentScreen;
		}
		base.switchToMe();
	}

	public static void tr()
	{
	}

	public void setInfo(int x, int y, int w, int h, string title, Command_HAIRMOD_PROTECTED cmd)
	{
		Canvas_HAIRMOD_PROTECTED.isPointerDown = (Canvas_HAIRMOD_PROTECTED.isPointerClick = false);
		Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
		indexFont = 0;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.title = title;
		center = cmd;
		left = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			lastScr.switchToMe();
		};
		left.action = action;
	}

	public override void update()
	{
		if (lastScr != null && lastScr != Canvas_HAIRMOD_PROTECTED.shop)
		{
			lastScr.update();
		}
		if (layer != null)
		{
			layer.update();
		}
	}

	public override bool keyPress(int keyCode)
	{
		layer.keyPress(keyCode);
		return false;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		lastScr.paint(g);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (indexFont == 0)
		{
			Res_HAIRMOD_PROTECTED.paintDlgFull(g, x, y, w, h);
			if (indexFont == 0)
			{
				MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, title, x + w / 2, y + 5, 2);
			}
			if (layer != null)
			{
				layer.paint(g, 0, 0);
			}
		}
		else
		{
			g.translate(x, y);
			Res_HAIRMOD_PROTECTED.paintDlgFull(g, 0, 0, w, h);
			MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, title, w / 2, 6, 2);
			if (layer != null)
			{
				layer.paint(g, 0, 35);
			}
		}
		base.paint(g);
	}

	public override void switchToMe(MyScreen_HAIRMOD_PROTECTED scr)
	{
		lastScr = scr;
		base.switchToMe(scr);
	}
}
