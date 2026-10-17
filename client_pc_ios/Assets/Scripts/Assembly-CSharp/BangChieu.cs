/// <summary>
/// Tab "Kỹ năng" của bảng nhân vật kiểu Pokémon - bản C# của game.pokemon.BangChieu (bản jar). Vẽ trong
/// hệ tọa độ nội dung của tab (gốc x0, y0 của bảng, rộng w, từ y = -16 đến ~104): điểm kỹ năng, hàng ô
/// chiêu màu hệ (cuộn ngang cmx, ô chọn viền vàng, chưa học xám, cấp ở góc), thẻ chiêu (tên Pokémon + viên
/// hệ, cấp x/9 + vạch, sát thương, MP, hồi chiêu, tầm, mô tả; chưa học: "Học ở cấp X"). Phím / chuột vẫn do
/// bảng gốc xử lý (selected, cmx, cmy) - lớp này chỉ vẽ.
/// </summary>
public static class BangChieu
{
	private static readonly byte[][] CHIEU_BUFF = new byte[][]
	{
		new byte[] { 4, 5 },
		new byte[] { 4, 5 },
		new byte[] { 4, 5, 6, 7 },
		new byte[] { 4, 5 },
		new byte[] { 4, 5 }
	};

	public static bool laBuff(int lop, int sk)
	{
		if (lop < 0 || lop >= CHIEU_BUFF.Length)
		{
			return false;
		}
		for (int i = 0; i < CHIEU_BUFF[lop].Length; i++)
		{
			if (CHIEU_BUFF[lop][i] == sk)
			{
				return true;
			}
		}
		return false;
	}

	/// <summary>Ô chiêu s x s (góc trên-trái x, y) của chiêu sk thuộc dòng của nhân vật đang chơi.</summary>
	public static void icon(MyGraphics g, int x, int y, int s, int sk)
	{
		int lop = Canvas.gameScr != null && Canvas.gameScr.mainChar != null ? Canvas.gameScr.mainChar.clazz : 0;
		KieuO.oChieu(g, x, y, s, s, PokeChieu.he(lop, sk), PokeChieu.hinh(lop, sk), false);
	}

	private static int cap(int sk)
	{
		return Char.skillLevelLearnt != null && sk >= 0 && sk < Char.skillLevelLearnt.Length ? Char.skillLevelLearnt[sk] : -1;
	}

	/// <summary>Cấp nhân vật cần để học (cap &lt;= 0) / nâng chiêu lên cấp tiếp (cap &gt;= 1).</summary>
	public static int capCan(int sk, int cap)
	{
		try
		{
			int i = cap < 0 ? 1 : cap;
			return SkillManager.LEVEL_ADD_SKILL[sk][i];
		}
		catch (System.Exception)
		{
			return 0;
		}
	}

	private static string giay(long ms)
	{
		long ds = (ms + 50) / 100;
		return ds / 10 + "." + ds % 10 + "s";
	}

