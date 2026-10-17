/// <summary>
/// Ban C# rut gon cua src/com/htbb/pokemon/LoaiPokemon.java, chi dung o client cho nhung
/// cho server CHUA gui dex (man chon nhan vat, man tao nhan vat). Sau khi vao game, dex
/// lay tu lenh -90 POKE_THAN, KHONG suy ra o day.
///
/// Phai giu KHOP voi LoaiPokemon.java (KHOI_DAU_THEO_CLASS va cot capTienHoa/dexSau).
/// Kiem thu: test/KiemPokemonClient.java doi chieu hai bang nay.
/// Nhanh Eevee do nguoi choi chon nen o day khong biet -> luon tra ve Eevee (133).
/// </summary>
public class LoaiPokemonClient
{
	public const int KHONG = -1;

	public const int EEVEE = 133;

	/// <summary>index = class cu: 0 Kiem khach, 1 Chien binh, 2 Phap su, 3 Dau si, 4 Cung thu.</summary>
	public static readonly int[] KHOI_DAU_THEO_CLASS = new int[5] { 4, 1, 133, 7, 172 };

	/// <summary>{dex, capTienHoa, dexSau}; capTienHoa = 0 la dang cuoi.</summary>
	private static readonly int[][] TIEN_HOA = new int[12][]
	{
		new int[3] { 4, 16, 5 },
		new int[3] { 5, 36, 6 },
		new int[3] { 1, 16, 2 },
		new int[3] { 2, 32, 3 },
		new int[3] { 7, 16, 8 },
		new int[3] { 8, 36, 9 },
		new int[3] { 172, 10, 25 },
		new int[3] { 25, 30, 26 },
		new int[3] { 6, 0, KHONG },
		new int[3] { 3, 0, KHONG },
		new int[3] { 9, 0, KHONG },
		new int[3] { 26, 0, KHONG }
	};

	/// <summary>Ten hien thi cua 5 dong khoi dau, index = class.</summary>
	public static readonly string[] TEN_KHOI_DAU = new string[5] { "Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu" };

	/// <summary>Chu he hien o man tao nhan vat, index = class.</summary>
	public static readonly string[] HE_KHOI_DAU = new string[5] { "Hệ Lửa", "Hệ Cỏ/Độc", "Hệ Thường", "Hệ Nước", "Hệ Điện" };

	/// <summary>Vai tro cu cua class (de nguoi choi biet loi danh), index = class.</summary>
	public static readonly string[] VAI_TRO = new string[5] { "Cận chiến cân bằng", "Cận chiến chịu đòn", "Phép, đánh xa", "Cận chiến thủ", "Đánh xa" };

	public static int khoiDau(int charClass)
	{
		if (charClass < 0 || charClass >= KHOI_DAU_THEO_CLASS.Length)
		{
			return KHONG;
		}
		return KHOI_DAU_THEO_CLASS[charClass];
	}

	public static int tienHoaKeTiep(int dex, int capDo)
	{
		for (int i = 0; i < TIEN_HOA.Length; i++)
		{
			if (TIEN_HOA[i][0] == dex)
			{
				if (TIEN_HOA[i][1] <= 0 || capDo < TIEN_HOA[i][1])
				{
					return KHONG;
				}
				return TIEN_HOA[i][2];
			}
		}
		return KHONG;
	}

	public static int dangTheoCap(int dexDau, int capDo)
	{
		int dex = dexDau;
		for (int i = 0; i < 5; i++)
		{
			int sau = tienHoaKeTiep(dex, capDo);
			if (sau == KHONG)
			{
				break;
			}
			dex = sau;
		}
		return dex;
	}

	/// <summary>Dex du doan tu class + cap (KHONG neu class ngoai 0..4).</summary>
	public static int duDoan(int charClass, int capDo)
	{
		int dau = khoiDau(charClass);
		if (dau == KHONG)
		{
			return KHONG;
		}
		return dangTheoCap(dau, capDo);
	}

	/// <summary>Ten loai (chi cac loai cua nguoi choi; loai khac tra ve chuoi rong).</summary>
	public static string ten(int dex)
	{
		switch (dex)
		{
		case 1:
			return "Bulbasaur";
		case 2:
			return "Ivysaur";
		case 3:
			return "Venusaur";
		case 4:
			return "Charmander";
		case 5:
			return "Charmeleon";
		case 6:
			return "Charizard";
		case 7:
			return "Squirtle";
		case 8:
			return "Wartortle";
		case 9:
			return "Blastoise";
		case 25:
			return "Pikachu";
		case 26:
			return "Raichu";
		case 133:
			return "Eevee";
		case 134:
			return "Vaporeon";
		case 135:
			return "Jolteon";
		case 136:
			return "Flareon";
		case 172:
			return "Pichu";
		case 196:
			return "Espeon";
		case 197:
			return "Umbreon";
		case 470:
			return "Leafeon";
		case 471:
			return "Glaceon";
		case 700:
			return "Sylveon";
		default:
			return string.Empty;
		}
	}
}
