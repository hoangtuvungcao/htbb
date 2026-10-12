public class Skill_Boss_Halloween_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	public int dx;

	public int dy;

	public int angle;

	public int pos;

	public int frame = 3;

	public int xTo;

	public int yTo;

	public int idimg;

	public short va;

	public short himg;

	public short vx;

	public short vy;

	private LiveActor_HAIRMOD_PROTECTED target;

	private bool isMatchX;

	private bool isMatchY;

	private int dx1;

	private int dy1;

	private sbyte[] framearray = new sbyte[12]
	{
		0, 0, 0, 1, 1, 1, 2, 2, 2, 3,
		3, 3
	};

	private sbyte nframe;

	public Skill_Boss_Halloween_HAIRMOD_PROTECTED(int x, int y, LiveActor_HAIRMOD_PROTECTED target)
	{
		base.x = x;
		base.y = y;
		this.target = target;
		xTo = target.x;
		yTo = target.y;
		idimg = 17;
		va = 12;
		yTo += 5;
		himg = 1;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Res_HAIRMOD_PROTECTED.getImgArrow(idimg) != null)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.getImgArrow(idimg), 0, frame * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][idimg], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][idimg], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][idimg], 0, x, y, 3);
		}
	}

	public override void update()
	{
		nframe++;
		if (nframe > framearray.Length - 1)
		{
			nframe = 0;
		}
		frame = framearray[nframe];
		if (target != null)
		{
			xTo = target.x;
			yTo = target.y;
		}
		dx = (short)(xTo - x);
		dy = (short)(yTo - (himg >> 1) - y);
		angle = Util_HAIRMOD_PROTECTED.angle(dx, dy);
		vx = (short)(va * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10);
		vy = (short)(va * Util_HAIRMOD_PROTECTED.Sin(angle) >> 10);
		x += vx;
		y += vy;
		dx1 = Util_HAIRMOD_PROTECTED.abs(x - xTo);
		dy1 = Util_HAIRMOD_PROTECTED.abs(y - yTo);
		if (dx1 <= vx)
		{
			x = (short)xTo;
			isMatchX = true;
		}
		if (dy1 < vy)
		{
			y = (short)yTo;
			isMatchY = true;
		}
		if ((isMatchX && isMatchY) || Res_HAIRMOD_PROTECTED.getDistance(x, y, xTo, yTo) <= 10)
		{
			x = xTo;
			y = yTo;
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 48);
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + target.damAttack, 0, target.x, target.y + target.dy - 15, 1, -2);
			wantDetroy = true;
			target.jump();
		}
		if (x != xTo && y != yTo && Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(x, y - 5, 15);
		}
	}
}
