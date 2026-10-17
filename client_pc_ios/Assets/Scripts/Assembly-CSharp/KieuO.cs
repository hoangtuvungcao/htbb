using System.Collections.Generic;

/// <summary>
/// Ô kiểu Pokémon dùng chung - bản C# của game.pokemon.KieuO (bản jar): ô chiêu theo màu hệ + hình
/// biểu tượng chiêu, ô lưới hành trang / rương / cửa hàng / trang bị (bo góc, có khe, sọc xen kẽ, ô
/// chọn viền vàng dày), huy hiệu số lượng, nhãn phím, viên hệ, phủ mờ (hồi chiêu), thanh cuộn.
/// Hình biểu tượng 12x12 (chữ '#') phóng 1..3 lần, viền tối 1 px; ô + hình đóng sẵn thành ảnh (GiaoDien).
/// </summary>
public static class KieuO
{
	public static readonly int[] MAU_HE = new int[18]
	{
		0xA8A878, 0xF08030, 0x6890F0, 0xF8D030, 0x78C850, 0x98D8D8, 0xC03028, 0xA040A0, 0xE0C068,
		0xA890F0, 0xF85888, 0xA8B820, 0xB8A038, 0x705898, 0x7038F8, 0x705848, 0xB8B8D0, 0xEE99AC
	};

	public static readonly string[] TEN_HE = new string[18]
	{
		"Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
		"Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"
	};

	public static int mauHe(int he)
	{
		return he >= 0 && he < MAU_HE.Length ? MAU_HE[he] : MAU_HE[0];
	}

	public static string tenHe(int he)
	{
		return he >= 0 && he < TEN_HE.Length ? TEN_HE[he] : TEN_HE[0];
	}

	public const byte HINH_VA = 0;
	public const byte HINH_VUOT = 1;
	public const byte HINH_LUA = 2;
	public const byte HINH_NANH = 3;
	public const byte HINH_LOC = 4;
	public const byte HINH_MAY = 5;
	public const byte HINH_TANG = 6;
	public const byte HINH_TIA = 7;
	public const byte HINH_NO = 8;
	public const byte HINH_ROI = 9;
	public const byte HINH_LA = 10;
	public const byte HINH_BOM = 11;
	public const byte HINH_KHIEN = 12;
	public const byte HINH_MAT_TROI = 13;
	public const byte HINH_NHANH = 14;
	public const byte HINH_SAO = 15;
	public const byte HINH_TIM = 16;
	public const byte HINH_NGU = 17;
	public const byte HINH_CONG = 18;
	public const byte HINH_MA = 19;
	public const byte HINH_THEP = 20;
	public const byte HINH_GIOT = 21;
	public const byte HINH_BONG = 22;
	public const byte HINH_SONG = 23;
	public const byte HINH_SET = 24;
	public const byte HINH_VONG = 25;
	public const byte HINH_LUOI = 26;

