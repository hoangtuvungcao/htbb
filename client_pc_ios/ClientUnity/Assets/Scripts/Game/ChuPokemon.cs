using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

/// <summary>
/// G7.C3: doi chu kiem hiep -> the gioi Pokemon tren client. Ban C# cua
/// src/com/htbb/pokemon/ChuPokemon.java (doiLuon). Tu dien lay tu ChuPokemonDuLieu.BANG -
/// file SINH bang tools/pokemon/sinh_sql_chu.py tu ban Java, khong sua tay.
///
/// Doi o MOT cho: MFont (moi chu ve ra man hinh, do rong chu, ngat dong) goi D(). Nho vay
/// khong phai sua tung chuoi trong GameScr/MenuWindows/PauseMenu... va chu server gui xuong
/// cung qua day (server da doi roi thi doi lai van ra y het - tu dien khong lap).
/// Tat PokeBody.CHE_DO_POKEMON thi tra nguyen chuoi. O nhap chu (TField) tat tam qua
/// KhongDoi de nguoi choi thay dung chu minh go.
/// </summary>
public static class ChuPokemon
{
	/// <summary>Dat true khi ve chu nguoi choi dang go (TField) - khong doi.</summary>
	public static bool KhongDoi;

	private static Regex mau;

	private static Dictionary<string, string> phanBiet;

	private static Dictionary<string, string> khongPhanBiet;

	private static readonly Dictionary<string, string> boNho = new Dictionary<string, string>();

	private const int BO_NHO_TOI_DA = 4096;

	public static string D(string s)
	{
		if (!PokeBody.CHE_DO_POKEMON || KhongDoi || string.IsNullOrEmpty(s))
		{
			return s;
		}
		string kq;
		if (boNho.TryGetValue(s, out kq))
		{
			return kq;
		}
		kq = DoiLuon(s);
		if (boNho.Count >= BO_NHO_TOI_DA)
		{
			boNho.Clear();
		}
		boNho[s] = kq;
		return kq;
	}

	private static string Khoa(string s)
	{
		return s.StartsWith("=") ? s.Substring(1) : s;
	}

	private static void Nap()
	{
		string[][] bang = ChuPokemonDuLieu.BANG;
		List<string[]> sx = new List<string[]>(bang);
		// Sap xep on dinh, khoa dai truoc (giong Collections.sort ben Java).
		List<KeyValuePair<int, string[]>> tam = new List<KeyValuePair<int, string[]>>();
		for (int i = 0; i < sx.Count; i++)
		{
			tam.Add(new KeyValuePair<int, string[]>(i, sx[i]));
		}
		tam.Sort((a, b) =>
		{
			int c = Khoa(b.Value[0]).Length - Khoa(a.Value[0]).Length;
			return c != 0 ? c : a.Key - b.Key;
		});
		phanBiet = new Dictionary<string, string>();
		khongPhanBiet = new Dictionary<string, string>();
		// Truoc khoa: khong la chu/gach duoi (chu so thi duoc: "1Giang hồ" trong hoi thoai NPC).
		StringBuilder sb = new StringBuilder("(?<![\\p{L}_])(?:");
		bool dau = true;
		foreach (KeyValuePair<int, string[]> kv in tam)
		{
			string k0 = kv.Value[0];
			string k = Khoa(k0);
			if (k0.StartsWith("="))
			{
				if (phanBiet.ContainsKey(k))
				{
					continue;
				}
				phanBiet[k] = kv.Value[1];
				sb.Append(dau ? "" : "|").Append("(?-i:").Append(Regex.Escape(k)).Append(')');
			}
			else
			{
				string t = k.ToLowerInvariant();
				if (khongPhanBiet.ContainsKey(t))
				{
					continue;
				}
				khongPhanBiet[t] = kv.Value[1];
				sb.Append(dau ? "" : "|").Append(Regex.Escape(k));
			}
			dau = false;
		}
		sb.Append(")(?!\\w)");
		mau = new Regex(sb.ToString(), RegexOptions.IgnoreCase | RegexOptions.CultureInvariant);
	}

	private static string TheoKieu(string goc, string moi)
	{
		if (moi.Length == 0)
		{
			return moi;
		}
		string[] tuGoc = goc.Split(' ');
		bool moiTuHoa = tuGoc.Length >= 2;
		foreach (string t in tuGoc)
		{
			if (t.Length == 0 || !char.IsUpper(t[0]))
			{
				moiTuHoa = false;
			}
		}
		bool coChu = false;
		bool hetHoa = true;
		foreach (char c in goc)
		{
			if (char.IsLetter(c))
			{
				coChu = true;
				if (!char.IsUpper(c))
				{
					hetHoa = false;
				}
			}
		}
		if (coChu && hetHoa && goc.Length > 1)
		{
			return moi.ToUpperInvariant();
		}
		if (moiTuHoa)
		{
			StringBuilder sb = new StringBuilder(moi.Length);
			bool dauTu = true;
			foreach (char c in moi)
			{
				sb.Append(dauTu ? char.ToUpperInvariant(c) : c);
				dauTu = c == ' ';
			}
			return sb.ToString();
		}
		if (char.IsUpper(goc[0]) && char.IsLower(moi[0]))
		{
			return char.ToUpperInvariant(moi[0]) + moi.Substring(1);
		}
		return moi;
	}

	/// <summary>Doi bat ke co (test).</summary>
	public static string DoiLuon(string s)
	{
		if (string.IsNullOrEmpty(s))
		{
			return s;
		}
		if (mau == null)
		{
			Nap();
		}
		return mau.Replace(s, m =>
		{
			string g = m.Value;
			string moi;
			if (phanBiet.TryGetValue(g, out moi))
			{
				return moi;
			}
			if (khongPhanBiet.TryGetValue(g.ToLowerInvariant(), out moi))
			{
				return TheoKieu(g, moi);
			}
			return g;
		});
	}
}
