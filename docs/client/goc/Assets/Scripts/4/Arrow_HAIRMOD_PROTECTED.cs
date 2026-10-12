public class Arrow_HAIRMOD_PROTECTED : IArrow_HAIRMOD_PROTECTED
{
	public static int[] ARROWINDEX = new int[18]
	{
		0, 15, 37, 52, 75, 105, 127, 142, 165, 195,
		217, 232, 255, 285, 307, 322, 345, 370
	};

	public Effect_HAIRMOD_PROTECTED end;

	public int power;

	public sbyte effect;

	public sbyte typeEffEnd = -1;

	private LiveActor_HAIRMOD_PROTECTED target;

	private int[] xw;

	private int[] yw;

	private int frame;

	private int transform;

	private int pos;

	private int imgIndex;

	public static sbyte[][] ARROWSIZE = new sbyte[2][]
	{
		new sbyte[20]
		{
			16, 24, 36, 24, 24, 36, 17, 28, 28, 73,
			32, 14, 21, 56, 56, 42, 46, 24, 29, 32
		},
		new sbyte[20]
		{
			16, 24, 36, 24, 24, 36, 17, 28, 28, 73,
			32, 13, 16, 54, 54, 41, 50, 24, 28, 32
		}
	};

	public static int[] TRANSFORM = new int[16]
	{
		0, 0, 0, 7, 6, 6, 6, 2, 2, 3,
		3, 4, 5, 5, 5, 1
	};

	public static sbyte[] FRAME = new sbyte[25]
	{
		0, 1, 2, 1, 0, 1, 2, 1, 0, 1,
		2, 1, 0, 1, 2, 1, 0, 1, 2, 1,
		0, 1, 2, 1, 0
	};

	private int frameRun;

	public Arrow_HAIRMOD_PROTECTED(int imgIndex)
	{
		this.imgIndex = imgIndex;
	}

	public override void setAngle(int angle)
	{
	}

	public override void onArrowTouchTarget()
	{
		if (power != 0 && power != 2000000)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + power, 0, target.x, target.y + target.dy - 15, 1, -2);
		}
		if (effect != 0 && effect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[effect], 0, target.x, target.y + target.dy - 15, 2, -2);
		}
		target.realHPSyncTime = 2;
		if (target.catagory == 1)
		{
			if (effect == 0)
			{
				((Monster_HAIRMOD_PROTECTED)target).jump();
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 11);
			}
			else if (effect == 2)
			{
				((Monster_HAIRMOD_PROTECTED)target).doublejump();
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 12);
			}
		}
		if (effect != 1)
		{
			if (imgIndex == 1)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 9);
			}
			if (imgIndex == 2)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x - 10, target.y + target.dy, 16);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x + 10, target.y + target.dy, 16);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 16);
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 16);
			}
			if (imgIndex == 5)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 30);
			}
			if (imgIndex == 7)
			{
				Skill_Eff_end_HAIRMOD_PROTECTED o = new Skill_Eff_end_HAIRMOD_PROTECTED(target.x, target.y + target.dy, transform);
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
				EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(o);
			}
			if (imgIndex == 8)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 11);
			}
			if (imgIndex == 13)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 50);
			}
			if (imgIndex == 18)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(target.x, target.y + target.dy - 10, 50);
			}
			if (typeEffEnd != -1)
			{
				Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = new Effect_HAIRMOD_PROTECTED(target.x, target.y - target.height / 2, typeEffEnd);
				effect_HAIRMOD_PROTECTED.loop = (sbyte)((typeEffEnd != 30) ? 1 : 0);
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(effect_HAIRMOD_PROTECTED);
			}
		}
		wantDestroy = true;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (Res_HAIRMOD_PROTECTED.getImgArrow(imgIndex) != null)
		{
			if (imgIndex == 18)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[imgIndex], 0, frameRun * ARROWSIZE[1][imgIndex], ARROWSIZE[0][imgIndex], ARROWSIZE[1][imgIndex], 0, xw[pos], yw[pos], 3);
			}
			else
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[imgIndex], 0, frame * ARROWSIZE[1][imgIndex], ARROWSIZE[0][imgIndex], ARROWSIZE[1][imgIndex], transform, xw[pos], yw[pos], 3);
			}
		}
	}

	public override void set(int type, int x, int y, int power, sbyte effect, LiveActor_HAIRMOD_PROTECTED owner, LiveActor_HAIRMOD_PROTECTED target)
	{
		this.effect = effect;
		this.target = target;
		this.power = power;
		int num = 0;
		int num2 = 0;
		num = target.x - x;
		num2 = target.y + target.dy - y;
		switch (type)
		{
		case 7:
			num = target.x + 10 - x;
			num2 = target.y + target.dy - y;
			break;
		case 8:
			num = target.x - 10 - x;
			num2 = target.y - 10 + target.dy - y;
			break;
		}
		int num3 = (GameScr_HAIRMOD_PROTECTED.abs(num) + GameScr_HAIRMOD_PROTECTED.abs(num2)) / 20;
		if (num3 < 2)
		{
			num3 = 2;
		}
		xw = new int[num3];
		yw = new int[num3];
		for (int i = 1; i < num3; i++)
		{
			xw[i] = x + i * num / num3;
			yw[i] = y + i * num2 / num3;
		}
		int num4 = findDirIndexFromAngle(Util_HAIRMOD_PROTECTED.angle(num, -num2));
		if (num4 < FRAME.Length)
		{
			frame = FRAME[num4];
		}
		if (num4 < TRANSFORM.Length)
		{
			transform = TRANSFORM[num4];
		}
	}

	public static int findDirIndexFromAngle(int angle)
	{
		for (int i = 0; i < ARROWINDEX.Length - 1; i++)
		{
			if (angle >= ARROWINDEX[i] && angle <= ARROWINDEX[i + 1])
			{
				if (i >= 16)
				{
					return 0;
				}
				return i;
			}
		}
		return 0;
	}

	public override void update()
	{
		if (imgIndex == 18)
		{
			frameRun = (frameRun + 1) % 4;
		}
		pos++;
		if (pos >= xw.Length)
		{
			pos = xw.Length;
		}
		if (pos == xw.Length)
		{
			onArrowTouchTarget();
		}
	}
}
