using System.Text;

/// <summary>
/// Tiền tệ (cùng quy ước client J2ME game.pokemon.TienTe): tên đầy đủ "xu" / "lượng" / "lượng khoá" thay chữ viết tắt
/// "$" / "lk", số có dấu chấm nghìn kiểu Việt Nam (1.250.000), số gọn (1,25 Tr / 12,5 N).
/// </summary>
public static class TienTe
{
	public const int XU = 0;
	public const int LUONG = 1;
	public const int LUONG_KHOA = 2;

	private static readonly string[] TEN = { "xu", "lượng", "lượng khoá" };

	public static string ten(int loai)
	{
		return loai >= 0 && loai < TEN.Length ? TEN[loai] : "";
	}

	public static string so(long v)
	{
		bool am = v < 0;
		string s = (am ? -v : v).ToString();
		if (s.StartsWith("-"))
		{
			return v.ToString();
		}
		StringBuilder b = new StringBuilder();
		int n = s.Length;
		for (int i = 0; i < n; i++)
		{
			if (i > 0 && (n - i) % 3 == 0)
			{
				b.Append('.');
			}
			b.Append(s[i]);
		}
		return (am ? "-" : "") + b.ToString();
	}

	/// <summary>"1.250 xu", "5 lượng".</summary>
	public static string chu(int loai, long v)
	{
		return so(v) + " " + ten(loai);
	}

	/// <summary>Số gọn cho chỗ chật: 12500 -> "12,5 N", 1250000 -> "1,25 Tr".</summary>
	public static string gon(long v)
	{
		long a = v < 0 ? -v : v;
		string dau = v < 0 ? "-" : "";
		if (a < 10000) return dau + so(a);
		if (a < 1000000L) return dau + phan(a, 1000L) + " N";
		if (a < 1000000000L) return dau + phan(a, 1000000L) + " Tr";
		return dau + phan(a, 1000000000L) + " Tỉ";
	}

	private static string phan(long a, long dv)
	{
		long nguyen = a / dv;
		long du = a % dv;
		if (nguyen >= 100) return so(nguyen);
		if (nguyen >= 10)
		{
			long l1 = du * 10 / dv;
			return l1 == 0 ? nguyen.ToString() : nguyen + "," + l1;
		}
		long l2 = du * 100 / dv;
		if (l2 == 0) return nguyen.ToString();
		if (l2 % 10 == 0) return nguyen + "," + (l2 / 10);
		return nguyen + "," + (l2 < 10 ? "0" : "") + l2;
	}
}
