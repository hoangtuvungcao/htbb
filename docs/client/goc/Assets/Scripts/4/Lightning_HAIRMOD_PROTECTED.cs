public class Lightning_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private static int[] color = new int[2] { 16579837, 11188220 };

	public mVector_HAIRMOD_PROTECTED listPos = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED[] list;

	private Position_HAIRMOD_PROTECTED posangle;

	private long countDown;

	private long timeDel;

	public bool isContinue;

	public bool isRemove = true;

	private int cou;

	private int dem;

	private int aa = 7;

	public void reset()
	{
		countDown = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 10;
		timeDel = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 10;
		listPos.removeAllElements();
	}

	public void setInfo(mVector_HAIRMOD_PROTECTED position, Position_HAIRMOD_PROTECTED angle, bool isContinue)
	{
		if (position.size() == 0)
		{
			return;
		}
		this.isContinue = isContinue;
		if (!isContinue)
		{
			orderVector(position);
		}
		listPos = position;
		posangle = angle;
		list = new mVector_HAIRMOD_PROTECTED[position.size()];
		for (int i = 0; i < list.Length; i++)
		{
			list[i] = new mVector_HAIRMOD_PROTECTED();
		}
		angle.follow = -1;
		list[0].addElement(angle);
		int num = -1;
		for (int j = 0; j < position.size(); j++)
		{
			int num2 = angle.x;
			int num3 = angle.y;
			if (isContinue && num != -1)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)position.elementAt(num);
				num2 = position_HAIRMOD_PROTECTED.x;
				num3 = position_HAIRMOD_PROTECTED.y;
			}
			num = (isContinue ? (num + 1) : rndIndexList(position));
			int num4 = rndIndex(position, 1);
			int num5 = list[num].size() - 1;
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)position.elementAt(num);
			int num6 = Util_HAIRMOD_PROTECTED.angle(position_HAIRMOD_PROTECTED2.x - num2, -(position_HAIRMOD_PROTECTED2.y - num3));
			int num7 = Res_HAIRMOD_PROTECTED.rnd(15) + 10;
			int num8 = 0;
			int num9 = 0;
			while (true)
			{
				num9 = 0;
				if (num8 != 0)
				{
					num9 = num6 - 5 + Res_HAIRMOD_PROTECTED.rnd(10);
				}
				num9 = Util_HAIRMOD_PROTECTED.fixangle(num9);
				int num10 = num7 * num8 * Util_HAIRMOD_PROTECTED.Cos(num9) >> 10;
				int num11 = -(num7 * num8 * Util_HAIRMOD_PROTECTED.Sin(num9)) >> 10;
				Position_HAIRMOD_PROTECTED o = new Position_HAIRMOD_PROTECTED(num2 + num10, num3 + num11, num5++);
				list[num].addElement(o);
				if (Util_HAIRMOD_PROTECTED.distance(num2, num3, num2 + num10, num3 + num11) >= Util_HAIRMOD_PROTECTED.distance(num2, num3, position_HAIRMOD_PROTECTED2.x, position_HAIRMOD_PROTECTED2.y) - 20)
				{
					break;
				}
				num8++;
			}
		}
		for (int k = 0; k < list.Length; k++)
		{
			int num12 = list[k].size();
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED3 = (Position_HAIRMOD_PROTECTED)position.elementAt(k);
			position_HAIRMOD_PROTECTED3.follow = (sbyte)(list[k].size() - 1);
			position_HAIRMOD_PROTECTED3.index = -1;
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED4 = new Position_HAIRMOD_PROTECTED(position_HAIRMOD_PROTECTED3.x, position_HAIRMOD_PROTECTED3.y, position_HAIRMOD_PROTECTED3.follow);
			position_HAIRMOD_PROTECTED4.index = -1;
			list[k].addElement(position_HAIRMOD_PROTECTED4);
			for (int l = 1; l < num12; l++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED5 = (Position_HAIRMOD_PROTECTED)list[k].elementAt(l);
				int num13 = Res_HAIRMOD_PROTECTED.rnd(2);
				for (int m = 0; m < num13; m++)
				{
					int angle2 = 180 + Res_HAIRMOD_PROTECTED.rnd(180);
					int num14 = 5 + Res_HAIRMOD_PROTECTED.rnd(10);
					int num15 = num14 * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle2)) >> 10;
					int num16 = -(num14 * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle2))) >> 10;
					Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED6 = new Position_HAIRMOD_PROTECTED(position_HAIRMOD_PROTECTED5.x + num15, position_HAIRMOD_PROTECTED5.y + num16, l);
					position_HAIRMOD_PROTECTED6.index = 0;
					list[k].addElement(position_HAIRMOD_PROTECTED6);
				}
			}
		}
	}

	public static mVector_HAIRMOD_PROTECTED orderVector(mVector_HAIRMOD_PROTECTED obj)
	{
		int num = obj.size();
		for (int i = 0; i < num - 1; i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)obj.elementAt(i);
			for (int j = i + 1; j < num; j++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)obj.elementAt(j);
				if (position_HAIRMOD_PROTECTED.x > position_HAIRMOD_PROTECTED2.x)
				{
					obj.setElementAt(position_HAIRMOD_PROTECTED, j);
					obj.setElementAt(position_HAIRMOD_PROTECTED2, i);
					position_HAIRMOD_PROTECTED = position_HAIRMOD_PROTECTED2;
				}
			}
		}
		return obj;
	}

	private int rndIndex(mVector_HAIRMOD_PROTECTED pos, int chieu)
	{
		int num = 0;
		for (int i = 0; i < pos.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)pos.elementAt(i);
			if (position_HAIRMOD_PROTECTED.index != -1)
			{
				num++;
			}
		}
		if (num != 0)
		{
			num = Res_HAIRMOD_PROTECTED.rnd(num);
			int num2 = 0;
			for (int j = 0; j < pos.size(); j++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)pos.elementAt(j);
				if (position_HAIRMOD_PROTECTED2.index != -1)
				{
					num2++;
					if (num == num2)
					{
						return j;
					}
				}
			}
			return -1;
		}
		return -1;
	}

	private int rndIndexList(mVector_HAIRMOD_PROTECTED pos)
	{
		int num = 0;
		for (int i = 0; i < pos.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)pos.elementAt(i);
			if (position_HAIRMOD_PROTECTED.index == -1)
			{
				num++;
			}
		}
		if (num != 0)
		{
			num = Res_HAIRMOD_PROTECTED.rnd(num);
			int num2 = 0;
			for (int j = 0; j < pos.size(); j++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)pos.elementAt(j);
				if (position_HAIRMOD_PROTECTED2.index == -1)
				{
					if (num == num2)
					{
						position_HAIRMOD_PROTECTED2.index = 0;
						return j;
					}
					num2++;
				}
			}
			return -1;
		}
		return -1;
	}

	public override void update()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 != 1)
		{
			return;
		}
		posangle.follow = -1;
		posangle.index = -1;
		for (int i = 0; i < listPos.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)listPos.elementAt(i);
			if (position_HAIRMOD_PROTECTED != null)
			{
				position_HAIRMOD_PROTECTED.index = -1;
				position_HAIRMOD_PROTECTED.follow = -1;
			}
		}
		if (isContinue && isRemove && listPos.size() > 1 && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 10 - timeDel > 30)
		{
			timeDel = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 10;
			posangle = (Position_HAIRMOD_PROTECTED)listPos.elementAt(0);
			listPos.removeElementAt(0);
		}
		if (posangle != null)
		{
			setInfo(listPos, posangle, isContinue);
		}
		if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 10 - countDown > 60 + (isContinue ? 20 : 0))
		{
			aa = 7;
			if (EffectManager_HAIRMOD_PROTECTED.hiEffects.conTains(this))
			{
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
			}
		}
		countDown++;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		dem = 0;
		Res_HAIRMOD_PROTECTED.imgSmoke.drawFrame(cou / 3, posangle.x, posangle.y, 0, 3, g);
		cou++;
		if (cou >= 12)
		{
			cou = 0;
		}
		if (list == null)
		{
			return;
		}
		for (int i = 0; i < list.Length; i++)
		{
			for (int j = 0; j < list[i].size(); j++)
			{
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)list[i].elementAt(j);
				if (position_HAIRMOD_PROTECTED == null || position_HAIRMOD_PROTECTED.follow < 0 || position_HAIRMOD_PROTECTED.follow >= list[i].size())
				{
					continue;
				}
				Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = (Position_HAIRMOD_PROTECTED)list[i].elementAt(position_HAIRMOD_PROTECTED.follow);
				if (position_HAIRMOD_PROTECTED2 != null)
				{
					paintLine(g, position_HAIRMOD_PROTECTED, position_HAIRMOD_PROTECTED2);
				}
				if (isContinue && isRemove)
				{
					dem++;
					if (dem >= aa)
					{
						aa += 7;
						return;
					}
				}
			}
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED3 = (Position_HAIRMOD_PROTECTED)listPos.elementAt(i);
			if (position_HAIRMOD_PROTECTED3 != null)
			{
				Res_HAIRMOD_PROTECTED.backSmoke.drawFrame(1 + position_HAIRMOD_PROTECTED3.count / 4, position_HAIRMOD_PROTECTED3.x, position_HAIRMOD_PROTECTED3.y, 0, 3, g);
				position_HAIRMOD_PROTECTED3.count++;
				if (position_HAIRMOD_PROTECTED3.count >= 12)
				{
					position_HAIRMOD_PROTECTED3.count = 0;
				}
			}
		}
	}

	private void paintLine(MyGraphics_HAIRMOD_PROTECTED g, Position_HAIRMOD_PROTECTED pos1, Position_HAIRMOD_PROTECTED pos2)
	{
		if (pos1 == null || pos2 == null)
		{
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(pos1.x, pos1.y, pos2.x, pos2.y, color[0]));
		if (pos1.index == -1)
		{
			mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(pos1.x - 1, pos1.y, pos2.x - 1, pos2.y, color[1]));
			if (isContinue && isRemove)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new mLine_HAIRMOD_PROTECTED(pos1.x + 1, pos1.y, pos2.x + 1, pos2.y, color[1]));
			}
		}
		g.totalLine = mVector_HAIRMOD_PROTECTED2;
		g.drawlineGL();
	}
}
