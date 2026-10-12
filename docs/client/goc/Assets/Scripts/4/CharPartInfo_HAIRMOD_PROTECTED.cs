using System;

public class CharPartInfo_HAIRMOD_PROTECTED
{
	public int type;

	public int id;

	public int timeRemove;

	public int[][] x;

	public int[][] y;

	public int[][] w;

	public int[][] h;

	public int[][] dx;

	public int[][] dy;

	public Image_HAIRMOD_PROTECTED image;

	private long timePaint;

	private int avxf;

	private int avyf;

	private int avx0;

	private int avy0;

	private int avw0;

	private int avh0;

	public DataInputStream_HAIRMOD_PROTECTED mD;

	public int time;

	public static sbyte[][] PART_OF_FRAME = new sbyte[5][]
	{
		new sbyte[6] { 0, 0, 1, 2, 3, 4 },
		new sbyte[6] { 0, 0, 1, 2, 3, 4 },
		new sbyte[6],
		new sbyte[6],
		new sbyte[6] { 0, 1, 0, 1, 0, 1 }
	};

	public CharPartInfo_HAIRMOD_PROTECTED(int type, int id)
	{
		this.type = type;
		this.id = id;
		dx = new int[4][];
		dy = new int[4][];
		for (int i = 0; i < dx.Length; i++)
		{
			dx[i] = new int[6];
			dy[i] = new int[6];
		}
		int num = 0;
		switch (type)
		{
		case 0:
		case 1:
			num = 5;
			break;
		case 2:
		case 3:
			num = 1;
			break;
		default:
			num = 2;
			break;
		}
		x = new int[4][];
		y = new int[4][];
		w = new int[4][];
		h = new int[4][];
		for (int j = 0; j < x.Length; j++)
		{
			x[j] = new int[num];
			y[j] = new int[num];
			w[j] = new int[num];
			h[j] = new int[num];
		}
		if (type < 4)
		{
			load(type, id);
		}
		else
		{
			loadCoat(type, id);
		}
	}

