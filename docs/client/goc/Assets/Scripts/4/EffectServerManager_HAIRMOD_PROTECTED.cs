public class EffectServerManager_HAIRMOD_PROTECTED
{
	public short ID;

	public short loop;

	public short loopLimit;

	public short radius;

	public short x;

	public short y;

	public short count;

	public short indexLoop;

	public short indexPos;

	public short idPlayer;

	public sbyte style;

	public sbyte loopType;

	public short[] xLoop;

	public short[] yLoop;

	public static mVector_HAIRMOD_PROTECTED effectList = new mVector_HAIRMOD_PROTECTED();

	public EffectServerManager_HAIRMOD_PROTECTED()
	{
		count = 0;
	}

	public void update()
	{
		EffectData_HAIRMOD_PROTECTED effect = getEffect(ID);
		if (effect == null)
		{
			return;
		}
		if (style == 0)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID(idPlayer);
			x = actor_HAIRMOD_PROTECTED.x;
			y = actor_HAIRMOD_PROTECTED.y;
		}
		if (count == loopLimit)
		{
			count = 0;
			EffectObj_HAIRMOD_PROTECTED effectObj_HAIRMOD_PROTECTED = new EffectObj_HAIRMOD_PROTECTED();
			effectObj_HAIRMOD_PROTECTED.ID = ID;
			effectObj_HAIRMOD_PROTECTED.idPlayer = idPlayer;
			effectObj_HAIRMOD_PROTECTED.style = style;
			switch (loopType)
			{
			case 0:
				effectObj_HAIRMOD_PROTECTED.x = x;
				effectObj_HAIRMOD_PROTECTED.y = y;
				break;
			case 1:
			{
				int num = Res_HAIRMOD_PROTECTED.rnd(radius);
				int angle = Res_HAIRMOD_PROTECTED.rnd(360);
				int num2 = num * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
				int num3 = -(num * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
				effectObj_HAIRMOD_PROTECTED.x = x;
				effectObj_HAIRMOD_PROTECTED.y = y;
				effectObj_HAIRMOD_PROTECTED.dx = (short)num2;
				effectObj_HAIRMOD_PROTECTED.dy = (short)num3;
				break;
			}
			case 2:
				effectObj_HAIRMOD_PROTECTED.x = x;
				effectObj_HAIRMOD_PROTECTED.y = y;
				if (style == 0)
				{
					effectObj_HAIRMOD_PROTECTED.dx = xLoop[indexPos];
					effectObj_HAIRMOD_PROTECTED.dy = yLoop[indexPos];
				}
				else
				{
					effectObj_HAIRMOD_PROTECTED.x += xLoop[indexPos];
					effectObj_HAIRMOD_PROTECTED.y += yLoop[indexPos];
				}
				break;
			}
			indexLoop++;
			indexPos++;
			if (xLoop != null && indexPos >= xLoop.Length)
			{
				indexPos = 0;
			}
			if (loop != -1 && indexLoop >= loop)
			{
				GameScr_HAIRMOD_PROTECTED.effManager.removeElement(this);
			}
			sbyte b = style;
			if (b == 0 || b == 1)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(effectObj_HAIRMOD_PROTECTED);
			}
		}
		count++;
	}

	public static EffectData_HAIRMOD_PROTECTED getEffect(short id)
	{
		for (int i = 0; i < effectList.size(); i++)
		{
			EffectData_HAIRMOD_PROTECTED effectData_HAIRMOD_PROTECTED = (EffectData_HAIRMOD_PROTECTED)effectList.elementAt(i);
			if (effectData_HAIRMOD_PROTECTED.ID == id)
			{
				return effectData_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}
}
