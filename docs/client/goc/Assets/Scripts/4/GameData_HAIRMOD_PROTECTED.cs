using System;
using System.IO;

public class GameData_HAIRMOD_PROTECTED
{
	private static GameData_HAIRMOD_PROTECTED me;

	public static FrameImage_HAIRMOD_PROTECTED imgSkillIcon;

	public static mVector_HAIRMOD_PROTECTED imgHorse = new mVector_HAIRMOD_PROTECTED();

	public static MyHashTable_HAIRMOD_PROTECTED listImgIcon = new MyHashTable_HAIRMOD_PROTECTED();

	private bool loadData;

	public static byte idWait = 0;

	public static Image_HAIRMOD_PROTECTED imgWait;

	public static GameData_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new GameData_HAIRMOD_PROTECTED());
	}

	public void getImage()
	{
		if (!loadData)
		{
			loadImgPotion();
			loadImgGemItem();
			loadData = true;
		}
	}

	public static void loadImgPotion()
	{
		try
		{
			RMS_HAIRMOD_PROTECTED.clearRecord("nqshImgPotion");
		}
		catch (Exception)
		{
		}
		sbyte[] array = RMS_HAIRMOD_PROTECTED.loadRMS("nqshImgPotionNew");
		int ver = 0;
		if (array == null)
		{
			ver = 0;
		}
		else
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			sbyte[] array2 = null;
			sbyte[][] array3 = null;
			try
			{
				short num = dataInputStream_HAIRMOD_PROTECTED.readShort();
				array2 = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref array2);
				sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
				for (int i = 0; i < b; i++)
				{
					short num2 = dataInputStream_HAIRMOD_PROTECTED.readShort();
					sbyte[] data = new sbyte[num2];
					dataInputStream_HAIRMOD_PROTECTED.read(ref data);
					sbyte b2 = dataInputStream_HAIRMOD_PROTECTED.readByte();
					array3 = new sbyte[b2][];
					for (int j = 0; j < b2; j++)
					{
						short num3 = dataInputStream_HAIRMOD_PROTECTED.readShort();
						array3[j] = new sbyte[num3];
						dataInputStream_HAIRMOD_PROTECTED.read(ref array3[j]);
					}
					createImgHorse(data, array3);
				}
				ver = dataInputStream_HAIRMOD_PROTECTED.readByte();
				dataInputStream_HAIRMOD_PROTECTED.close();
			}
			catch (IOException ex2)
			{
				Out_HAIRMOD_PROTECTED.LogError("  loi   loadImgPotion" + ex2.StackTrace);
			}
		}
		GameService_HAIRMOD_PROTECTED.gI().doGetImage(2, ver);
	}

	private static void createImgHorse(sbyte[] data, sbyte[][] header)
	{
		for (int i = 0; i < header.Length; i++)
		{
			imgHorse.addElement(Res_HAIRMOD_PROTECTED.createImgByHeader(header[i], data));
		}
	}

	public static void saveImgPotion(sbyte ver, sbyte[] arrayPotion, sbyte[] arrayItem, sbyte[][] dataHorse, sbyte[][][] headHorse)
	{
		DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
		try
		{
			dataOutputStream_HAIRMOD_PROTECTED.writeShort((short)arrayPotion.Length);
			dataOutputStream_HAIRMOD_PROTECTED.write(arrayPotion);
			dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)dataHorse.Length);
			for (int i = 0; i < dataHorse.Length; i++)
			{
				dataOutputStream_HAIRMOD_PROTECTED.writeShort((short)dataHorse[i].Length);
				dataOutputStream_HAIRMOD_PROTECTED.write(dataHorse[i]);
				dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)headHorse[i].Length);
				for (int j = 0; j < headHorse[i].Length; j++)
				{
					dataOutputStream_HAIRMOD_PROTECTED.writeShort((short)headHorse[i][j].Length);
					dataOutputStream_HAIRMOD_PROTECTED.write(headHorse[i][j]);
				}
				createImgHorse(dataHorse[i], headHorse[i]);
			}
			dataOutputStream_HAIRMOD_PROTECTED.writeByte(ver);
			RMS_HAIRMOD_PROTECTED.saveRMS("nqshImgPotionNew", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
			dataOutputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(" loi saveImgPotion" + ex.StackTrace);
		}
	}

	public static void saveImgSkill(int ver, sbyte[] array)
	{
		imgSkillIcon = new FrameImage_HAIRMOD_PROTECTED(Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length), 20, 20);
	}

	public static void saveImgGem(sbyte ver, sbyte[] array)
	{
		DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
		try
		{
			dataOutputStream_HAIRMOD_PROTECTED.writeByte(ver);
			dataOutputStream_HAIRMOD_PROTECTED.writeShort((short)array.Length);
			dataOutputStream_HAIRMOD_PROTECTED.write(array);
			RMS_HAIRMOD_PROTECTED.saveRMS("gemItem", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
			dataOutputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(" loi saveImgGem " + ex.StackTrace);
		}
	}

	public static void paintIconCayThan(MyGraphics_HAIRMOD_PROTECTED g, short id, int x, int y)
	{
		ImageIcon_HAIRMOD_PROTECTED imgIcon = getImgIcon(id);
		if (imgIcon != null)
		{
			if (!imgIcon.isLoad)
			{
				g.drawImage(imgIcon.img, x, y, 3);
				return;
			}
			g.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, x, y, 3);
			setIndexWait();
		}
	}

	public static void loadImgGemItem()
	{
		sbyte[] array = RMS_HAIRMOD_PROTECTED.loadRMS("gemItem");
		int ver = 0;
		if (array == null)
		{
			ver = 0;
		}
		else
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			sbyte[] array2 = null;
			try
			{
				ver = dataInputStream_HAIRMOD_PROTECTED.readByte();
				short num = dataInputStream_HAIRMOD_PROTECTED.readShort();
				array2 = new sbyte[num];
				dataInputStream_HAIRMOD_PROTECTED.read(ref array2);
				dataInputStream_HAIRMOD_PROTECTED.close();
			}
			catch (Exception ex)
			{
				Out_HAIRMOD_PROTECTED.LogError(ex.StackTrace + " loi load gamedata");
			}
		}
		GameService_HAIRMOD_PROTECTED.gI().doGetImage(3, ver);
	}

	public static void loadbg()
	{
		try
		{
			if (imgWait == null)
			{
				imgWait = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/waiting");
			}
		}
		catch (Exception)
		{
		}
	}

	public static void setIndexWait()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
		{
			idWait = (byte)((idWait + 1) % (imgWait.getHeight() / 18));
		}
	}

	public static void paintIconTuBinh(MyGraphics_HAIRMOD_PROTECTED g, short id, int x, int y, int align)
	{
		ImageIcon_HAIRMOD_PROTECTED imgIcon = getImgIcon(id);
		if (imgIcon != null)
		{
			if (!imgIcon.isLoad)
			{
				g.drawImage(imgIcon.img, x, y, align);
				return;
			}
			g.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, x, y, 3);
			setIndexWait();
		}
	}

	public static void paintIcon(MyGraphics_HAIRMOD_PROTECTED g, short id, int x, int y)
	{
		ImageIcon_HAIRMOD_PROTECTED imgIcon = getImgIcon(id);
		if (imgIcon == null)
		{
			return;
		}
		if (!imgIcon.isLoad)
		{
			int width = imgIcon.img.getWidth();
			int num = imgIcon.img.getHeight();
			int num2 = 1;
			if (num > 25)
			{
				num /= 3;
				num2 = 3;
			}
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
			{
				imgIcon.indexFrame = (byte)((imgIcon.indexFrame + 1) % num2);
			}
			g.drawRegion(imgIcon.img, 0, imgIcon.indexFrame * num, width, num, 0, x, y, 3);
		}
		else
		{
			g.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, x, y, 3);
			setIndexWait();
		}
	}

	public static void paintIcon(MyGraphics_HAIRMOD_PROTECTED g, short id, int x, int y, int align)
	{
		ImageIcon_HAIRMOD_PROTECTED imgIcon = getImgIcon(id);
		if (imgIcon == null)
		{
			return;
		}
		if (!imgIcon.isLoad)
		{
			int width = imgIcon.img.getWidth();
			int num = imgIcon.img.getHeight();
			int num2 = 1;
			if (num > 25)
			{
				num /= 3;
				num2 = 3;
			}
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 5 == 0)
			{
				imgIcon.indexFrame = (byte)((imgIcon.indexFrame + 1) % num2);
			}
			g.drawRegion(imgIcon.img, 0, imgIcon.indexFrame * num, width, num, 0, x, y, align);
		}
		else
		{
			g.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, x, y, 3);
			setIndexWait();
		}
	}

	public static ImageIcon_HAIRMOD_PROTECTED getImgIcon(short id)
	{
		ImageIcon_HAIRMOD_PROTECTED imageIcon_HAIRMOD_PROTECTED = (ImageIcon_HAIRMOD_PROTECTED)listImgIcon.get(id + string.Empty);
		if (imageIcon_HAIRMOD_PROTECTED == null)
		{
			imageIcon_HAIRMOD_PROTECTED = new ImageIcon_HAIRMOD_PROTECTED();
			imageIcon_HAIRMOD_PROTECTED.id = id;
			imageIcon_HAIRMOD_PROTECTED.isLoad = true;
			listImgIcon.put(string.Empty + id, imageIcon_HAIRMOD_PROTECTED);
			GameService_HAIRMOD_PROTECTED.gI().doGetImgIcon(id);
			imageIcon_HAIRMOD_PROTECTED.timeGetBack = (int)(mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000);
		}
		else
		{
			if (imageIcon_HAIRMOD_PROTECTED.img == null && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - imageIcon_HAIRMOD_PROTECTED.timeGetBack > 30)
			{
				GameService_HAIRMOD_PROTECTED.gI().doGetImgIcon(id);
				imageIcon_HAIRMOD_PROTECTED.timeGetBack = (int)(mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000);
			}
			imageIcon_HAIRMOD_PROTECTED.timeRemove = (int)(mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000);
		}
		return imageIcon_HAIRMOD_PROTECTED;
	}
}
