public class Explosion_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	private int f;

	private int p1;

	public Explosion_HAIRMOD_PROTECTED(short x, short y)
	{
		catagory = Actor_HAIRMOD_PROTECTED.CAT_EXPLOTION;
		base.x = x;
		base.y = y;
		p1 = 0;
	}

	public override bool isPaint()
	{
		if (x < GameScr_HAIRMOD_PROTECTED.cmx)
		{
			return false;
		}
		if (x > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w)
		{
			return false;
		}
		if (y < GameScr_HAIRMOD_PROTECTED.cmy)
		{
			return false;
		}
		if (y > GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h + 30)
		{
			return false;
		}
		return true;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (isPaint())
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgExplosion, 0, f * 24, 24, 24, 0, x - 12, y - 24, 0);
		}
	}

	public override void setPosTo(short x, short y)
	{
		base.x = x;
		base.y = y;
	}

	public override void update()
	{
		p1++;
		if (p1 > 8)
		{
			p1 = 0;
			wantDestroy = true;
		}
		f = p1 >> 1;
	}
}
