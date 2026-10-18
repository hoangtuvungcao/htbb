using UnityEngine;

/// <summary>
/// Kênh nhật ký của client. Trước đây cả bốn hàm đều rỗng nên mọi lỗi client tự báo (vd "LOAD TILE ERR"
/// của Tilemap.loadMap) biến mất. println/Log/LogWarning chỉ ghi ở bản Development (mã gốc gọi chúng cả
/// trong vòng vẽ); LogError luôn ghi.
/// </summary>
public class Out
{
	public static void println(string str)
	{
		if (Debug.isDebugBuild)
		{
			Debug.Log(str);
		}
	}

	public static void Log(string str)
	{
		if (Debug.isDebugBuild)
		{
			Debug.Log(str);
		}
	}

	public static void LogError(string str)
	{
		Debug.LogError(str);
	}

	public static void LogWarning(string str)
	{
		if (Debug.isDebugBuild)
		{
			Debug.LogWarning(str);
		}
	}
}
