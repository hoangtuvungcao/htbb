public class ThienThach_Skill_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private int xTo;

	private int yTo;

	private int v;

	private int dame;

	private byte index;

	public ThienThach_Skill_HAIRMOD_PROTECTED(int x, int y, int dam)
	{
		xTo = x;
		yTo = y;
		base.x = x - 100;
		base.y = y - 200;
		v = 1;
		dame = dam;
	}

	public override void update()
	{
		v++;
		EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 32);
		int a = Util_HAIRMOD_PROTECTED.angle(xTo - x, -(yTo - y));
		int num = v * Util_HAIRMOD_PROTECTED.Cos(a) >> 10;
		int num2 = -(v * Util_HAIRMOD_PROTECTED.Sin(a)) >> 10;
		x += num;
		y += num2;
		if (Util_HAIRMOD_PROTECTED.distance(x, y, xTo, yTo) <= v)
		{
			if (dame != 2000000)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + dame, 0, x, y - 15, 1, -2);
			}
			EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
			EffBack_HAIRMOD_PROTECTED o = new EffBack_HAIRMOD_PROTECTED(xTo, yTo, 28, 3, 32, 31, false, false);
			EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(o);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintImgEff(g, 31, 0, index / 3 * 20, 20, 20, x, y, 3);
		index++;
		if (index >= 6)
		{
			index = 0;
		}
	}
}
