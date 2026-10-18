using System;
using UnityEngine;

public class MonsterTemplate
{
	public static MyHashTable ALL_DATE_NEW_MONSTER = new MyHashTable();

	public int type = -1;

	public Image image;

	public Image[] Arrimage;

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

	public ImageObj imageObj;

	public mVector dataMonsterNew = new mVector();

	public long timePaint;

	private sbyte[] arrayDataMonsterNew;

	/// <summary>
	/// Pokemon cua tung template quai, index = idTemplate (0..255, lenh 26 dem bang 1 byte).
	/// Nhan qua lenh -91 POKE_QUAI ngay sau lenh 26 - luc do Res.monsterTemplates[id] con
	/// null (tao luoi trong Monster.setType), nen luu o mang tinh rieng, khong luu tren doi tuong.
	/// pokeDex = 0: ve kieu cu (anh -47). pokeTiLe: phan tram (100 = 1x).
	/// </summary>
	public static short[] pokeDex = new short[256];

	public static bool[] pokeShiny = new bool[256];

	public static short[] pokeTiLe = new short[256];

	public static void datPoke(int idTemplate, int dex, bool shiny, int tiLe)
	{
		if (idTemplate < 0 || idTemplate >= pokeDex.Length)
		{
			return;
		}
		pokeDex[idTemplate] = (short)dex;
		pokeShiny[idTemplate] = shiny;
		pokeTiLe[idTemplate] = (short)((tiLe <= 0) ? 100 : tiLe);
	}

	public static void xoaPoke()
	{
		for (int i = 0; i < pokeDex.Length; i++)
		{
			pokeDex[i] = 0;
			pokeShiny[i] = false;
			pokeTiLe[i] = 100;
		}
	}

	public static int layPokeDex(int idTemplate)
	{
		if (idTemplate < 0 || idTemplate >= pokeDex.Length)
		{
			return 0;
		}
		return pokeDex[idTemplate];
	}

	/// <summary>Template nay ve bang PokeBody va co du sprite trong client.</summary>
	public static bool laPokemon(int idTemplate)
	{
		int dex = layPokeDex(idTemplate);
		return dex > 0 && PokeBody.coSprite(dex, pokeShiny[idTemplate]);
	}

	public MonsterTemplate()
	{
	}

	public MonsterTemplate(int type, string name, string image, sbyte moveType, sbyte speed, sbyte height, sbyte w, sbyte h, sbyte xCenter, sbyte yCenter, sbyte xadd, sbyte yadd)
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

	public void paint(MyGraphics g, int x, int y, int flip, int anchor, int f)
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
		timePaint = mSystem.currentTimeMillis();
	}

	public void resetImage()
	{
		if (mSystem.currentTimeMillis() - timePaint > 60000)
		{
			image = null;
			imageObj = null;
			dataMonsterNew.removeAllElements();
			ALL_DATE_NEW_MONSTER.remove(string.Empty + spalate);
		}
	}

	public mVector getDataMonster()
	{
		try
		{
			if (type > -1)
			{
				mVector mVector2 = (mVector)ALL_DATE_NEW_MONSTER.get(string.Empty + spalate);
				if (mVector2 != null)
				{
					dataMonsterNew = mVector2;
				}
				else
				{
					DataInputStream dataInputStream = new DataInputStream(Main.res + "/boss/" + spalate);
					sbyte b = dataInputStream.readByte();
					for (int i = 0; i < b; i++)
					{
						short num = dataInputStream.readShort();
						sbyte[] array = new sbyte[num];
						for (int j = 0; j < array.Length; j++)
						{
							array[j] = dataInputStream.readByte();
						}
						dataMonsterNew.addElement(new DataEffect(array));
					}
					ALL_DATE_NEW_MONSTER.put(string.Empty + spalate, dataMonsterNew);
				}
			}
			timePaint = mSystem.currentTimeMillis();
		}
		catch (Exception)
		{
		}
		return dataMonsterNew;
	}

	public void loadImage()
	{
		// Template da la Pokemon (lenh -91) hoac do vat PMD (VatThePokemon) thi khong xin anh quai cu (-47) nua.
		if (laPokemon(type) || VatThePokemon.laVatThe(type))
		{
			return;
		}
		if (!isLoad && type > -1)
		{
			GameService.gI().doRequestImageMonster(type, palate, spalate);
			isLoad = true;
		}
	}

	public void onImage(bool isSave, sbyte[] arr)
	{
		try
		{
			if ((palate >= 70 && palate <= 88) || palate == 90 || palate == 91 || palate == 92 || isNewMonster == 1)
			{
				imageObj = new ImageObj(arr);
			}
			else
			{
				FilePack.initByArray(arr);
				if (palate == 39 || palate == 40)
				{
					imageObj = new ImageObj(palate, spalate);
				}
				else
				{
					sbyte[] header = FilePack.instance.loadFile(spalate + "_h");
					sbyte[] data = FilePack.instance.loadFile("data");
					image = Res.createImgByHeader(header, data);
					int num = image.getHeight();
					isMaxHight = false;
					if (num > 1024)
					{
						isMaxHight = true;
						Arrimage = new Image[num / h];
						for (int i = 0; i < Arrimage.Length; i++)
						{
							Arrimage[i] = Image.createImage(image, 0, i * h, w, h, 0);
						}
						image.texture = null;
						image = null;
						Resources.UnloadUnusedAssets();
						GC.Collect();
					}
				}
				FilePack.reset();
			}
		}
		catch (Exception)
		{
		}
		isLoad = false;
	}

	public Image getImage(int index)
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
			mVector mVector2 = (mVector)ALL_DATE_NEW_MONSTER.get(string.Empty + spalate);
			if (mVector2 != null)
			{
				dataMonsterNew = mVector2;
				return;
			}
			DataInputStream dataInputStream = new DataInputStream(array);
			sbyte b = dataInputStream.readByte();
			for (int i = 0; i < b; i++)
			{
				short num = dataInputStream.readShort();
				sbyte[] array2 = new sbyte[num];
				for (int j = 0; j < array2.Length; j++)
				{
					array2[j] = dataInputStream.readByte();
				}
				dataMonsterNew.addElement(new DataEffect(array2));
			}
			ALL_DATE_NEW_MONSTER.put(string.Empty + spalate, dataMonsterNew);
			dataInputStream.close();
		}
		catch (Exception)
		{
		}
	}
}
