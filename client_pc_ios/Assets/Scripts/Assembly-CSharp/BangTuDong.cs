using UnityEngine;

/// <summary>
/// Bảng "Tự động đánh" bản PC (bản jar: game.render.screen.BangTuDong) kiểu Pokémon như Menu nhanh: Bắt đầu / Dừng,
/// thứ tự 4 chiêu, ngưỡng thuốc HP / MP, lọc nhặt đồ, về hồi phục, vùng đánh; dòng trên cùng là giờ còn lại hôm
/// nay do MÁY CHỦ báo (lệnh -100). Logic + lưu cài đặt ở TuDong. Mở từ Menu nhanh, nút "Tự động", tab Khác ->
/// "Tự động đánh", hoặc bảng Auto gốc. Đóng: nút x, bấm ra ngoài, F3 / Esc.
/// </summary>
public static class BangTuDong
{
	public static bool dangMo;

	private static int chon;

	private static bool dungPhim;

	private const int BAT = 0, CHIEU = 1, HP = 5, MP = 6, NHAT = 7, HOI = 8, VUNG = 9, HOI_SINH = 10;

	private const int SO_MUC = 11;

	private static readonly int[] HP_MUC = new int[] { 0, 30, 50, 70 };

	private static readonly int[] MP_MUC = new int[] { 0, 20, 30, 50 };

	private const int COT = 2;

	private const int RONG_O = 190;

	/// <summary>Mở bảng (và xin máy chủ trạng thái mới).</summary>
	public static void mo()
	{
		TuDong.nap();
		MenuNhanh.mo = false;
		dangMo = true;
		chon = 0;
		dungPhim = false;
		TuDong.xinTrangThai();
		AmThanh.phat(AmThanh.BAM);
	}

	private static MainChar mc()
	{
		return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
	}

	private static string tenChieu(int s)
	{
		if (s < 0)
		{
			return "tự chọn";
		}
		MainChar m = mc();
		string t = m == null ? null : PokeChieu.ten(m.clazz, s);
		return t ?? ("chiêu " + (s + 1));
	}

	private static string ten(int m)
	{
		switch (m)
		{
		case BAT:
			return (TuDong.dang || TuDong.choBat) ? "Dừng tự động" : "Bắt đầu";
		case HP:
			if (!TuDong.choThuoc())
			{
				return "Thuốc: máy chủ tắt";
			}
			return TuDong.nguongHp <= 0 ? "Thuốc HP: tắt" : "Thuốc HP < " + TuDong.nguongHp + "%";
		case MP:
			if (!TuDong.choThuoc())
			{
				return "Thuốc MP: máy chủ tắt";
			}
			return TuDong.nguongMp <= 0 ? "Thuốc MP: tắt" : "Thuốc MP < " + TuDong.nguongMp + "%";
		case NHAT:
			return "Nhặt: " + TuDong.TEN_NHAT[TuDong.nhat];
		case HOI:
			return TuDong.veHoiPhuc ? "Về hồi phục: bật" : "Về hồi phục: tắt";
		case VUNG:
			return "Vùng: " + TuDong.banKinh() / 16 + " ô";
		case HOI_SINH:
			return TuDong.tuHoiSinh ? "Tự hồi sinh: bật" : "Tự hồi sinh: tắt";
		default:
			return (m - CHIEU + 1) + ". " + tenChieu(TuDong.thuTu[m - CHIEU]);
		}
	}

	/// <summary>1 bật (xanh lá), 0 tắt (xám), -1 lệnh (xanh dương), -2 dừng (đỏ).</summary>
	private static int trangThai(int m)
	{
		switch (m)
		{
		case BAT:
			return (TuDong.dang || TuDong.choBat) ? -2 : 1;
		case HP:
			return (TuDong.choThuoc() && TuDong.nguongHp > 0) ? 1 : 0;
		case MP:
			return (TuDong.choThuoc() && TuDong.nguongMp > 0) ? 1 : 0;
		case NHAT:
			return TuDong.nhat > 0 ? 1 : 0;
		case HOI:
			return TuDong.veHoiPhuc ? 1 : 0;
		case HOI_SINH:
			return TuDong.tuHoiSinh ? 1 : 0;
		default:
			return -1;
		}
	}

	private static int sau(int[] ds, int v)
	{
		for (int i = 0; i < ds.Length; i++)
		{
			if (ds[i] == v)
			{
				return ds[(i + 1) % ds.Length];
			}
		}
		return ds[0];
	}

	private static void doiChieu(int i)
	{
		sbyte[] ds = TuDong.chieuDanh(mc());
		int hien = TuDong.thuTu[i];
		int vt = -1;
		for (int k = 0; k < ds.Length; k++)
		{
			if (ds[k] == hien)
			{
				vt = k;
			}
		}
		for (int buoc = vt + 1; buoc <= ds.Length; buoc++)
		{
			if (buoc == ds.Length)
			{
				TuDong.thuTu[i] = -1;
				return;
			}
			bool trung = false;
			for (int j = 0; j < TuDong.thuTu.Length; j++)
			{
				trung |= j != i && TuDong.thuTu[j] == ds[buoc];
			}
			if (!trung)
			{
				TuDong.thuTu[i] = ds[buoc];
				return;
			}
		}
		TuDong.thuTu[i] = -1;
	}

