public class ArrowTarget_HAIRMOD_PROTECTED : IArrow_HAIRMOD_PROTECTED
{
	private sbyte imgIndex;

	private sbyte d;

	private sbyte v;

	private sbyte timeDelay;

	public short x;

	public short y;

	public short xShadow;

	public short yShadow;

	public short xTo;

	public short yTo;

	public short angle;

	public short angleShadow;

	public bool isTarget;

	private int power;

	public ArrowTarget_HAIRMOD_PROTECTED(sbyte type)
	{
		imgIndex = type;
		timeDelay = 0;
	}

	public override void update()
	{
		int num = v * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10;
		int num2 = -(v * Util_HAIRMOD_PROTECTED.Sin(angle)) >> 10;
		x += (short)num;
		y += (short)num2;
		int num3 = v * Util_HAIRMOD_PROTECTED.Cos(angleShadow) >> 10;
		int num4 = -(v * Util_HAIRMOD_PROTECTED.Sin(angleShadow)) >> 10;
		xShadow += (short)num3;
		yShadow += (short)num4;
		if (timeDelay == 0 && Util_HAIRMOD_PROTECTED.distance(xTo, yTo, x, y) <= v * 2)
		{
			x = (xShadow = xTo);
			y = (yShadow = yTo);
			v = 0;
			if (!isTarget)
			{
				timeDelay = (sbyte)(Res_HAIRMOD_PROTECTED.rnd(30) + 20);
				Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.addElement(this);
				Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, (short)(y + 10));
			}
			else
			{
				if (power != 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + power, 0, x, y - 15, 1, -2);
				}
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y - 20, 12);
			}
			GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
		}
		else if (Util_HAIRMOD_PROTECTED.distance(xTo, yTo, x, y) > 1000)
		{
			GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
		}
		if (timeDelay > 0)
		{
			timeDelay--;
			if (timeDelay == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.removeElement(this);
			}
		}
	}

	public override void onArrowTouchTarget()
	{
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Res_HAIRMOD_PROTECTED.getImgArrow(imgIndex) != null)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, xShadow, yShadow, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[imgIndex], 0, Arrow_HAIRMOD_PROTECTED.FRAME[d] * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][imgIndex], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][imgIndex], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][imgIndex], Arrow_HAIRMOD_PROTECTED.TRANSFORM[d], x, y, 3);
		}
	}

	public void set(int type, int x, int y, int power, sbyte effect, int xTo, int yTo, sbyte v, bool isTarget)
	{
		this.power = power;
		this.isTarget = isTarget;
		this.v = v;
		int dx = xTo - x;
		int num = yTo - y;
		this.x = (xShadow = (short)x);
		this.y = (short)y;
		this.xTo = (short)xTo;
		this.yTo = (short)yTo;
		yShadow = (short)(y + 30);
		d = (sbyte)Arrow_HAIRMOD_PROTECTED.findDirIndexFromAngle(Util_HAIRMOD_PROTECTED.angle(dx, -num));
		angle = (short)Util_HAIRMOD_PROTECTED.angle(xTo - x, -(yTo - y));
		angleShadow = (short)Util_HAIRMOD_PROTECTED.angle(xTo - xShadow, -(yTo - yShadow));
	}

	public override void set(int type, int x, int y, int power, sbyte effect, LiveActor_HAIRMOD_PROTECTED owner, LiveActor_HAIRMOD_PROTECTED target)
	{
	}

	public override void setAngle(int angle)
	{
	}
}
