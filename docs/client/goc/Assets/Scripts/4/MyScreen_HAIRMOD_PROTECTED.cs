public abstract class MyScreen_HAIRMOD_PROTECTED : MainJ2ME_HAIRMOD_PROTECTED
{
	public static short deltaY = 17;

	public int btnFocus;

	public new static int ITEM_HEIGHT = 20;

	public MyScreen_HAIRMOD_PROTECTED()
	{
	}

	public virtual void init()
	{
	}

	public virtual void switchToMe()
	{
		Canvas_HAIRMOD_PROTECTED.currentScreen = this;
	}

	public virtual void switchToMe(MyScreen_HAIRMOD_PROTECTED scr)
	{
		Canvas_HAIRMOD_PROTECTED.currentScreen = this;
	}

	public virtual bool keyPress(int keyCode)
	{
		return false;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (Res_HAIRMOD_PROTECTED.imgCmdBar != null)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCmdBar, 0, Canvas_HAIRMOD_PROTECTED.h + 3, MyGraphics_HAIRMOD_PROTECTED.LEFT | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
		}
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == this && Canvas_HAIRMOD_PROTECTED.currentDialog == null && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			base.paint(g);
		}
		MultitabClient_HAIRMOD_PROTECTED.paint(g);
	}

	public virtual void update()
	{
	}

	public override void updateKey()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == this)
		{
			base.updateKey();
		}
	}

	public virtual void pointerPress(int dx, int dy)
	{
	}

	public virtual void startXayBot(int idbot, int type, int sl)
	{
	}
}
