using System.Security.Cryptography;
using System.Text;
using UnityEngine;

/// <summary>
/// Mã máy gửi cuối gói đăng nhập (GameService.login; server lọc qua data.CamTheoMay).
/// Bước xác thực hai chiều -88 của server game gốc đã bỏ: server HTBB không trả lời -88.
/// </summary>
public static class KiemServer
{
	/// <summary>Mã máy: chuỗi ngẫu nhiên sinh một lần, lưu PlayerPrefs (chỉ chữ/số, server lọc như vậy).</summary>
	public static string maMay()
	{
		string s = PlayerPrefs.GetString("kpahmay", string.Empty);
		if (s.Length >= 16)
		{
			return s;
		}
		const string chu = "0123456789abcdefghijklmnopqrstuvwxyz";
		byte[] b = new byte[24];
		using (RandomNumberGenerator rng = RandomNumberGenerator.Create())
		{
			rng.GetBytes(b);
		}
		StringBuilder sb = new StringBuilder("unity");
		for (int i = 0; i < b.Length; i++)
		{
			sb.Append(chu[b[i] % chu.Length]);
		}
		s = sb.ToString();
		PlayerPrefs.SetString("kpahmay", s);
		PlayerPrefs.Save();
		return s;
	}
}
