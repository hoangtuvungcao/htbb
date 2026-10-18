/// <summary>
/// Hộp thoại đáy màn kiểu Pokémon (giống pc-java / Android HD): thẻ trắng viền navy dày, chân dung tròn
/// của NPC bên trái, viên tên đỏ nhô trên mép, chữ ngắt dòng theo bề ngang đo thật, ▼ nhấp nháy góc phải.
/// Hiện khi đang nói chuyện với NPC (menu NPC mở + NPC đang có lời thoại); menu lựa chọn đặt phía trên
/// hộp, sát phải - không đè hộp (Menu.paintMenu đặt lại menuX / menuY theo hopY()).
/// </summary>
public static class HopThoaiPoke
{
	public const int CAO = 112;

	private const int CD = 84;

	public static int rong()
	{
		return System.Math.Min(Canvas.w - 32, 900);
	}

	public static int hopX()
	{
		return (Canvas.w - rong()) / 2;
	}

	public static int hopY()
	{
		return Canvas.h - CAO - 10;
	}

	/// <summary>NPC đang nói (đang chọn + có lời thoại) khi menu NPC mở; null nếu không.</summary>
	public static NPC npcDangNoi()
	{
		if (!UiPokemon.bat() || Canvas.gameScr == null || !Canvas.menu.showMenu)
		{
			return null;
		}
		NPC n = Canvas.gameScr.focusedActor as NPC;
		if (n == null || n.chat == null || n.chat.dong() == null || n.chat.dong().Length == 0)
		{
			return null;
		}
		return n;
	}

	public static BucVe taoHop(int w, int h)
	{
		const int K = 2;
		BucVe b = new BucVe((w + 2) * K, (h + 4) * K);
		GiaoDien.khoiB(b, 2 * K, 4 * K, w * K, h * K, 12 * K, GiaoDien.BONG);
		GiaoDien.khoiB(b, 0, 0, w * K, h * K, 12 * K, GiaoDien.VIEN);
		GiaoDien.khoiB(b, 3 * K, 3 * K, (w - 6) * K, (h - 6) * K, 9 * K, GiaoDien.DO);
		GiaoDien.khoiDocB(b, 4 * K, 4 * K, (w - 8) * K, (h - 8) * K, 8 * K, GiaoDien.TRANG, 0xEEF3FA);
		return b.thuNho(K);
	}

	public static void ve(MyGraphics g, string ten, string[] cau, int dex)
	{
		Canvas.resetTrans(g);
		int w = rong();
		int x = hopX();
		int y = hopY();
		string k = "uiHT|" + w + "|" + CAO;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			a = GiaoDien.luuAnh(k, taoHop(w, CAO));
		}
		g.drawImage(a, x, y);
		int xChu = x + 18;
		if (dex > 0 && ChanDungPoke.paintTron(g, dex, false, x + 14 + CD / 2, y + CAO / 2, CD))
		{
			xChu = x + 14 + CD + 14;
		}
		// viên tên đỏ nhô trên mép trên (đo bề ngang chữ)
		if (!string.IsNullOrEmpty(ten))
		{
			int wt = MFont.borderFont.getWidth(ten) + 26;
			GiaoDien.khung(g, xChu - 4, y - 13, wt, 24, 12, GiaoDien.VIEN, GiaoDien.DO_SANG, GiaoDien.DO_TOI, true);
			MFont.borderFont.drawString(g, ten, xChu - 4 + wt / 2, y - 13 + (24 - MFont.borderFont.getHeight()) / 2, MFont.CENTER);
		}
		// chữ: ghép lại rồi ngắt theo bề ngang thật, tối đa 4 dòng
		string toan = string.Join(" ", cau);
		int wChu = x + w - 34 - xChu;
		string[] dong = MFont.blackFont.splitFontBStrInLine(toan, wChu);
		int hDong = MFont.blackFont.getHeight() + 4;
		int soDong = System.Math.Min(dong.Length, (CAO - 30) / hDong);
		int yChu = y + 20;
		for (int i = 0; i < soDong; i++)
		{
			MFont.blackFont.drawString(g, dong[i], xChu, yChu + i * hDong, MFont.LEFT);
		}
		GiaoDien.muiXuong(g, x + w - 24, y + CAO - 20);
	}
}
