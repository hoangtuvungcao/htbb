public class Position_HAIRMOD_PROTECTED
{
	public int x;

	public int y;

	public int anchor;

	public sbyte follow;

	public sbyte count;

	public sbyte dir = 1;

	public short index = -1;

	public short indexColor;

	public int id = -1000;

	public Position_HAIRMOD_PROTECTED(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Position_HAIRMOD_PROTECTED(int x, int y, int fol)
	{
		this.x = x;
		this.y = y;
		follow = (sbyte)fol;
	}

	public Position_HAIRMOD_PROTECTED()
	{
	}
}
