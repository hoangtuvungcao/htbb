public class TileTop_HAIRMOD_PROTECTED
{
	public short x;

	public short y;

	public short index;

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, Image_HAIRMOD_PROTECTED img)
	{
		g.drawImagaByDrawTexture(img, x << 4, y << 4);
	}

	public void paintSmall(MyGraphics_HAIRMOD_PROTECTED g)
	{
	}
}
