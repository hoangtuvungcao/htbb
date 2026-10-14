using UnityEngine;

// Plugin native gốc của TeaMobi (SMS, mua hàng...) không có trong bản giải nén: thay các hàm
// [DllImport("__Internal")] bằng hàm rỗng, nếu không Android / iOS (IL2CPP) báo "undefined symbol" khi link.
public class iOSPlugins
{
	public static string devide;

	public static string Myname;

	private static void _SMSsend(string tophone, string withtext, int n)
	{
	}

	private static int _unpause()
	{
		return 0;
	}

	private static int _checkRotation()
	{
		return 0;
	}

	private static int _back()
	{
		return 0;
	}

	private static int _Send()
	{
		return 0;
	}

	private static void _purchaseItem(string itemID, string userName, string gameID)
	{
	}

	public static int Check()
	{
		devide = iPhoneSettings.generation.ToString();
		string text = string.Empty + devide[2];
		if (text == "h" && devide.Length > 6)
		{
			Myname = SystemInfo.operatingSystem.ToString();
			string text2 = string.Empty + Myname[10];
			if (text2 != "2" && text2 != "3")
			{
				return 0;
			}
			return 1;
		}
		if (devide == "Unknown" && ScaleGUI.WIDTH * ScaleGUI.HEIGHT < 786432f)
		{
			return 0;
		}
		return -1;
	}

	public static void SMSsend(string phonenumber, string bodytext, int n)
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			_SMSsend(phonenumber, bodytext, n);
		}
	}

	public static void back()
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			_back();
		}
	}

	public static void Send()
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			_Send();
		}
	}

	public static int unpause()
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			return _unpause();
		}
		return 0;
	}

	public static int checkRotation()
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			return _checkRotation();
		}
		return 0;
	}

	public static void purchaseItem(string itemID, string userName, string gameID)
	{
		if (Application.platform != RuntimePlatform.OSXEditor)
		{
			_purchaseItem(itemID, userName, gameID);
		}
	}
}
