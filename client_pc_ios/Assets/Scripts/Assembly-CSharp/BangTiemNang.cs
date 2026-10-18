/// <summary>
/// Tab "Tiềm năng" kiểu Pokémon - bản C# của game.pokemon.BangTiemNang (bản jar): thay ảnh inv_1 kiếm
/// hiệp (hàng chỉ số có kiếm, cuộn giấy...). Giữ đúng bố cục cũ để phím / chuột của bảng gốc không đổi:
/// hàng "Điểm cộng" ở y = -20, 5 hàng chỉ số ở y = i * 19 (cao 18).
/// </summary>
public static class BangTiemNang
{
	private static readonly string[] TEN = { "Sức mạnh", "Khéo léo", "Tinh thần", "Sức khoẻ", "May mắn" };

	private static readonly byte[] HINH = { KieuO.HINH_VA, KieuO.HINH_NHANH, KieuO.HINH_SAO, KieuO.HINH_TIM, KieuO.HINH_MAT_TROI };

	private static readonly byte[] HE = { 6, 9, 10, 17, 3 };

	private static void oBieuTuong(MyGraphics g, int x, int y, int mau, int hinh)
	{
		string k = "tn|" + mau + "|" + hinh;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(16, 16);
			GiaoDien.khoiB(b, 0, 0, 16, 16, 5, GiaoDien.VIEN);
			GiaoDien.khoiDocB(b, 1, 1, 14, 14, 4, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 4), GiaoDien.pha(mau, 0, 1, 3));
			b.veDiem(KieuO.diemAnhHinh(hinh, 1), 14, 14, 1, 1);
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, x, y);
	}

	private static void vienSo(MyGraphics g, int xPhai, int y, string s, bool sang)
	{
		MFont f = MFont.borderFont;
		int w = f.getWidth(s) + 10;
		if (w < 22)
		{
			w = 22;
		}
		GiaoDien.khung(g, xPhai - w, y, w, 14, 6, GiaoDien.VIEN, sang ? GiaoDien.VANG_NHAT : 0x3A5F96, sang ? GiaoDien.VANG_TOI : 0x23497F, false);
		f.drawString(g, s, xPhai - w / 2, y, MFont.CENTER);
	}

	/// <summary>w = bề rộng vùng nội dung; chon = hàng đang chọn (0..4), doiTab = đang chọn tab.</summary>
	public static void ve(MyGraphics g, Char c, int chon, bool doiTab, int w)
	{
		if (c == null)
		{
			return;
		}
		int r = w + 4;
		if (r < 100)
		{
			r = 100;
		}
		int x0 = -2;
		int diem = c.basePointLeft;
		GiaoDien.khoiDoc(g, x0, -20, r, 18, 4, diem > 0 ? 0x4E9A3C : 0x2A4F82, diem > 0 ? 0x2F6E25 : 0x1D3F70);
		oBieuTuong(g, x0 + 1, -19, GiaoDien.VANG, KieuO.HINH_CONG);
		MFont.borderFont.drawString(g, "Điểm cộng", x0 + 21, -18, MFont.LEFT);
		vienSo(g, x0 + r - 3, -18, diem.ToString(), diem > 0);
		int[] gt = { c.strength, c.agility, c.spirit, c.health, c.luck };
		for (int i = 0; i < 5; i++)
		{
			int y = i * 19;
			bool dangChon = !doiTab && i == chon;
			if (dangChon)
			{
				GiaoDien.chon(g, x0, y, r, 18);
			}
			else
			{
				GiaoDien.khoi(g, x0, y, r, 18, 4, (i & 1) == 0 ? KieuO.O_CHAN : KieuO.O_LE);
			}
			oBieuTuong(g, x0 + 1, y + 1, KieuO.mauHe(HE[i]), HINH[i]);
			MFont.arialFontW.drawString(g, TEN[i], x0 + 21, y + 3, MFont.LEFT);
			int xPhai = x0 + r - 3;
			if (dangChon && diem > 0)
			{
				GiaoDien.khung(g, xPhai - 12, y + 2, 12, 14, 4, GiaoDien.VIEN, GiaoDien.VANG_NHAT, GiaoDien.VANG, false);
				g.setColor(GiaoDien.VIEN);
				g.fillRect(xPhai - 9, y + 8, 6, 2);
				g.fillRect(xPhai - 7, y + 6, 2, 6);
				xPhai -= 14;
			}
			vienSo(g, xPhai, y + 2, gt[i].ToString(), false);
		}
	}
}
