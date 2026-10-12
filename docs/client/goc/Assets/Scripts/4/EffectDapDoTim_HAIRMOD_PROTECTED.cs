public class EffectDapDoTim_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	public int angle;

	public int xto;

	public int yto;

	public int pos;

	public int thongsolathinh;

	private int[] xw;

	private int[] yw;

	public int dx;

	public int dy;

	public int idimg;

	public int time;

	private sbyte frame;

	public EffectDapDoTim_HAIRMOD_PROTECTED(int x, int y, int xto, int yto)
	{
		base.x = x;
		base.y = y;
		this.xto = xto;
		this.yto = yto;
		setSpeed();
		time = 10;
	}

	private void setSpeed()
	{
		dx = xto - x;
		dy = yto - y;
		angle = Util_HAIRMOD_PROTECTED.angle(dx, dy);
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
			yw[i] = y + i * dy / num;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawRegion(Res_HAIRMOD_PROTECTED.getImgArrow(10), 0, frame * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][10], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][10], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][10], 0, xw[pos], yw[pos], MyGraphics_HAIRMOD_PROTECTED.VCENTER | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
	}

	public override void update()
	{
		if (time >= 0)
		{
			time--;
		}
		frame++;
		if (frame >= 3)
		{
			frame = 0;
		}
		if (time < 0)
		{
			if (pos < xw.Length)
			{
				pos++;
			}
			if (pos >= xw.Length)
			{
				pos = xw.Length - 1;
				xw[pos] = xto;
				yw[pos] = yto;
				wantDetroy = true;
			}
		}
	}
}
