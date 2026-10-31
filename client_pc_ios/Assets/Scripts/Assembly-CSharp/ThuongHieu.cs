/// <summary>
/// Ten game va thuong hieu - MOT cho duy nhat phia client (viec G7.E1/E2 trong
/// docs/kien-truc.md). Ban server: src/com/htbb/pokemon/ThuongHieu.java
/// (test/KiemHinhPokemon so hai ban phai giong nhau). Doi ten game: sua TEN_GAME o day va o ban
/// Java; ten build/ung dung (Unity productName, ten file .exe/.apk, ma ung dung) lay tu day qua
/// Assets/Editor/BuildCli.cs.
///
/// Man tai / chon may chu / dang nhap: ve tieu de Pokemon (logo hoac ten game + hang chan dung
/// 5 Pokemon khoi dau) thay cho anh logo goc cu (png/logo).
/// </summary>
public static class ThuongHieu
{
	public const string TEN_GAME = "Huyền Thoại Bảo Bối";

	/// <summary>Ten viet tat (logo nho, ten thu muc luu, nhan tin).</summary>
	public const string TEN_VIET_TAT = "HTBB";

	/// <summary>Unity productName (khong dau de an toan cho ten cua so/thu muc).</summary>
	public const string TEN_SAN_PHAM = "Huyen Thoai Bao Boi";

	/// <summary>Ten file build (.exe/.apk), khong khoang trang.</summary>
	public const string TEN_TEP = "HuyenThoaiBaoBoi";

	/// <summary>applicationIdentifier Android/iOS.</summary>
	public const string MA_UNG_DUNG = "vn.htbb.game";

	/// <summary>Unity companyName.</summary>
	public const string TEN_CONG_TY = "HTBB";

	/// <summary>
	/// May chu Pokemon CO DINH "host:cong" (bang ban jar MAY_CHU_CHINH): khac rong thi client chi vao may
	/// chu nay, khong tai danh sach robots.txt. Thu tu: tham so dong lenh -htbbMayChu host:cong > hang so nay >
	/// gia tri BuildCli.Win64 dong vao luc build (Resources/htbb_phat_hanh.txt, lay tu -htbbMayChu / bien
	/// MAY_CHU_CHINH / tools/phat-hanh/may_chu.conf) > MAY_CHU_THU (chi ban Development / Editor).
	/// Ban phat hanh KHONG build duoc khi khong co may chu (BuildCli dung lai) - khong con roi ve danh sach may chu cua game goc.
	/// </summary>
	public const string MAY_CHU_CHINH = "";

	/// <summary>May chu thu tren may (server HTBB chay local) cho ban Development / Editor.</summary>
	public const string MAY_CHU_THU = "127.0.0.1:19129";

	/// <summary>
	/// Link phienban.json cua kenh phat hanh HTBB (CapNhat.cs doc muc "pc"). RONG = TAT tu cap nhat PC.
	/// Thu tu: hang so nay > gia tri dong vao luc build (CAP_NHAT_URL / -htbbCapNhat / tools/phat-hanh/may_chu.conf).
	/// Link kenh phat hanh cu cua game goc luon bi bo qua (xem capNhatUrl, laKenhCu).
	/// </summary>
	public const string CAP_NHAT_URL = "";

	/// <summary>Ten phien ban cho nguoi choi; ban build dong gia tri tu tools/phat-hanh/phien_ban.conf.</summary>
	public const string PHIEN_BAN = "0.9.27-opentest";

	/// <summary>Ten tep cau hinh BuildCli sinh luc build trong Assets/Resources (khong dua len git).</summary>
	public const string TEP_PHAT_HANH = "htbb_phat_hanh";

	private static bool daDocMayChu;

	private static string mayChu;

	private static System.Collections.Generic.Dictionary<string, string> phatHanh;

	/// <summary>Gia tri KHOA=... BuildCli dong vao ban build (Resources/htbb_phat_hanh.txt); khong co = "".</summary>
	public static string giaTriPhatHanh(string khoa)
	{
		if (phatHanh == null)
		{
			phatHanh = new System.Collections.Generic.Dictionary<string, string>();
			try
			{
				UnityEngine.TextAsset t = UnityEngine.Resources.Load<UnityEngine.TextAsset>(TEP_PHAT_HANH);
				if (t != null)
				{
					foreach (string dong in t.text.Split('\n'))
					{
						string d = dong.Trim();
						int i = d.IndexOf('=');
						if (i > 0 && !d.StartsWith("#"))
						{
							phatHanh[d.Substring(0, i).Trim()] = d.Substring(i + 1).Trim();
						}
					}
				}
			}
			catch (System.Exception)
			{
				// khong co tep (Editor / ban dev): dung hang so
			}
		}
		string v;
		return phatHanh.TryGetValue(khoa, out v) ? v : string.Empty;
	}

	/// <summary>"host:cong" may chu co dinh dang dung, null = dung danh sach may chu (robots.txt) nhu cu.</summary>
	public static string mayChuCoDinh()
	{
		if (daDocMayChu)
		{
			return mayChu;
		}
		daDocMayChu = true;
		string[] args = System.Environment.GetCommandLineArgs();
		int i = System.Array.IndexOf(args, "-htbbMayChu");
		string dongVao = giaTriPhatHanh("MAY_CHU_CHINH");
		if (i >= 0 && i + 1 < args.Length && args[i + 1].IndexOf(':') > 0)
		{
			mayChu = args[i + 1].Trim();
		}
		else if (MAY_CHU_CHINH.Length > 0)
		{
			mayChu = MAY_CHU_CHINH;
		}
		else if (dongVao.IndexOf(':') > 0)
		{
			mayChu = dongVao;
		}
		else if (UnityEngine.Debug.isDebugBuild)
		{
			mayChu = MAY_CHU_THU;
		}
		return mayChu;
	}

