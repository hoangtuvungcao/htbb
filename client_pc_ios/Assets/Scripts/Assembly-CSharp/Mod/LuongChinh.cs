using System.Collections.Generic;

/// <summary>
/// Hàng đợi việc chạy ở luồng chính Unity. Code mod port 1:1 từ jar có chỗ chạy trong thread nền (vòng Tiện ích
/// 100 ms, tự đăng nhập lại, nuôi pet đăng nhập lại) mà Unity chỉ cho đụng GUI / ảnh / đổi màn ở luồng chính:
/// thread nền vẫn ngủ / chờ như bản jar, còn việc đụng game thì gửi vào đây. Main.FixedUpdate gọi chayHet() mỗi
/// khung hình (kể cả ở màn chọn server / nhân vật, nơi GameScr.update không chạy).
/// </summary>
public static class LuongChinh
{
	private static readonly Queue<System.Action> viec = new Queue<System.Action>();

	public static void chay(System.Action a)
	{
		lock (viec)
		{
			viec.Enqueue(a);
		}
	}

	public static void chayHet()
	{
		while (true)
		{
			System.Action a;
			lock (viec)
			{
				if (viec.Count == 0)
				{
					return;
				}
				a = viec.Dequeue();
			}
			try
			{
				a();
			}
			catch (System.Exception ex)
			{
				UnityEngine.Debug.LogWarning("[MOD] luong chinh: " + ex);
			}
		}
	}
}
