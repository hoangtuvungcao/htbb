public class AEOSkillHoa_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private int idSkill;

	public int degree;

	private short d = 20;

	private static sbyte[][] frame = new sbyte[2][]
	{
		new sbyte[6] { 0, 0, 1, 1, 2, 2 },
		new sbyte[5] { 1, 1, 2, 2, 2 }
	};

	private static sbyte[] typeEff = new sbyte[2] { 15, 15 };

	private long time;

	private int delay;

	private Char_HAIRMOD_PROTECTED attacker;

	private new int f;

	private int startX;

	private int startY;

	public AEOSkillHoa_HAIRMOD_PROTECTED(int x, int y, int type)
		: base(x, y, type)
	{
	}

	public AEOSkillHoa_HAIRMOD_PROTECTED(int degree)
		: base(degree)
	{
		this.degree = degree;
	}

	public AEOSkillHoa_HAIRMOD_PROTECTED(int x, int y, sbyte idSkill)
		: base(x)
	{
		base.x = x;
		base.y = y;
		this.idSkill = idSkill;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		switch (idSkill)
		{
		case 0:
			Res_HAIRMOD_PROTECTED.paintImgEff(g, typeEff[idSkill], 0, frame[idSkill][f] * Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.WIDTH[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], x + startX, y + startY, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, typeEff[idSkill], 0, frame[idSkill][f] * Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.WIDTH[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], x - 3 + startX, y + startY, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			break;
		case 1:
			Res_HAIRMOD_PROTECTED.paintImgEff(g, typeEff[idSkill], 0, frame[idSkill][f] * Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.WIDTH[typeEff[idSkill]], Effect_HAIRMOD_PROTECTED.HEIGHT[typeEff[idSkill]], x, y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			break;
		}
	}

	public void setPos(int x, int y)
	{
		startX = x;
		startY = y;
	}

	public void setCharAttack(Char_HAIRMOD_PROTECTED c)
	{
		attacker = c;
	}

	public override void update()
	{
		switch (idSkill)
		{
		case 0:
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
			{
				f = (f + 1) % frame[idSkill].Length;
			}
			if (degree > 360)
			{
				degree -= 360;
			}
			if (d < 80)
			{
				d += 10;
			}
			else
			{
				d = 20;
			}
			if (degree == 0)
			{
				x = d;
			}
			else if (degree == 180)
			{
				x = (short)(-d);
			}
			else
			{
				x = (short)(Util_HAIRMOD_PROTECTED.Cos(degree) * d / 1024);
			}
			if (degree == 90)
			{
				y = d;
			}
			else if (degree == 270)
			{
				y = (short)(-d);
			}
			else
			{
				y = (short)(Util_HAIRMOD_PROTECTED.Sin(degree) * d / 1024);
			}
			break;
		case 1:
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
			{
				f = (f + 1) % frame[idSkill].Length;
			}
			if (delay > 0)
			{
				if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - time >= 1000)
				{
					time = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
					delay--;
				}
			}
			else
			{
				wantDetroy = true;
			}
			break;
		}
	}
}