	private static readonly string[] HINH = {
		// 0 va chạm
		".....##....." + ".#...##...#." + "..#..##..#.." + "...#....#..." + "............" + "####....####"
		+ "####....####" + "............" + "...#....#..." + "..#..##..#.." + ".#...##...#." + ".....##.....",
		// 1 vết cào
		"#...#...#..." + "##..##..##.." + ".#...#...#.." + ".##..##..##." + "..#...#...#." + "..##..##..##"
		+ "...#...#...#" + "...#...#...#" + "....#...#..." + "....#...#..." + "............" + "............",
		// 2 lửa
		".....#......" + ".....##....." + "....###....." + "....####..#." + "...#####.##." + "..########.."
		+ ".##########." + ".####..####." + ".###....###." + ".###....###." + "..###..###.." + "...######...",
		// 3 nanh
		"............" + "############" + "############" + ".##########." + ".####..####." + ".####..####."
		+ "..###..###.." + "..###..###.." + "..##....##.." + "...#....#..." + "...#....#..." + "............",
		// 4 lốc
		"############" + "............" + ".##########." + "............" + "..########.." + "............"
		+ "...######..." + "............" + "....####...." + "............" + ".....###...." + "......##....",
		// 5 mây
		"............" + "............" + "............" + "....###....." + "...#####.##." + "..#########."
		+ ".##########." + "############" + "############" + ".##########." + "............" + "............",
		// 6 tăng
		".....##....." + "....####...." + "...######..." + "..###..###.." + ".###....###." + "###......###"
		+ ".....##....." + "....####...." + "...######..." + "..###..###.." + ".###....###." + "###......###",
		// 7 tia
		"............" + "............" + "........###." + ".......#####" + "############" + "############"
		+ "############" + ".......#####" + "........###." + "............" + "............" + "............",
		// 8 nổ
		"#....#.....#" + ".#..###...#." + ".##.####.##." + "..########.." + ".##########." + "############"
		+ ".##########." + "..########.." + ".##########." + ".##.####.##." + ".#...###..#." + "#.....#....#",
		// 9 roi
		".........###" + "........###." + ".......##..." + "......##...." + ".....##....." + "....##......"
		+ "....##......" + ".....##....." + "......##...." + ".....##....." + "...###......" + ".###........",
		// 10 lá
		"..........##" + ".......#####" + ".....######." + "....#######." + "...######.#." + "..######.##."
		+ "..#####.###." + ".#####.###.." + ".####.####.." + ".###.####..." + ".##.####...." + "##..........",
		// 11 bom
		"....####...." + "..########.." + ".##..######." + ".#...######." + "##..########" + "############"
		+ "############" + "############" + ".##########." + ".##########." + "..########.." + "....####....",
		// 12 khiên
		".##########." + "############" + "############" + "#####..#####" + "###......###" + "#####..#####"
		+ "#####..#####" + ".##########." + ".##########." + "..########.." + "...######..." + ".....##.....",
		// 13 mặt trời
		".....##....." + ".#...##...#." + "..#......#.." + "....####...." + "...######..." + "##.######.##"
		+ "##.######.##" + "...######..." + "....####...." + "..#......#.." + ".#...##...#." + ".....##.....",
		// 14 nhanh
		"............" + "##....##...." + ".##....##..." + "..##....##.." + "...##....##." + "....##....##"
		+ "....##....##" + "...##....##." + "..##....##.." + ".##....##..." + "##....##...." + "............",
		// 15 sao
		".....##....." + ".....##....." + "....####...." + "....####...." + "############" + ".##########."
		+ "..########.." + "...######..." + "...######..." + "..###..###.." + "..##....##.." + ".##......##.",
		// 16 tim
		"............" + ".###....###." + "#####..#####" + "############" + "############" + "############"
		+ ".##########." + "..########.." + "...######..." + "....####...." + ".....##....." + "............",
		// 17 ngủ
		"............" + ".#########.." + ".#########.." + ".......##..." + "......##...." + ".....##....."
		+ "....##......" + "...##......." + "..##........" + ".#########.." + ".#########.." + "............",
		// 18 cộng
		"............" + "....####...." + "....####...." + "....####...." + ".##########." + ".##########."
		+ ".##########." + ".##########." + "....####...." + "....####...." + "....####...." + "............",
		// 19 ma
		"...######..." + "..########.." + ".##########." + ".##..##..##." + ".##..##..##." + ".##########."
		+ ".##########." + ".##########." + ".##########." + ".##########." + ".##.####.##." + ".#...##...#.",
		// 20 thép
		"...######..." + "..########.." + ".##########." + "####....####" + "###......###" + "###......###"
		+ "###......###" + "###......###" + "####....####" + ".##########." + "..########.." + "...######...",
		// 21 giọt
		".....##....." + ".....##....." + "....####...." + "....####...." + "...######..." + "..########.."
		+ ".##########." + ".###..#####." + ".##..######." + ".##########." + "..########.." + "...######...",
		// 22 bong bóng
		".......####." + "......##..##" + "......#....#" + "......##..##" + ".###...####." + "##.##......."
		+ "#...#......." + "##.##..###.." + ".###..##.##." + "......#...#." + "......##.##." + ".......###..",
		// 23 sóng
		"............" + "..##....##.." + ".####..####." + "##..####..##" + "#....##....#" + "............"
		+ "..##....##.." + ".####..####." + "##..####..##" + "#....##....#" + "............" + "............",
		// 24 sét
		"......####.." + ".....####..." + "....####...." + "...####....." + "..#########." + ".#########.."
		+ ".....####..." + "....####...." + "...###......" + "..##........" + ".##........." + "#...........",
		// 25 vòng
		"...######..." + "..##....##.." + ".##......##." + "##...##...##" + "#...####...#" + "#..######..#"
		+ "#..######..#" + "#...####...#" + "##...##...##" + ".##......##." + "..##....##.." + "...######...",
		// 26 lưới
		"............" + ".#..#..#..#." + "############" + ".#..#..#..#." + ".#..#..#..#." + "############"
		+ ".#..#..#..#." + ".#..#..#..#." + "############" + ".#..#..#..#." + "............" + "............"
	};
	private const int CO_HINH = 12;

