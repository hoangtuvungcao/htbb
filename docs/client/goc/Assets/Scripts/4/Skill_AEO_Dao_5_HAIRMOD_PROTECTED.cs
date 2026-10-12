public class Skill_AEO_Dao_5_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private LiveActor_HAIRMOD_PROTECTED c;

	private bool isNo;

	private bool isEff;

	private int angle;

	private int dis;

	private int countDown;

	private int v;

	private int xGoc;

	private int yGoc;

	private int[] index;

	public string[] textAttack;

	private new int type;

	public bool isDoubleDregon;

	private static sbyte[][] indexImg = new sbyte[3][]
	{
		new sbyte[2] { 15, 28 },
		new sbyte[2] { 4, 4 },
		new sbyte[2] { 15, 28 }
	};

	private static byte[][] wImg = new byte[6][]
	{
		new byte[2] { 17, 36 },
		new byte[2] { 32, 31 },
		new byte[2] { 14, 14 },
		new byte[2] { 14, 14 },
		new byte[2] { 17, 36 },
		new byte[2] { 17, 36 }
	};

	private static byte[] indexLimit1 = new byte[3] { 3, 5, 3 };

	public Skill_AEO_Dao_5_HAIRMOD_PROTECTED(int x0, int y0, LiveActor_HAIRMOD_PROTECTED c, int t, bool isEff)
	{
		this.isEff = isEff;
		type = t;
		x = c.x;
		y = c.y - 10;
		xGoc = x0;
		yGoc = y0 - 40;
		this.c = c;
		angle = Util_HAIRMOD_PROTECTED.angle(c.x - 10 - xGoc, -(c.y - yGoc));
		dis = Util_HAIRMOD_PROTECTED.distance(xGoc, yGoc, c.x - 10, c.y);
		index = new int[3];
		for (int i = 0; i < 3; i++)
		{
			index[i] = Res_HAIRMOD_PROTECTED.rnd(6);
		}
		textAttack = new string[2];
	}

	public override void update()
	{
		angle = Util_HAIRMOD_PROTECTED.angle(x - xGoc, -(y - yGoc));
		dis = Util_HAIRMOD_PROTECTED.distance(xGoc, yGoc, x, y);
		countDown += 6;
		v++;
		if (!isNo)
		{
			x = c.x;
			y = c.y - 10;
		}
		if (countDown <= dis || isNo)
		{
			return;
		}
		EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.x, c.y - 10, 12);
		EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
		if (c.catagory == 1)
		{
			((Monster_HAIRMOD_PROTECTED)c).isTarget = false;
		}
		c.jump();
		isNo = true;
		if (textAttack[0] != null)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(textAttack[0], 0, x, y - 15, 1, -2);
		}
		if (textAttack[1] != null)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(textAttack[1], 0, x, y - 15, 2, -2);
		}
		if (!isEff)
		{
			return;
		}
		EffBack_HAIRMOD_PROTECTED effBack_HAIRMOD_PROTECTED = null;
		if (type == 1)
		{
			effBack_HAIRMOD_PROTECTED = new EffBack_HAIRMOD_PROTECTED(x, y, 4, 5, 14, 14, false, false);
		}
		else
		{
			effBack_HAIRMOD_PROTECTED = new EffBack_HAIRMOD_PROTECTED(x, y, 28, 3, 32, 31, true, false);
			if (type == 2)
			{
				Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED = new Skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED(c.x + SkillAnimate_HAIRMOD_PROTECTED.splashDuaX[c.dir], c.y + SkillAnimate_HAIRMOD_PROTECTED.splashDuaY[c.dir], c, c, 5);
				skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED.isDoubleDragon = isDoubleDregon;
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_AEO_DUA_DAO_5_HAIRMOD_PROTECTED);
			}
		}
		EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(effBack_HAIRMOD_PROTECTED);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = countDown * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
		int num2 = -(countDown * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
		g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, xGoc + num, yGoc + num2, 3);
		for (int i = 0; i < 3; i++)
		{
			Res_HAIRMOD_PROTECTED.paintImgEff(g, indexImg[type][0], 0, index[i] / 2 * wImg[type * 2][1], wImg[type * 2][0], wImg[type * 2][1], xGoc + num + Res_HAIRMOD_PROTECTED.rnd(10) - 5, yGoc + num2 + Res_HAIRMOD_PROTECTED.rnd(10) - 10, 3);
			index[i]++;
			if (index[i] >= indexLimit1[type])
			{
				index[i] = 0;
			}
		}
	}
}
