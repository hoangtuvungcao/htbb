using System;
using System.IO;
using System.Text;
using System.Threading;
using UnityEngine;

public class RMS
{
	// Ten kho luu da nam tren may nguoi choi tu ban phat hanh dau (dat theo game goc). GIU NGUYEN chuoi: doi ten =
	// mat mat khau da nho / o nhanh / may chu da chon / anh da tai cua nguoi choi cu. Cung ten voi ban jar (RMS.java).
	// Danh sach ngoai le: du_lieu/ten_cu_cho_phep.csv (server/test/KiemTenCu.java).

	/// <summary>nho tai khoan / mat khau (LoginScr)</summary>
	public const string KHO_DANG_NHAP = "nqshlogin"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>danh sach may chu da chon (ServerListScr)</summary>
	public const string KHO_DS_MAY_CHU = "ipnqsh"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>may chu dang nhap tu dong (ServerListScr)</summary>
	public const string KHO_MAY_CHU_TU_DONG = "ipnqshauto"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>danh sach ip (saveRMSStringIp)</summary>
	public const string KHO_IP = "nqshIP"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>thanh o nhanh</summary>
	public const string KHO_O_NHANH = "nqshQuickSlot"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>anh binh thuoc / ngua da dung (GameData.loadImgPotion; GameMidlet xoa de dung lai moi lan mo)</summary>
	public const string KHO_ANH_THUOC = "nqshImgPotionNew"; // ten kho cu tren may nguoi choi - giu de khong mat du lieu

	/// <summary>kho anh thuoc doi cu hon nua: chi de XOA (GameData.loadImgPotion)</summary>
	public const string KHO_ANH_THUOC_CU = "nqshImgPotion"; // ten kho cu can xoa tren may nguoi choi

	private const int INTERVAL = 5;

	private const int MAXTIME = 500;

	public static int status;

	public static sbyte[] data;

	public static string filename;

	public static void saveRMS(string filename, sbyte[] data)
	{
		if (Thread.CurrentThread.Name == Main.mainThreadName)
		{
			__saveRMS(filename, data);
		}
		else
		{
			_saveRMS(filename, data);
		}
	}

	public static sbyte[] loadRMS(string filename)
	{
		if (Thread.CurrentThread.Name == Main.mainThreadName)
		{
			return __loadRMS(filename);
		}
		return _loadRMS(filename);
	}

