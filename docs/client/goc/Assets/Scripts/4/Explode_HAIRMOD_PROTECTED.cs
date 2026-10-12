public class Explode_HAIRMOD_PROTECTED : Exp_HAIRMOD_PROTECTED
{
	private int f;

	private int p;

	private static sbyte[] eff = new sbyte[1] { 15 };

	private static sbyte[][] frame = new sbyte[1][] { new sbyte[8] { 0, 1, 2, 1, 0, 1, 2, 0 } };

	private byte type;

	public Explode_HAIRMOD_PROTECTED(int x, int y, int typeEff)
	{
		base.x = x;
		base.y = y;
		type = (byte)typeEff;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintImgEff(g, eff[type], 0, frame[type][f] * Effect_HAIRMOD_PROTECTED.HEIGHT[eff[type]], Effect_HAIRMOD_PROTECTED.WIDTH[eff[type]], Effect_HAIRMOD_PROTECTED.HEIGHT[eff[type]], x, y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
	}

	public override void update()
	{
		p++;
		if (p % 2 == 0)
		{
			f++;
			if (f >= frame[type].Length)
			{
				f = 0;
				destroy = true;
			}
		}
	}
}
