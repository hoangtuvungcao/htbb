public class Skill_AEO_CUNG_5_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED list;

	private Char_HAIRMOD_PROTECTED c;

	private int v = 12;

	private int dir = 1;

	private int count;

	private int dem;

	public Skill_AEO_CUNG_5_HAIRMOD_PROTECTED(mVector_HAIRMOD_PROTECTED mst, Char_HAIRMOD_PROTECTED c)
	{
		list = mst;
		v = 12;
		this.c = c;
		for (int i = 0; i < list.size(); i++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(i);
			if (liveActor_HAIRMOD_PROTECTED is Monster_HAIRMOD_PROTECTED)
			{
				((Monster_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).state = 3;
			}
			else if (liveActor_HAIRMOD_PROTECTED is Char_HAIRMOD_PROTECTED)
			{
				((Char_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).state = 2;
			}
		}
	}

	public override void update()
	{
		count++;
		if (dem > 6 && v == 0)
		{
			dir = -1;
		}
		if (dir == -1 && v != 11)
		{
			v++;
		}
		if (v > 0 && dir == 1)
		{
			v--;
		}
		if (dir == -1 && v == 11)
		{
			for (int i = 0; i < list.size(); i++)
			{
				LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(i);
				if (liveActor_HAIRMOD_PROTECTED is Monster_HAIRMOD_PROTECTED)
				{
					((Monster_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).state = 0;
				}
				else if (liveActor_HAIRMOD_PROTECTED is Char_HAIRMOD_PROTECTED)
				{
					((Char_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).state = 0;
				}
			}
			EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
		}
		for (int j = 0; j < list.size(); j++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(j);
			liveActor_HAIRMOD_PROTECTED2.dy -= (short)(v * dir);
		}
		if (v != 0 || dir != 1 || count % 3 != 0)
		{
			return;
		}
		if (dem <= 3)
		{
			for (int k = 0; k < list.size(); k++)
			{
				LiveActor_HAIRMOD_PROTECTED to = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(k);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(0, c, to, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
			}
		}
		dem++;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
	}
}
