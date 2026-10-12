public class NameAttributeItem_HAIRMOD_PROTECTED
{
	public sbyte ispercent;

	public sbyte colorPaint;

	public string name = string.Empty;

	public short id;

	public NameAttributeItem_HAIRMOD_PROTECTED(short id, string name, sbyte ispercent, sbyte colorPaint)
	{
		this.id = id;
		this.name = name;
		this.colorPaint = colorPaint;
		this.ispercent = ispercent;
	}

	public sbyte getColorPaint(bool kick)
	{
		sbyte b = colorPaint;
		if (b == 1 && kick)
		{
			b = 2;
		}
		return b;
	}
}
