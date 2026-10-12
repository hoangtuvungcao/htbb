using System;
using System.Collections;
using UnityEngine;

public class Tilemap_HAIRMOD_PROTECTED
{
	public const int T_BLOCK = 2;

	public static int w;

	public static int h;

	public static int pxw;

	public static int pxh;

	public static mVector_HAIRMOD_PROTECTED trees = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED pointPop = new mVector_HAIRMOD_PROTECTED();

	public static short[] map;

	public static short[] mapTop;

	public static int[] type;

	public static int[] typeOfTile;

	public static Image_HAIRMOD_PROTECTED imgMap;

	public static short lv = 0;

	public static sbyte idXaphu = -1;

	public static int idLinhGac = 0;

	public static int[] mangMau = new int[232]
	{
		7447569, 7447569, 7447569, 7447569, 7447569, 5667859, 5598228, 10983769, 11045976, 11839592,
		11839592, 9335092, 9335092, 8156254, 9934470, 7102535, 11839592, 11839592, 11045976, 11045976,
		12815439, 7102535, 7102535, 9934470, 11045976, 13354172, 10990977, 9023045, 10983769, 11045976,
		10983769, 7447569, 9481003, 10724429, 11839592, 10724429, 9481003, 9481003, 9481003, 9335092,
		6788111, 6788111, 6788111, 6788111, 6788111, 7447569, 7102535, 4602408, 7102535, 8156254,
		8156254, 7102535, 7102535, 7102535, 8156254, 10125380, 10125380, 7102535, 8156254, 7102535,
		11051417, 11776680, 8156254, 9209206, 13354172, 9934470, 9209206, 11776680, 11776680, 13354172,
		11045976, 11045976, 12892301, 12892301, 11776680, 13354172, 11776680, 9934470, 2039834, 4602408,
		12892301, 13354172, 13354172, 11839592, 11839592, 11839592, 10724429, 10724429, 10724429, 8156254,
		9209206, 9209206, 9209206, 9209206, 13354172, 11051417, 11051417, 11051417, 13354172, 9934470,
		7102535, 4602408, 7102535, 9209206, 9209206, 9934470, 9209206, 9209206, 9209206, 10125380,
		6850651, 8156254, 9335092, 8156254, 6850651, 9335092, 6850651, 7102535, 7102535, 9934470,
		7102535, 4602408, 9335092, 9335092, 4602408, 2039834, 8156254, 9335092, 9335092, 10125380,
		10125380, 10125380, 10125380, 9335092, 11839592, 7978400, 10990977, 10990977, 11839592, 11839592,
		10990977, 7978400, 7978400, 8371088, 7447569, 7447569, 7447569, 7447569, 8371088, 12815439,
		11839592, 7379247, 4625939, 10125380, 10125380, 9335092, 9335092, 11045976, 9023045, 14215622,
		14215622, 10125380, 5667859, 5667859, 5667859, 5667859, 5667859, 5667859, 5667859, 5667859,
		5598228, 5667859, 5667859, 5598228, 5598228, 4542228, 5598228, 5598228, 5598228, 5598228,
		5598228, 6788111, 6205843, 6205843, 7379247, 7379247, 6850651, 6850651, 6205843, 7978400,
		11776680, 9023045, 6850651, 4542228, 4542228, 5667859, 6205843, 6205843, 6205843, 6205843,
		4625939, 9481003, 9335092, 6788111, 7379247, 5667859, 7447569, 6850651, 9209206, 8156254,
		12815439, 10125380, 9335092, 12815439, 7379247, 7447569, 7379247, 7379247, 7447569, 7447569,
		7379247, 7379247, 9209206, 9209206, 7102535, 8156254, 7102535, 7102535, 8156254, 8156254,
		9209206, 9209206
	};

