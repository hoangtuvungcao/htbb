/// <summary>
/// Du lieu khung hinh cua 1 loai Pokemon, doc tu file Resources/poke/&lt;khoa&gt;_dl.bytes.
///
/// Dinh dang (big-endian, xem docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 7.1):
///   u8 phienBan = 1, u8 soDongTac = 6
///   lap soDongTac lan: u16 rongKhung, u16 caoKhung, u8 soKhung, u8 thoiLuong[soKhung],
///                      u16 yAtlas, i8 dxChan[4], i8 dyChan[4]
///   u8 coDongTacThat[6]
///
/// Lop nay KHONG dung UnityEngine de bien dich rieng va kiem thu duoc ngoai Unity
/// (xem test o scratchpad cua dot chuyen doi). Phan nap anh/ve nam o PokeBody.cs.
/// KHONG nham voi DataEffect/ImageObj (dinh dang khung cua quai cu, gui qua mang).
/// </summary>
public class PokeDuLieu
{
	public const int DUNG = 0;

	public const int CHAY = 1;

	public const int DANH_GAN = 2;

	public const int DANH_XA = 3;

	public const int TRUNG_DON = 4;

	public const int GUC_NGA = 5;

	public const int SO_DONG_TAC = 6;

	/// <summary>So lan Main.FixedUpdate chay trong 1 giay (Fixed Timestep 0.02 s,
	/// ProjectSettings/TimeManager.asset). Canvas.gameTick tang 1 lan moi FixedUpdate.</summary>
	public const int TICK_MOI_GIAY = 50;

	/// <summary>Don vi thoiLuong trong file: 1/60 giay (AnimData.xml cua PMD).</summary>
	public const int DON_VI_THOI_LUONG = 60;

	public int phienBan;

	public PokeDongTac[] dongTac;

	public bool[] coThat = new bool[SO_DONG_TAC];

	/// <summary>Doc du lieu; tra ve null neu file hong (khong nem ngoai le).</summary>
	public static PokeDuLieu doc(byte[] b)
	{
		if (b == null || b.Length < 2)
		{
			return null;
		}
		try
		{
			int p = 0;
			PokeDuLieu kq = new PokeDuLieu();
			kq.phienBan = b[p++];
			if (kq.phienBan != 1)
			{
				return null;
			}
			int soDongTac = b[p++];
			if (soDongTac <= 0)
			{
				return null;
			}
			kq.dongTac = new PokeDongTac[soDongTac];
			for (int i = 0; i < soDongTac; i++)
			{
				PokeDongTac d = new PokeDongTac();
				d.rongKhung = (b[p] << 8) | b[p + 1];
				p += 2;
				d.caoKhung = (b[p] << 8) | b[p + 1];
				p += 2;
				d.soKhung = b[p++];
				if (d.soKhung <= 0 || d.rongKhung <= 0 || d.caoKhung <= 0)
				{
					return null;
				}
				d.thoiLuong = new int[d.soKhung];
				d.tongThoiLuong = 0;
				for (int k = 0; k < d.soKhung; k++)
				{
					int t = b[p++];
					if (t <= 0)
					{
						t = 1;
					}
					d.thoiLuong[k] = t;
					d.tongThoiLuong += t;
				}
				d.yAtlas = (b[p] << 8) | b[p + 1];
				p += 2;
				for (int h = 0; h < 4; h++)
				{
					d.dxChan[h] = (sbyte)b[p++];
				}
				for (int h = 0; h < 4; h++)
				{
					d.dyChan[h] = (sbyte)b[p++];
				}
				kq.dongTac[i] = d;
			}
			for (int i = 0; i < SO_DONG_TAC; i++)
			{
				// Phan coDongTacThat la phu: file thieu phan nay van dung duoc.
				kq.coThat[i] = p < b.Length && b[p++] != 0;
			}
			return kq;
		}
		catch (System.IndexOutOfRangeException)
		{
			return null;
		}
	}

	/// <summary>Dong tac theo so; dong tac thieu trong file thi dung dong tac 0 (dung).</summary>
	public PokeDongTac layDongTac(int so)
	{
		if (so < 0 || so >= dongTac.Length)
		{
			so = 0;
		}
		return dongTac[so];
	}

	/// <summary>Doi so tick game (50/giay) sang don vi 1/60 giay.</summary>
	public static int doiTick(int tickGame)
	{
		if (tickGame < 0)
		{
			tickGame = 0;
		}
		return tickGame * DON_VI_THOI_LUONG / TICK_MOI_GIAY;
	}

	/// <summary>Dong tac co lap lai khong. Trung don va guc nga dung lai o khung cuoi.</summary>
	public static bool laDongTacLap(int so)
	{
		return so != TRUNG_DON && so != GUC_NGA;
	}
}

public class PokeDongTac
{
	public int rongKhung;

	public int caoKhung;

	public int soKhung;

	public int[] thoiLuong;

	public int tongThoiLuong;

	public int yAtlas;

	public int[] dxChan = new int[4];

	public int[] dyChan = new int[4];

	/// <summary>Khung can ve sau t (don vi 1/60 s) ke tu luc bat dau dong tac.</summary>
	public int khungTai(int t, bool lap)
	{
		if (t < 0)
		{
			t = 0;
		}
		if (t >= tongThoiLuong)
		{
			if (!lap)
			{
				return soKhung - 1;
			}
			t %= tongThoiLuong;
		}
		for (int i = 0; i < soKhung; i++)
		{
			if (t < thoiLuong[i])
			{
				return i;
			}
			t -= thoiLuong[i];
		}
		return soKhung - 1;
	}

	/// <summary>Toa do y cua hang huong dir trong atlas (dir theo client: DOWN0 UP1 LEFT2 RIGHT3).</summary>
	public int yHang(int dir)
	{
		if (dir < 0 || dir > 3)
		{
			dir = 0;
		}
		return yAtlas + dir * caoKhung;
	}
}
