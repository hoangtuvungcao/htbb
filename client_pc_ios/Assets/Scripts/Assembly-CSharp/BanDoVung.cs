/// <summary>
/// "Bản đồ lớn" kiểu Pokémon - bản PC của game.pokemon.BanDoVung (bản jar): vẽ bằng code thay ảnh bản đồ
/// kiếm hiệp (địa danh in sẵn). Biển xanh, đảo cỏ quanh mỗi địa điểm, đường cát nối 2 điểm gần nhất,
/// chấm đỏ = thị trấn, điểm đang đứng nhấp nháy vàng + huy hiệu Pokémon của người chơi + tên.
/// Tọa độ điểm như bản gốc (ảnh 197 x 168), co giãn theo khung w x h (gốc tọa độ = góc khung).
/// Phần tĩnh (biển, đảo, đường, chấm) đóng một ảnh theo (w, h, điểm đang đứng); sóng / vòng sáng vẽ mỗi khung.
/// </summary>
public static class BanDoVung
{
	private static int qx(int x, int w)
	{
		return 6 + (x - 14) * (w - 12) / 172;
	}

	private static int qy(int y, int h)
	{
		return 34 + (y - 48) * (h - 44) / 122;
	}

	public static void ve(MyGraphics g, short[][] pts, int cur, int w, int h)
	{
		int n = pts.Length;
		int[] x = new int[n];
		int[] y = new int[n];
		for (int i = 0; i < n; i++)
		{
			x[i] = qx(pts[i][0], w);
			y[i] = qy(pts[i][1], h);
		}
		int bx = 4;
		int by = 29;
		int bw = w - 8;
		int bh = h - 33;
		string k = "bdv|" + w + "|" + h + "|" + cur + "|" + n;
		Image a = GiaoDien.layAnh(k);
		if (a == null)
		{
			BucVe b = new BucVe(bw, bh);
			b.translate(-bx, -by);
			GiaoDien.khoiDocB(b, bx, by, bw, bh, 3, 0x5FA8E0, 0x2E6FB8);
			for (int i = 0; i < n; i++)
			{
				GiaoDien.khoiB(b, x[i] - 15, y[i] - 12, 30, 24, 11, 0xE8D8A0);
			}
			for (int i = 0; i < n; i++)
			{
				GiaoDien.khoiB(b, x[i] - 13, y[i] - 10, 26, 20, 9, GiaoDien.LA);
			}
			b.setColor(0xF0E2B0);
			for (int i = 0; i < n; i++)
			{
				int ia = -1;
				int ib = -1;
				int da = int.MaxValue;
				int db = int.MaxValue;
				for (int j = 0; j < n; j++)
				{
					if (j == i)
					{
						continue;
					}
					int dx = x[j] - x[i];
					int dy = y[j] - y[i];
					int d = dx * dx + dy * dy;
					if (d == 0)
					{
						continue;
					}
					if (d < da)
					{
						db = da;
						ib = ia;
						da = d;
						ia = j;
					}
					else if (d < db)
					{
						db = d;
						ib = j;
					}
				}
				if (ia >= 0)
				{
					b.drawLine(x[i], y[i], x[ia], y[ia]);
					b.drawLine(x[i], y[i] + 1, x[ia], y[ia] + 1);
				}
				if (ib >= 0)
				{
					b.drawLine(x[i], y[i], x[ib], y[ib]);
				}
			}
			for (int i = 0; i < n; i++)
			{
				GiaoDien.khoiB(b, x[i] - 4, y[i] - 4, 9, 9, 4, GiaoDien.VIEN);
				GiaoDien.khoiB(b, x[i] - 3, y[i] - 3, 7, 7, 3, i == cur ? GiaoDien.VANG : GiaoDien.DO);
				b.setColor(GiaoDien.TRANG);
				b.fillRect(x[i] - 1, y[i] - 2, 2, 1);
			}
			a = GiaoDien.luuAnh(k, b);
		}
		g.drawImage(a, bx, by);
		// sóng nhỏ trôi
		g.setColor(0x8CC4F0);
		for (int i = 0; i < 14; i++)
		{
			int sx = bx + (i * 53 + Canvas.gameTick / 4) % (bw - 4);
			int sy = by + 6 + i * 37 % (bh - 8);
			g.fillRect(sx, sy, 4, 1);
		}
		if (cur >= 0 && cur < n)
		{
			int r = 6 + (Canvas.gameTick % 12 < 6 ? 1 : 0);
			GiaoDien.vong(g, x[cur], y[cur], r, GiaoDien.VANG);
			int yCd = y[cur] - 20;
			if (yCd < by + 13)
			{
				yCd = y[cur] + 20;
			}
			MainChar mc = Canvas.gameScr != null ? Canvas.gameScr.mainChar : null;
			int dex = mc != null ? mc.pokeDex : 0;
			if (dex <= 0 || !ChanDungPoke.paintHuyHieu(g, dex, mc.pokeShiny, x[cur], yCd, 26))
			{
				GiaoDien.pokeBall(g, x[cur], yCd, 8, 0);
			}
		}
		if (cur >= 0 && cur < Res.nameBigMap.Length)
		{
			string ten = TenMapPokemon.doi(Res.nameBigMap[cur]);
			int tw = MFont.borderFont.getWidth(ten) + 14;
			GiaoDien.khung(g, w / 2 - tw / 2, h - 22, tw, 18, 8, GiaoDien.VIEN, GiaoDien.XANH_SANG, GiaoDien.NAVY, false);
			MFont.borderFont.drawString(g, ten, w / 2, h - 21, MFont.CENTER);
		}
	}
}