	private static void lam(int m)
	{
		AmThanh.phat(AmThanh.BAM);
		switch (m)
		{
		case BAT:
			TuDong.batTat();
			if (TuDong.choBat)
			{
				dangMo = false;
			}
			return;
		case HP:
			if (TuDong.choThuoc())
			{
				TuDong.nguongHp = sau(HP_MUC, TuDong.nguongHp);
			}
			break;
		case MP:
			if (TuDong.choThuoc())
			{
				TuDong.nguongMp = sau(MP_MUC, TuDong.nguongMp);
			}
			break;
		case NHAT:
			TuDong.nhat = (TuDong.nhat + 1) % TuDong.TEN_NHAT.Length;
			break;
		case HOI:
			TuDong.veHoiPhuc = !TuDong.veHoiPhuc;
			break;
		case HOI_SINH:
			TuDong.tuHoiSinh = !TuDong.tuHoiSinh;
			break;
		case VUNG:
		{
			int toiDa = TuDong.svBanKinh > 0 ? TuDong.svBanKinh / 16 : 99;
			int v = sau(TuDong.VUNG_O, TuDong.vungO);
			if (v > toiDa)
			{
				v = TuDong.VUNG_O[0];
			}
			TuDong.vungO = v;
			break;
		}
		default:
			doiChieu(m - CHIEU);
			break;
		}
		TuDong.luu();
	}

	// ------------------------------------------------------------------ bố cục (màn logic PC)

	private static int caoO()
	{
		return MFont.blackFont.getHeight() + 12;
	}

	private static int caoChu()
	{
		return MFont.borderFont.getHeight() + 2;
	}

	private static int rongBang()
	{
		return COT * RONG_O + (COT - 1) * 6 + 20;
	}

	private static int hangCua(int m)
	{
		return m == BAT ? 0 : 1 + (m - 1) / COT;
	}

	private static int dauBang()
	{
		return 36 + 2 * caoChu() + 4;
	}

	private static int caoBang()
	{
		return dauBang() + (hangCua(SO_MUC - 1) + 1) * (caoO() + 6) + 22;
	}

	private static int xBang()
	{
		return (Canvas.w - rongBang()) / 2;
	}

	private static int yBang()
	{
		int y = (Canvas.h - caoBang()) / 2;
		return y < 4 ? 4 : y;
	}

	private static int xO(int m)
	{
		if (m == BAT)
		{
			return xBang() + 10;
		}
		return xBang() + 10 + (m - 1) % COT * (RONG_O + 6);
	}

	private static int yO(int m)
	{
		return yBang() + dauBang() + hangCua(m) * (caoO() + 6);
	}

	private static int rongMuc(int m)
	{
		return m == BAT ? rongBang() - 20 : RONG_O;
	}

	private static bool trong(int x, int y, int x0, int y0, int w, int h)
	{
		return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
	}

	private static int xChuot()
	{
		return (int)ScaleGUI.scaleX(Input.mousePosition.x);
	}

	private static int yChuot()
	{
		return (int)ScaleGUI.scaleY(Screen.height - Input.mousePosition.y);
	}

	private static string cat(MFont f, string s, int w)
	{
		if (s == null)
		{
			return string.Empty;
		}
		string kq = s;
		while (kq.Length > 4 && f.getWidth(kq) > w)
		{
			kq = kq.Substring(0, kq.Length - 2) + "…";
			if (f.getWidth(kq) <= w)
			{
				break;
			}
			kq = kq.Substring(0, kq.Length - 1);
		}
		return kq;
	}

	// ------------------------------------------------------------------ vẽ

