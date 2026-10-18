using UnityEngine;

/// <summary>
/// Hệ thiết kế thương hiệu HTBB cho mọi menu / cửa sổ bản Unity PC (cùng kiểu với bản pc-java GiaoDienHD và
/// client_jar/shared/brand): viền navy #1E2A44, nhấn vàng #FFCB05 / cam #F08030, dải tiêu đề đỏ #E3350D,
/// bảng kính bo tròn (thân mờ + ánh sáng mép trên + viền sáng trong), chữ trắng viền tối, trạng thái rê chuột.
///
/// Mọi khối vẽ ở cỡ 2 lần rồi thu nhỏ (BucVe.thuNho - khử răng cưa) và giữ ảnh trong bộ nhớ đệm GiaoDien
/// theo khóa, nên mỗi khung chỉ là vài lệnh vẽ ảnh. Các hook "// pokemon: giao diện" trong Res.paintDlg*,
/// Res.paintSelected, MainJ2ME.paint (nút lệnh), MyScreen (thanh lệnh), Menu.paintMenu, TField.paint,
/// MenuWindows / WindowInfoScr (thanh tab) gọi vào đây; BangMauPoke đổi màu tô cũ (vàng chanh, xanh rêu...)
/// sang bảng màu thương hiệu cho các chỗ vẽ lẻ.
/// </summary>
public static class UiPokemon
{
	public const int CAM = 0xF08030;

	public const int CAM_SANG = 0xFFA850;

	public const int CAM_TOI = 0xB85A10;

	private const int K = 2;

	// ------------------------------------------------------------------ vẽ thô ở cỡ K lần

	private static int thut(int r, int i)
	{
		int d = r - i;
		double t = System.Math.Sqrt(System.Math.Max(0, r * r - d * d));
		return r - (int)t;
	}

