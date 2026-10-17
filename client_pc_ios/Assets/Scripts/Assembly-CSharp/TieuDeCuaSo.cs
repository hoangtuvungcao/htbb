using System;
using System.Runtime.InteropServices;

/// <summary>
/// Tieu de cua so PC co dau: "Huyền Thoại Bảo Bối". Unity lay tieu de cua so tu productName
/// (ProjectSettings, de khong dau "Huyen Thoai Bao Boi" cho ten thu muc luu an toan), nen tren
/// Windows dat lai bang SetWindowTextW. Nen tang khac / Editor: khong lam gi.
/// </summary>
public static class TieuDeCuaSo
{
#if UNITY_STANDALONE_WIN && !UNITY_EDITOR
	[DllImport("user32.dll")]
	private static extern IntPtr GetActiveWindow();

	[DllImport("user32.dll", CharSet = CharSet.Unicode)]
	private static extern bool SetWindowTextW(IntPtr hWnd, string text);
#endif

	public static void dat()
	{
#if UNITY_STANDALONE_WIN && !UNITY_EDITOR
		try
		{
			IntPtr h = GetActiveWindow();
			if (h != IntPtr.Zero)
			{
				SetWindowTextW(h, PokeBody.CHE_DO_POKEMON ? ThuongHieu.TEN_GAME : ThuongHieu.TEN_GAME_CU);
			}
		}
		catch (Exception)
		{
			// chi la tieu de - khong bao gio lam hong luc mo game
		}
#endif
	}
}
