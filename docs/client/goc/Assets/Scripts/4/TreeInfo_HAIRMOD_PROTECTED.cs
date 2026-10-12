using System;

public class TreeInfo_HAIRMOD_PROTECTED
{
	public int dx;

	public int dy;

	public int startx;

	public int starty;

	public int endx;

	public int endy;

	public int w;

	public int h;

	public int id;

	public Image_HAIRMOD_PROTECTED imgTree;

	private long timeGetImage;

	public void onImage(sbyte[] arr, int index)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(arr);
			sbyte[] array = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int i = 0; i < array.Length; i++)
			{
				array[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			imgTree = Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length);
			sbyte[] array2 = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int j = 0; j < array2.Length; j++)
			{
				array2[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED2 = new DataInputStream_HAIRMOD_PROTECTED(array2);
			dx = dataInputStream_HAIRMOD_PROTECTED2.readByte();
			dy = dataInputStream_HAIRMOD_PROTECTED2.readByte();
			startx = dataInputStream_HAIRMOD_PROTECTED2.readUnsignedByte();
			starty = dataInputStream_HAIRMOD_PROTECTED2.readUnsignedByte();
			endx = dataInputStream_HAIRMOD_PROTECTED2.readUnsignedByte();
			endy = dataInputStream_HAIRMOD_PROTECTED2.readUnsignedByte();
			w = imgTree.getWidth();
			h = imgTree.getHeight();
			dataInputStream_HAIRMOD_PROTECTED.close();
			dataInputStream_HAIRMOD_PROTECTED2.close();
		}
		catch (Exception)
		{
			imgTree = null;
		}
	}

	public Image_HAIRMOD_PROTECTED getImage()
	{
		if (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeGetImage > 180000 && imgTree == null)
		{
			GameService_HAIRMOD_PROTECTED.gI().getTreeImage(id, 0, 0);
		}
		timeGetImage = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		return imgTree;
	}

	public int getHeight()
	{
		if (imgTree != null)
		{
			return imgTree.getHeight();
		}
		return 0;
	}

	public int getWidth()
	{
		if (imgTree != null)
		{
			return imgTree.getWidth();
		}
		return 0;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (getImage() != null)
		{
			g.drawImage(imgTree, x, y, 0);
		}
	}

	public void loadData()
	{
		try
		{
			imgTree = Image_HAIRMOD_PROTECTED.createImage("/t6.png");
			DataInputStream_HAIRMOD_PROTECTED resourceAsStream = DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(Main_HAIRMOD_PROTECTED.res + "t6");
			dx = resourceAsStream.readUnsignedByte();
			dy = resourceAsStream.readUnsignedByte();
			if (dx > 127)
			{
				dx -= 256;
			}
			if (dy > 127)
			{
				dy -= 256;
			}
			startx = resourceAsStream.readUnsignedByte();
			starty = resourceAsStream.readUnsignedByte();
			endx = resourceAsStream.readUnsignedByte();
			endy = resourceAsStream.readUnsignedByte();
			w = imgTree.getWidth();
			h = imgTree.getHeight();
		}
		catch (Exception)
		{
		}
	}
}
