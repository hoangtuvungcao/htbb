public class PartFrame_HAIRMOD_PROTECTED
{
	public short dx;

	public short dy;

	public sbyte idSmallImg;

	public sbyte flip;

	public sbyte onTop;

	public sbyte[] data;

	public short id;

	public PartFrame_HAIRMOD_PROTECTED(short dx, short dy, sbyte idSmall)
	{
		idSmallImg = idSmall;
		this.dx = dx;
		this.dy = dy;
	}

	public PartFrame_HAIRMOD_PROTECTED(int id, sbyte[] data)
	{
		this.id = (short)id;
		this.data = data;
	}
}
