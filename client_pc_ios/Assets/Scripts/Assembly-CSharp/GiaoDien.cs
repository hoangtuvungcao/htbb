using System.Collections.Generic;

/// <summary>
/// Bộ vẽ giao diện kiểu Pokémon cho bản PC - bản C# của game.pokemon.GiaoDien (bản jar, client_jar/src).
/// Cùng bảng màu với web HTBB-Web: đỏ Poké #E3350D, vàng Pikachu #FFCB05, xanh #3D7DCA / navy #003A70,
/// xanh lá #78C850, viền tối #1E2A44.
///
/// Khác bản jar: hình (khối bo góc, dải màu, Poké Ball...) vẽ MỘT lần vào BucVe rồi giữ ảnh trong bộ
/// nhớ đệm theo khóa (loại|cỡ|màu); mỗi khung chỉ vẽ ảnh (1 lệnh) thay vì hàng chục fillRect IMGUI.
/// Ảnh lọc Point nên phóng theo cửa sổ (ScaleGUI) vẫn sắc nét. Phần thay đổi liên tục (thanh HP) vẽ
/// thẳng bằng fillRect (vài lệnh). Các hàm *B(BucVe ...) là bản chép nguyên văn từ jar.
/// </summary>
public static class GiaoDien
{
	public const int DO = 0xE3350D;
	public const int DO_SANG = 0xF4613A;
	public const int DO_TOI = 0xA5240A;
	public const int VANG = 0xFFCB05;
	public const int VANG_NHAT = 0xFFE680;
	public const int VANG_TOI = 0xB98A00;
	public const int XANH = 0x3D7DCA;
	public const int XANH_SANG = 0x6FA6EA;
	public const int XANH_NHAT = 0xCFE0F5;
	public const int NAVY = 0x003A70;
	public const int LA = 0x78C850;
	public const int LA_TOI = 0x3E8A30;
	public const int VIEN = 0x1E2A44;
	public const int TRANG = 0xFFFFFF;
	public const int KEM = 0xF5F8FC;
	public const int XAM = 0xB4BDCA;
	public const int XAM_TOI = 0x5E6A80;
	public const int THAN_TREN = 0x2D62AA;
	public const int THAN_DUOI = 0x1A3B6E;
	public const int HP_XANH = 0x48D058;
	public const int HP_VANG = 0xF8C020;
	public const int HP_DO = 0xF04830;
	public const int MP_XANH = 0x3FA0F0;
	public const int EXP_XANH = 0x40C8F8;
	public const int BONG = 0x0B1222;

	/// <summary>độ thụt từng hàng của góc bo tròn bán kính r (1..12)</summary>
	private static readonly byte[][] GOC = new byte[13][];

	private static readonly int[] SIN15 = { 0, 265, 512, 724, 887, 989, 1024 };

	static GiaoDien()
	{
		for (int r = 1; r < GOC.Length; r++)
		{
			GOC[r] = new byte[r];
			for (int i = 0; i < r; i++)
			{
				int d = r - i;
				GOC[r][i] = (byte)(r - canBac2(r * r - d * d));
			}
		}
	}

	// ================================================================== bộ nhớ đệm ảnh

	private const int TOI_DA_ANH = 400;

	private static readonly Dictionary<string, Image> dem = new Dictionary<string, Image>();

	/// <summary>Ảnh đã tạo theo khóa (null = chưa có).</summary>
	public static Image layAnh(string khoa)
	{
		Image a;
		return dem.TryGetValue(khoa, out a) ? a : null;
	}

	/// <summary>Đóng BucVe thành ảnh và giữ theo khóa (đầy thì xóa hết - ảnh giao diện tạo lại rất nhanh).</summary>
	public static Image luuAnh(string khoa, BucVe b)
	{
		if (dem.Count >= TOI_DA_ANH)
		{
			foreach (Image cu in dem.Values)
			{
				if (cu != null && cu.texture != null)
				{
					UnityEngine.Object.Destroy(cu.texture);
				}
			}
			dem.Clear();
		}
		Image a = b.taoAnh();
		dem[khoa] = a;
		return a;
	}