	public static int[] mangMauThanh = new int[216]
	{
		7646756, 7710985, 8298508, 6591244, 7710985, 5469216, 5927994, 11313261, 12561257, 9005099,
		9005099, 10130554, 12483939, 12433586, 9678706, 8955217, 10982999, 10982999, 10982999, 7710985,
		8955217, 11969371, 11969371, 8955217, 9944360, 7646756, 8955217, 6591244, 6591244, 6591244,
		6591244, 7710985, 12760979, 12299640, 393730, 4471597, 12695960, 13485254, 13091513, 11969371,
		11969371, 12561257, 11969371, 8955217, 10982999, 7567964, 8420208, 9208948, 9735300, 9472127,
		13091513, 10787996, 10722964, 10722964, 13485254, 10722964, 6643524, 3746841, 5204027, 9210493,
		9208948, 10130554, 9210493, 9210493, 9273959, 10845517, 8422472, 9005897, 8081700, 9393985,
		9215100, 2957328, 9141309, 8298508, 7710985, 7710985, 7646756, 10329734, 10335148, 9736847,
		4808279, 7171437, 4808279, 4808279, 4808279, 4808279, 6776159, 4808279, 4808279, 6776159,
		6257522, 4808279, 4808279, 9213060, 15525606, 15525606, 7171437, 8684684, 4999999, 4808279,
		4808279, 8355711, 9735300, 8141601, 7607052, 7419423, 9472127, 14736597, 9472127, 9735300,
		12433586, 13156544, 13485254, 11908533, 8684684, 4808279, 3425086, 4808279, 7171437, 9535598,
		9535598, 4808279, 5263440, 11102029, 9458228, 11301724, 9393985, 9535598, 9005897, 12169623,
		12760979, 12760979, 12169623, 12169623, 12169623, 12169623, 12169623, 10130554, 11511169, 10722179,
		9866080, 12433586, 6591244, 6074253, 6074253, 7837996, 7837996, 6388807, 5279594, 6074253,
		7060133, 10335148, 6144586, 5927994, 6074253, 6928533, 6074253, 6928533, 5084928, 9944360,
		9141309, 6591244, 7837996, 7832322, 7646756, 7439963, 9210493, 7567964, 13013578, 10126662,
		12020275, 12285530, 10722964, 10722964, 11577496, 9736847, 11577496, 9736847, 13091513, 13947078,
		15525606, 15525606, 12483939, 9393985, 11301724, 12483939, 10722964, 14736597, 10787996, 9735300,
		14012362, 8419440, 11301724, 9535598, 11301724, 9535598, 9005897, 11301724, 11301724, 9393985,
		11102029, 9472127, 10130554, 6301476, 7607052, 9393985, 9210493, 11775140, 11775140, 11908533,
		10787996, 9736847, 9735300, 8419440, 8419440, 8355711
	};

	public static int[] mangMauHang = new int[116]
	{
		5655105, 4077107, 4077107, 3879469, 5655105, 4997949, 4997949, 4077107, 1708304, 3288105,
		3288105, 3024422, 3288105, 3288105, 4077107, 4077107, 4537142, 4997949, 3879469, 3879469,
		3879469, 4077107, 4537142, 4537142, 4077107, 4077107, 4077107, 4077107, 4077107, 3879469,
		4537142, 10983781, 12430706, 12168568, 10983781, 9532223, 3287583, 2169361, 6836272, 10718035,
		11771492, 11771492, 11771492, 11771492, 11771492, 8817476, 8817476, 11838057, 11771492, 10519888,
		11047001, 11047001, 10519888, 11838057, 11771492, 10718035, 11047001, 10718035, 10519888, 11771492,
		11771492, 11771492, 11771492, 9532223, 9532223, 9532223, 10848073, 10718035, 10848073, 10848073,
		10848073, 10848073, 11566403, 7964513, 7630924, 9526323, 7630924, 7964513, 9532223, 7964513,
		6836272, 9526323, 11838057, 12430706, 12104060, 10925703, 12104060, 10925703, 8043677, 8043677,
		8043677, 8043677, 8701089, 8043677, 4537142, 3288105, 5655105, 5655105, 3288105, 6836272,
		5655105, 4077107, 3879469, 4537142, 3879469, 4537142, 3879469, 4537142, 4077107, 5655105,
		6510669, 4997949, 4537142, 6510669, 4997949, 3879469
	};

	public static Image_HAIRMOD_PROTECTED[][] img = new Image_HAIRMOD_PROTECTED[3][];

	public static sbyte typeTile = 0;

	public static MyHashTable_HAIRMOD_PROTECTED tileTop = new MyHashTable_HAIRMOD_PROTECTED();