	private static void _saveRMS(string filename, sbyte[] data)
	{
		if (status != 0)
		{
			Debug.LogError("Cannot save RMS " + filename + " because current is saving " + RMS.filename);
			return;
		}
		RMS.filename = filename;
		RMS.data = data;
		status = 2;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG TO SAVE RMS " + filename);
		}
	}

	public static void saveRMSInt(string file, int x)
	{
		try
		{
			saveRMS(file, new sbyte[1] { (sbyte)x });
		}
		catch (Exception)
		{
		}
	}

	public static string loadRMSString(string filename)
	{
		sbyte[] array = loadRMS(filename);
		if (array == null)
		{
			return null;
		}
		return Encoding.UTF8.GetString(convertSbyteToByte(array));
	}

	public static byte[] convertSbyteToByte(sbyte[] var)
	{
		byte[] array = new byte[var.Length];
		for (int i = 0; i < var.Length; i++)
		{
			if (var[i] > 0)
			{
				array[i] = (byte)var[i];
			}
			else
			{
				array[i] = (byte)(var[i] + 256);
			}
		}
		return array;
	}

	private static sbyte[] _loadRMS(string filename)
	{
		if (status != 0)
		{
			Debug.LogError("Cannot load RMS " + filename + " because current is loading " + RMS.filename);
			return null;
		}
		RMS.filename = filename;
		data = null;
		status = 3;
		int i;
		for (i = 0; i < 500; i++)
		{
			Thread.Sleep(5);
			if (status == 0)
			{
				break;
			}
		}
		if (i == 500)
		{
			Debug.LogError("TOO LONG TO LOAD RMS " + filename);
		}
		return data;
	}

	public static void update()
	{
		if (status == 2)
		{
			status = 1;
			__saveRMS(filename, data);
			status = 0;
		}
		else if (status == 3)
		{
			status = 1;
			data = __loadRMS(filename);
			status = 0;
		}
	}

	public static void saveRMSStringIp(string[] ip, short[] port, string[] name)
	{
		DataOutputStream dataOutputStream = new DataOutputStream();
		try
		{
			dataOutputStream.writeByte((sbyte)ip.Length);
			for (int i = 0; i < ip.Length; i++)
			{
				dataOutputStream.writeUTF(ip[i]);
				dataOutputStream.writeShort(port[i]);
				dataOutputStream.writeUTF(name[i]);
			}
			saveRMS(KHO_IP, dataOutputStream.toByteArray());
			dataOutputStream.close();
		}
		catch (Exception)
		{
		}
	}

	public static void saveNumberSuport(string number)
	{
		DataOutputStream dataOutputStream = new DataOutputStream();
		try
		{
			dataOutputStream.writeUTF(number);
			sbyte[] array = dataOutputStream.toByteArray();
			saveRMS("numbersupport", array);
			dataOutputStream.close();
		}
		catch (Exception)
		{
		}
	}

	public static void saveQuickSlot()
	{
		DataOutputStream dataOutputStream = new DataOutputStream();
		try
		{
			dataOutputStream.writeUTF(Canvas.gameScr.mainChar.name);
			for (int i = 0; i < MainChar.quickSlot.Length; i++)
			{
				for (int j = 0; j < MainChar.quickSlot[i].Length; j++)
				{
					QuickSlot quickSlot = MainChar.quickSlot[i][j];
					dataOutputStream.writeByte((sbyte)quickSlot.quickslotType);
					if (quickSlot.quickslotType == QuickSlot.TYPE_POTION)
					{
						dataOutputStream.writeByte((sbyte)quickSlot.getPotionType());
					}
					else
					{
						dataOutputStream.writeByte(quickSlot.getSkillType());
					}
					dataOutputStream.writeBoolean(quickSlot.isBuff);
					dataOutputStream.writeByte((sbyte)GameScr.indexTabSlot);
				}
			}
			sbyte[] array = dataOutputStream.toByteArray();
			saveRMS(KHO_O_NHANH, array);
			dataOutputStream.close();
		}
		catch (Exception)
		{
		}
	}

	public static void loadQuickSlot()
	{
		sbyte[] array = loadRMS(KHO_O_NHANH);
		if (array == null)
		{
			return;
		}
		DataInputStream dataInputStream = new DataInputStream(array);
		try
		{
			string text = dataInputStream.readUTF();
			if (!text.Equals(Canvas.gameScr.mainChar.name))
			{
				return;
			}
			for (int i = 0; i < MainChar.quickSlot.Length; i++)
			{
				for (int j = 0; j < MainChar.quickSlot[i].Length; j++)
				{
					QuickSlot quickSlot = MainChar.quickSlot[i][j];
					quickSlot.quickslotType = dataInputStream.readUnsignedByte();
					sbyte b = dataInputStream.readByte();
					quickSlot.isBuff = dataInputStream.readBoolean();
					if (quickSlot.quickslotType == QuickSlot.TYPE_POTION)
					{
						quickSlot.setIsPotion(b);
					}
					else
					{
						quickSlot.setIsSkill(b, Canvas.gameScr.mainChar.clazz, quickSlot.isBuff);
					}
					GameScr.indexTabSlot = dataInputStream.readByte();
				}
			}
			dataInputStream.close();
		}
		catch (IOException)
		{
			Out.println("ERROR LOAD QUICKSLOT");
		}
	}

	private static void __saveRMS(string filename, sbyte[] data)
	{
		string value = ByteArrayToString(ArrayCast.cast(data));
		PlayerPrefs.SetString(filename, value);
		// ghi xuống đĩa ngay: tắt game bằng nút X / bị kill vẫn giữ tài khoản, danh sách server...
		PlayerPrefs.Save();
	}

	private static sbyte[] __loadRMS(string filename)
	{
		string hex = PlayerPrefs.GetString(filename);
		byte[] array;
		try
		{
			array = StringToByteArray(hex);
		}
		catch (Exception ex)
		{
			Debug.LogError("PARSE RMS STRING FAIL " + ex.StackTrace);
			return null;
		}
		if (array.Length == 0)
		{
			return null;
		}
		return ArrayCast.cast(array);
	}

	public static void clearRecord(string name)
	{
		PlayerPrefs.DeleteKey(name);
	}

	public static void deleteAll()
	{
		Debug.LogWarning("ALL RMS CLEAR");
		PlayerPrefs.DeleteAll();
	}

	public static string ByteArrayToString(byte[] ba)
	{
		string text = BitConverter.ToString(ba);
		return text.Replace("-", string.Empty);
	}

	public static byte[] StringToByteArray(string hex)
	{
		int length = hex.Length;
		byte[] array = new byte[length / 2];
		for (int i = 0; i < length; i += 2)
		{
			array[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
		}
		return array;
	}

	public static void saveRMSString(string filename, string info)
	{
		byte[] array = StringToByteArray(info);
		if (Thread.CurrentThread.Name == Main.mainThreadName)
		{
			__saveRMS(filename, data);
		}
		else
		{
			_saveRMS(filename, data);
		}
	}
}
