using System.Runtime.InteropServices;
using UnityEngine;

/// <summary>
/// Cau noi toi cac ham native cua ban iOS goc (gui SMS, mua vat pham, xoay man hinh).
///
/// VI SAO CO KHOI #if: sau khoi nay la sau ham native duoc khai bao
/// [DllImport("__Internal")]. IL2CPP sinh tham chieu toi chung DU LUC CHAY khong he goi,
/// nen:
///   - WebGL : hong ngay o khau lien ket - "undefined symbol: _SMSsend" va nam ham khac.
///   - Windows: bien dich duoc, nhung Application.platform != 0 van dung tren Windows
///     (0 la OSXEditor), nen neu co doan nao goi toi thi se nem DllNotFoundException
///     giua luc choi.
/// Bo chung ra khoi moi nen tang tru iOS thi ca hai van de tren deu het, va hanh vi tren
/// iOS khong doi mot chut nao.
///
/// Cac ham boc ben duoi giu nguyen ten va chu ky de cho goi khong phai sua gi.
/// </summary>
public class iOSPlugins
{
	public static string devide;

	public static string Myname;

#if UNITY_IOS && !UNITY_EDITOR
	[DllImport("__Internal")]
	private static extern void _SMSsend(string tophone, string withtext, int n);

	[DllImport("__Internal")]
	private static extern int _unpause();

	[DllImport("__Internal")]
	private static extern int _checkRotation();

	[DllImport("__Internal")]
	private static extern int _back();

	[DllImport("__Internal")]
	private static extern int _Send();

	[DllImport("__Internal")]
	private static extern void _purchaseItem(string itemID, string userName, string gameID);
#endif

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
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			_SMSsend(phonenumber, bodytext, n);
		}
#endif
	}

	public static void back()
	{
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			_back();
		}
#endif
	}

	public static void Send()
	{
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			_Send();
		}
#endif
	}

	public static int unpause()
	{
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			return _unpause();
		}
#endif
		return 0;
	}

	public static int checkRotation()
	{
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			return _checkRotation();
		}
#endif
		return 0;
	}

	public static void purchaseItem(string itemID, string userName, string gameID)
	{
#if UNITY_IOS && !UNITY_EDITOR
		if (Application.platform != 0)
		{
			_purchaseItem(itemID, userName, gameID);
		}
#endif
	}
}
