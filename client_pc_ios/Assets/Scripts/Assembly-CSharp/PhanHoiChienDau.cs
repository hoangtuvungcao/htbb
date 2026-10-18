using UnityEngine;

/// <summary>
/// Phan hoi chien dau kieu Pokemon cho ban PC (chu bay len tren dau khi danh / trung don).
///
/// Mot cho duy nhat: GameScr.startFlyText goi phanLoai() (khi PokeBody.CHE_DO_POKEMON), nen ca
/// ~60 cho trong code goc dang goi startFlyText("-" + dam ...) / EFF_NAME[...] deu doi kieu ma khong
/// phai sua tung file. Kieu chu:
///   so sat thuong   "-123"          trang vien toi
///   chi mang        "Chí mạng!"     vang, chu to; so sat thuong cung luc cung doi vang
///   hieu qua cao    "Hiệu quả cao!" cam; so sat thuong cung luc doi cam (server gui effect 5)
///   truot           "Trượt"         xam
///   hoi mau         "+123"          xanh la
///   len cap         "Lên cấp!"      vang kim
/// Kem theo: nhay trang (hit flash) tren Pokemon trung don (LiveActor.pokeNhayDen) va am thanh (AmThanh).
/// </summary>
public static class PhanHoiChienDau
{
	public const int KIEU_GOC = 0;

	public const int KIEU_DAM = 1;

	public const int KIEU_CHI_MANG = 2;

	public const int KIEU_HIEU_QUA_CAO = 3;

	public const int KIEU_TRUOT = 4;

	public const int KIEU_HOI = 5;

	public const int KIEU_NHAN = 6;

	public const int KIEU_LEN_CAP = 7;

	public const string CHU_CHI_MANG = "Chí mạng!";

	public const string CHU_HIEU_QUA_CAO = "Hiệu quả cao!";

	public const string CHU_TRUOT = "Trượt";

	public const string CHU_LEN_CAP = "Lên cấp!";

	public const string CHU_IT_HIEU_QUA = "Ít hiệu quả";

	/// <summary>So tick game nhay trang khi trung don.</summary>
	public const int TICK_NHAY = 5;

	private static readonly Color MAU_DAM = Color.white;

	private static readonly Color MAU_CHI_MANG = new Color(1f, 0.86f, 0.1f);

	private static readonly Color MAU_HIEU_QUA_CAO = new Color(1f, 0.55f, 0.1f);

	private static readonly Color MAU_TRUOT = new Color(0.72f, 0.74f, 0.78f);

	private static readonly Color MAU_HOI = new Color(0.35f, 0.95f, 0.4f);

	private static readonly Color MAU_NHAN = new Color(0.6f, 0.9f, 1f);

	private static readonly Color MAU_LEN_CAP = new Color(1f, 0.8f, 0.25f);

	private static readonly Color MAU_VIEN = new Color(0.08f, 0.1f, 0.16f);

	// Nhan (chi mang / hieu qua cao) va so sat thuong co the den truoc hoac sau nhau trong cung tick.
	private static int tickNhan = -100;

	private static int kieuNhan;

	private static int xNhan;

	private static int yNhan;

	// gợi ý hệ số khắc hệ cho số sát thương kế tiếp (PokeHieuUng.trungDon đặt ngay trước startFlyText)
	private static int goiYHeSo = 100;

	private static int goiYTick = -100;

	/// <summary>Hệ số % (HePoke.heSo: 200 150 100 70 50 30) cho số sát thương sắp bay lên.</summary>
	public static void goiY(int heSo)
	{
		goiYHeSo = heSo;
		goiYTick = Canvas.gameTick;
	}

	private const int GAN_X = 40;

	private const int GAN_Y = 30;

	private const int CUNG_LUC = 3;

	private static bool gan(int x1, int y1, int x2, int y2)
	{
		return Mathf.Abs(x1 - x2) <= GAN_X && Mathf.Abs(y1 - y2) <= GAN_Y;
	}

