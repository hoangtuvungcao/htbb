public class EffectLeg_HAIRMOD_PROTECTED : EffBack_HAIRMOD_PROTECTED
{
	public EffectLeg_HAIRMOD_PROTECTED(int x, int y, int type, int limit, int w, int h, bool isEffVong, bool isEffUpDown)
		: base(x, y, type, limit, w, h, isEffVong, isEffUpDown)
	{
		base.x = x;
		base.y = y;
		base.type = type;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.getWayPoint(), 0, type * 24, 39, 24, 0, x, y, 3);
		}
	}
}
