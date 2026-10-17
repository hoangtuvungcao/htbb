/// <summary>
/// Tab "Trang bị" kiểu Pokémon - bản PC của game.pokemon.BangTrangBi (bản jar). Bố cục PC giữ nguyên chỗ
/// của bảng gốc (WindowInfoScr.paintCharWearing: 2 cột 5 ô 40 px hai bên hình nhân vật + 2 ô 30 px phía
/// dưới, icon đồ ở posPaintWearingX/Y) - chỉ thay khung ô xám bằng ô bo góc, ô chọn viền vàng và thêm tên
/// ô theo tên đồ Pokémon mới (Khăn quàng, Đai, Băng đô, Huy hiệu, Vòng cổ, Găng đấm, Ruy băng...).
/// </summary>
public static class BangTrangBi
{
	private static readonly string[] TEN_TRAI = { "Phiến đá hệ", "Khăn quàng", "Băng đô", "Đai", "Lông vũ tốc độ" };

	private static readonly string[] TEN_PHAI = { null, "Vòng cổ", "Huy hiệu", "Huy hiệu", "Găng đấm" };

	private static readonly string[] TEN_CAM = { "Than lửa", "Hạt giống thần", "Khăn lụa", "Nước thần", "Nam châm" };

	/// <summary>Tên loại đồ (type của ItemTemplate) theo tên Pokémon mới; null nếu không rõ.</summary>
	public static string tenLoai(int type)
	{
		switch (type)
		{
		case 0:
			return "Khăn quàng";
		case 1:
			return "Đai";
		case 2:
			return "Băng đô";
		case 3:
			return "Than lửa";
		case 4:
			return "Hạt giống thần";
		case 5:
			return "Khăn lụa";
		case 6:
			return "Nước thần";
		case 7:
			return "Nam châm";
		case 8:
			return "Huy hiệu";
		case 9:
			return "Vòng cổ";
		case 10:
			return "Lông vũ tốc độ";
		case 11:
			return "Găng đấm";
		case 12:
			return "Phiến đá hệ";
		case 13:
			return "Cuốc đào mỏ";
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
			return "Đồ thú cưỡi";
		case 19:
			return "Ruy băng";
		case 25:
			return "Thú cưỡi";
		case 26:
			return "Trứng Pokémon";
		case 27:
			return "Pokémon đi theo";
		default:
			return null;
		}
	}

	private static string tenO(int chon, int numW, int lop)
	{
		if (chon == 4)
		{
			return "Ruy băng";
		}
		int cot = chon % numW;
		int hang = chon / numW;
		if (hang < 0 || hang > 4)
		{
			return null;
		}
		if (cot == 0)
		{
			return TEN_TRAI[hang];
		}
		if (cot == 2)
		{
			if (hang == 0)
			{
				return lop >= 0 && lop < TEN_CAM.Length ? TEN_CAM[lop] : "Đồ cầm tay";
			}
			return TEN_PHAI[hang];
		}
		return "Thú cưỡi";
	}

	/// <summary>Khung ô + ô chọn + tên ô ở bố cục PC (xTr, yTr = gốc bảng). Đồ vẽ sau bởi bảng gốc.</summary>
	public static void vePc(MyGraphics g, int xTr, int yTr, int chon, int numW, bool hienChon, int lop)
	{
		for (int i = 0; i < 5; i++)
		{
			KieuO.oDo(g, -3 + xTr, -10 + i * 42 + yTr, 41, true, (i & 1) == 1);
			KieuO.oDo(g, 147 + xTr, -10 + i * 42 + yTr, 41, true, (i & 1) == 0);
		}
		KieuO.oDo(g, 43 + xTr, 165 + yTr, 31, true, false);
		KieuO.oDo(g, 110 + xTr, 165 + yTr, 31, true, true);
		if (!hienChon)
		{
			return;
		}
		int x;
		int y;
		int s;
		if (chon % 3 != 1)
		{
			int cot = (chon == 2 || chon == 5 || chon == 8 || chon == 11 || chon == 14) ? 1 : 0;
			x = -3 + cot * 150 + xTr;
			y = chon / numW * 42 - 10 + yTr;
			s = 41;
		}
		else if (chon != 4)
		{
			x = 110 + xTr;
			y = 165 + yTr;
			s = 31;
		}
		else
		{
			x = 43 + xTr;
			y = 165 + yTr;
			s = 31;
		}
		KieuO.nenChon(g, x, y, s);
		KieuO.chonO(g, x, y, s);
		string ten = tenO(chon, numW, lop);
		if (ten != null)
		{
			MFont f = MFont.borderFont;
			int wt = f.getWidth(ten) + 16;
			int xt = 92 + xTr - wt / 2;
			int yt = 200 + yTr;
			GiaoDien.khung(g, xt, yt, wt, 18, 8, GiaoDien.VIEN, GiaoDien.DO_SANG, GiaoDien.DO_TOI, false);
			f.drawString(g, ten, xt + wt / 2, yt + 2, MFont.CENTER);
		}
	}
}
