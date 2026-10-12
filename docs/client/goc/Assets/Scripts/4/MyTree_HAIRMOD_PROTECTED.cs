public class MyTree_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public int himg = 1;

	public byte idImg;

	public short idTree;

	public MyTree_HAIRMOD_PROTECTED(int id, int x, int y, int idImg)
	{
		idTree = (short)id;
		base.x = (short)x;
		base.y = (short)y;
		this.idImg = (byte)idImg;
		catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_TREE;
		himg = 1;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (idTree <= -1)
		{
			return;
		}
		ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 3200));
		if (imgIcon != null && !imgIcon.isLoad)
		{
			if (himg == 1)
			{
				himg = imgIcon.img.getHeight();
			}
			g.drawImage(imgIcon.img, x, y, 33);
		}
	}

	public override void setPosTo(short x, short y)
	{
	}
}