	private static readonly Dictionary<int, int[]> diemHinh = new Dictionary<int, int[]>();

	/// <summary>Điểm ảnh ARGB của hình (nét trắng viền tối), cạnh = 12 * phong + 2.</summary>
	public static int[] diemAnhHinh(int hinh, int phong)
	{
		if (hinh < 0 || hinh >= HINH.Length)
		{
			hinh = 0;
		}
		int k = phong < 1 ? 1 : (phong > 3 ? 3 : phong);
		int khoa = hinh * 4 + k;
		int[] q;
		if (diemHinh.TryGetValue(khoa, out q))
		{
			return q;
		}
		string s = HINH[hinh];
		int c = CO_HINH * k + 2;
		int[] p = new int[c * c];
		for (int y = 0; y < CO_HINH; y++)
		{
			for (int x = 0; x < CO_HINH; x++)
			{
				if (s[y * CO_HINH + x] != '#')
				{
					continue;
				}
				for (int dy = 0; dy < k; dy++)
				{
					for (int dx = 0; dx < k; dx++)
					{
						p[(1 + y * k + dy) * c + 1 + x * k + dx] = unchecked((int)0xFFFFFFFF);
					}
				}
			}
		}
		q = new int[c * c];
		for (int y = 0; y < c; y++)
		{
			for (int x = 0; x < c; x++)
			{
				if (p[y * c + x] != 0)
				{
					q[y * c + x] = p[y * c + x];
					continue;
				}
				bool ke = false;
				for (int dy = -1; dy <= 1 && !ke; dy++)
				{
					for (int dx = -1; dx <= 1; dx++)
					{
						int xx = x + dx;
						int yy = y + dy;
						if (xx >= 0 && yy >= 0 && xx < c && yy < c && p[yy * c + xx] != 0)
						{
							ke = true;
							break;
						}
					}
				}
				if (ke)
				{
					q[y * c + x] = unchecked((int)0xC0141C30);
				}
			}
		}
		diemHinh[khoa] = q;
		return q;
	}