	/// <summary>
	/// Link phienban.json de tu cap nhat PC; null = TAT. Bien moi truong HTBB_CAPNHAT_URL (chi de thu tren may)
	/// > CAP_NHAT_URL > gia tri dong vao luc build. O che do Pokemon KHONG BAO GIO doc kenh phat hanh cu cua game goc (ban PC
	/// HTBB se tu tai game cu de len chinh no).
	/// </summary>
	public static string capNhatUrl()
	{
		string link = System.Environment.GetEnvironmentVariable("HTBB_CAPNHAT_URL");
		if (string.IsNullOrEmpty(link))
		{
			link = CAP_NHAT_URL;
		}
		if (string.IsNullOrEmpty(link))
		{
			link = giaTriPhatHanh("CAP_NHAT_URL");
		}
		link = link == null ? string.Empty : link.Trim();
		if (link.Length == 0)
		{
			return null;
		}
		if (laKenhCu(link))
		{
			UnityEngine.Debug.LogWarning("CapNhat: bo qua kenh phat hanh cu cua game goc (" + link + ") o ban Huyen Thoai Bao Boi");
			return null;
		}
		return link;
	}

	/// <summary>Link / may chu thuoc kenh phat hanh cu cua game goc (repo + ten mien cu) - BuildCli dung build, CapNhat bo qua.</summary>
	public static bool laKenhCu(string link)
	{
		if (string.IsNullOrEmpty(link))
		{
			return false;
		}
		string l = link.ToLowerInvariant();
		// TUONG-THICH-TEN-CU: ba cach viet ten mien / repo kenh cu cua game goc - giu chuoi de con nhan ra va chan
		return l.Contains("nqsh-h2") || l.Contains("nqsh_h2") || l.Contains("nqshh2");
	}

	/// <summary>
	/// Muc may chu luu tu ban cu (ten hoac dia chi cua server game goc): bo, khong hien / khong vao (giong ban jar
	/// TaiDanhSach.tenHien + ServerListScr.loadIP).
	/// </summary>
	public static bool laMayChuCu(string ten, string diaChi)
	{
		string t = ten == null ? string.Empty : ten.ToUpperInvariant();
		// TUONG-THICH-TEN-CU: ten server game goc con nam trong danh sach da luu tren may nguoi choi - giu chuoi de loc
		return t.Contains("NQSH") || t.Contains("KPAH") || laKenhCu(diaChi);
	}

	/// <summary>Ten phien ban hien cho nguoi choi (vd "0.9.0-opentest (900)").</summary>
	public static string phienBan()
	{
		string ten = giaTriPhatHanh("PHIEN_BAN");
		if (ten.Length == 0)
		{
			ten = PHIEN_BAN;
		}
		return ten + " (" + UnityEngine.Application.version + ")";
	}

	/// <summary>Dong gioi thieu/ghi cong ngan duoi tieu de (quyet dinh Q10: phi thuong mai).</summary>
	public const string GIOI_THIEU = "Game fan Pokémon phi thương mại - Pokémon © Nintendo / Game Freak";

	public static string tenGame()
	{
		return TEN_GAME;
	}

	public static string tenSanPham()
	{
		return TEN_SAN_PHAM;
	}

	public static string tenTep()
	{
		return TEN_TEP;
	}

	public static string maUngDung()
	{
		return MA_UNG_DUNG;
	}

	/// <summary>So class (dong Pokemon khoi dau) ve tren tieu de.</summary>
	public const int SO_KHOI_DAU = 5;

	private const int CANH_CHAN_DUNG = 32;

	private const int KHOANG_CHAN_DUNG = 38;

	/// <summary>
	/// Ve tieu de Pokemon, tam ngang xTam, dong ten game o yTam, hang chan dung ngay duoi.
	/// Tra ve false (de ve logo cu) khi chua co gi de ve.
	/// </summary>
	public static bool veTieuDe(MyGraphics g, int xTam, int yTam)
	{
		// logo wordmark chung (client_jar/shared/brand, cùng bản J2ME / web): khoảng [yTam - 100, yTam + 60]
		if (BannerParallax.veLogo(g, xTam, System.Math.Max(8, yTam - 100), yTam + 60, System.Math.Min(360, Canvas.w - 40)))
		{
			return true;
		}
		bool daVe = false;
		int x0 = xTam - (SO_KHOI_DAU - 1) * KHOANG_CHAN_DUNG / 2;
		int yChanDung = yTam + 14 + CANH_CHAN_DUNG / 2;
		for (int i = 0; i < SO_KHOI_DAU; i++)
		{
			int dex = LoaiPokemonClient.khoiDau(i);
			if (dex > 0 && ChanDungPoke.paintHuyHieu(g, dex, false, x0 + i * KHOANG_CHAN_DUNG, yChanDung, CANH_CHAN_DUNG + 2))
			{
				daVe = true;
			}
		}
		try
		{
			MFont chu = (MFont.bigFont != null) ? MFont.bigFont : MFont.borderFont;
			if (chu != null)
			{
				chu.drawString(g, TEN_GAME, xTam, yTam - 8, 2);
				daVe = true;
			}
			if (MFont.smallFont != null)
			{
				MFont.smallFont.drawString(g, GIOI_THIEU, xTam, yChanDung + CANH_CHAN_DUNG / 2 + 4, 2);
			}
		}
		catch (System.Exception)
		{
			// Font chua nap (man tai dau tien): chi co chan dung cung du.
		}
		return daVe;
	}
}