	public static void ve(MyGraphics g, int soChieu, int chon, bool doiTab, int cmx, int dyThe, int w)
	{
		MainChar mc = Canvas.gameScr.mainChar;
		int lop = mc.clazz;
		int diem = mc.skillPointLeft;
		GiaoDien.moClip(g);
		// ---- đầu: điểm kỹ năng
		string sd = diem.ToString();
		int wd = MFont.borderFont.getWidth(sd) + 12;
		int xd = w + 4 - wd;
		GiaoDien.khung(g, xd, -17, wd, 16, 7, GiaoDien.VIEN, diem > 0 ? GiaoDien.VANG_NHAT : 0x8A93A3, diem > 0 ? GiaoDien.VANG_TOI : 0x5E6A80, false);
		MFont.borderFont.drawString(g, sd, xd + wd / 2, -16, MFont.CENTER);
		string nhan = "Điểm kỹ năng:";
		string so = (chon + 1) + "/" + soChieu;
		int wSo = MFont.smallFont.getWidth(so);
		if (MFont.arialFontW.getWidth(nhan) > xd - 3 - (-6 + wSo + 4))
		{
			nhan = "Điểm:";
		}
		MFont.arialFontW.drawString(g, nhan, xd - 3, -16, MFont.RIGHT);
		MFont.smallFont.drawString(g, so, -6, -12, MFont.LEFT);
		// ---- hàng ô chiêu
		if (chon < 0)
		{
			chon = 0;
		}
		if (chon > soChieu - 1)
		{
			chon = soChieu - 1;
		}
		g.setClip(-4, 0, w + 8, 28);
		g.translate(-cmx, 0);
		for (int i = 0; i < soChieu; i++)
		{
			int x = i * 23 + 1;
			int c = cap(i);
			KieuO.oChieu(g, x, 3, 21, 21, PokeChieu.he(lop, i), PokeChieu.hinh(lop, i), c <= 0);
			if (c > 0)
			{
				KieuO.soLuong(g, x + 21, 3 + 21, c >= PokeChieu.CAP_TOI_DA ? "max" : c.ToString());
			}
		}
		if (!doiTab)
		{
			KieuO.chonO(g, chon * 23 + 1, 3, 22);
		}
		g.translate(cmx, 0);
		GiaoDien.moClip(g);
		bool conTrai = cmx > 2;
		bool conPhai = soChieu * 23 - cmx > w + 4;
		if (Canvas.gameTick % 10 < 7)
		{
			if (conTrai)
			{
				GiaoDien.muiNgang(g, -10, 13, 8, -1, GiaoDien.VANG);
			}
			if (conPhai)
			{
				GiaoDien.muiNgang(g, w + 3, 13, 8, 1, GiaoDien.VANG);
			}
		}
		if (doiTab)
		{
			return;
		}
		// ---- thẻ chiêu
		int x0 = -9;
		int wThe = w + 21;
		int y0 = 28;
		int hThe = 77;
		GiaoDien.khung(g, x0, y0, wThe, hThe, 4, 0x0E1A30, 0x1F4682, 0x173662, false);
		g.setClip(x0 + 1, y0 + 1, wThe - 2, hThe - 2);
		int cc = cap(chon);
		int he = PokeChieu.he(lop, chon);
		string ten = PokeChieu.ten(lop, chon);
		if (ten == null)
		{
			try
			{
				ten = SkillManager.SKILL_NAME[lop][chon];
			}
			catch (System.Exception)
			{
				ten = string.Empty;
			}
		}
		int y = y0 + 2 - dyThe;
		int wHe = KieuO.rongVienHe(he);
		int xHe = x0 + wThe - 3 - wHe;
		MFont fTen = MFont.normalFont[0];
		ten = GiaoDien.catVua(fTen, ten, xHe - (x0 + 4) - 3);
		fTen.drawString(g, ten, x0 + 4, y + 1, MFont.LEFT);
		KieuO.vienHe(g, xHe, y, he);
		y += 17;
		g.setColor(0x3D6AA8);
		g.fillRect(x0 + 4, y - 1, wThe - 8, 1);
		MFont f = MFont.arialFontW;
		if (cc <= 0)
		{
			f.drawString(g, "Chưa học", x0 + 4, y, MFont.LEFT);
			int can = capCan(chon, cc);
			bool du = mc.level >= can;
			string s = du ? "Có thể học" : "Học ở cấp " + can;
			int ws = MFont.borderFont.getWidth(s) + 10;
			int xs = x0 + wThe - 4 - ws;
			GiaoDien.khung(g, xs, y, ws, 15, 7, GiaoDien.VIEN, du ? GiaoDien.LA : GiaoDien.DO_SANG, du ? GiaoDien.LA_TOI : GiaoDien.DO_TOI, false);
			MFont.borderFont.drawString(g, s, xs + ws / 2, y + 1, MFont.CENTER);
			y += 17;
			if (du && diem <= 0)
			{
				f.drawString(g, "Cần 1 điểm kỹ năng", x0 + 4, y, MFont.LEFT);
				y += 12;
			}
		}
		else
		{
			string sc = "Cấp " + cc + "/" + PokeChieu.CAP_TOI_DA;
			f.drawString(g, sc, x0 + 4, y, MFont.LEFT);
			int xv = x0 + 8 + f.getWidth(sc);
			for (int i = 0; i < PokeChieu.CAP_TOI_DA; i++)
			{
				int xx = xv + i * 4;
				g.setColor(GiaoDien.VIEN);
				g.fillRect(xx, y + 4, 4, 6);
				g.setColor(i < cc ? GiaoDien.VANG : 0x3A4F78);
				g.fillRect(xx + 1, y + 5, 2, 4);
			}
			if (cc < PokeChieu.CAP_TOI_DA)
			{
				int xl = xv + PokeChieu.CAP_TOI_DA * 4 + 4;
				GiaoDien.muiTen(g, xl, y + 3, 8, GiaoDien.LA);
				MFont.normalFont[1].drawString(g, "Lv" + capCan(chon, cc), xl + 7, y - 1, MFont.LEFT);
			}
			y += 13;
			int cot2 = x0 + 4 + (wThe - 8) / 2 + 4;
			int mp = 0;
			int dam = 0;
			long cd = 0;
			int tam = 0;
			try
			{
				mp = SkillManager.getSkillMP(chon, cc, lop);
				dam = SkillManager.getSkillDamPercent(chon, cc, lop);
				cd = SkillManager.getSkillCooldown((sbyte)chon, (sbyte)lop, cc);
				tam = SkillManager.getSkillRange((sbyte)chon, (sbyte)lop);
			}
			catch (System.Exception)
			{
			}
			bool buff = laBuff(lop, chon);
			dong(g, x0 + 4, y, buff ? "Tỉ lệ" : "ST", dam + "%");
			dong(g, cot2, y, "PP", mp.ToString());
			y += 12;
			dong(g, x0 + 4, y, "Hồi", giay(cd));
			if (!buff || tam > 0)
			{
				dong(g, cot2, y, "Tầm", tam.ToString());
			}
			y += 13;
		}
		string[] mt = f.splitFontBStrInLine(PokeChieu.moTa(lop, chon), wThe - 10);
		for (int i = 0; mt != null && i < mt.Length; i++)
		{
			f.drawString(g, mt[i], x0 + 4, y, MFont.LEFT);
			y += 12;
		}
		GiaoDien.moClip(g);
	}

	private static void dong(MyGraphics g, int x, int y, string nhan, string gt)
	{
		MFont.arialFontW.drawString(g, nhan, x, y, MFont.LEFT);
		int w = MFont.arialFontW.getWidth(nhan) + 4;
		MFont.normalFont[0].drawString(g, gt, x + w, y - 1, MFont.LEFT);
	}
}
