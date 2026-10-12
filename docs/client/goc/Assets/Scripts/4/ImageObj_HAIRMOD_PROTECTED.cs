using System;

public class ImageObj_HAIRMOD_PROTECTED
{
	private Image_HAIRMOD_PROTECTED[] img = new Image_HAIRMOD_PROTECTED[1];

	public sbyte[] index;

	public sbyte[] x0;

	public sbyte[] y0;

	public sbyte[] w;

	public sbyte[] h;

	public short[] dx;

	public short[] dy;

	public ImageObj_HAIRMOD_PROTECTED()
	{
	}

	public ImageObj_HAIRMOD_PROTECTED(int type, int palate)
	{
		try
		{
			sbyte[] header = FilePack_HAIRMOD_PROTECTED.instance.loadFile(palate + "_h");
			sbyte[] data = FilePack_HAIRMOD_PROTECTED.instance.loadFile("data");
			img[0] = Res_HAIRMOD_PROTECTED.createImgByHeader(header, data);
			sbyte[] data2 = FilePack_HAIRMOD_PROTECTED.instance.loadFile("pos");
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(data2);
			int num = dataInputStream_HAIRMOD_PROTECTED.readByte();
			index = new sbyte[num];
			x0 = new sbyte[num];
			y0 = new sbyte[num];
			w = new sbyte[num];
			h = new sbyte[num];
			dx = new short[num];
			dy = new short[num];
			for (int i = 0; i < num; i++)
			{
				index[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				x0[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				y0[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				w[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				h[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				dx[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				dy[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
		}
		catch (Exception)
		{
		}
	}

	public ImageObj_HAIRMOD_PROTECTED(sbyte[] array)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			img = new Image_HAIRMOD_PROTECTED[dataInputStream_HAIRMOD_PROTECTED.readByte()];
			for (int i = 0; i < img.Length; i++)
			{
				int num = dataInputStream_HAIRMOD_PROTECTED.readShort();
				sbyte[] data = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref data);
				img[i] = Image_HAIRMOD_PROTECTED.createImage(data, 0, num);
			}
		}
		catch (Exception)
		{
		}
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int index, int trans, int dirX, int dirY)
	{
		g.drawRegion(img[0], x0[index], y0[index], w[index], h[index], trans, x + dx[index] * dirX, y + dy[index] * dirY, 3);
	}

	public Image_HAIRMOD_PROTECTED getImage(int indexImg)
	{
		if (img == null)
		{
			return null;
		}
		return img[indexImg];
	}

	public void initImage(int nImg)
	{
		img = new Image_HAIRMOD_PROTECTED[nImg];
	}
}