	/// <summary>Khối bo góc màu argb (có alpha) trên BucVe.</summary>
	private static void khoiA(BucVe b, int x, int y, int w, int h, int r, int argb)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		r = System.Math.Min(r, System.Math.Min(w, h) / 2);
		b.setColorA(argb);
		b.fillRect(x, y + r, w, h - 2 * r);
		for (int i = 0; i < r; i++)
		{
			int t = thut(r, i);
			b.fillRect(x + t, y + i, w - 2 * t, 1);
			b.fillRect(x + t, y + h - 1 - i, w - 2 * t, 1);
		}
	}

	private static int phaA(int a, int b, int t, int n)
	{
		int aa = (int)((uint)a >> 24);
		int ab = (int)((uint)b >> 24);
		int al = aa + (ab - aa) * t / System.Math.Max(1, n);
		return al << 24 | GiaoDien.pha(a & 0xFFFFFF, b & 0xFFFFFF, t, n);
	}

	/// <summary>Khối bo góc dải màu dọc (argb) - chỉ thay màu từng hàng.</summary>
	private static void khoiDocA(BucVe b, int x, int y, int w, int h, int r, int tren, int duoi)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		r = System.Math.Min(r, System.Math.Min(w, h) / 2);
		for (int i = 0; i < h; i++)
		{
			int t = i < r ? thut(r, i) : (i >= h - r ? thut(r, h - 1 - i) : 0);
			b.setColorA(phaA(tren, duoi, i, h));
			b.fillRect(x + t, y + i, w - 2 * t, 1);
		}
	}

	/// <summary>Viền bo góc dày d (argb): khối ngoài trừ khối trong (vẽ bằng 4 dải + góc).</summary>
	private static void vienA(BucVe b, int x, int y, int w, int h, int r, int d, int argb)
	{
		r = System.Math.Min(r, System.Math.Min(w, h) / 2);
		b.setColorA(argb);
		int r2 = System.Math.Max(0, r - d);
		for (int i = 0; i < h; i++)
		{
			int t = i < r ? thut(r, i) : (i >= h - r ? thut(r, h - 1 - i) : 0);
			int ti;
			if (i < d || i >= h - d)
			{
				b.fillRect(x + t, y + i, w - 2 * t, 1);
				continue;
			}
			int j = i - d;
			int hh = h - 2 * d;
			ti = j < r2 ? thut(r2, j) : (j >= hh - r2 ? thut(r2, hh - 1 - j) : 0);
			int trongTrai = x + d + ti;
			int trongPhai = x + w - d - ti;
			b.fillRect(x + t, y + i, trongTrai - (x + t), 1);
			b.fillRect(trongPhai, y + i, x + w - t - trongPhai, 1);
		}
	}

	// ------------------------------------------------------------------ bảng kính

	/// <summary>Ảnh (đã khử răng cưa) của bangKinh - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoBangKinh(int w, int h)
	{
		int W = (w + 2) * K;
		int H = (h + 3) * K;
		BucVe b = new BucVe(W, H);
		int r = 9 * K;
		khoiA(b, 1 * K, 3 * K, w * K, h * K, r, 0x500B1222);
		khoiDocA(b, 0, 0, w * K, h * K, r, unchecked((int)0xEB24508A), unchecked((int)0xF00E2042));
		khoiDocA(b, K, K, (w - 2) * K, System.Math.Min(h / 2, 26) * K, r - K, 0x46FFFFFF, 0x00FFFFFF);
		vienA(b, 0, 0, w * K, h * K, r, 2, unchecked((int)0xFF1E2A44));
		vienA(b, 2, 2, w * K - 4, h * K - 4, r - 2, 1, unchecked((int)0xA096C3FF));
		return b.thuNho(K);
	}

	/// <summary>Bảng kính xanh đêm (chữ trắng): bóng đổ, thân mờ dải navy, ánh sáng mép trên, viền navy + viền trong xanh sáng.</summary>
	public static void bangKinh(MyGraphics g, int x, int y, int w, int h)
	{
		if (w < 8 || h < 8)
		{
			return;
		}
		string k = "uiBK|" + w + "|" + h;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoBangKinh(w, h));
		}
		g.drawImage(a, x, y);
		ghiKhung(g, x, y, w, h);
	}

	/// <summary>Ảnh (đã khử răng cưa) của theKinh - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoTheKinh(int w, int h)
	{
		BucVe b = new BucVe((w + 2) * K, (h + 3) * K);
		int r = 9 * K;
		khoiA(b, 1 * K, 3 * K, w * K, h * K, r, 0x460B1222);
		khoiDocA(b, 0, 0, w * K, h * K, r, unchecked((int)0xF0FFFFFF), unchecked((int)0xE6E8F0FC));
		vienA(b, 0, 0, w * K, h * K, r, 2, unchecked((int)0xFF1E2A44));
		vienA(b, 2, 2, w * K - 4, h * K - 4, r - 2, 1, unchecked((int)0xC8FFFFFF));
		return b.thuNho(K);
	}

	/// <summary>Thẻ kính trắng (chữ tối) - hộp thoại / thông báo trên nền sáng.</summary>
	public static void theKinh(MyGraphics g, int x, int y, int w, int h)
	{
		if (w < 8 || h < 8)
		{
			return;
		}
		string k = "uiTK|" + w + "|" + h;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoTheKinh(w, h));
		}
		g.drawImage(a, x, y);
		ghiKhung(g, x, y, w, h);
	}

	/// <summary>Ảnh (đã khử răng cưa) của thanhDo - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoThanhDo(int w, int h)
	{
		BucVe b = new BucVe(w * K, (h + 2) * K);
		int r = 7 * K;
		khoiDocA(b, 0, 0, w * K, h * K, r, unchecked((int)0xFFF4613A), unchecked((int)0xFFA5240A));
		khoiDocA(b, K, K, (w - 2) * K, h / 2 * K, r - K, 0x50FFFFFF, 0x00FFFFFF);
		b.setColorA(unchecked((int)0xFFFFCB05));
		b.fillRect(4 * K, h * K, (w - 8) * K, K);
		b.setColorA(unchecked((int)0xFF1E2A44));
		b.fillRect(4 * K, (h + 1) * K, (w - 8) * K, K);
		if (h >= 16 && w > 80)
		{
			int rb = (h / 2 - 3) * K;
			GiaoDien.pokeBallB(b, 6 * K + rb, h * K / 2, rb, 0);
		}
		return b.thuNho(K);
	}

	/// <summary>Dải tiêu đề đỏ bo góc trên + vạch vàng dưới + Poké Ball nhỏ, chữ trắng viền.</summary>
	public static void thanhDo(MyGraphics g, int x, int y, int w, int h, string tieuDe)
	{
		if (w < 12 || h < 8)
		{
			return;
		}
		string k = "uiTD|" + w + "|" + h;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoThanhDo(w, h));
		}
		g.drawImage(a, x, y);
		if (!string.IsNullOrEmpty(tieuDe) && MFont.borderFont != null)
		{
			MFont.borderFont.drawString(g, GiaoDien.tieuDe(tieuDe), x + w / 2, y + (h - MFont.borderFont.getHeight()) / 2, MFont.CENTER);
		}
	}

	/// <summary>Bảng kính + dải tiêu đề đỏ (cửa sổ có tên).</summary>
	public static void bangCoTieuDe(MyGraphics g, int x, int y, int w, int h, string tieuDe)
	{
		bangKinh(g, x, y, w, h);
		thanhDo(g, x + 4, y + 4, w - 8, 24, tieuDe);
	}

	// ------------------------------------------------------------------ khung đang mở (nút lệnh nằm trong khung)

	/// <summary>Khung lớn vẽ gần nhất trong khung hình này (tọa độ tuyệt đối) - MainJ2ME đặt nút lệnh vào đây.</summary>
	public static int khungFrame = -1;

	public static int khungX;

	public static int khungY;

	public static int khungW;

	public static int khungH;

	private static void ghiKhung(MyGraphics g, int x, int y, int w, int h)
	{
		if (w < 140 || h < 70)
		{
			return; // thẻ nhỏ (bóng thoại, thẻ thông tin) không phải khung chứa nút
		}
		khungFrame = Time.frameCount;
		khungX = x + g.getTranslateX();
		khungY = y + g.getTranslateY();
		khungW = w;
		khungH = h;
	}

	public static bool coKhung()
	{
		return khungFrame == Time.frameCount;
	}

	/// <summary>Nút đóng tròn đỏ dấu X (góc phải trên khung), d = đường kính.</summary>
	public static void nutDong(MyGraphics g, int x, int y, int d, bool re, bool nhan)
	{
		string k = "uiX|" + d + "|" + (re ? 1 : 0) + "|" + (nhan ? 1 : 0);
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoNutDong(d, re, nhan));
		}
		g.drawImage(a, x, y);
	}

	public static BucVe taoNutDong(int d, bool re, bool nhan)
	{
		int D = d * K;
		BucVe b = new BucVe(D + 2 * K, D + 2 * K);
		if (re)
		{
			b.setColorA(0x70FFCB05);
			b.fillArc(0, 0, D + 2 * K, D + 2 * K, 0, 360);
		}
		b.setColor(GiaoDien.VIEN);
		b.fillArc(K, K, D, D, 0, 360);
		b.setColor(nhan ? GiaoDien.DO_TOI : (re ? 0xFF7A55 : GiaoDien.DO_SANG));
		b.fillArc(K + 2, K + 2, D - 4, D - 4, 0, 360);
		b.setColorA(0x50FFFFFF);
		b.fillArc(K + 5, K + 4, D - 10, D / 2 - 2, 0, 180);
		b.setColor(GiaoDien.TRANG);
		int c = K + D / 2;
		int l = D / 4;
		for (int t = -K; t <= K; t++)
		{
			b.drawLine(c - l + t, c - l, c + l + t, c + l);
			b.drawLine(c + l + t, c - l, c - l + t, c + l);
		}
		return b.thuNho(K);
	}

	/// <summary>Nút "trở lại" (góc trái trên khung): viên navy, mũi tên vàng + chữ.</summary>
	public static void nutTroLai(MyGraphics g, int x, int y, int w, int h, string chu, bool re, bool nhan)
	{
		int m1 = nhan ? 0x1A3B6E : (re ? GiaoDien.XANH_SANG : 0x2D62AA);
		int m2 = nhan ? 0x0E2042 : (re ? GiaoDien.XANH : 0x1A3B6E);
		GiaoDien.khung(g, x, y, w, h, h / 2, GiaoDien.VIEN, m1, m2, false);
		GiaoDien.muiNgang(g, x + 7, y + h / 2, 10, -1, GiaoDien.VANG);
		MFont.borderFont.drawString(g, GiaoDien.catVua(MFont.borderFont, chu, w - 22), x + 14 + (w - 14) / 2, y + (h - MFont.borderFont.getHeight()) / 2, MFont.CENTER);
	}

	// ------------------------------------------------------------------ nút

	public const int NUT_THUONG = 0;

	public const int NUT_RE = 1;

	public const int NUT_NHAN = 2;

	public const int NUT_TAT = 3;

	/// <summary>Ảnh (đã khử răng cưa) của nut - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoNut(int w, int h, int tt)
	{
		BucVe b = new BucVe((w + 4) * K, (h + 4) * K);
		int r = h / 2 * K;
		int ox = 2 * K;
		int oy = 2 * K + (tt == NUT_NHAN ? K : 0);
		if (tt == NUT_RE)
		{
			khoiA(b, 0, 0, (w + 4) * K, (h + 4) * K, r + 2 * K, 0x70FFCB05);
		}
		else if (tt != NUT_NHAN)
		{
			khoiA(b, ox + K, oy + 2 * K, w * K, h * K, r, 0x600B1222);
		}
		int tren = tt == NUT_TAT ? unchecked((int)0xFFB4BDCA) : (tt == NUT_RE ? unchecked((int)0xFFFFE680) : (tt == NUT_NHAN ? unchecked((int)0xFFF0A030) : unchecked((int)0xFFFFD84A)));
		int duoi = tt == NUT_TAT ? unchecked((int)0xFF5E6A80) : (tt == NUT_RE ? unchecked((int)0xFFFF9030) : (tt == NUT_NHAN ? unchecked((int)0xFFB85A10) : unchecked((int)0xFFF08030)));
		khoiDocA(b, ox, oy, w * K, h * K, r, tren, duoi);
		khoiDocA(b, ox + 2 * K, oy + K, (w - 4) * K, h / 2 * K, r - 2 * K, tt == NUT_NHAN ? 0x20FFFFFF : 0x70FFFFFF, 0x00FFFFFF);
		vienA(b, ox, oy, w * K, h * K, r, 2, unchecked((int)0xFF1E2A44));
		return b.thuNho(K);
	}

	/// <summary>Nút viên thuốc: vàng -> cam viền navy (thường), sáng + quầng vàng (rê), tối + lún 1 px (nhấn), xám (tắt).</summary>
	public static void nut(MyGraphics g, int x, int y, int w, int h, string chu, int tt)
	{
		string k = "uiN|" + w + "|" + h + "|" + tt;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoNut(w, h, tt));
		}
		g.drawImage(a, x - 2, y - 2);
		if (!string.IsNullOrEmpty(chu) && MFont.borderFont != null)
		{
			MFont.borderFont.drawString(g, chu, x + w / 2, y + (h - MFont.borderFont.getHeight()) / 2 + (tt == NUT_NHAN ? 1 : 0), MFont.CENTER);
		}
	}

	/// <summary>Trạng thái nút theo chuột (nhan = đang giữ phím / chuột trên nút).</summary>
	public static int trangThaiNut(int x, int y, int w, int h, bool nhan)
	{
		if (nhan)
		{
			return NUT_NHAN;
		}
		return chuotTrong(x, y, w, h) ? NUT_RE : NUT_THUONG;
	}

	public static int xChuot()
	{
		return (int)ScaleGUI.scaleX(Input.mousePosition.x);
	}

	public static int yChuot()
	{
		return (int)ScaleGUI.scaleY(Screen.height - Input.mousePosition.y);
	}

	public static bool chuotTrong(int x, int y, int w, int h)
	{
		int mx = xChuot();
		int my = yChuot();
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	// ------------------------------------------------------------------ ô nhập, hàng chọn, thanh lệnh

	/// <summary>Ảnh (đã khử răng cưa) của oNhap - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoONhap(int w, int h, bool dangGo)
	{
		BucVe b = new BucVe((w + 4) * K, (h + 4) * K);
		int r = 6 * K;
		int o = 2 * K;
		if (dangGo)
		{
			khoiA(b, 0, 0, (w + 4) * K, (h + 4) * K, r + o, 0x60FFCB05);
		}
		khoiDocA(b, o, o, w * K, h * K, r, unchecked((int)0xFFE9EEF6), unchecked((int)0xFFFFFFFF));
		vienA(b, o, o, w * K, h * K, r, dangGo ? 3 : 2, dangGo ? unchecked((int)0xFFFFB000) : unchecked((int)0xFF1E2A44));
		return b.thuNho(K);
	}

	/// <summary>Ô nhập chữ: thân trắng kem, viền navy; đang gõ = viền vàng dày + quầng.</summary>
	public static void oNhap(MyGraphics g, int x, int y, int w, int h, bool dangGo)
	{
		string k = "uiON|" + w + "|" + h + "|" + (dangGo ? 1 : 0);
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoONhap(w, h, dangGo));
		}
		g.drawImage(a, x - 2, y - 2);
	}

	/// <summary>Ảnh (đã khử răng cưa) của hangChon - thuần BucVe, dùng cả cho công cụ xem trước.</summary>
	public static BucVe taoHangChon(int w, int h, bool chon)
	{
		BucVe b = new BucVe(w * K, h * K);
		int r = System.Math.Min(6, h / 2) * K;
		if (chon)
		{
			khoiDocA(b, 0, 0, w * K, h * K, r, unchecked((int)0xFFFFD84A), unchecked((int)0xFFF08030));
			khoiDocA(b, K, K, (w - 2) * K, h / 2 * K, r - K, 0x60FFFFFF, 0x00FFFFFF);
			vienA(b, 0, 0, w * K, h * K, r, 2, unchecked((int)0xFF1E2A44));
		}
		else
		{
			khoiA(b, 0, 0, w * K, h * K, r, 0x386FA6EA);
			vienA(b, 0, 0, w * K, h * K, r, 1, unchecked((int)0x806FA6EA));
		}
		return b.thuNho(K);
	}

	/// <summary>Hàng danh sách đang chọn (viên vàng-cam) hoặc rê chuột (viên xanh sáng mờ).</summary>
	public static void hangChon(MyGraphics g, int x, int y, int w, int h, bool chon)
	{
		string k = "uiHC|" + w + "|" + h + "|" + (chon ? 1 : 0);
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoHangChon(w, h, chon));
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Thanh lệnh đáy màn (thay ảnh imgCmdBar): navy, vạch đỏ + vàng mép trên.</summary>
	public static void thanhLenh(MyGraphics g, int hThanh)
	{
		string k = "uiTL|" + Canvas.w + "|" + hThanh;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(Canvas.w, hThanh);
			for (int i = 0; i < hThanh; i++)
			{
				b.setColorA(phaA(unchecked((int)0xE01A3B6E), unchecked((int)0xF00B1730), i, hThanh));
				b.fillRect(0, i, Canvas.w, 1);
			}
			b.setColor(GiaoDien.DO);
			b.fillRect(0, 0, Canvas.w, 2);
			b.setColor(GiaoDien.VANG);
			b.fillRect(0, 2, Canvas.w, 1);
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, 0, Canvas.h - hThanh);
	}

	/// <summary>Thanh tab: viên tên tab, tab đang chọn đỏ có vạch vàng, tab rê chuột xanh sáng.</summary>
	public static void tab(MyGraphics g, int x, int y, int w, int h, string ten, bool chon, bool re)
	{
		int mau1 = chon ? GiaoDien.DO_SANG : (re ? GiaoDien.XANH_SANG : 0x2D62AA);
		int mau2 = chon ? GiaoDien.DO_TOI : (re ? GiaoDien.XANH : 0x1A3B6E);
		GiaoDien.khung(g, x, y, w, h, 7, GiaoDien.VIEN, mau1, mau2, false);
		if (chon)
		{
			g.setColor(GiaoDien.VANG);
			g.fillRect(x + 6, y + h - 3, w - 12, 2);
		}
		MFont f = chon ? MFont.borderFont : MFont.arialFontW;
		f.drawString(g, GiaoDien.catVua(f, ten, w - 6), x + w / 2, y + (h - f.getHeight()) / 2, MFont.CENTER);
	}
}

