public class WeaponsLazer_HAIRMOD_PROTECTED : IArrow_HAIRMOD_PROTECTED
{
	private int angle0;

	private int lim;

	private int x;

	private int y;

	private int xkc;

	private int ykc;

	private int power;

	private int time;

	private int timeStarpaint = -1;

	public int xlim;

	public int ylim;

	private int[] color = new int[3] { 16741818, 8388469, 7710463 };

	private int idColor;

	public WeaponsLazer_HAIRMOD_PROTECTED(LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, int xadd, int yadd, int power, int idColor)
	{
		x = from.x + xadd;
		y = from.y + yadd;
		xlim = to.x;
		ylim = to.y - 28;
		xkc = xlim - x;
		ykc = ylim - y;
		angle0 = Util_HAIRMOD_PROTECTED.angle(xkc, ykc);
		lim = 6;
		this.power = power;
		this.idColor = idColor;
		to.jump();
		EffectManager_HAIRMOD_PROTECTED.addHiEffect(to.x, to.y + to.dy - 10, 11);
		Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + power, 0, to.x, to.y + to.dy - 15, 1, -2);
	}

	public WeaponsLazer_HAIRMOD_PROTECTED(int x, int y, int xTo, int yTo, int time, int lim)
	{
		this.x = x;
		this.y = y;
		xlim = xTo;
		ylim = yTo;
		xkc = xlim - x;
		ykc = ylim - y;
		angle0 = Util_HAIRMOD_PROTECTED.angle(xkc, ykc);
		this.lim = lim;
		this.time = time;
	}

	public WeaponsLazer_HAIRMOD_PROTECTED(int x, int y, int xTo, int yTo, int time, int lim, int timepaint)
	{
		this.x = x;
		this.y = y;
		xlim = xTo;
		ylim = yTo;
		xkc = xlim - x;
		ykc = ylim - y;
		angle0 = Util_HAIRMOD_PROTECTED.angle(xkc, ykc);
		this.lim = lim;
		this.time = time;
		timeStarpaint = timepaint;
	}

	public override void onArrowTouchTarget()
	{
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (timeStarpaint >= 0)
		{
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if ((angle0 > 60 && angle0 < 120) || (angle0 > 240 && angle0 < 300))
		{
			g.setColor(color[idColor]);
			for (int i = 0; i < lim; i++)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x + i, y, x + i + xkc, y + ykc, color[idColor]));
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x - i, y, x - i + xkc, y + ykc, color[idColor]));
			}
			g.setColor(16777215);
			for (int j = 0; j < lim / 2; j++)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x + j, y, x + j + xkc, y + ykc, 16777215));
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x - j, y, x - j + xkc, y + ykc, 16777215));
			}
		}
		else
		{
			g.setColor(color[idColor]);
			for (int k = 0; k < lim; k++)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x, y + k, x + xkc, y + ykc + k, color[idColor]));
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x, y - k, x + xkc, y + ykc - k, color[idColor]));
			}
			g.setColor(16777215);
			for (int l = 0; l < lim / 2; l++)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x, y + l, x + xkc, y + ykc + l, 16777215));
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(x, y - l, x + xkc, y + ykc - l, 16777215));
			}
		}
		g.totalLine = mVector_HAIRMOD_PROTECTED2;
		g.drawlineGL();
	}

	public void set(int type, int x, int y, int power, byte effect, LiveActor_HAIRMOD_PROTECTED owner, LiveActor_HAIRMOD_PROTECTED target)
	{
	}

	public override void setAngle(int angle)
	{
	}

	public void update2()
	{
		if (timeStarpaint >= 0)
		{
			timeStarpaint--;
		}
		if (time >= 0)
		{
			time--;
		}
		if (time <= 0)
		{
			lim--;
		}
		if (lim <= 0)
		{
			wantDestroy = true;
		}
	}

	public override void update()
	{
		lim--;
		if (lim <= 0)
		{
			if (Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.conTains(this))
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.removeElement(this);
			}
			if (GameScr_HAIRMOD_PROTECTED.arrowsUp.conTains(this))
			{
				GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
			}
		}
	}
}
