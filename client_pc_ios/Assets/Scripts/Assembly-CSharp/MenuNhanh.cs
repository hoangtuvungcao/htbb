using UnityEngine;

/// <summary>
/// Menu nhanh của mod - bản PC của game.render.screen.MenuNhanh (bản jar). Ở chế độ Pokémon thay hai cột
/// nút tròn 52 px (Up / Tiêu / PK / Mã PK / Nháy / ĐS / Dừng / Đổi pet - AutoGuard.veNutUp) từng che nửa
/// phải màn chơi: chỉ còn MỘT Poké Ball nhỏ sát mép trái; bấm (hoặc F3) mở bảng nút dạng viên thuốc:
/// xanh lá = đang bật, xám = tắt, xanh dương = lệnh làm ngay, đỏ = dừng hết. Đóng: nút x, bấm ra ngoài,
/// F3 hoặc Esc. Chuột rê = viền vàng; phím mũi tên + Enter cũng dùng được. "Tự ẩn nút": Poké Ball ẩn
/// trong lúc nhân vật đang đánh. Gọi từ AutoGuard.veNutUp / nutUpTick và Canvas.mapKeyPress.
/// </summary>
public static class MenuNhanh
{
	public const int PHIM_F3 = -24;

	public const int PHIM_ESC = -26;

	public static bool mo;

	private static int chon;

	private static bool anKhiDanh;

	private static bool daNap;

	private static long lucDanh;

	private static bool dungPhim;

	private const int LUYEN = 0;
	private const int GIAO = 1;
	private const int TU_PK = 2;
	private const int MA_PK = 3;
	private const int NHAY = 4;
	private const int CO_PK = 5;
	private const int DOI_PET = 6;
	private const int DUNG = 7;
	private const int AN = 8;

	private static readonly string[] TEN = { "Luyện cấp", "Giao hàng", "Tự PK", "Mã PK", "Nhảy vùng", "Bật cờ PK", "Đổi Pet", "Dừng hết", "Tự ẩn nút" };

	private static readonly int[] DS_DU = { LUYEN, GIAO, TU_PK, MA_PK, NHAY, CO_PK, DOI_PET, DUNG, AN };

	private static readonly int[] DS_GON = { LUYEN, GIAO, TU_PK, DUNG, AN };

	public static bool bat()
	{
		return HudPokemon.bat();
	}

	private static void nap()
	{
		if (daNap)
		{
			return;
		}
		daNap = true;
		try
		{
			anKhiDanh = PlayerPrefs.GetInt("htbbAnNut", 0) == 1;
		}
		catch (System.Exception)
		{
		}
	}

	private static int[] dsMuc()
	{
		return AutoPk.hienNut ? DS_DU : DS_GON;
	}

	/// <summary>1 = đang bật, 0 = tắt, -1 = lệnh, -2 = lệnh dừng (đỏ).</summary>
	private static int trangThai(int m)
	{
		switch (m)
		{
		case LUYEN:
			return AutoUp.dang ? 1 : 0;
		case GIAO:
			return AutoVanTieu.dang ? 1 : 0;
		case TU_PK:
			return AutoPk.danh() ? 1 : 0;
		case MA_PK:
			return AutoPk.maPk ? 1 : 0;
		case AN:
			return anKhiDanh ? 1 : 0;
		case DUNG:
			return -2;
		default:
			return -1;
		}
	}

	private static void lam(int m)
	{
		AmThanh.phat(AmThanh.BAM);
		switch (m)
		{
		case LUYEN:
			AutoUp.batTat();
			break;
		case GIAO:
			AutoVanTieu.batTat();
			break;
		case TU_PK:
			AutoPk.batTat();
			break;
		case MA_PK:
			AutoPk.nutMaPk();
			break;
		case NHAY:
			mo = false;
			AutoPk.nutNhay();
			break;
		case CO_PK:
			AutoPk.nutDs();
			break;
		case DOI_PET:
			AutoPk.nutDoiPet();
			break;
		case DUNG:
			AutoPk.nutDung();
			break;
		case AN:
			anKhiDanh = !anKhiDanh;
			try
			{
				PlayerPrefs.SetInt("htbbAnNut", anKhiDanh ? 1 : 0);
			}
			catch (System.Exception)
			{
			}
			break;
		}
	}

	private static bool dangChay()
	{
		return AutoUp.dang || AutoVanTieu.dang || AutoPk.dang();
	}

	private static bool dungManChoi()
	{
		return Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr;
	}

	// ------------------------------------------------------------------ vị trí (màn logic PC)

