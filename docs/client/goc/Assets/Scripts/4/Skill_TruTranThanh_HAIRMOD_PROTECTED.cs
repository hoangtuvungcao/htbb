public class Skill_TruTranThanh_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED lisAttack = new mVector_HAIRMOD_PROTECTED();

	private LiveActor_HAIRMOD_PROTECTED from;

	private int[] color = new int[0];

	private int xF;

	private int yF;

	private int vx;

	private int dem;

	private bool isEnd;

	public Skill_TruTranThanh_HAIRMOD_PROTECTED(int x, int y, LiveActor_HAIRMOD_PROTECTED From, mVector_HAIRMOD_PROTECTED list)
	{
		base.x = x;
		base.y = y;
		for (int i = 0; i < 20; i++)
		{
			int num = (sbyte)(20 - Res_HAIRMOD_PROTECTED.rnd(40));
			int num2 = (sbyte)(20 - Res_HAIRMOD_PROTECTED.rnd(40));
			xF = x + num;
			yF = y + num2;
		}
		from = From;
		lisAttack = list;
	}

	public override void update()
	{
		if (xF < x)
		{
			xF += vx;
		}
		if (xF >= x)
		{
			xF -= vx;
		}
		if (yF < y)
		{
			yF += vx;
		}
		if (yF > y)
		{
			yF -= vx;
		}
		vx++;
		if (Math_HAIRMOD_PROTECTED.abs(x - xF) <= 5 && Math_HAIRMOD_PROTECTED.abs(y - yF) <= 5)
		{
			isEnd = true;
			vx = 0;
			EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < 5; i++)
		{
			if (!isEnd)
			{
				g.drawRegion(Char_HAIRMOD_PROTECTED.imgLight, 0, 7, 7, 7, 0, xF, yF, 0);
				g.drawRegion(Char_HAIRMOD_PROTECTED.imgLight, 0, 14, 7, 7, 0, xF + 7 - Res_HAIRMOD_PROTECTED.rnd(15), yF + 7 - Res_HAIRMOD_PROTECTED.rnd(15), 0);
			}
		}
	}
}
