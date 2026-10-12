public class CuongThi_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private int vNhay;

	private int dirNhay = 1;

	private int timeNhay;

	private int nhay;

	private int angle;

	private int indexDoc;

	private bool isNhay;

	private bool isFire;

	public static byte[] clip = new byte[4] { 0, 1, 2, 2 };

	public CuongThi_HAIRMOD_PROTECTED()
	{
		z = 0;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y, 3);
			if (dynamicEffBottom != null)
			{
				for (int i = 0; i < dynamicEffBottom.size(); i++)
				{
					((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
				}
			}
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image != null)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, clip[dir] * 31, 20, 31, (dir == 3) ? 2 : 0, x + vangx, y - nhay + z + vangy + dy, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			}
			if (isFire)
			{
				int num = 10 * (indexDoc / 3) * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10;
				int num2 = -(10 * (indexDoc / 3) * Util_HAIRMOD_PROTECTED.Sin(angle)) >> 10;
				int num3 = x + num;
				int num4 = y + num2 - 25;
				g.drawRegion(Res_HAIRMOD_PROTECTED.loadImgEff(33), 0, (11 - indexDoc) / 3 * 14, 14, 14, 0, num3, num4, 3);
			}
			if (dynamicEffTop != null)
			{
				for (int j = 0; j < dynamicEffTop.size(); j++)
				{
					((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(j)).paint(g, x, y);
				}
			}
		}
		paintEffSkill(g);
	}

	public override void update()
	{
		base.update();
		if (timeNhay == 0)
		{
			nhay += vNhay;
			vNhay += dirNhay * 2;
			if (Util_HAIRMOD_PROTECTED.abs(vNhay) > 4)
			{
				dirNhay *= -1;
			}
			if (vNhay == 0 && dirNhay == 1)
			{
				timeNhay = 8;
			}
		}
		if (timeNhay > 0 && !isNhay)
		{
			timeNhay--;
		}
		if (x != xTo || y != yTo)
		{
			isNhay = false;
		}
		else
		{
			isNhay = true;
		}
		if (!isFreeze && isFire)
		{
			indexDoc++;
			if (indexDoc >= 12)
			{
				isFire = false;
				indexDoc = 0;
			}
		}
	}

	public override void move()
	{
		if (FuntioncanMove() && timeNhay == 0)
		{
			base.move();
		}
	}

	public override void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		if (isFire)
		{
			return;
		}
		LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(0);
		isFire = true;
		indexDoc = 0;
		angle = Util_HAIRMOD_PROTECTED.angle(liveActor_HAIRMOD_PROTECTED.x - x, -(liveActor_HAIRMOD_PROTECTED.y - y));
		if (list == null || list.size() <= 0)
		{
			return;
		}
		for (int i = 0; i < list.size(); i++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(i);
			if (liveActor_HAIRMOD_PROTECTED2 != null)
			{
				int num = liveActor_HAIRMOD_PROTECTED2.hp;
				liveActor_HAIRMOD_PROTECTED2.doublejump();
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + num, 0, liveActor_HAIRMOD_PROTECTED2.x, liveActor_HAIRMOD_PROTECTED2.y + liveActor_HAIRMOD_PROTECTED2.dy - 15, 1, -2);
			}
		}
	}
}
