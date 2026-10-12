public class Skill_TruRong_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private int[] xw;

	private int[] yw;

	public int dx;

	public int dy;

	public int pos;

	public int time;

	public int lim;

	public int height;

	public int xto;

	public int yto;

	public int imgIndex;

	private sbyte frame;

	private sbyte frame1;

	private int TimeStarAtack;

	private bool isShot;

	private LiveActor_HAIRMOD_PROTECTED target;

	public Skill_TruRong_HAIRMOD_PROTECTED(int x, int y, int xto, int yto, LiveActor_HAIRMOD_PROTECTED target, int timeAtt)
	{
		this.target = target;
		base.x = x;
		base.y = y + 30;
		this.xto = xto;
		this.yto = yto;
		imgIndex = 15;
		lim = y - 60;
		TimeStarAtack = timeAtt;
		setspeed();
	}

	public void setspeed()
	{
		dx = xto - x;
		dy = yto - lim;
		int num = (Math_HAIRMOD_PROTECTED.abs(dx) + Math_HAIRMOD_PROTECTED.abs(dy)) / 20;
		if (num < 2)
		{
			num = 2;
		}
		xw = new int[num];
		yw = new int[num];
		for (int i = 0; i < num; i++)
		{
			xw[i] = x + i * dx / num;
			yw[i] = lim + i * dy / num;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (TimeStarAtack < 0)
		{
			if (y > lim)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.getImgeffect(55), 0, frame1 * Res_HAIRMOD_PROTECTED.getImgeffect(55).getHeight() / 2, Res_HAIRMOD_PROTECTED.getImgeffect(55).getWidth(), Res_HAIRMOD_PROTECTED.getImgeffect(55).getHeight() / 2, 0, x, y, MyGraphics_HAIRMOD_PROTECTED.VCENTER | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			}
			if (isShot && Res_HAIRMOD_PROTECTED.getImgArrow(imgIndex) != null)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[imgIndex], 0, frame * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][imgIndex], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][imgIndex], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][imgIndex], 0, xw[pos], yw[pos], 3);
			}
		}
	}

	public override void update()
	{
		frame = (sbyte)((frame + 1) % 3);
		frame1 = (sbyte)((frame1 + 1) % 2);
		if (TimeStarAtack >= 0)
		{
			TimeStarAtack--;
		}
		if (TimeStarAtack >= 0)
		{
			return;
		}
		if (y > lim)
		{
			y -= 10;
		}
		if (y <= lim && time <= 6)
		{
			time++;
		}
		if (time == 2)
		{
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 54);
		}
		if (time >= 6 && !isShot)
		{
			isShot = true;
		}
		if (time > 6 && pos < xw.Length)
		{
			pos++;
		}
		if (pos >= xw.Length)
		{
			pos = xw.Length - 1;
			xw[pos] = xto;
			yw[pos] = yto;
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(xto, yto, 53);
			wantDetroy = true;
			if (target != null && target.damAttack != 0 && target.damAttack != 2000000)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + target.damAttack, 0, target.x, target.y + target.dy - 15, 1, -2);
			}
		}
	}
}
