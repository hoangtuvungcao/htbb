public class DynamicObj_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public Image_HAIRMOD_PROTECTED img;

	public sbyte w;

	public sbyte h;

	public sbyte nFrame;

	public sbyte id;

	public DynamicObj_HAIRMOD_PROTECTED()
	{
		catagory = 100;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (img != null)
		{
			g.drawRegion(img, 0, id * height, img.getWidth(), height, 0, x + 8, y + 8, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
	}

	public override void setPosTo(short x, short y)
	{
		base.x = x;
		base.y = y;
	}

	public override void paintName(MyGraphics_HAIRMOD_PROTECTED g)
	{
	}

	public override void update()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 3 == 0)
		{
			id = (sbyte)((id + 1) % nFrame);
		}
	}
}
