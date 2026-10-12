public class ModelImgMap_HAIRMOD_PROTECTED
{
	public int x;

	public int y;

	public int id;

	public int wImg;

	public int hImg;

	public Image_HAIRMOD_PROTECTED image;

	public void setPos(int id, bool isLoad, int typePaint)
	{
		this.id = id;
		int num = id % 4;
		if (isLoad && image == null)
		{
			image = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/gc/m" + id);
			wImg = image.getWidth();
			hImg = image.getHeight();
			switch (typePaint)
			{
			case 0:
				x = num % 2 * wImg;
				y = num / 2 * hImg;
				break;
			case 1:
				x = num * wImg;
				y = 0;
				break;
			case 2:
				x = 0;
				y = num * hImg;
				break;
			}
		}
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (canPaint())
		{
			g.drawImagaByDrawTexture(image, x, y);
		}
	}

	public bool canPaint()
	{
		if (x > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w || x + wImg < GameScr_HAIRMOD_PROTECTED.cmx || y > GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h || y + hImg < GameScr_HAIRMOD_PROTECTED.cmy)
		{
			return false;
		}
		return true;
	}

	public void checkLoadImg()
	{
	}
}