/// <summary>
/// Đổi màu tô "kiếm hiệp" cũ của bản gốc (vàng chanh, xanh rêu, nâu vàng, tím than...) sang bảng màu
/// thương hiệu HTBB ở chế độ Pokémon - gọi trong MyGraphics.setColor. Chỉ đổi các màu khung / nền / chọn đã
/// liệt kê; chữ (đen, trắng) và màu nhân vật giữ nguyên.
/// </summary>
public static class BangMauPoke
{
	public static int doi(int rgb)
	{
		switch (rgb)
		{
		case 25695:
			return 0x1A3B6E; // #00645F xanh rêu -> thân navy
		case 16774720:
			return 0xFFCB05; // #FFF640 vàng chanh -> vàng thương hiệu
		case 10595790:
			return 0x6FA6EA; // #A1ADCE chọn tím nhạt -> xanh sáng
		case 6513507:
			return 0x1E2A44; // lưới xám đậm -> viền navy
		case 8684162:
			return 0x3D6AA8; // lưới xám -> xanh vừa
		case 8092027:
			return 0x3D6AA8;
		case 277044:
			return 0x14305A; // #043A34 thân hộp thoại
		case 34949:
			return 0x3D7DCA; // #008885 ô chọn
		case 2181450:
			return 0x1A3B6E; // #214956 nền menu
		case 11495168:
			return UiPokemon.CAM; // #AF6700 chọn menu -> cam
		case 3276851:
			return 0x0E2042; // #320033 tím than
		case 14527502:
			return UiPokemon.CAM; // #DDAC0E nâu vàng
		case 8346120:
			return 0x1E2A44; // #7F5A08 viền nâu
		case 15852810:
			return 0xFFCB05; // #F1E50A
		case 14595691:
			return 0xF4613A; // Res.color[3]
		case 11241794:
			return 0x3D7DCA; // Res.color[4]
		case 4858880:
			return 0x003A70; // Res.color[5]
		case 7278866:
			return 0xA5240A; // #6F1112 đỏ đậm
		case 6568449:
			return 0x5E3A10;
		case 1379840:
			return 0x1A3B6E; // ô chọn nhân vật
		default:
			return rgb;
		}
	}
}
