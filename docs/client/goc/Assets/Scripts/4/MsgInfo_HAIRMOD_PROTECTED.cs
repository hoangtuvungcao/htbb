public class MsgInfo_HAIRMOD_PROTECTED
{
	public string message;

	public string[] arr;

	private int x;

	private int w;

	private long timeRemove = -1L;

	public MsgInfo_HAIRMOD_PROTECTED(string message)
	{
		this.message = message;
		arr = MFont_HAIRMOD_PROTECTED.arialFontW.splitFontBStrInLine(message, Canvas_HAIRMOD_PROTECTED.w - 8);
		w = MFont_HAIRMOD_PROTECTED.arialFontW.getWidth(arr[0]);
		x = Canvas_HAIRMOD_PROTECTED.w + w / 2;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < arr.Length; i++)
		{
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, arr[i], x, i * 13, 2);
		}
	}

	public void update()
	{
		if (x != Canvas_HAIRMOD_PROTECTED.w / 2)
		{
			if (Canvas_HAIRMOD_PROTECTED.w / 2 - x >> 1 == 0)
			{
				x = Canvas_HAIRMOD_PROTECTED.w / 2;
			}
			else
			{
				x += Canvas_HAIRMOD_PROTECTED.w / 2 - x >> 1;
			}
		}
		if (x == Canvas_HAIRMOD_PROTECTED.w / 2)
		{
			if (timeRemove == -1)
			{
				timeRemove = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 6;
			}
			if (timeRemove - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 <= 0)
			{
				GameScr_HAIRMOD_PROTECTED.msgWorld.removeElement(this);
			}
		}
	}
}
