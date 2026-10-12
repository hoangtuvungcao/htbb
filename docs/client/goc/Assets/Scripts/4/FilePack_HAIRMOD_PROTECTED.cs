using System;
using System.IO;
using UnityEngine;

public class FilePack_HAIRMOD_PROTECTED
{
	public static FilePack_HAIRMOD_PROTECTED instance;

	public string[] fname;

	private int[] fpos;

	private int[] flen;

	private sbyte[] fullData;

	private int nFile;

	private int hSize;

	private string name;

	private static sbyte[] code = new sbyte[13]
	{
		78, 103, 117, 121, 101, 110, 86, 97, 110, 77,
		105, 110, 104
	};

	private int codeLen = code.Length;

	public static string[] charAvatar = new string[5]
	{
		Main_HAIRMOD_PROTECTED.res + "/c/leg/",
		Main_HAIRMOD_PROTECTED.res + "/c/body/",
		Main_HAIRMOD_PROTECTED.res + "/c/head/",
		Main_HAIRMOD_PROTECTED.res + "/c/hat/",
		Main_HAIRMOD_PROTECTED.res + "/c/coat/"
	};

	public static string cBody = Main_HAIRMOD_PROTECTED.res + "/body";

	public static string cHat = Main_HAIRMOD_PROTECTED.res + "/hat";

	public static string cLeg = Main_HAIRMOD_PROTECTED.res + "/leg";

	public static string cHead = Main_HAIRMOD_PROTECTED.res + "/head";

	public static string main = Main_HAIRMOD_PROTECTED.res + "/main";

	public static string font = Main_HAIRMOD_PROTECTED.res + "/font";

	public static string npc = Main_HAIRMOD_PROTECTED.res + "/npc";

	public static string eff = Main_HAIRMOD_PROTECTED.res + "/eff";

	public static string tree = "/tree/";

	public static string type = "/type.sh";

	public static string wps = Main_HAIRMOD_PROTECTED.res + "/wpsplash/";

	public static string arrow = Main_HAIRMOD_PROTECTED.res + "/arrow";

	public static string effPublic = Main_HAIRMOD_PROTECTED.res + "/skillpublic";

	public static string ground = Main_HAIRMOD_PROTECTED.res + "/g";

	public static string nation = Main_HAIRMOD_PROTECTED.res + "/nation";

	public static string box = Main_HAIRMOD_PROTECTED.res + "/box";

	private DataInputStream_HAIRMOD_PROTECTED file;

	public FilePack_HAIRMOD_PROTECTED()
	{
	}

	public FilePack_HAIRMOD_PROTECTED(string name, sbyte[] array)
	{
		int num = 0;
		int num2 = 0;
		this.name = name;
		hSize = 0;
		if (array == null)
		{
			open();
		}
		else
		{
			openbyArray(array);
		}
		if (file == null)
		{
			instance = null;
			return;
		}
		try
		{
			nFile = encode(file.readUnsignedByte());
			hSize++;
			fname = new string[nFile];
			fpos = new int[nFile];
			flen = new int[nFile];
			for (int i = 0; i < nFile; i++)
			{
				int num3 = encode(file.readByte());
				sbyte[] data = new sbyte[num3];
				file.read(ref data);
				encode(data);
				fname[i] = new string(mSystem_HAIRMOD_PROTECTED.ToCharArray(data));
				fpos[i] = num;
				flen[i] = encode(file.readUnsignedShort());
				num += flen[i];
				num2 += flen[i];
				hSize += num3 + 3;
			}
			fullData = new sbyte[num2];
			file.readFully(ref fullData);
			encode(fullData);
		}
		catch (IOException)
		{
		}
		close();
	}

	public FilePack_HAIRMOD_PROTECTED(string name)
	{
		int num = 0;
		int num2 = 0;
		this.name = name;
		hSize = 0;
		open();
		try
		{
			nFile = encode(file.readUnsignedByte());
			hSize++;
			fname = new string[nFile];
			fpos = new int[nFile];
			flen = new int[nFile];
			for (int i = 0; i < nFile; i++)
			{
				int num3 = encode(file.readByte());
				sbyte[] data = new sbyte[num3];
				file.read(ref data);
				encode(data);
				fname[i] = new string(mSystem_HAIRMOD_PROTECTED.ToCharArray(data));
				fpos[i] = num;
				flen[i] = encode(file.readUnsignedShort());
				num += flen[i];
				num2 += flen[i];
				hSize += num3 + 3;
			}
			fullData = new sbyte[num2];
			file.readFully(ref fullData);
			encode(fullData);
		}
		catch (Exception)
		{
		}
		close();
	}

	public static void reset()
	{
		if (instance != null)
		{
			instance.close();
		}
		instance = null;
	}

	public static Image_HAIRMOD_PROTECTED getImg(string path)
	{
		return instance.loadImage(path + ".png");
	}

	public static void init(string path)
	{
		instance = new FilePack_HAIRMOD_PROTECTED(path);
	}

	public static void initByArray(sbyte[] array)
	{
		instance = new FilePack_HAIRMOD_PROTECTED(string.Empty, array);
	}

	private int encode(int i)
	{
		return i;
	}

	private void encode(sbyte[] bytes)
	{
		int num = bytes.Length;
		for (int i = 0; i < num; i++)
		{
			bytes[i] ^= code[i % codeLen];
		}
	}

	private void open()
	{
		file = new DataInputStream_HAIRMOD_PROTECTED(name);
	}

	private void openbyArray(sbyte[] array)
	{
		file = new DataInputStream_HAIRMOD_PROTECTED(array);
	}

	public void close()
	{
		try
		{
			if (file != null)
			{
				file.close();
			}
		}
		catch (IOException)
		{
		}
	}

	public sbyte[] loadFile(string fileName)
	{
		try
		{
			for (int i = 0; i < nFile; i++)
			{
				if (fname[i].CompareTo(fileName) == 0)
				{
					sbyte[] array = new sbyte[flen[i]];
					Array.Copy(fullData, fpos[i], array, 0, flen[i]);
					return array;
				}
			}
		}
		catch (Exception)
		{
			Debug.Log("File '" + fileName + "' not found!");
		}
		return null;
	}

	public Image_HAIRMOD_PROTECTED loadImage(string fileName)
	{
		for (int i = 0; i < nFile; i++)
		{
			if (fname[i].CompareTo(fileName) == 0)
			{
				return Image_HAIRMOD_PROTECTED.createImage(fullData, fpos[i], flen[i]);
			}
		}
		return null;
	}

	public sbyte[] loadData(string name)
	{
		for (int i = 0; i < nFile; i++)
		{
			if (fname[i].CompareTo(name) == 0)
			{
				sbyte[] array = new sbyte[flen[i]];
				Array.Copy(fullData, fpos[i], array, 0, flen[i]);
				return array;
			}
		}
		return null;
	}

	public sbyte[] getBinaryFile(string name)
	{
		for (int i = 0; i < nFile; i++)
		{
			if (fname[i].CompareTo(name) == 0)
			{
				sbyte[] array = new sbyte[flen[i]];
				Array.Copy(fullData, fpos[i], array, 0, flen[i]);
				return array;
			}
		}
		Debug.Log("File '" + name + "' not found!");
		return null;
	}
}
