using UnityEngine;

/// <summary>
/// Kenh ghi nhat ky rieng cua client.
///
/// TRUOC DAY CA BON HAM DEU RONG - moi loi client tu bao deu bien mat khong dau vet.
/// Do khong phai chuyen nho: Tilemap.loadMap bat ngoai le roi bao bang
/// Out.println("LOAD TILE ERR " + level), nen ban do nen KHONG NAP DUOC ma nhat ky van
/// sach tinh. Nhin vao Player.log thi tuong moi thu binh thuong, trong khi man hinh chi
/// con nen co phang thay vi canh lang.
///
/// Cac cho goi Debug.* THANG (vi du "Create Image ... fail" trong Image.cs) van hien,
/// nen su vang mat cua Out cang de bi hieu nham la "khong co loi".
///
/// 118 cho goi, chi 4 cho nam trong ham paint/update nen khong lam ngap nhat ky.
/// </summary>
public class Out
{
	/// <summary>Dat false neu can tat het cho nhe (vi du ban phat hanh).</summary>
	public static bool bat = true;

	public static void println(string str)
	{
		if (bat)
		{
			Debug.Log(str);
		}
	}

	public static void Log(string str)
	{
		if (bat)
		{
			Debug.Log(str);
		}
	}

	public static void LogError(string str)
	{
		if (bat)
		{
			Debug.LogError(str);
		}
	}

	public static void LogWarning(string str)
	{
		if (bat)
		{
			Debug.LogWarning(str);
		}
	}
}