	// ================================================================== tiện ích

	private static int canBac2(int n)
	{
		if (n <= 0)
		{
			return 0;
		}
		int x = 0;
		while ((x + 1) * (x + 1) <= n)
		{
			x++;
		}
		return x;
	}

	/// <summary>Pha màu a -> b theo t / n.</summary>
	public static int pha(int a, int b, int t, int n)
	{
		if (n <= 0 || a == b)
		{
			return a;
		}
		if (t < 0)
		{
			t = 0;
		}
		else if (t > n)
		{
			t = n;
		}
		int r = (a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t / n;
		int gg = (a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t / n;
		int c = (a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t / n;
		return r << 16 | gg << 8 | c;
	}

	private static int thut(int r, int hang, int h)
	{
		int i = hang < r ? hang : (hang >= h - r ? h - 1 - hang : -1);
		return i < 0 ? 0 : GOC[r][i];
	}

	private static int banKinh(int r, int w, int h)
	{
		if (r > w / 2)
		{
			r = w / 2;
		}
		if (r > h / 2)
		{
			r = h / 2;
		}
		if (r >= GOC.Length)
		{
			r = GOC.Length - 1;
		}
		return r < 0 ? 0 : r;
	}

	// ================================================================== vẽ vào BucVe (chép từ jar)

	public static void khoiB(BucVe g, int x, int y, int w, int h, int r, int mau)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		g.setColor(mau);
		r = banKinh(r, w, h);
		if (r <= 0)
		{
			g.fillRect(x, y, w, h);
			return;
		}
		g.fillRect(x, y + r, w, h - 2 * r);
		for (int i = 0; i < r; i++)
		{
			int t = GOC[r][i];
			g.fillRect(x + t, y + i, w - 2 * t, 1);
			g.fillRect(x + t, y + h - 1 - i, w - 2 * t, 1);
		}
	}

	public static void khoiDocB(BucVe g, int x, int y, int w, int h, int r, int tren, int duoi)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		if (tren == duoi)
		{
			khoiB(g, x, y, w, h, r, tren);
			return;
		}
		r = banKinh(r, w, h);
		for (int i = 0; i < h; i++)
		{
			int t = r > 0 ? thut(r, i, h) : 0;
			g.setColor(pha(tren, duoi, i, h));
			g.fillRect(x + t, y + i, w - 2 * t, 1);
		}
	}

	public static void hopB(BucVe g, int x, int y, int w, int h)
	{
		khoiB(g, x + 1, y + 2, w, h, 6, BONG);
		khoiB(g, x, y, w, h, 6, VIEN);
		khoiB(g, x + 2, y + 2, w - 4, h - 4, 4, XANH_NHAT);
		khoiB(g, x + 3, y + 3, w - 6, h - 6, 3, TRANG);
	}

	public static void bangB(BucVe g, int x, int y, int w, int h)
	{
		khoiB(g, x + 1, y + 2, w, h, 6, BONG);
		khoiB(g, x, y, w, h, 6, VIEN);
		khoiB(g, x + 2, y + 2, w - 4, h - 4, 4, TRANG);
		khoiDocB(g, x + 4, y + 4, w - 8, h - 8, 3, THAN_TREN, THAN_DUOI);
	}

	public static void daiB(BucVe g, int x, int y, int w, int h, bool coBong)
	{
		if (h < 6)
		{
			return;
		}
		khoiDocB(g, x, y, w, h, 3, DO_SANG, DO_TOI);
		g.setColor(0xFF9070);
		g.fillRect(x + 3, y + 1, w - 6, 1);
		g.setColor(VIEN);
		g.fillRect(x, y + h, w, 1);
		if (coBong && h >= 12 && w > 60)
		{
			int r = h / 2 - 2;
			if (r > 6)
			{
				r = 6;
			}
			pokeBallB(g, x + 4 + r, y + h / 2, r, 0);
		}
	}

	private static int sin(int goc)
	{
		goc = ((goc % 360) + 360) % 360;
		int k = (goc + 7) / 15 % 24;
		int q = k / 6;
		int m = k % 6;
		switch (q)
		{
		case 0:
			return SIN15[m];
		case 1:
			return SIN15[6 - m];
		case 2:
			return -SIN15[m];
		default:
			return -SIN15[6 - m];
		}
	}

	private static int cos(int goc)
	{
		return sin(goc + 90);
	}

	public static void pokeBallB(BucVe g, int cx, int cy, int r, int goc)
	{
		if (r < 3)
		{
			r = 3;
		}
		int d = r * 2;
		g.setColor(VIEN);
		g.fillArc(cx - r - 1, cy - r - 1, d + 3, d + 3, 0, 360);
		g.setColor(DO);
		g.fillArc(cx - r, cy - r, d + 1, d + 1, goc, 180);
		g.setColor(TRANG);
		g.fillArc(cx - r, cy - r, d + 1, d + 1, goc + 180, 180);
		if (r >= 6)
		{
			int hx = cx + (cos(goc + 125) * (r * 5 / 10)) / 1024;
			int hy = cy - (sin(goc + 125) * (r * 5 / 10)) / 1024;
			g.setColor(DO_SANG);
			g.fillArc(hx - r / 4, hy - r / 4, r / 2 + 1, r / 2 + 1, 0, 360);
		}
		int c = cos(goc);
		int s = sin(goc);
		int ux = c * r / 1024;
		int uy = -s * r / 1024;
		int t = r >= 9 ? 2 : 1;
		int vx = s * t / 1024;
		int vy = c * t / 1024;
		if (vx == 0 && vy == 0)
		{
			vy = 1;
		}
		g.setColor(VIEN);
		g.fillTriangle(cx + ux + vx, cy + uy + vy, cx + ux - vx, cy + uy - vy, cx - ux - vx, cy - uy - vy);
		g.fillTriangle(cx + ux + vx, cy + uy + vy, cx - ux - vx, cy - uy - vy, cx - ux + vx, cy - uy + vy);
		g.drawLine(cx + ux, cy + uy, cx - ux, cy - uy);
		int rb = r / 3;
		if (rb < 2)
		{
			rb = 2;
		}
		g.setColor(VIEN);
		g.fillArc(cx - rb - 1, cy - rb - 1, rb * 2 + 3, rb * 2 + 3, 0, 360);
		g.setColor(TRANG);
		g.fillArc(cx - rb, cy - rb, rb * 2 + 1, rb * 2 + 1, 0, 360);
		if (rb >= 3)
		{
			g.setColor(XAM);
			g.drawArc(cx - rb + 1, cy - rb + 1, rb * 2 - 2, rb * 2 - 2, 0, 360);
		}
	}

	// ================================================================== vẽ lên màn hình (ảnh đệm)

	/// <summary>Bỏ vùng cắt (cả màn hình) nhưng giữ gốc tọa độ đang dịch của người gọi.</summary>
	public static void moClip(MyGraphics g)
	{
		g.setClip(-g.getTranslateX(), -g.getTranslateY(), Canvas.w, Canvas.h);
	}

	/// <summary>Khối chữ nhật bo góc tô một màu.</summary>
	public static void khoi(MyGraphics g, int x, int y, int w, int h, int r, int mau)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		string k = "k|" + w + "|" + h + "|" + r + "|" + mau;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w, h);
			khoiB(b, 0, 0, w, h, r, mau);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Khối bo góc tô dải màu dọc tren -> duoi.</summary>
	public static void khoiDoc(MyGraphics g, int x, int y, int w, int h, int r, int tren, int duoi)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		string k = "d|" + w + "|" + h + "|" + r + "|" + tren + "|" + duoi;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w, h);
			khoiDocB(b, 0, 0, w, h, r, tren, duoi);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Khối viền: viền màu vien dày 1, thân dải tren -> duoi, bóng đổ tùy chọn (một ảnh).</summary>
	public static void khung(MyGraphics g, int x, int y, int w, int h, int r, int vien, int tren, int duoi, bool bong)
	{
		if (w <= 2 || h <= 2)
		{
			return;
		}
		string k = "kv|" + w + "|" + h + "|" + r + "|" + vien + "|" + tren + "|" + duoi + "|" + (bong ? 1 : 0);
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w + (bong ? 1 : 0), h + (bong ? 2 : 0));
			if (bong)
			{
				khoiB(b, 1, 2, w, h, r, BONG);
			}
			khoiB(b, 0, 0, w, h, r, vien);
			khoiDocB(b, 1, 1, w - 2, h - 2, r > 0 ? r - 1 : 0, tren, duoi);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Hộp thoại trắng viền tối (kiểu hộp thoại Pokémon) - chữ bên trong nên dùng font tối.</summary>
	public static void hop(MyGraphics g, int x, int y, int w, int h)
	{
		string k = "hop|" + w + "|" + h;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w + 1, h + 2);
			hopB(b, 0, 0, w, h);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Bảng thân xanh đậm, viền trắng + viền tối - chữ trắng đọc rõ.</summary>
	public static void bang(MyGraphics g, int x, int y, int w, int h)
	{
		string k = "bang|" + w + "|" + h;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w + 1, h + 2);
			bangB(b, 0, 0, w, h);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>"TOP BANG HỘI" -> "Top bang hội" (chữ hoa có dấu của font tiêu đề khó đọc).</summary>
	public static string tieuDe(string s)
	{
		if (s == null || s.Length < 4 || s.IndexOf(' ') < 0)
		{
			return s;
		}
		for (int i = 0; i < s.Length; i++)
		{
			if (char.IsLower(s[i]))
			{
				return s;
			}
		}
		return s.Substring(0, 1) + s.Substring(1).ToLowerInvariant();
	}

	/// <summary>Bảng có dải tiêu đề đỏ cao hTieuDe.</summary>
	public static void bangTieuDe(MyGraphics g, int x, int y, int w, int h, int hTieuDe, string tieuDe)
	{
		bang(g, x, y, w, h);
		dai(g, x + 4, y + 4, w - 8, hTieuDe - 6, tieuDe);
	}

	/// <summary>Dải tiêu đề đỏ bo góc + Poké Ball nhỏ bên trái + chữ trắng viền.</summary>
	public static void dai(MyGraphics g, int x, int y, int w, int h, string tieu)
	{
		if (h < 6)
		{
			return;
		}
		string k = "dai|" + w + "|" + h;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w, h + 1);
			daiB(b, 0, 0, w, h, true);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
		if (!string.IsNullOrEmpty(tieu) && MFont.borderFont != null)
		{
			tieu = tieuDe(tieu);
			int fh = MFont.borderFont.getHeight();
			MFont.borderFont.drawString(g, tieu, x + w / 2, y + (h - fh) / 2 + 1, MFont.CENTER);
		}
	}

	/// <summary>Ô đang chọn trên nền tối: thân xanh sáng, viền vàng.</summary>
	public static void chon(MyGraphics g, int x, int y, int w, int h)
	{
		khung(g, x, y, w, h, 3, VANG, XANH_SANG, XANH, false);
	}

	/// <summary>Ô đang chọn trên nền trắng: vàng nhạt viền vàng đậm.</summary>
	public static void chonSang(MyGraphics g, int x, int y, int w, int h)
	{
		khung(g, x, y, w, h, 3, VANG_TOI, VANG_NHAT, VANG, false);
	}

	/// <summary>Mũi tên tam giác chỉ sang phải, đỉnh trái (x, y), cao h.</summary>
	public static void muiTen(MyGraphics g, int x, int y, int h, int mau)
	{
		string k = "mt|" + h + "|" + mau;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(h / 2 + 2, h + 1);
			b.setColor(mau);
			b.fillTriangle(0, 0, 0, h, h / 2 + 1, h / 2);
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Mũi tên trái (huong = -1) / phải (1) cao h, tâm dọc ở yTam.</summary>
	public static void muiNgang(MyGraphics g, int x, int yTam, int h, int huong, int mau)
	{
		string k = "mn|" + h + "|" + huong + "|" + mau;
		Image a = layAnh(k);
		int rong = h / 2 + 1;
		if (a == null)
		{
			BucVe b = new BucVe(rong + 1, h + 1);
			b.setColor(mau);
			if (huong < 0)
			{
				b.fillTriangle(0, h / 2, rong, 0, rong, h);
			}
			else
			{
				b.fillTriangle(rong, h / 2, 0, 0, 0, h);
			}
			a = luuAnh(k, b);
		}
		g.drawImage(a, x, yTam - h / 2);
	}

	/// <summary>Tam giác ▼ nhấp nháy ở góc hộp thoại.</summary>
	public static void muiXuong(MyGraphics g, int xTam, int y)
	{
		if (Canvas.gameTick % 10 >= 6)
		{
			return;
		}
		Image a = layAnh("mx");
		if (a == null)
		{
			BucVe b = new BucVe(9, 5);
			b.setColor(DO);
			b.fillTriangle(0, 0, 8, 0, 4, 4);
			a = luuAnh("mx", b);
		}
		g.drawImage(a, xTam - 4, y);
	}

	public static int mauHp(long gt, long max)
	{
		if (max <= 0)
		{
			return HP_XANH;
		}
		long p = gt * 100 / max;
		return p > 50 ? HP_XANH : (p > 20 ? HP_VANG : HP_DO);
	}

	/// <summary>Thanh HP / MP / EXP: loai 0 = HP (xanh -> vàng -> đỏ), 1 = MP, 2 = EXP. Vẽ thẳng (giá trị đổi liên tục).</summary>
	public static void thanh(MyGraphics g, int x, int y, int w, int h, long gt, long max, int loai)
	{
		if (w < 4 || h < 3)
		{
			return;
		}
		if (gt < 0)
		{
			gt = 0;
		}
		if (max > 0 && gt > max)
		{
			gt = max;
		}
		khoi(g, x, y, w, h, 1, VIEN);
		g.setColor(0x3A4258);
		g.fillRect(x + 1, y + 1, w - 2, h - 2);
		int dai = max <= 0 ? 0 : (int)((w - 2) * gt / max);
		if (gt > 0 && dai < 1)
		{
			dai = 1;
		}
		if (dai <= 0)
		{
			return;
		}
		int mau = loai == 0 ? mauHp(gt, max) : (loai == 1 ? MP_XANH : EXP_XANH);
		g.setColor(mau);
		g.fillRect(x + 1, y + 1, dai, h - 2);
		if (h >= 5)
		{
			g.setColor(pha(mau, TRANG, 1, 2));
			g.fillRect(x + 1, y + 1, dai, 1);
		}
	}

	/// <summary>Hình tròn tô màu tâm (cx, cy) bán kính r (+ viền tùy chọn: vien &lt; 0 = không).</summary>
	public static void tron(MyGraphics g, int cx, int cy, int r, int mau, int vien)
	{
		string k = "tr|" + r + "|" + mau + "|" + vien;
		Image a = layAnh(k);
		int d = r * 2 + 2;
		if (a == null)
		{
			BucVe b = new BucVe(d, d);
			if (vien >= 0)
			{
				b.setColor(vien);
				b.fillArc(0, 0, d, d, 0, 360);
				b.setColor(mau);
				b.fillArc(1, 1, d - 2, d - 2, 0, 360);
			}
			else
			{
				b.setColor(mau);
				b.fillArc(1, 1, d - 2, d - 2, 0, 360);
			}
			a = luuAnh(k, b);
		}
		g.drawImage(a, cx - r - 1, cy - r - 1);
	}

	/// <summary>Vòng tròn viền 1 px tâm (cx, cy) bán kính r.</summary>
	public static void vong(MyGraphics g, int cx, int cy, int r, int mau)
	{
		string k = "vg|" + r + "|" + mau;
		Image a = layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(r * 2 + 1, r * 2 + 1);
			b.setColor(mau);
			b.drawArc(0, 0, r * 2, r * 2, 0, 360);
			a = luuAnh(k, b);
		}
		g.drawImage(a, cx - r, cy - r);
	}

	/// <summary>Poké Ball tâm (cx, cy) bán kính r, quay goc độ (bước 15 độ được đệm riêng).</summary>
	public static void pokeBall(MyGraphics g, int cx, int cy, int r, int goc)
	{
		if (r < 3)
		{
			r = 3;
		}
		goc = (goc % 360 + 360) % 360 / 15 * 15;
		string k = "pb|" + r + "|" + goc;
		Image a = layAnh(k);
		int o = r + 2;
		if (a == null)
		{
			BucVe b = new BucVe(o * 2 + 2, o * 2 + 2);
			pokeBallB(b, o, o, r, goc);
			a = luuAnh(k, b);
		}
		g.drawImage(a, cx - o, cy - o);
	}

	/// <summary>Poké Ball đang quay (chờ tải) - lắc qua lại rồi quay tròn như lúc bắt Pokémon.</summary>
	public static void dangTai(MyGraphics g, int cx, int cy, int r)
	{
		int t = Canvas.gameTick % 40;
		int goc;
		if (t < 24)
		{
			goc = -t * 15;
		}
		else
		{
			int[] lac = { 0, 15, 30, 15, 0, -15, -30, -15, 0, 15, 30, 15, 0, -15, -30, -15 };
			goc = lac[(t - 24) % lac.Length];
		}
		khoi(g, cx - r + 2, cy + r - 1, r * 2 - 4, 4, 2, BONG);
		pokeBall(g, cx, cy, r, goc);
	}

	/// <summary>Viên thuốc (pill) màu mau, chữ trắng viền ở giữa.</summary>
	public static void vien(MyGraphics g, int x, int y, int w, int h, int mau, string chu)
	{
		khung(g, x, y, w, h, h / 2, VIEN, pha(mau, TRANG, 1, 3), mau, false);
		if (chu != null && MFont.borderFont != null)
		{
			MFont.borderFont.drawString(g, chu, x + w / 2, y + (h - MFont.borderFont.getHeight()) / 2 + 1, MFont.CENTER);
		}
	}

	/// <summary>Màu huy hiệu hệ theo chuỗi hệ ("Hệ Lửa", "Hệ Cỏ/Độc"...).</summary>
	public static int mauHe(string he)
	{
		if (he == null)
		{
			return 0xA8A878;
		}
		if (he.IndexOf("Lửa") >= 0)
		{
			return 0xF08030;
		}
		if (he.IndexOf("Nước") >= 0)
		{
			return 0x6890F0;
		}
		if (he.IndexOf("Cỏ") >= 0)
		{
			return 0x78C850;
		}
		if (he.IndexOf("Điện") >= 0)
		{
			return 0xE8B800;
		}
		if (he.IndexOf("Độc") >= 0)
		{
			return 0xA040A0;
		}
		return 0xA8A878;
	}

	/// <summary>Huy hiệu hệ: chữ hệ (bỏ "Hệ ") trên viên màu hệ, trả về bề ngang.</summary>
	public static int huyHieuHe(MyGraphics g, int x, int y, string he)
	{
		string chu = he == null ? string.Empty : (he.StartsWith("Hệ ") ? he.Substring(3) : he);
		int w = MFont.borderFont.getWidth(chu) + 12;
		vien(g, x, y, w, MFont.borderFont.getHeight() + 3, mauHe(he), chu);
		return w;
	}

	/// <summary>Cắt chuỗi cho vừa wMax (thêm ".."), theo font f.</summary>
	public static string catVua(MFont f, string s, int wMax)
	{
		if (s == null || f == null || f.getWidth(s) <= wMax)
		{
			return s;
		}
		while (s.Length > 2 && f.getWidth(s + "..") > wMax)
		{
			s = s.Substring(0, s.Length - 1);
		}
		return s + "..";
	}
}
