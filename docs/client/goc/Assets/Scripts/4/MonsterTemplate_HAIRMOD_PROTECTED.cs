using System;
using UnityEngine;

public class MonsterTemplate_HAIRMOD_PROTECTED
{
	public static MyHashTable_HAIRMOD_PROTECTED ALL_DATE_NEW_MONSTER = new MyHashTable_HAIRMOD_PROTECTED();

	public int type = -1;

	public Image_HAIRMOD_PROTECTED image;

	public Image_HAIRMOD_PROTECTED[] Arrimage;

	public sbyte moveType;

	public sbyte speed = 1;

	public sbyte height;

	public sbyte w;

	public sbyte h;

	public sbyte xCenter;

	public sbyte yCenter;

	public sbyte xadd;

	public sbyte yadd;

	public sbyte isNewMonster;

	public string name;

	public sbyte he;

	public sbyte palate;

	public sbyte spalate;

	public int maxhp;

	public bool isLoad;

	public bool isMaxHight;

	public ImageObj_HAIRMOD_PROTECTED imageObj;

	public mVector_HAIRMOD_PROTECTED dataMonsterNew = new mVector_HAIRMOD_PROTECTED();

	public long timePaint;

	private sbyte[] arrayDataMonsterNew;

	public MonsterTemplate_HAIRMOD_PROTECTED()
	{
	}

	public MonsterTemplate_HAIRMOD_PROTECTED(int type, string name, string image, sbyte moveType, sbyte speed, sbyte height, sbyte w, sbyte h, sbyte xCenter, sbyte yCenter, sbyte xadd, sbyte yadd)
	{
		this.type = type;
		this.name = name;
		this.moveType = moveType;
		this.speed = speed;
		this.height = height;
		this.w = w;
		this.h = h;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.xadd = xadd;
		this.yadd = yadd;
		isMaxHight = false;
	}

	public void setInfo(int type, string name, string image, sbyte moveType, sbyte speed, sbyte height, sbyte w, sbyte h, sbyte xCenter, sbyte yCenter, sbyte xadd, sbyte yadd)
	{
		this.type = type;
		this.name = name;
		this.moveType = moveType;
		this.speed = speed;
		this.height = height;
		this.w = w;
		this.h = h;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.xadd = xadd;
		this.yadd = yadd;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int flip, int anchor, int f)
	{
		if (image != null)
		{
			try
			{
				g.drawRegion(image, 0, f * h, w, h, flip, x - xCenter, y - yCenter, anchor);
			}
			catch (Exception)
			{
			}
		}
		timePaint = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
	}

	public void resetImage()
	{
		if (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timePaint > 60000)
		{
			image = null;
			imageObj = null;
			dataMonsterNew.removeAllElements();
			ALL_DATE_NEW_MONSTER.remove(string.Empty + spalate);
		}
	}

	public mVector_HAIRMOD_PROTECTED getDataMonster()
	{
		try
		{
			if (type > -1)
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)ALL_DATE_NEW_MONSTER.get(string.Empty + spalate);
				if (mVector_HAIRMOD_PROTECTED2 != null)
				{
					dataMonsterNew = mVector_HAIRMOD_PROTECTED2;
				}
				else
				{
					DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(Main_HAIRMOD_PROTECTED.res + "/boss/" + spalate);
					sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
					for (int i = 0; i < b; i++)
					{
						short num = dataInputStream_HAIRMOD_PROTECTED.readShort();
						sbyte[] array = new sbyte[num];
						for (int j = 0; j < array.Length; j++)
						{
							array[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
						}
						dataMonsterNew.addElement(new DataEffect_HAIRMOD_PROTECTED(array));
					}
					ALL_DATE_NEW_MONSTER.put(string.Empty + spalate, dataMonsterNew);
				}
			}
			timePaint = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		}
		catch (Exception)
		{
		}
		return dataMonsterNew;
	}

	public void loadImage()
	{
		if (!isLoad && type > -1)
		{
			GameService_HAIRMOD_PROTECTED.gI().doRequestImageMonster(type, palate, spalate);
			isLoad = true;
		}
	}

	public void onImage(bool isSave, sbyte[] arr)
	{
		try
		{
			if ((palate >= 70 && palate <= 88) || palate == 90 || palate == 91 || palate == 92 || isNewMonster == 1)
			{
				imageObj = new ImageObj_HAIRMOD_PROTECTED(arr);
			}
			else
			{
				FilePack_HAIRMOD_PROTECTED.initByArray(arr);
				if (palate == 39 || palate == 40)
				{
					imageObj = new ImageObj_HAIRMOD_PROTECTED(palate, spalate);
				}
				else
				{
					sbyte[] header = FilePack_HAIRMOD_PROTECTED.instance.loadFile(spalate + "_h");
					sbyte[] data = FilePack_HAIRMOD_PROTECTED.instance.loadFile("data");
					image = Res_HAIRMOD_PROTECTED.createImgByHeader(header, data);
					int num = image.getHeight();
					isMaxHight = false;
					if (num > 1024)
					{
						isMaxHight = true;
						Arrimage = new Image_HAIRMOD_PROTECTED[num / h];
						for (int i = 0; i < Arrimage.Length; i++)
						{
							Arrimage[i] = Image_HAIRMOD_PROTECTED.createImage(image, 0, i * h, w, h, 0);
						}
						image.texture = null;
						image = null;
						Resources.UnloadUnusedAssets();
						GC.Collect();
					}
				}
				FilePack_HAIRMOD_PROTECTED.reset();
			}
		}
		catch (Exception)
		{
		}
		isLoad = false;
	}

	public Image_HAIRMOD_PROTECTED getImage(int index)
	{
		if (imageObj == null)
		{
			return null;
		}
		return imageObj.getImage(index);
	}

	public void doSetDataMonsterNew(sbyte[] array)
	{
		try
		{
			arrayDataMonsterNew = array;
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)ALL_DATE_NEW_MONSTER.get(string.Empty + spalate);
			if (mVector_HAIRMOD_PROTECTED2 != null)
			{
				dataMonsterNew = mVector_HAIRMOD_PROTECTED2;
				return;
			}
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
			sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
			for (int i = 0; i < b; i++)
			{
				short num = dataInputStream_HAIRMOD_PROTECTED.readShort();
				sbyte[] array2 = new sbyte[num];
				for (int j = 0; j < array2.Length; j++)
				{
					array2[j] = dataInputStream_HAIRMOD_PROTECTED.readByte();
				}
				dataMonsterNew.addElement(new DataEffect_HAIRMOD_PROTECTED(array2));
			}
			ALL_DATE_NEW_MONSTER.put(string.Empty + spalate, dataMonsterNew);
			dataInputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception)
		{
		}
	}
}