	/// <summary>Ảnh hình biểu tượng (nét trắng viền tối), phong = 1..3.</summary>
	public static Image anhHinh(int hinh, int phong)
	{
		string k = "hinh|" + hinh + "|" + phong;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			int[] q = diemAnhHinh(hinh, phong);
			int c = CO_HINH * (phong < 1 ? 1 : (phong > 3 ? 3 : phong)) + 2;
			BucVe b = new BucVe(c, c);
			b.veDiem(q, c, c, 0, 0);
			a = GiaoDien.luuAnh(k, b);
		}
		return a;
	}

	/// <summary>Phủ màu argb (có alpha) lên vùng w x h.</summary>
	public static void phu(MyGraphics g, int x, int y, int w, int h, int argb)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}
		g.setColor(argb & 0xFFFFFF, ((uint)argb >> 24) / 255f);
		g.fillRect(x, y, w, h);
	}

	/// <summary>Phủ mờ ô w x hDu nhưng chỉ phần trên cao h (thanh hồi chiêu).</summary>
	public static void phuCat(MyGraphics g, int x, int y, int w, int hDu, int h, int argb)
	{
		if (h > hDu)
		{
			h = hDu;
		}
		phu(g, x, y, w, h, argb);
	}

	/// <summary>Ô chiêu w x h: viền tối, thân dải màu hệ, hình biểu tượng trắng ở giữa; khoa = chưa học (xám).</summary>
	public static void oChieu(MyGraphics g, int x, int y, int w, int h, int he, int hinh, bool khoa)
	{
		string k = "oc|" + w + "|" + h + "|" + he + "|" + hinh + "|" + (khoa ? 1 : 0);
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(w, h);
			int mau = khoa ? 0x8A93A3 : mauHe(he);
			int r = w >= 30 ? 6 : 4;
			GiaoDien.khoiB(b, 0, 0, w, h, r, GiaoDien.VIEN);
			GiaoDien.khoiDocB(b, 1, 1, w - 2, h - 2, r - 1, GiaoDien.pha(mau, GiaoDien.TRANG, 2, 5), GiaoDien.pha(mau, 0, 1, 4));
			if (w >= 10 && h >= 10)
			{
				b.setColor(GiaoDien.pha(mau, GiaoDien.TRANG, 3, 5));
				b.fillRect(r, 2, w - 2 * r, 1);
			}
			int phong = w >= 44 && h >= 44 ? 3 : (w >= 30 && h >= 30 ? 2 : 1);
			int c = CO_HINH * phong + 2;
			b.veDiem(diemAnhHinh(hinh, phong), c, c, (w - c) / 2, (h - c) / 2);
			if (khoa)
			{
				b.setColorA(0x70202838);
				b.fillRect(1, 1, w - 2, h - 2);
			}
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Viên hệ (pill) màu hệ, chữ trắng; trả về bề ngang.</summary>
	public static int vienHe(MyGraphics g, int x, int y, int he)
	{
		int w = rongVienHe(he);
		int h = MFont.borderFont.getHeight() + 3;
		int mau = mauHe(he);
		GiaoDien.khung(g, x, y, w, h, h / 2, GiaoDien.VIEN, mau, GiaoDien.pha(mau, 0, 2, 5), false);
		MFont.borderFont.drawString(g, tenHe(he), x + w / 2, y + 1, MFont.CENTER);
		return w;
	}

	public static int rongVienHe(int he)
	{
		return MFont.borderFont.getWidth(tenHe(he)) + 12;
	}

	/// <summary>Chuỗi số gọn: 25000 -> "25k".</summary>
	public static string soGon(long n)
	{
		if (n >= 100000000L)
		{
			return n / 1000000L + "m";
		}
		if (n >= 10000L)
		{
			return n / 1000L + "k";
		}
		return n.ToString();
	}

	/// <summary>Huy hiệu số lượng, góc dưới-phải tại (xPhai, yDuoi) - nền tối bo góc, số trắng.</summary>
	public static void soLuong(MyGraphics g, int xPhai, int yDuoi, string s)
	{
		if (string.IsNullOrEmpty(s))
		{
			return;
		}
		MFont f = MFont.smallFont;
		int w = f.getWidth(s) + 4;
		int h = f.getHeight() + 1;
		GiaoDien.khoi(g, xPhai - w, yDuoi - h, w, h, 2, GiaoDien.BONG);
		f.drawString(g, s, xPhai - w + 2, yDuoi - h, MFont.LEFT);
	}

	/// <summary>Nhãn phím nhỏ (góc trên-trái ô): nền đỏ, chữ trắng.</summary>
	public static void nhanPhim(MyGraphics g, int x, int y, string s)
	{
		MFont f = MFont.smallFont;
		int w = f.getWidth(s) + 6;
		int h = f.getHeight() + 1;
		GiaoDien.khung(g, x, y, w, h, 3, GiaoDien.VIEN, GiaoDien.DO_SANG, GiaoDien.DO_TOI, false);
		f.drawString(g, s, x + 3, y, MFont.LEFT);
	}

	public const int O_CHAN = 0x23497F;
	public const int O_LE = 0x1D3F70;
	public const int O_TRONG = 0x18345E;

	/// <summary>Ô lưới đồ s x s (gồm khe 1 px): co = có đồ, le = sọc xen kẽ.</summary>
	public static void oDo(MyGraphics g, int x, int y, int s, bool co, bool le)
	{
		oDoMau(g, x, y, s, co, co ? (le ? O_LE : O_CHAN) : O_TRONG);
	}

	/// <summary>Ô rương (nâu).</summary>
	public static void oRuong(MyGraphics g, int x, int y, int s, bool co, bool le)
	{
		oDoMau(g, x, y, s, co, co ? (le ? 0x6A4A1E : 0x7A5726) : 0x4A3414);
	}

	public static void oDoMau(MyGraphics g, int x, int y, int s, bool co, int nen)
	{
		string k = "od|" + s + "|" + (co ? 1 : 0) + "|" + nen;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			int c = s - 1;
			int r = s >= 30 ? 5 : 3;
			BucVe b = new BucVe(c, c);
			GiaoDien.khoiB(b, 0, 0, c, c, r, 0x0E1A30);
			GiaoDien.khoiB(b, 1, 1, c - 2, c - 2, r - 1, nen);
			if (co)
			{
				b.setColor(GiaoDien.pha(nen, GiaoDien.TRANG, 1, 5));
				b.fillRect(r, 1, c - 2 * r, 1);
			}
			else
			{
				b.setColor(0x2C4A78);
				b.fillRect(c / 2 - 1, c / 2 - 1, 2, 2);
			}
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	/// <summary>Lưới đồ: numW cột, hàng tu..den-1, ô s x s; soCo ô có đồ; chon = ô chọn (-1 không).</summary>
	public static void luoi(MyGraphics g, int numW, int tu, int den, int s, int soCo, int chon)
	{
		for (int i = tu; i < den; i++)
		{
			for (int j = 0; j < numW; j++)
			{
				int o = i * numW + j;
				oDo(g, j * s, i * s, s, o < soCo, ((i + j) & 1) == 1);
				if (o == chon)
				{
					nenChon(g, j * s, i * s, s);
				}
			}
		}
	}

	/// <summary>Viền chọn vàng dày 2 px quanh ô s x s tại (x, y) (vẽ SAU đồ), nhấp nháy nhẹ.</summary>
	public static void chonO(MyGraphics g, int x, int y, int s)
	{
		chonO(g, x, y, s, s);
	}

	public static void chonO(MyGraphics g, int x, int y, int sw, int sh)
	{
		bool sang = Canvas.gameTick % 10 < 5;
		string k = "co|" + sw + "|" + sh + "|" + (sang ? 1 : 0);
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			int cw = sw - 1;
			int ch = sh - 1;
			BucVe b = new BucVe(cw + 4, ch + 4);
			b.translate(2, 2);
			b.setColor(GiaoDien.VIEN);
			b.drawRect(-2, -2, cw + 3, ch + 3);
			b.setColor(sang ? GiaoDien.VANG : 0xFFE24A);
			b.drawRect(-1, -1, cw + 1, ch + 1);
			b.drawRect(0, 0, cw - 1, ch - 1);
			b.setColor(GiaoDien.TRANG);
			b.fillRect(1, 1, 2, 1);
			b.fillRect(1, 1, 1, 2);
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, x - 2, y - 2);
	}

	/// <summary>Viền sáng mảnh khi chuột rê lên ô (PC).</summary>
	public static void reO(MyGraphics g, int x, int y, int sw, int sh)
	{
		string k = "re|" + sw + "|" + sh;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(sw + 1, sh + 1);
			b.setColorA(unchecked((int)0xC0FFFFFF));
			b.drawRect(0, 0, sw, sh);
			b.setColorA(0x30FFFFFF);
			b.fillRect(1, 1, sw - 1, sh - 1);
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, x - 1, y - 1);
	}

	/// <summary>Nền sáng cho ô đang chọn (vẽ TRƯỚC đồ).</summary>
	public static void nenChon(MyGraphics g, int x, int y, int s)
	{
		int c = s - 1;
		GiaoDien.khoiDoc(g, x + 1, y + 1, c - 2, c - 2, 2, GiaoDien.XANH_SANG, GiaoDien.XANH);
	}

	/// <summary>Màu khung theo độ hiếm (colorName của đồ), -1 = thường.</summary>
	public static int mauHiem(int c)
	{
		switch (c)
		{
		case 1:
			return 0x48D058;
		case 2:
			return 0x3FA0F0;
		case 3:
			return 0xC060F0;
		case 4:
			return 0xFFCB05;
		case 5:
			return 0xF04830;
		default:
			return -1;
		}
	}

	/// <summary>Khung độ hiếm 1 px bên trong ô s x s.</summary>
	public static void khungHiem(MyGraphics g, int x, int y, int s, int mau)
	{
		if (mau < 0)
		{
			return;
		}
		int c = s - 1;
		g.setColor(mau);
		g.drawRect(x + 1, y + 1, c - 3, c - 3);
	}

	/// <summary>"+N" cấp cường hóa góc trên-trái.</summary>
	public static void capCuong(MyGraphics g, int x, int y, int n)
	{
		if (n <= 0)
		{
			return;
		}
		string s = "+" + n;
		MFont f = MFont.smallFontColor[1];
		int w = f.getWidth(s) + 2;
		GiaoDien.khoi(g, x, y, w, f.getHeight(), 2, GiaoDien.BONG);
		f.drawString(g, s, x + 1, y, MFont.LEFT);
	}

	/// <summary>Thanh cuộn dọc mảnh: tổng tong, đang thấy tu..tu+thay.</summary>
	public static void thanhCuon(MyGraphics g, int x, int y, int h, int tong, int tu, int thay)
	{
		if (tong <= thay || tong <= 0 || h < 8)
		{
			return;
		}
		GiaoDien.khoi(g, x, y, 4, h, 2, 0x0E1A30);
		int dai = h * thay / tong;
		if (dai < 8)
		{
			dai = 8;
		}
		int vt = (h - dai) * tu / (tong - thay);
		if (vt < 0)
		{
			vt = 0;
		}
		if (vt > h - dai)
		{
			vt = h - dai;
		}
		GiaoDien.khoi(g, x, y + vt, 4, dai, 2, GiaoDien.VANG);
	}

	/// <summary>Chấm trang: n trang, đang ở trang i, căn giữa tại xTam.</summary>
	public static void chamTrang(MyGraphics g, int xTam, int y, int n, int i)
	{
		if (n <= 1)
		{
			return;
		}
		if (n > 12)
		{
			n = 12;
		}
		int x = xTam - (n * 8 - 2) / 2;
		for (int k = 0; k < n; k++)
		{
			GiaoDien.tron(g, x + k * 8 + 3, y + 3, 3, k == i ? GiaoDien.VANG : 0x5E7AA8, GiaoDien.VIEN);
		}
	}
}