	public static void ve(MyGraphics g)
	{
		Canvas.resetTrans(g);
		int x0 = xBang();
		int y0 = yBang();
		int w = rongBang();
		int hB = caoBang();
		GiaoDien.bang(g, x0, y0, w, hB);
		GiaoDien.dai(g, x0 + 4, y0 + 4, w - 8, 24, "Tự động đánh");
		int xd = x0 + w - 28;
		GiaoDien.tron(g, xd + 9, y0 + 16, 9, GiaoDien.TRANG, GiaoDien.VIEN);
		MFont.blackFont.drawString(g, "x", xd + 9, y0 + 8, MFont.CENTER);
		string d1;
		if (!TuDong.coServer)
		{
			d1 = mSystem.currentTimeMillis() - TuDong.lucXin > 5000L ? "Máy chủ chưa hỗ trợ Tự động đánh" : "Đang hỏi máy chủ...";
		}
		else if ((TuDong.svCo & TuDong.CO_TINH_NANG) == 0)
		{
			d1 = "Máy chủ đang tạm khoá";
		}
		else
		{
			d1 = "Hôm nay còn " + TuDong.gio(TuDong.conLai()) + " / " + TuDong.gio(TuDong.svToiDa);
		}
		string d2 = TuDong.dang ? "Đang chạy: " + TuDong.trangThai : TuDong.thongBao;
		if (string.IsNullOrEmpty(d2))
		{
			d2 = "Chỉ khi online - EXP theo năng lượng";
		}
		int yChu = y0 + 34;
		MFont.borderFont.drawString(g, cat(MFont.borderFont, d1, w - 20), x0 + w / 2, yChu, MFont.CENTER);
		MFont.blackFont.drawString(g, cat(MFont.blackFont, d2, w - 20), x0 + w / 2, yChu + caoChu(), MFont.CENTER);
		int hO = caoO();
		int xm = xChuot();
		int ym = yChuot();
		for (int m = 0; m < SO_MUC; m++)
		{
			int tt = trangThai(m);
			int x = xO(m);
			int y = yO(m);
			int wO = rongMuc(m);
			bool re = trong(xm, ym, x, y, wO, hO);
			if (re)
			{
				chon = m;
				dungPhim = false;
			}
			if (m == chon && (dungPhim || re))
			{
				GiaoDien.khoi(g, x - 3, y - 3, wO + 6, hO + 6, hO / 2 + 3, GiaoDien.VANG);
			}
			int mau = tt == 1 ? GiaoDien.LA_TOI : (tt == 0 ? 0xD8DEE8 : (tt == -2 ? GiaoDien.DO : GiaoDien.XANH));
			int mau2 = tt == 1 ? GiaoDien.LA : (tt == 0 ? GiaoDien.TRANG : (tt == -2 ? GiaoDien.DO_SANG : GiaoDien.XANH_SANG));
			GiaoDien.khung(g, x, y, wO, hO, hO / 2, GiaoDien.VIEN, mau2, mau, false);
			MFont f = tt == 0 ? MFont.blackFont : MFont.borderFont;
			f.drawString(g, cat(f, ten(m), wO - 16), x + wO / 2, y + (hO - f.getHeight()) / 2, MFont.CENTER);
		}
		MFont.arialFontW.drawString(g, "Chuột / mũi tên + Enter: chọn   -   F3 / Esc: đóng", x0 + w / 2, y0 + hB - 18, MFont.CENTER);
	}

	// ------------------------------------------------------------------ chuột

	public static void tick()
	{
		bool click = Canvas.isPointerClick;
		Canvas.isPointerDown = false;
		Canvas.isPointerClick = false;
		if (!click)
		{
			return;
		}
		dungPhim = false;
		int x0 = xBang();
		int y0 = yBang();
		int w = rongBang();
		if (!trong(Canvas.px, Canvas.py, x0, y0, w, caoBang()) || trong(Canvas.px, Canvas.py, x0 + w - 32, y0, 32, 32))
		{
			dangMo = false;
			return;
		}
		for (int m = 0; m < SO_MUC; m++)
		{
			if (trong(Canvas.px, Canvas.py, xO(m), yO(m), rongMuc(m), caoO()))
			{
				chon = m;
				lam(m);
				return;
			}
		}
	}

	// ------------------------------------------------------------------ phím

	public static bool phim(int keyCode)
	{
		dungPhim = true;
		switch (keyCode)
		{
		case MenuNhanh.PHIM_F3:
		case MenuNhanh.PHIM_ESC:
		case -7:
		case -22:
			dangMo = false;
			return true;
		case -5:
		case 10:
		case 32:
			lam(chon);
			return true;
		case -1:
			chon = chon == BAT ? SO_MUC - 1 : (chon - COT < 1 ? BAT : chon - COT);
			return true;
		case -2:
			chon = chon == BAT ? 1 : (chon + COT >= SO_MUC ? BAT : chon + COT);
			return true;
		case -3:
			chon = (chon - 1 + SO_MUC) % SO_MUC;
			return true;
		case -4:
			chon = (chon + 1) % SO_MUC;
			return true;
		}
		return true;
	}

	// ------------------------------------------------------------------ thanh trạng thái (dưới khung HP)

	public static void veThanh(MyGraphics g, string s, int y)
	{
		if (Canvas.currentScreen != Canvas.gameScr || dangMo)
		{
			return;
		}
		Canvas.resetTrans(g);
		MFont f = MFont.borderFont;
		string c = cat(f, s, Canvas.w / 2);
		int w = f.getWidth(c) + 20;
		int h = f.getHeight() + 6;
		int x = 6;
		GiaoDien.khung(g, x, y, w, h, h / 2, GiaoDien.VIEN, TuDong.dang ? GiaoDien.LA : GiaoDien.XANH_SANG, TuDong.dang ? GiaoDien.LA_TOI : GiaoDien.XANH, false);
		f.drawString(g, c, x + 10, y + 3, MFont.LEFT);
	}
}