	private const int R_BONG = 15;

	private static int xBong()
	{
		return R_BONG + 6;
	}

	/// <summary>Mép trái, dưới khung HP và chữ phụ, trên tên vùng.</summary>
	private static int yBong()
	{
		return Canvas.h * 9 / 20;
	}

	private static bool anBong()
	{
		if (!anKhiDanh || Canvas.gameScr == null)
		{
			return false;
		}
		Char c = Canvas.gameScr.mainChar;
		long now = mSystem.currentTimeMillis();
		if (c != null && c.state == Char.A_ATTACK)
		{
			lucDanh = now;
		}
		return now - lucDanh < 3000L;
	}

	private const int COT = 3;

	private const int RONG_O = 128;

	private static int caoO()
	{
		return MFont.blackFont.getHeight() + 12;
	}

	private static int rongBang()
	{
		return COT * RONG_O + (COT - 1) * 6 + 20;
	}

	private static int caoBang(int n)
	{
		int hang = (n + COT - 1) / COT;
		return 36 + hang * (caoO() + 6) + 8;
	}

	private static int xBang()
	{
		return (Canvas.w - rongBang()) / 2;
	}

	private static int yBang(int n)
	{
		int y = (Canvas.h - caoBang(n)) / 2;
		return y < 4 ? 4 : y;
	}

	private static int xO(int i)
	{
		return xBang() + 10 + i % COT * (RONG_O + 6);
	}

	private static int yO(int i, int n)
	{
		return yBang(n) + 36 + i / COT * (caoO() + 6);
	}

	private static bool trong(int x, int y, int x0, int y0, int w, int h)
	{
		return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
	}

	private static bool trongBong(int x, int y)
	{
		int r = R_BONG + 6;
		return trong(x, y, xBong() - r, yBong() - r, 2 * r, 2 * r);
	}

	private static int xChuot()
	{
		return (int)ScaleGUI.scaleX(Input.mousePosition.x);
	}

	private static int yChuot()
	{
		return (int)ScaleGUI.scaleY(Screen.height - Input.mousePosition.y);
	}

	// ------------------------------------------------------------------ vẽ

