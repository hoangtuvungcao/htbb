public class CharCountDown_HAIRMOD_PROTECTED
{
	private long maxTime;

	private int w;

	public bool WantDestroy;

	public string tile = string.Empty;

	private long timesv;

	private int timeper;

	private int per;

	public CharCountDown_HAIRMOD_PROTECTED(long time, string tile)
	{
		this.tile = tile;
		maxTime = time;
		w = 60;
		timeper = (int)(maxTime - mSystem_HAIRMOD_PROTECTED.currentTimeMillis());
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		int num = 0;
		long num2 = maxTime - mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		num = (int)(60 - num2 * w / timeper);
		g.drawRegion(GameScr_HAIRMOD_PROTECTED.imghealth[1], 0, 9, 62, 9, 0, x, y, 0);
		g.drawRegion(GameScr_HAIRMOD_PROTECTED.imghealth[0], 0, 7, num, 7, 0, x, y + 1, 0);
		MFont_HAIRMOD_PROTECTED.arialFont.drawString(g, tile, x + w / 2 + 1, y - 10 + 1 - 3, 2);
		MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, tile, x + w / 2, y - 10 - 3, 2);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, string.Empty + per + "%", x + w / 2, y, 2);
	}

	public void update()
	{
		long num = maxTime - mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		per = (int)(100 - num * 100 / timeper);
		if (maxTime - mSystem_HAIRMOD_PROTECTED.currentTimeMillis() <= 0)
		{
			WantDestroy = true;
		}
	}

	public static string converSecon2minutes(int time)
	{
		int num = time % 60;
		int num2 = time / 60 % 60;
		if (num2 <= 0)
		{
			return num + string.Empty;
		}
		return num2 + ":" + num;
	}
}