	private static bool laSo(string s, int tu)
	{
		bool coSo = false;
		for (int i = tu; i < s.Length; i++)
		{
			char c = s[i];
			if (c >= '0' && c <= '9')
			{
				coSo = true;
			}
			else if (c != ' ' && c != '.' && c != ',')
			{
				return false;
			}
		}
		return coSo;
	}

	/// <summary>
	/// Phan loai chu bay len, doi chu hien thi (vd "miss" -> "Trượt"), nhay trang + am thanh.
	/// Tra ve KIEU_GOC de GameScr ve nhu cu.
	/// </summary>
	public static int phanLoai(GameScr gs, ref string chu, int x, int y)
	{
		if (string.IsNullOrEmpty(chu))
		{
			return KIEU_GOC;
		}
		string t = chu.Trim();
		string thuong = t.ToLower();
		switch (thuong)
		{
		case "miss":
			chu = CHU_TRUOT;
			AmThanh.phat(AmThanh.TRUOT);
			return KIEU_TRUOT;
		case "chi mang":
			chu = CHU_CHI_MANG;
			ghiNhan(gs, KIEU_CHI_MANG, x, y);
			AmThanh.phat(AmThanh.CHI_MANG);
			return KIEU_CHI_MANG;
		case "hieu qua cao":
			chu = CHU_HIEU_QUA_CAO;
			ghiNhan(gs, KIEU_HIEU_QUA_CAO, x, y);
			AmThanh.phat(AmThanh.HIEU_QUA_CAO);
			return KIEU_HIEU_QUA_CAO;
		case "xuyen giap":
			chu = "Xuyên giáp!";
			return KIEU_NHAN;
		case "baokich":
			chu = "Bạo kích!";
			return KIEU_NHAN;
		case "level-up":
			chu = CHU_LEN_CAP;
			return KIEU_LEN_CAP;
		}
		if (t.Length > 1 && t[0] == '+' && laSo(t, 1))
		{
			chu = "+" + t.Substring(1).Trim();
			AmThanh.phat(AmThanh.HOI_MAU);
			return KIEU_HOI;
		}
		bool dam = (t.Length > 1 && t[0] == '-' && laSo(t, 1)) || laSo(t, 0);
		if (!dam)
		{
			return KIEU_GOC;
		}
		chu = (t[0] == '-') ? ("-" + t.Substring(1).Trim()) : ("-" + t);
		LiveActor trung = nhayTrungDon(gs, x, y);
		int kieu = KIEU_DAM;
		int d = Canvas.gameTick - tickNhan;
		if (d >= 0 && d <= CUNG_LUC && gan(x, y, xNhan, yNhan))
		{
			kieu = kieuNhan;
		}
		int dg = Canvas.gameTick - goiYTick;
		if (kieu == KIEU_DAM && dg >= 0 && dg <= 1)
		{
			goiYTick = -100;
			if (goiYHeSo >= 150)
			{
				kieu = KIEU_HIEU_QUA_CAO;
				themNhan(gs, CHU_HIEU_QUA_CAO, KIEU_HIEU_QUA_CAO, x, y - 14);
				AmThanh.phat(AmThanh.HIEU_QUA_CAO);
			}
			else if (goiYHeSo <= 70)
			{
				themNhan(gs, CHU_IT_HIEU_QUA, KIEU_TRUOT, x, y - 14);
			}
		}
		if (kieu == KIEU_DAM)
		{
			AmThanh.phat((trung != null && gs.mainChar == trung) ? AmThanh.BI_DANH : AmThanh.DANH);
		}
		return kieu;
	}

	/// <summary>Thêm nhãn chữ (không qua phanLoai) vào ô chữ bay trống của GameScr.</summary>
	private static void themNhan(GameScr gs, string chu, int kieu, int x, int y)
	{
		if (gs == null || gs.flyTextKieu == null)
		{
			return;
		}
		for (int i = 0; i < gs.flyTextState.Length; i++)
		{
			if (gs.flyTextState[i] == -1)
			{
				gs.flyTextString[i] = chu;
				gs.flyTextX[i] = x;
				gs.flyTextY[i] = y;
				gs.flyTextDx[i] = 0;
				gs.flyTextDy[i] = -1;
				gs.flyTextState[i] = 0;
				gs.flyTextColor[i] = 0;
				gs.flyTextKieu[i] = kieu;
				return;
			}
		}
	}