	public static void ve(MyGraphics g)
	{
		nap();
		if (!dungManChoi())
		{
			mo = false;
			return;
		}
		if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode)
		{
			return;
		}
		Canvas.resetTrans(g);
		if (!mo)
		{
			if (anBong())
			{
				return;
			}
			bool re = trongBong(xChuot(), yChuot());
			GiaoDien.pokeBall(g, xBong(), yBong(), R_BONG, re ? (int)(Canvas.gameTick * 15 % 360) : 0);
			if (dangChay() && Canvas.gameTick % 16 < 12)
			{
				GiaoDien.tron(g, xBong() + R_BONG - 2, yBong() - R_BONG + 2, 4, 0x7CFC6C, GiaoDien.VIEN);
			}
			if (re)
			{
				MFont.borderFont.drawString(g, "Menu nhanh (F3)", xBong() + R_BONG + 6, yBong() - 7, MFont.LEFT);
			}
			return;
		}
		int[] ds = dsMuc();
		int n = ds.Length;
		if (chon >= n)
		{
			chon = n - 1;
		}
		int x0 = xBang();
		int y0 = yBang(n);
		int w = rongBang();
		GiaoDien.bang(g, x0, y0, w, caoBang(n));
		GiaoDien.dai(g, x0 + 4, y0 + 4, w - 8, 24, "Menu nhanh");
		int xd = x0 + w - 28;
		GiaoDien.tron(g, xd + 9, y0 + 16, 9, GiaoDien.TRANG, GiaoDien.VIEN);
		MFont.blackFont.drawString(g, "x", xd + 9, y0 + 8, MFont.CENTER);
		int wO = RONG_O;
		int hO = caoO();
		int xm = xChuot();
		int ym = yChuot();
		for (int i = 0; i < n; i++)
		{
			int m = ds[i];
			int tt = trangThai(m);
			int x = xO(i);
			int y = yO(i, n);
			bool re = trong(xm, ym, x, y, wO, hO);
			if (re)
			{
				chon = i;
				dungPhim = false;
			}
			if (i == chon && (dungPhim || re))
			{
				GiaoDien.khoi(g, x - 3, y - 3, wO + 6, hO + 6, hO / 2 + 3, GiaoDien.VANG);
			}
			int mau = tt == 1 ? GiaoDien.LA_TOI : (tt == 0 ? 0xD8DEE8 : (tt == -2 ? GiaoDien.DO : GiaoDien.XANH));
			int mau2 = tt == 1 ? GiaoDien.LA : (tt == 0 ? GiaoDien.TRANG : (tt == -2 ? GiaoDien.DO_SANG : GiaoDien.XANH_SANG));
			GiaoDien.khung(g, x, y, wO, hO, hO / 2, GiaoDien.VIEN, mau2, mau, false);
			int xChu = x + 12;
			if (tt >= 0)
			{
				GiaoDien.tron(g, x + 12, y + hO / 2, 5, tt == 1 ? 0x9CFF8C : 0x8A94A6, GiaoDien.VIEN);
				xChu = x + 22;
			}
			MFont f = tt == 0 ? MFont.blackFont : MFont.borderFont;
			int yChu = y + (hO - f.getHeight()) / 2;
			if (tt < 0)
			{
				f.drawString(g, TEN[m], x + wO / 2, yChu, MFont.CENTER);
			}
			else
			{
				f.drawString(g, TEN[m], xChu, yChu, MFont.LEFT);
			}
		}
		MFont.arialFontW.drawString(g, "Chuột / mũi tên + Enter: chọn   -   F3 / Esc: đóng", x0 + w / 2, y0 + caoBang(n) - 2, MFont.CENTER);
	}

	// ------------------------------------------------------------------ chuột

	/// <summary>Chuột: bấm Poké Ball mở bảng; bảng mở thì nuốt mọi cú bấm (nhân vật không chạy theo).</summary>
	public static void tick()
	{
		nap();
		if (!dungManChoi())
		{
			mo = false;
			return;
		}
		if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode)
		{
			return;
		}
		if (!mo)
		{
			if (anBong())
			{
				return;
			}
			if (Canvas.isPointerDown && trongBong(Canvas.px, Canvas.py))
			{
				Canvas.isPointerDown = false;
			}
			if (Canvas.isPointerClick && trongBong(Canvas.px, Canvas.py) && trongBong(Canvas.pxLast, Canvas.pyLast))
			{
				Canvas.isPointerClick = false;
				Canvas.isPointerDown = false;
				moBang(false);
			}
			return;
		}
		bool click = Canvas.isPointerClick;
		Canvas.isPointerDown = false;
		Canvas.isPointerClick = false;
		if (!click)
		{
			return;
		}
		dungPhim = false;
		int[] ds = dsMuc();
		int n = ds.Length;
		int x0 = xBang();
		int y0 = yBang(n);
		int w = rongBang();
		if (!trong(Canvas.px, Canvas.py, x0, y0, w, caoBang(n)) || trong(Canvas.px, Canvas.py, x0 + w - 32, y0, 32, 32))
		{
			mo = false;
			return;
		}
		for (int i = 0; i < n; i++)
		{
			if (trong(Canvas.px, Canvas.py, xO(i), yO(i, n), RONG_O, caoO()))
			{
				chon = i;
				lam(ds[i]);
				return;
			}
		}
	}

	/// <summary>Điểm (x, y) thuộc Poké Ball / bảng đang mở (AutoUp không coi là người chơi chạm để dừng).</summary>
	public static bool trongNut(int x, int y)
	{
		if (!bat() || !dungManChoi())
		{
			return false;
		}
		if (mo)
		{
			return true;
		}
		return !anBong() && trongBong(x, y);
	}

	public static void moBang(bool bangPhim)
	{
		nap();
		mo = true;
		chon = 0;
		dungPhim = bangPhim;
		AmThanh.phat(AmThanh.BAM);
	}

	// ------------------------------------------------------------------ phím

	public static bool phim(int keyCode)
	{
		if (!bat())
		{
			return false;
		}
		if (!dungManChoi() || Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode)
		{
			mo = false;
			return false;
		}
		if (!mo)
		{
			if (keyCode == PHIM_F3)
			{
				moBang(true);
				return true;
			}
			return false;
		}
		int[] ds = dsMuc();
		int n = ds.Length;
		dungPhim = true;
		switch (keyCode)
		{
		case PHIM_F3:
		case PHIM_ESC:
		case -7:
		case -22:
			mo = false;
			return true;
		case -5:
		case 10:
		case 32:
			lam(ds[chon]);
			return true;
		case -1:
			chon = (chon - COT + n) % n;
			return true;
		case -2:
			chon = (chon + COT) % n;
			return true;
		case -3:
			chon = (chon - 1 + n) % n;
			return true;
		case -4:
			chon = (chon + 1) % n;
			return true;
		}
		return true;
	}
}