	public static short size = 16;

	public static mVector_HAIRMOD_PROTECTED mapchangeLocations = new mVector_HAIRMOD_PROTECTED();

	public static bool isOfflineMap;

	public static string mapName = string.Empty;

	public static int sip = 4;

	public static void reset()
	{
		try
		{
			Res_HAIRMOD_PROTECTED.resetTrangBi();
			Res_HAIRMOD_PROTECTED.removeImgEffect();
			Res_HAIRMOD_PROTECTED.removeWeaPone();
			Res_HAIRMOD_PROTECTED.removeMonsterTemplet();
			Res_HAIRMOD_PROTECTED.removePartInfo();
			mapchangeLocations.removeAllElements();
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.removeAllElements();
			Canvas_HAIRMOD_PROTECTED.gameScr.npc_Limit.removeAllElements();
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar);
			if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myPet != null)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myPet);
			}
			IsAnimal_HAIRMOD_PROTECTED.img = new Image_HAIRMOD_PROTECTED[10];
			if (Canvas_HAIRMOD_PROTECTED.currentScreen != null)
			{
				Canvas_HAIRMOD_PROTECTED.currentScreen.btnFocus = 0;
			}
			idLinhGac = 0;
			Resources.UnloadUnusedAssets();
			GC.Collect();
		}
		catch (Exception)
		{
		}
	}

	public static void loadMap(int level, sbyte[] byteMap)
	{
		try
		{
			Res_HAIRMOD_PROTECTED.loadTileImage(level);
			map = null;
			type = null;
			for (int i = 0; i < trees.size(); i++)
			{
				Tree_HAIRMOD_PROTECTED tree_HAIRMOD_PROTECTED = (Tree_HAIRMOD_PROTECTED)trees.elementAt(i);
				if (tree_HAIRMOD_PROTECTED != null)
				{
					tree_HAIRMOD_PROTECTED = null;
				}
			}
			trees.removeAllElements();
			lv = (short)level;
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = null;
			dataInputStream_HAIRMOD_PROTECTED = ((byteMap != null) ? new DataInputStream_HAIRMOD_PROTECTED(byteMap) : DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(Main_HAIRMOD_PROTECTED.res + "/" + level));
			w = dataInputStream_HAIRMOD_PROTECTED.read();
			h = dataInputStream_HAIRMOD_PROTECTED.read();
			pxw = w << 4;
			pxh = h << 4;
			map = new short[w * h];
			tileTop.clear();
			type = new int[w * h];
			for (int j = 0; j < w * h; j++)
			{
				map[j] = (short)dataInputStream_HAIRMOD_PROTECTED.read();
				if (map[j] == 254)
				{
					map[j] = 255;
				}
				if (map[j] != 255 && map[j] != -1)
				{
					type[j] = typeOfTile[map[j]];
				}
			}
			while (true)
			{
				int num = dataInputStream_HAIRMOD_PROTECTED.read();
				if (num == 255)
				{
					break;
				}
				int num2 = dataInputStream_HAIRMOD_PROTECTED.read();
				int num3 = dataInputStream_HAIRMOD_PROTECTED.read();
				int num4 = dataInputStream_HAIRMOD_PROTECTED.read();
				for (int k = num2; k < num2 + num4; k++)
				{
					for (int l = num; l < num + num3; l++)
					{
						short num5 = (short)dataInputStream_HAIRMOD_PROTECTED.read();
						if (num5 != 255)
						{
							TileTop_HAIRMOD_PROTECTED tileTop_HAIRMOD_PROTECTED = new TileTop_HAIRMOD_PROTECTED();
							tileTop_HAIRMOD_PROTECTED.y = (short)k;
							tileTop_HAIRMOD_PROTECTED.x = (short)l;
							tileTop_HAIRMOD_PROTECTED.index = num5;
							tileTop.put(string.Empty + (k * w + l), tileTop_HAIRMOD_PROTECTED);
						}
					}
				}
			}
			while (true)
			{
				int num6 = dataInputStream_HAIRMOD_PROTECTED.read();
				if (num6 == 255)
				{
					break;
				}
				int num7 = dataInputStream_HAIRMOD_PROTECTED.read();
				short num8 = (short)dataInputStream_HAIRMOD_PROTECTED.read();
				if (num8 != 255)
				{
					TileTop_HAIRMOD_PROTECTED tileTop_HAIRMOD_PROTECTED2 = new TileTop_HAIRMOD_PROTECTED();
					tileTop_HAIRMOD_PROTECTED2.y = (short)num7;
					tileTop_HAIRMOD_PROTECTED2.x = (short)num6;
					tileTop_HAIRMOD_PROTECTED2.index = num8;
					tileTop.put(string.Empty + (num7 * w + num6), tileTop_HAIRMOD_PROTECTED2);
				}
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			while (true)
			{
				int num9 = dataInputStream_HAIRMOD_PROTECTED.read();
				if (num9 == 254)
				{
					num9 = 255;
				}
				if (num9 == 255)
				{
					break;
				}
				int y = dataInputStream_HAIRMOD_PROTECTED.read();
				int num10 = dataInputStream_HAIRMOD_PROTECTED.read();
				mVector_HAIRMOD_PROTECTED2.addElement(new Tree_HAIRMOD_PROTECTED(num9, y, num10));
			}
			trees.removeAllElements();
			pointPop.removeAllElements();
			for (int m = 0; m < mVector_HAIRMOD_PROTECTED2.size(); m++)
			{
				trees.addElement(mVector_HAIRMOD_PROTECTED2.elementAt(m));
			}
			isOfflineMap = dataInputStream_HAIRMOD_PROTECTED.read() == 1;
			int num11 = dataInputStream_HAIRMOD_PROTECTED.read();
			int num12 = -1;
			int num13 = -1;
			int num14 = 0;
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
			int num15 = -1;
			int num16 = -1;
			int num17 = -1;
			int num18 = 0;
			int num19 = 0;
			for (int n = 0; n < num11; n++)
			{
				int num20 = dataInputStream_HAIRMOD_PROTECTED.read();
				int num21 = dataInputStream_HAIRMOD_PROTECTED.read();
				if (num15 == -1)
				{
					num16 = num20;
					num17 = num21;
					num15 = 0;
					mVector_HAIRMOD_PROTECTED3.addElement(num20 + string.Empty);
					mVector_HAIRMOD_PROTECTED4.addElement(num21 + string.Empty);
					num19 = num20;
					num18 = num21;
				}
				else if (num20 == num16 && num21 != num17 && GameScr_HAIRMOD_PROTECTED.abs(num21 - num18) <= 1)
				{
					mVector_HAIRMOD_PROTECTED3.addElement(num20 + string.Empty);
					mVector_HAIRMOD_PROTECTED4.addElement(num21 + string.Empty);
					num19 = num20;
					num18 = num21;
				}
				else if (num21 == num17 && num20 != num16 && num20 - num19 <= 1)
				{
					mVector_HAIRMOD_PROTECTED3.addElement(num20 + string.Empty);
					mVector_HAIRMOD_PROTECTED4.addElement(num21 + string.Empty);
					num19 = num20;
					num18 = num21;
				}
				else if ((num20 != num16 && num21 != num17) || GameScr_HAIRMOD_PROTECTED.abs(num21 - num18) > 1 || num20 - num19 > 1)
				{
					int min = getMin(mVector_HAIRMOD_PROTECTED3);
					int min2 = getMin(mVector_HAIRMOD_PROTECTED4);
					if (mVector_HAIRMOD_PROTECTED3.size() > 1)
					{
						min = 16 * (min + getMax(mVector_HAIRMOD_PROTECTED3));
						min2 = 16 * (min2 + getMax(mVector_HAIRMOD_PROTECTED4));
						min /= 2;
						min2 /= 2;
					}
					else
					{
						min *= 16;
						min2 *= 16;
					}
					int num22 = 0;
					if (w >= 20 && h >= 20)
					{
						if (min / 16 < 3)
						{
							min += 32;
							min2 -= 16;
						}
						else if (min / 16 >= w - 3)
						{
							min -= 32;
							min2 -= 16;
						}
						else
						{
							num22 = 8;
						}
						if (min2 / 16 < 4)
						{
							min2 += 32;
							num22 = 4;
						}
						else if (min2 / 16 > h - 4)
						{
							min2 -= 32;
							num22 = -5;
						}
						else
						{
							min2 += 24;
						}
					}
					PopupName_HAIRMOD_PROTECTED o = new PopupName_HAIRMOD_PROTECTED("vào", min + num22, min2);
					pointPop.addElement(o);
					mVector_HAIRMOD_PROTECTED3.removeAllElements();
					mVector_HAIRMOD_PROTECTED4.removeAllElements();
					num16 = num20;
					num17 = num21;
					num19 = num20;
					num18 = num21;
					num15 = 0;
					mVector_HAIRMOD_PROTECTED3.addElement(num20 + string.Empty);
					mVector_HAIRMOD_PROTECTED4.addElement(num21 + string.Empty);
				}
				type[num21 * w + num20] = 2000000000 + n;
				CMLocation_HAIRMOD_PROTECTED cMLocation_HAIRMOD_PROTECTED = null;
				int[] array = new int[2]
				{
					dataInputStream_HAIRMOD_PROTECTED.read(),
					dataInputStream_HAIRMOD_PROTECTED.read()
				};
				short num23 = 0;
				for (int num24 = 1; num24 >= 0; num24--)
				{
					num23 <<= 8;
					num23 |= (short)(0xFF & array[num24]);
				}
				int toX = dataInputStream_HAIRMOD_PROTECTED.read();
				int toY = dataInputStream_HAIRMOD_PROTECTED.read();
				cMLocation_HAIRMOD_PROTECTED = new CMLocation_HAIRMOD_PROTECTED(num23, toX, toY);
				mapchangeLocations.addElement(cMLocation_HAIRMOD_PROTECTED);
			}
			int min3 = getMin(mVector_HAIRMOD_PROTECTED3);
			int min4 = getMin(mVector_HAIRMOD_PROTECTED4);
			if (mVector_HAIRMOD_PROTECTED3.size() > 1)
			{
				min3 = 16 * (min3 + getMax(mVector_HAIRMOD_PROTECTED3));
				min4 = 16 * (min4 + getMax(mVector_HAIRMOD_PROTECTED4));
				min3 /= 2;
				min4 /= 2;
			}
			else
			{
				min3 *= 16;
				min4 *= 16;
			}
			int num25 = 0;
			if (w >= 20 && h >= 20)
			{
				if (min3 / 16 < 3)
				{
					min3 += 32;
					min4 -= 16;
				}
				else if (min3 / 16 >= w - 3)
				{
					min3 -= 32;
					min4 -= 16;
				}
				else
				{
					num25 = 8;
				}
				if (min4 / 16 < 3)
				{
					min4 += 32;
					num25 = 4;
				}
				else if (min4 / 16 > h - 3)
				{
					min4 -= 32;
					num25 = -5;
				}
				else
				{
					min4 += 24;
				}
			}
			PopupName_HAIRMOD_PROTECTED o2 = new PopupName_HAIRMOD_PROTECTED("vào", min3 + num25, min4);
			pointPop.addElement(o2);
			int num26 = dataInputStream_HAIRMOD_PROTECTED.read();
			FilePack_HAIRMOD_PROTECTED filePack_HAIRMOD_PROTECTED = new FilePack_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.npc, null);
			if (Canvas_HAIRMOD_PROTECTED.gameScr != null)
			{
				for (int num27 = 0; num27 < num26; num27++)
				{
					NPC_HAIRMOD_PROTECTED nPC_HAIRMOD_PROTECTED = new NPC_HAIRMOD_PROTECTED(dataInputStream_HAIRMOD_PROTECTED.read(), dataInputStream_HAIRMOD_PROTECTED.read(), dataInputStream_HAIRMOD_PROTECTED.read(), filePack_HAIRMOD_PROTECTED);
					if (GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan != null && GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.index == nPC_HAIRMOD_PROTECTED.type)
					{
						GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.x = nPC_HAIRMOD_PROTECTED.x;
						GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.y = nPC_HAIRMOD_PROTECTED.y;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(nPC_HAIRMOD_PROTECTED);
				}
			}
			filePack_HAIRMOD_PROTECTED.close();
			filePack_HAIRMOD_PROTECTED = null;
			FilePack_HAIRMOD_PROTECTED.reset();
			imgMap = null;
			imgMap = Image_HAIRMOD_PROTECTED.createImage(w, h);
			for (int num28 = 0; num28 < w; num28++)
			{
				for (int num29 = 0; num29 < h; num29++)
				{
					MyGraphics_HAIRMOD_PROTECTED myGraphics_HAIRMOD_PROTECTED = new MyGraphics_HAIRMOD_PROTECTED();
					if (tileTop.get(string.Empty + (num29 * w + num28)) != null)
					{
						if (Res_HAIRMOD_PROTECTED.mauTile == 0)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMauThanh[((TileTop_HAIRMOD_PROTECTED)tileTop.get(string.Empty + (num29 * w + num28))).index]));
						}
						else if (Res_HAIRMOD_PROTECTED.mauTile == 1)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMau[((TileTop_HAIRMOD_PROTECTED)tileTop.get(string.Empty + (num29 * w + num28))).index]));
						}
						else if (Res_HAIRMOD_PROTECTED.mauTile == 2)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMauHang[((TileTop_HAIRMOD_PROTECTED)tileTop.get(string.Empty + (num29 * w + num28))).index]));
						}
						continue;
					}
					int num30 = map[num29 * w + num28];
					if (num30 != 255)
					{
						if (Res_HAIRMOD_PROTECTED.mauTile == 0)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMauThanh[num30]));
						}
						else if (Res_HAIRMOD_PROTECTED.mauTile == 1)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMau[num30]));
						}
						else if (Res_HAIRMOD_PROTECTED.mauTile == 2)
						{
							imgMap.texture.SetPixel(num28, imgMap.getHeight() - 1 - num29, myGraphics_HAIRMOD_PROTECTED.setColorMiniMap(mangMauHang[num30]));
						}
					}
				}
			}
			imgMap.texture.Apply();
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println("LOAD TILE ERR " + level);
			Canvas_HAIRMOD_PROTECTED.instance.init();
			Session_ME_HAIRMOD_PROTECTED.gI().close();
			loadMap(0, null);
			ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
		}
		if (Canvas_HAIRMOD_PROTECTED.gameScr != null)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.setPosMiniMap();
		}
		int wMiniMap = GameScr_HAIRMOD_PROTECTED.wMiniMap;
		if (wMiniMap > w)
		{
			wMiniMap = w;
		}
		int hMiniMap = GameScr_HAIRMOD_PROTECTED.hMiniMap;
		if (hMiniMap > h)
		{
			hMiniMap = h;
		}
		GameScr_HAIRMOD_PROTECTED.mapFind = new sbyte[wMiniMap][];
		for (int num31 = 0; num31 < wMiniMap; num31++)
		{
			GameScr_HAIRMOD_PROTECTED.mapFind[num31] = new sbyte[hMiniMap];
		}
	}

	public static int getMax(mVector_HAIRMOD_PROTECTED cord)
	{
		int num = -10;
		for (int i = 0; i < cord.size(); i++)
		{
			int num2 = int.Parse((string)cord.elementAt(i));
			if (num < num2)
			{
				num = num2;
			}
		}
		return num;
	}

	public static int getMin(mVector_HAIRMOD_PROTECTED cord)
	{
		int num = 10000;
		for (int i = 0; i < cord.size(); i++)
		{
			int num2 = int.Parse((string)cord.elementAt(i));
			if (num > num2)
			{
				num = num2;
			}
		}
		return num;
	}

	public static void setNameMap(int idmap)
	{
		switch (idmap)
		{
		case 0:
			mapName = "Sơn Nam";
			idXaphu = 0;
			break;
		case 1:
			mapName = "Dao Châu";
			break;
		case 2:
			mapName = "Tiên Du";
			break;
		case 3:
			mapName = "Phù Liệt";
			idXaphu = 1;
			break;
		case 4:
			mapName = "Kỳ Bố";
			idXaphu = 2;
			break;
		case 5:
			mapName = "Hàm Tử";
			idXaphu = 3;
			break;
		case 6:
			mapName = "Thạch Giang";
			idXaphu = 4;
			break;
		case 7:
			mapName = "Đông Sơn";
			idXaphu = 5;
			break;
		case 8:
			mapName = "Tử Quan";
			idXaphu = 6;
			break;
		case 9:
			mapName = "Trường Giang";
			idXaphu = 7;
			break;
		case 10:
			mapName = "Lộc Trĩ";
			idXaphu = 8;
			break;
		case 11:
			mapName = "Sơn Lâm";
			break;
		case 100:
			mapName = "Nhà bán HP";
			break;
		case 101:
			mapName = "Nhà bán MP";
			break;
		case 102:
			mapName = "Nhà thợ rèn";
			break;
		case 110:
			mapName = "Hang động";
			break;
		case 111:
			mapName = "Hang mãng xà";
			break;
		case 112:
			mapName = "Hang thằn lằn";
			break;
		case 202:
			mapName = "Đấu trường";
			idXaphu = 9;
			break;
		case 70:
		case 80:
			idXaphu = 0;
			break;
		default:
			mapName = string.Empty;
			break;
		}
	}

	public static void paintTile(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (map == null)
		{
			return;
		}
		for (int i = Canvas_HAIRMOD_PROTECTED.gameScr.gssx; i < Canvas_HAIRMOD_PROTECTED.gameScr.gssxe; i++)
		{
			for (int j = Canvas_HAIRMOD_PROTECTED.gameScr.gssy; j < Canvas_HAIRMOD_PROTECTED.gameScr.gssye; j++)
			{
				int num = j * w + i;
				int num2 = 255;
				if (num < map.Length)
				{
					num2 = map[num];
					if (num2 != 255)
					{
						g.drawImagaByDrawTexture(img[typeTile][num2], i << sip, j << sip);
					}
				}
			}
		}
	}

	public static void paintTileTop(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (tileTop.size() <= 0)
		{
			return;
		}
		IDictionaryEnumerator enumerator = tileTop.GetEnumerator();
		while (enumerator.MoveNext())
		{
			string k = enumerator.Key.ToString();
			TileTop_HAIRMOD_PROTECTED tileTop_HAIRMOD_PROTECTED = (TileTop_HAIRMOD_PROTECTED)tileTop.get(k);
			if (tileTop_HAIRMOD_PROTECTED.y >= Canvas_HAIRMOD_PROTECTED.gameScr.gssy && tileTop_HAIRMOD_PROTECTED.y <= Canvas_HAIRMOD_PROTECTED.gameScr.gssye && tileTop_HAIRMOD_PROTECTED.x >= Canvas_HAIRMOD_PROTECTED.gameScr.gssx - 1 && tileTop_HAIRMOD_PROTECTED.x <= Canvas_HAIRMOD_PROTECTED.gameScr.gssxe + 1)
			{
				tileTop_HAIRMOD_PROTECTED.paint(g, img[typeTile][tileTop_HAIRMOD_PROTECTED.index]);
			}
		}
	}

	public static int tileTypeAt(int xx, int yy)
	{
		return type[yy * w + xx];
	}

	public static int tileTypeAtPixel(int px, int py)
	{
		if ((py >> sip) * w + (px >> sip) < type.Length)
		{
			return type[(py >> sip) * w + (px >> sip)];
		}
		return -1;
	}

	public static bool tileTypeAtPixel(int px, int py, int t)
	{
		int num = (py >> sip) * w + (px >> sip);
		if (num < 0 || num >= type.Length)
		{
			return true;
		}
		return (type[num] & t) == t;
	}

	public static CMLocation_HAIRMOD_PROTECTED toOtherMapAt(int px, int py)
	{
		if ((py >> sip) * w + (px >> sip) >= type.Length || (py >> sip) * w + (px >> sip) < 0)
		{
			return null;
		}
		return (type[(py >> sip) * w + (px >> sip)] < 2000000000) ? null : ((CMLocation_HAIRMOD_PROTECTED)mapchangeLocations.elementAt(type[(py >> sip) * w + (px >> sip)] - 2000000000));
	}

	public static void killTileTypeAt(int px, int py, int t)
	{
		type[(py >> sip) * w + (px >> sip)] &= ~t;
	}

	public static int tileYofPixel(int py)
	{
		return py >> sip << sip;
	}

	public static int tileXofPixel(int px)
	{
		return px >> sip << sip;
	}
}
