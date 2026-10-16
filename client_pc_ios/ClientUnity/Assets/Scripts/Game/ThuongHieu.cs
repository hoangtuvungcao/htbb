/// <summary>
/// Ten game va thuong hieu - MOT cho duy nhat phia client (viec G7.E1/E2 trong
/// docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 15). Ban server: src/com/kpah/pokemon/ThuongHieu.java
/// (test/KiemHinhPokemon so hai ban phai giong nhau). Doi ten game: sua TEN_GAME o day va o ban
/// Java; ten build/ung dung (Unity productName, ten file .exe/.apk, ma ung dung) lay tu day qua
/// Assets/Editor/XayDung.cs khi chay "xay-client.bat chuanbi".
///
/// Man tai / chon may chu / dang nhap: khi PokeBody.CHE_DO_POKEMON thi ve tieu de Pokemon (ten
/// game + hang chan dung 5 Pokemon khoi dau) thay cho anh logo KPAH cu (png/logo).
/// </summary>
public static class ThuongHieu
{
	public const string TEN_GAME = "KPAH Pokémon";

	/// <summary>Ten cu, dung khi tat che do Pokemon.</summary>
	public const string TEN_GAME_CU = "Khí Phách Anh Hùng";

	/// <summary>Unity productName (khong dau de an toan cho ten cua so/thu muc).</summary>
	public const string TEN_SAN_PHAM = "KPAH Pokemon";

	public const string TEN_SAN_PHAM_CU = "Khi Phach Anh Hung";

	/// <summary>Ten file build (.exe/.apk), khong khoang trang.</summary>
	public const string TEN_TEP = "KPAHPokemon";

	public const string TEN_TEP_CU = "KhiPhachAnhHung";

	/// <summary>applicationIdentifier Android/iOS.</summary>
	public const string MA_UNG_DUNG = "com.kpah.pokemon";

	public const string MA_UNG_DUNG_CU = "com.kpah.game";

	/// <summary>Dong gioi thieu/ghi cong ngan duoi tieu de (quyet dinh Q10: phi thuong mai).</summary>
	public const string GIOI_THIEU = "Fan game phi thương mại - Pokémon © Nintendo / Game Freak";

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
			if (dex > 0 && PokeBody.paintChanDung(g, dex, false, x0 + i * KHOANG_CHAN_DUNG, yChanDung, CANH_CHAN_DUNG))
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
