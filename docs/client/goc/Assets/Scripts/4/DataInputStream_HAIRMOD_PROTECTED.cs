using System.Threading;
using UnityEngine;

public class DataInputStream_HAIRMOD_PROTECTED
{
	private const int INTERVAL = 5;

	private const int MAXTIME = 500;

	private myReader_HAIRMOD_PROTECTED r;

	public static DataInputStream_HAIRMOD_PROTECTED istemp;

	private static int status;

	private static string filenametemp;

	public DataInputStream_HAIRMOD_PROTECTED(string filename)
	{
		TextAsset textAsset = (TextAsset)Resources.Load(filename, typeof(TextAsset));
		r = new myReader_HAIRMOD_PROTECTED(ArrayCast_HAIRMOD_PROTECTED.cast(textAsset.bytes));
	}

	public DataInputStream_HAIRMOD_PROTECTED(sbyte[] data)
	{
		r = new myReader_HAIRMOD_PROTECTED(data);
	}

	public static void update()
	{
		if (status == 2)
		{
			status = 1;
			istemp = __getResourceAsStream(filenametemp);
			status = 0;
		}
	}

	public static DataInputStream_HAIRMOD_PROTECTED getResourceAsStream(string filename)
	{
		return __getResourceAsStream(filename);
	}

	private static DataInputStream_HAIRMOD_PROTECTED _getResourceAsStream(string filename)
	{
		if (status != 0)
		{
			for (int i = 0; i < 500; i++)
			{
				Thread.Sleep(5);
				if (status == 0)
				{
					break;
				}
			}
			if (status != 0)
			{
				Debug.LogError("CANNOT GET INPUTSTREAM " + filename + " WHEN GETTING " + filenametemp);
				return null;
			}
		}
		istemp = null;
		filenametemp = filename;
		status = 2;
		int j;
		for (j = 0; j < 500; j++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (j == 500)
		{
			Debug.LogError("TOO LONG FOR CREATE INPUTSTREAM " + filename);
			status = 0;
			return null;
		}
		return istemp;
	}

	private static DataInputStream_HAIRMOD_PROTECTED __getResourceAsStream(string filename)
	{
		return new DataInputStream_HAIRMOD_PROTECTED(filename);
	}

	public short readShort()
	{
		return r.readShort();
	}

	public int readInt()
	{
		return r.readInt();
	}

	public void read(ref sbyte[] data)
	{
		r.read(ref data);
	}

	public void close()
	{
		r.Close();
	}

	public string readUTF()
	{
		return r.readUTF();
	}

	public sbyte readByte()
	{
		return r.readByte();
	}

	public int read()
	{
		return r.readUnsignedByte();
	}

	public bool readBoolean()
	{
		return r.readBoolean();
	}

	public int readUnsignedByte()
	{
		return (byte)r.readByte();
	}

	public int readUnsignedShort()
	{
		return r.readUnsignedShort();
	}

	public void readFully(ref sbyte[] data)
	{
		r.read(ref data);
	}

	public int available()
	{
		return r.available();
	}
}