	public void load(int type)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.charAvatar[type] + id);
			sbyte[] array = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int i = 0; i < array.Length; i++)
			{
				array[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			image = Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length);
			sbyte[] array2 = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int j = 0; j < array2.Length; j++)
			{
				array2[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED2 = new DataInputStream_HAIRMOD_PROTECTED(array2);
			avx0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avy0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avw0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avh0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avxf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avyf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			try
			{
				for (int k = 0; k < 4; k++)
				{
					for (int l = 0; l < x[k].Length; l++)
					{
						x[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
						y[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
						w[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
						h[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
					}
					for (int m = 0; m < 6; m++)
					{
						dx[k][m] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
						dy[k][m] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
					}
				}
			}
			catch (Exception)
			{
			}
			dataInputStream_HAIRMOD_PROTECTED2.close();
			dataInputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception)
		{
		}
		finally
		{
			try
			{
			}
			catch (Exception)
			{
			}
		}
	}

	public void load(int type, int id)
	{
		DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = null;
		try
		{
			FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.charAvatar[type] + id);
			image = FilePack_HAIRMOD_PROTECTED.getImg(id + string.Empty);
			dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.instance.loadData(id + ".d"));
			FilePack_HAIRMOD_PROTECTED.reset();
			avx0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avy0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avw0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avh0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avxf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avyf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			try
			{
				for (int i = 0; i < 4; i++)
				{
					for (int j = 0; j < x[i].Length; j++)
					{
						x[i][j] = dataInputStream_HAIRMOD_PROTECTED.read();
						y[i][j] = dataInputStream_HAIRMOD_PROTECTED.read();
						w[i][j] = dataInputStream_HAIRMOD_PROTECTED.read();
						h[i][j] = dataInputStream_HAIRMOD_PROTECTED.read();
					}
					for (int k = 0; k < 6; k++)
					{
						dx[i][k] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
						dy[i][k] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
					}
				}
			}
			catch (Exception ex)
			{
				Out_HAIRMOD_PROTECTED.println(" 1 Loi tai ham load khong arr CharPartInfo" + ex.ToString());
			}
		}
		catch (Exception ex2)
		{
			Out_HAIRMOD_PROTECTED.println(" 2 Loi tai ham load khong arr CharPartInfo" + ex2.ToString());
		}
		finally
		{
			try
			{
				dataInputStream_HAIRMOD_PROTECTED.close();
			}
			catch (Exception ex3)
			{
				Out_HAIRMOD_PROTECTED.println(" 3 Loi tai ham load khong arr CharPartInfo" + ex3.ToString());
			}
		}
	}

	public void load(sbyte[] arr, int type, int Id)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(arr);
			sbyte[] array = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int i = 0; i < array.Length; i++)
			{
				array[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			image = Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length);
			sbyte[] array2 = new sbyte[dataInputStream_HAIRMOD_PROTECTED.readShort()];
			for (int j = 0; j < array2.Length; j++)
			{
				array2[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED2 = new DataInputStream_HAIRMOD_PROTECTED(array2);
			avx0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avy0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avw0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avh0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avxf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			avyf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
			try
			{
				if (type < 4)
				{
					for (int k = 0; k < 4; k++)
					{
						for (int l = 0; l < x[k].Length; l++)
						{
							x[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
							y[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
							w[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
							h[k][l] = dataInputStream_HAIRMOD_PROTECTED2.read();
						}
						for (int m = 0; m < 6; m++)
						{
							dx[k][m] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
							dy[k][m] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
						}
					}
					return;
				}
				for (int n = 0; n < 3; n++)
				{
					for (int num = 0; num < x[n].Length; num++)
					{
						x[n][num] = dataInputStream_HAIRMOD_PROTECTED2.read();
						y[n][num] = dataInputStream_HAIRMOD_PROTECTED2.read();
						w[n][num] = dataInputStream_HAIRMOD_PROTECTED2.read();
						h[n][num] = dataInputStream_HAIRMOD_PROTECTED2.read();
					}
				}
				for (int num2 = 0; num2 < 4; num2++)
				{
					for (int num3 = 0; num3 < 6; num3++)
					{
						dx[num2][num3] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
						dy[num2][num3] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED2);
					}
				}
			}
			catch (Exception)
			{
				Out_HAIRMOD_PROTECTED.println("ERROR in CharPartInfo.load()");
			}
		}
		catch (Exception)
		{
		}
	}

	public void loadCoat(int type, int id)
	{
		try
		{
			DataInputStream_HAIRMOD_PROTECTED resourceAsStream = DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(FilePack_HAIRMOD_PROTECTED.charAvatar[type] + id);
			sbyte[] array = new sbyte[resourceAsStream.readShort()];
			for (int i = 0; i < array.Length; i++)
			{
				array[i] = resourceAsStream.readByte();
			}
			image = Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length);
			sbyte[] array2 = new sbyte[resourceAsStream.readShort()];
			for (int j = 0; j < array2.Length; j++)
			{
				array2[j] = resourceAsStream.readByte();
			}
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array2);
			avx0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avy0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avw0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avh0 = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avxf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			avyf = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
			for (int k = 0; k < 3; k++)
			{
				for (int l = 0; l < x[k].Length; l++)
				{
					x[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
					y[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
					w[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
					h[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
				}
			}
			for (int m = 0; m < 4; m++)
			{
				for (int n = 0; n < 6; n++)
				{
					dx[m][n] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
					dy[m][n] = Res_HAIRMOD_PROTECTED.readSignByte(dataInputStream_HAIRMOD_PROTECTED);
				}
			}
			dataInputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println("Loi tai ham loadCoat CharPartInfo" + ex.ToString());
		}
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int xp, int yp, int dir, int frame)
	{
		if (type < 0)
		{
			return;
		}
		sbyte arg = 0;
		int num = dir;
		if (dir > 2)
		{
			arg = 2;
			num = 2;
		}
		if (image != null)
		{
			int num2 = dx[num][frame];
			int num3 = dy[num][frame];
			int num4 = w[num][PART_OF_FRAME[type][frame]];
			int num5 = h[num][PART_OF_FRAME[type][frame]];
			int num6 = x[num][PART_OF_FRAME[type][frame]];
			int num7 = y[num][PART_OF_FRAME[type][frame]];
			if (num6 > image.getWidth())
			{
				num6 = 0;
			}
			if (num7 > image.getHeight())
			{
				num7 = 0;
			}
			if (num6 + num4 > image.getWidth())
			{
				num4 = image.getWidth() - num6;
			}
			if (num7 + num5 > image.getHeight())
			{
				num5 = image.getHeight() - num7;
			}
			if (dir > 2)
			{
				num2 = -dx[num][frame] - num4;
				num3 = dy[num][frame];
			}
			g.drawRegion(image, num6, num7, num4, num5, arg, xp + num2, yp + num3, 0);
		}
		else
		{
			Res_HAIRMOD_PROTECTED.paintDefault(g, xp, yp, dir, frame, type);
		}
		timePaint = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
	}

	public void paintStatic(MyGraphics_HAIRMOD_PROTECTED g, short xp, short yp, int dir, int frame)
	{
		try
		{
			if (image != null)
			{
				int num = w[dir][PART_OF_FRAME[type][frame]];
				int num2 = h[dir][PART_OF_FRAME[type][frame]];
				int num3 = x[dir][PART_OF_FRAME[type][frame]];
				int num4 = y[dir][PART_OF_FRAME[type][frame]];
				if (num3 > image.getWidth())
				{
					num3 = 0;
				}
				if (num4 > image.getHeight())
				{
					num4 = 0;
				}
				if (num3 + num > image.getWidth())
				{
					num = image.getWidth() - num3;
				}
				if (num4 + num2 > image.getHeight())
				{
					num2 = image.getHeight() - num4;
				}
				g.drawRegion(image, num3, num4, num, num2, 0, xp, yp, 3);
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintAvatar(MyGraphics_HAIRMOD_PROTECTED g, short xp, short yp, int frame)
	{
		try
		{
			if (image != null)
			{
				g.drawRegion(image, avx0, avy0, avw0, avh0, 0, xp + avxf, yp + avyf, 0);
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintImage(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		g.drawImage(image, x, y, 0);
	}
}
