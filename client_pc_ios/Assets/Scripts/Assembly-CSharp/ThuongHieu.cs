/// <summary>
/// Ten game va thuong hieu - MOT cho duy nhat phia client (viec G7.E1/E2 trong
/// docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 15). Ban server: src/com/htbb/pokemon/ThuongHieu.java
/// (test/KiemHinhPokemon so hai ban phai giong nhau). Doi ten game: sua TEN_GAME o day va o ban
/// Java; ten build/ung dung (Unity productName, ten file .exe/.apk, ma ung dung) lay tu day qua
/// Assets/Editor/XayDung.cs khi chay "xay-client.bat chuanbi".
///
/// Man tai / chon may chu / dang nhap: khi PokeBody.CHE_DO_POKEMON thi ve tieu de Pokemon (ten
/// game + hang chan dung 5 Pokemon khoi dau) thay cho anh logo goc cu (png/logo).
/// </summary>
public static class ThuongHieu
{
	public const string TEN_GAME = "Huyền Thoại Bảo Bối";

	/// <summary>Ten viet tat (logo nho, ten thu muc luu, nhan tin).</summary>
	public const string TEN_VIET_TAT = "HTBB";

	/// <summary>Ten cu, dung khi tat che do Pokemon.</summary>
	public const string TEN_GAME_CU = "Khí Phách Anh Hùng";

	/// <summary>Unity productName (khong dau de an toan cho ten cua so/thu muc).</summary>
	public const string TEN_SAN_PHAM = "Huyen Thoai Bao Boi";

	public const string TEN_SAN_PHAM_CU = "Khi Phach Anh Hung";

	/// <summary>Ten file build (.exe/.apk), khong khoang trang.</summary>
	public const string TEN_TEP = "HuyenThoaiBaoBoi";

	public const string TEN_TEP_CU = "KhiPhachAnhHung";

	/// <summary>applicationIdentifier Android/iOS.</summary>
	public const string MA_UNG_DUNG = "vn.htbb.game";

	/// <summary>Unity companyName.</summary>
	public const string TEN_CONG_TY = "HTBB";

	public const string MA_UNG_DUNG_CU = "com.htbb.game";

	/// <summary>
	/// May chu Pokemon CO DINH "host:cong" (bang ban jar MAY_CHU_CHINH): khac rong thi client chi vao may
	/// chu nay, khong tai danh sach robots.txt. Tham so dong lenh -htbbMayChu host:cong ghi de; ban build
	/// Development (Debug.isDebugBuild) / Editor khi ca hai deu trong thi dung MAY_CHU_THU.
	/// </summary>
	public const string MAY_CHU_CHINH = "";

	/// <summary>May chu thu tren may (server HTBB chay local) cho ban Development / Editor.</summary>
	public const string MAY_CHU_THU = "127.0.0.1:19129";

	private static bool daDocMayChu;

	private static string mayChu;

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
		if (i >= 0 && i + 1 < args.Length && args[i + 1].IndexOf(':') > 0)
		{
			mayChu = args[i + 1].Trim();
		}
		else if (MAY_CHU_CHINH.Length > 0)
		{
			mayChu = MAY_CHU_CHINH;
		}
		else if (UnityEngine.Debug.isDebugBuild)
		{
			mayChu = MAY_CHU_THU;
		}
		return mayChu;
	}

	/// <summary>Dong gioi thieu/ghi cong ngan duoi tieu de (quyet dinh Q10: phi thuong mai).</summary>
	public const string GIOI_THIEU = "Game Pokémon fan phi thương mại - Pokémon © Nintendo / Game Freak";

	public static string tenGame()
	{
		return PokeBody.CHE_DO_POKEMON ? TEN_GAME : TEN_GAME_CU;
	}

	public static string tenSanPham()
	{
		return PokeBody.CHE_DO_POKEMON ? TEN_SAN_PHAM : TEN_SAN_PHAM_CU;
	}

	public static string tenTep()
	{
		return PokeBody.CHE_DO_POKEMON ? TEN_TEP : TEN_TEP_CU;
	}

	public static string maUngDung()
	{
		return PokeBody.CHE_DO_POKEMON ? MA_UNG_DUNG : MA_UNG_DUNG_CU;
	}

	/// <summary>So class (dong Pokemon khoi dau) ve tren tieu de.</summary>
	public const int SO_KHOI_DAU = 5;

	private const int CANH_CHAN_DUNG = 32;

	private const int KHOANG_CHAN_DUNG = 38;

	/// <summary>
	/// Ve tieu de Pokemon, tam ngang xTam, dong ten game o yTam, hang chan dung ngay duoi.
	/// Tra ve false (de ve logo cu) khi tat che do Pokemon hoac chua co gi de ve.
	/// </summary>
	public static bool veTieuDe(MyGraphics g, int xTam, int yTam)
	{
		if (!PokeBody.CHE_DO_POKEMON)
		{
			return false;
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
