public class CharBuff_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	public int timeLife = -1;

	public long tick;

	private int timeReceive;

	private int count;

	public CharBuff_HAIRMOD_PROTECTED(int x, int y, int type)
		: base(x, y, type)
	{
		tick = 0L;
	}

	public void setTimeLive(int time)
	{
		timeLife = time;
		count = 0;
		tick = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() + timeLife * 1000;
		if (timeLife <= 0)
		{
			wantDetroy = true;
		}
	}

	public bool isCenter()
	{
		return type == 20 || type == 22 || type == 23 || type == 24 || type == 25 || type == 27;
	}

	public override void update()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			f = (f + 1) % Effect_HAIRMOD_PROTECTED.FRAME[type].Length;
		}
		if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() > tick)
		{
			wantDetroy = true;
		}
	}

	public void setXY(int x, int y)
	{
		base.x = x;
		base.y = y;
	}
}
