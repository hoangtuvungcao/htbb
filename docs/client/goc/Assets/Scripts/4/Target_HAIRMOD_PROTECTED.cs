public class Target_HAIRMOD_PROTECTED
{
	public short x;

	public short y;

	public byte index;

	public Target_HAIRMOD_PROTECTED()
	{
		index = 0;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (x != -1 && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.state == 1 && y != -1 && Res_HAIRMOD_PROTECTED.imgTG != null)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgTG, 0, index * 38, 38, 38, 0, x * 16, y * 16, 33);
		}
	}

	public void update()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			index++;
			if (index > 6)
			{
				index = 0;
			}
		}
	}
}
