public class Skill_Da_Roi_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private short angle;

	private short v = 10;

	private short xGoc;

	private short yGoc;

	private short distant;

	private short angLimit;

	private short xFrom;

	private short yFrom;

	private short xTo;

	private short yTo;

	private short angHuong;

	private short dir;

	private short typeSkill;

	private int hpLost;

	public mVector_HAIRMOD_PROTECTED list;

	private int xCu;

	private int yCu;

	private int[] index = new int[3] { 0, 2, 4 };

	public Skill_Da_Roi_HAIRMOD_PROTECTED(int xTo, int yTo, int x, int y, int hpLost)
	{
		this.hpLost = hpLost;
		if (xTo < x)
		{
			dir = 1;
		}
		else
		{
			dir = -1;
		}
		this.xTo = (short)xTo;
		this.yTo = (short)yTo;
		base.x = (xFrom = (short)x);
		base.y = (yFrom = (short)y);
		angle = (short)Util_HAIRMOD_PROTECTED.angle(xTo - x, -(yTo - y));
		angHuong = angle;
		int num = Util_HAIRMOD_PROTECTED.distance(xTo, yTo, x, y);
		int num2 = num / 2 * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
		int num3 = -(num / 2 * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
		int num4 = Util_HAIRMOD_PROTECTED.fixangle(angle + 90 * dir);
		int num5 = num / 3;
		int num6 = 0;
		num5 += 45 - ((angHuong >= 180) ? (Util_HAIRMOD_PROTECTED.abs(angHuong - 270) / 2) : (Util_HAIRMOD_PROTECTED.abs(angHuong - 90) / 2));
		int num7 = num5 * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(num4)) >> 10;
		int num8 = -(num5 * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(num4))) >> 10;
		xGoc = (short)(x + num2 + num7);
		yGoc = (short)(y + num3 + num8);
		distant = (short)Util_HAIRMOD_PROTECTED.distance(xGoc, yGoc, x, y);
		angle = (short)Util_HAIRMOD_PROTECTED.angle(x - xGoc, -(y - yGoc));
		angLimit = (short)Util_HAIRMOD_PROTECTED.angle(xTo - xGoc, -(yTo - yGoc));
	}

	public void setTypeSkill(byte type)
	{
		typeSkill = type;
	}

	public override void update()
	{
		angle = (short)Util_HAIRMOD_PROTECTED.fixangle(angle + v * dir);
		int num = distant * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
		int num2 = -(distant * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
		xCu = x;
		yCu = y;
		if (type != 1)
		{
			x = xGoc + num;
		}
		y = yGoc + num2;
		int num3 = Util_HAIRMOD_PROTECTED.distance(xCu, yCu, x, y);
		if (num3 > 10)
		{
			v = (short)(10 - num3 % 10 / 2);
		}
		if (Util_HAIRMOD_PROTECTED.abs(angle - angLimit) >= v)
		{
			return;
		}
		if (type != 1)
		{
			if (hpLost != 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + hpLost, 0, xTo, yTo - 45, 1, -2);
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("MISS", 0, xTo, yTo - 45, 1, -2);
			}
		}
		EffectManager_HAIRMOD_PROTECTED.addHiEffect(xTo, yTo - 10, 12);
		EffBack_HAIRMOD_PROTECTED effBack_HAIRMOD_PROTECTED = null;
		if (type != 1)
		{
			effBack_HAIRMOD_PROTECTED = new EffBack_HAIRMOD_PROTECTED(xTo, yTo, 28, 3, 32, 31, false, false);
			EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(effBack_HAIRMOD_PROTECTED);
		}
		else
		{
			effBack_HAIRMOD_PROTECTED = new EffBack_HAIRMOD_PROTECTED(xTo, yTo, 28, 3, 32, 31, false, true);
			EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(effBack_HAIRMOD_PROTECTED);
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(xTo, yTo, 48);
		}
		EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
		if (type == 1)
		{
			EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(new Skill_Cau_Lua_HAIRMOD_PROTECTED(xTo, yTo, list));
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = Util_HAIRMOD_PROTECTED.distance(xFrom, yFrom, x, y);
		int num2 = num * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angHuong)) >> 10;
		int num3 = -(num * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angHuong))) >> 10;
		g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, xFrom + num2, yFrom + num3, 3);
		if (type == 1)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgEffNew, x, y, 3);
			return;
		}
		for (int i = 0; i < 3; i++)
		{
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 4, 0, index[i] * 14, 14, 14, x + Res_HAIRMOD_PROTECTED.rnd(10) - 5, y + Res_HAIRMOD_PROTECTED.rnd(10) - 10, 3);
			index[i]++;
			if (index[i] >= 5)
			{
				index[i] = 0;
			}
		}
	}
}