	/// <summary>Nhan den sau so: doi kieu cac so sat thuong vua bay len gan do.</summary>
	private static void ghiNhan(GameScr gs, int kieu, int x, int y)
	{
		tickNhan = Canvas.gameTick;
		kieuNhan = kieu;
		xNhan = x;
		yNhan = y;
		if (gs == null || gs.flyTextKieu == null)
		{
			return;
		}
		for (int i = 0; i < gs.flyTextKieu.Length; i++)
		{
			if (gs.flyTextState[i] != -1 && gs.flyTextState[i] <= 2 * CUNG_LUC && gs.flyTextKieu[i] == KIEU_DAM
				&& gan(x, y, gs.flyTextX[i], gs.flyTextY[i]))
			{
				gs.flyTextKieu[i] = kieu;
			}
		}
	}

	/// <summary>Hit flash: Pokemon gan cho so sat thuong nhat (so bay tu y - 15 .. y - 45 tren chan).</summary>
	private static LiveActor nhayTrungDon(GameScr gs, int x, int y)
	{
		if (gs == null || gs.actors == null)
		{
			return null;
		}
		LiveActor gan = null;
		int tot = int.MaxValue;
		for (int i = 0; i < gs.actors.size(); i++)
		{
			LiveActor a = gs.actors.elementAt(i) as LiveActor;
			if (a == null)
			{
				continue;
			}
			int dx = Mathf.Abs(a.x - x);
			int dy = a.y - y;
			if (dx > 24 || dy < 0 || dy > 70)
			{
				continue;
			}
			int diem = dx * dx + (dy - 20) * (dy - 20);
			if (diem < tot)
			{
				tot = diem;
				gan = a;
			}
		}
		if (gan != null)
		{
			gan.pokeNhayDen = Canvas.gameTick + TICK_NHAY;
		}
		return gan;
	}

	/// <summary>Ve mot chu bay len (state 0..30, cang lon cang mo).</summary>
	public static void ve(MyGraphics g, string chu, int kieu, int x, int y, int state)
	{
		MFont f = MFont.smallFont;
		Color mau = MAU_DAM;
		switch (kieu)
		{
		case KIEU_CHI_MANG:
			mau = MAU_CHI_MANG;
			f = MFont.bigFont ?? MFont.borderFont;
			break;
		case KIEU_HIEU_QUA_CAO:
			mau = MAU_HIEU_QUA_CAO;
			f = MFont.bigFont ?? MFont.borderFont;
			break;
		case KIEU_TRUOT:
			mau = MAU_TRUOT;
			break;
		case KIEU_HOI:
			mau = MAU_HOI;
			break;
		case KIEU_NHAN:
			mau = MAU_NHAN;
			break;
		case KIEU_LEN_CAP:
			mau = MAU_LEN_CAP;
			f = MFont.borderFont ?? MFont.smallFont;
			break;
		}
		if (f == null)
		{
			f = MFont.normalFont[0];
		}
		if (f == null)
		{
			return;
		}
		float alpha = (state > 20) ? Mathf.Clamp01(1f - (state - 20) / 10f) : 1f;
		Color c1 = f.color1;
		Color c2 = f.color2;
		try
		{
			Color vien = MAU_VIEN;
			vien.a = alpha * 0.85f;
			f.color1 = vien;
			f.color2 = vien;
			f._drawString(g, chu, x + 1, y + 1, 2);
			f._drawString(g, chu, x - 1, y + 1, 2);
			f._drawString(g, chu, x, y + 2, 2);
			mau.a = alpha;
			f.color1 = mau;
			f.color2 = mau;
			f._drawString(g, chu, x, y, 2);
		}
		finally
		{
			f.color1 = c1;
			f.color2 = c2;
		}
	}
}
