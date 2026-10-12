public class EffBack_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private short[] xE;

	private short[] yE;

	private sbyte[] index;

	private sbyte[] count;

	public new int type;

	public int limit;

	public int w0;

	public int h0;

	private bool isEffVong;

	private bool isEffUpDown;

	private int aa;

	public EffBack_HAIRMOD_PROTECTED(int x, int y, int type, int limit, int w, int h, bool isEffVong, bool isEffUpDown)
	{
		this.isEffVong = isEffVong;
		w0 = w;
		h0 = h;
		this.limit = limit;
		this.type = type;
		base.x = x;
		base.y = y;
		xE = new short[6];
		yE = new short[6];
		index = new sbyte[6];
		count = new sbyte[6];
		for (int i = 0; i < 6; i++)
		{
			xE[i] = (short)(x - 10 + Res_HAIRMOD_PROTECTED.rnd(20) - 10);
			yE[i] = (short)(y + Res_HAIRMOD_PROTECTED.rnd(20));
			index[i] = (sbyte)Res_HAIRMOD_PROTECTED.rnd(6);
		}
		this.isEffUpDown = isEffUpDown;
	}

	public override void update()
	{
		for (int i = 0; i < xE.Length; i++)
		{
			index[i]++;
			if (index[i] < limit * 3)
			{
				continue;
			}
			xE[i] = (short)(x + Res_HAIRMOD_PROTECTED.rnd(30) - 15);
			yE[i] = (short)(y + Res_HAIRMOD_PROTECTED.rnd(14) + 3);
			index[i] = (sbyte)Res_HAIRMOD_PROTECTED.rnd(6);
			count[i]++;
			if (count[i] > 3)
			{
				if (!isEffUpDown)
				{
					EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
				}
				else
				{
					EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				}
			}
		}
		aa++;
		if (aa >= 6)
		{
			aa = 0;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < 6; i++)
		{
			Res_HAIRMOD_PROTECTED.paintImgEff(g, type, 0, index[i] / 3 * h0, w0, h0, xE[i], yE[i], MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
		if (isEffVong)
		{
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 13, 0, aa / 2 * 38, 58, 38, x, y - 3, 3);
		}
	}
}
