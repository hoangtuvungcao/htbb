using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Chân dung Pokémon "liền mạch" cho bản PC (thay ô chân dung PMD có nền in sẵn, nhìn rời rạc trên bản đồ):
///  - huyHieu: hình nhân vật cắt sát (khung đứng, hướng xuống, nền trong suốt từ atlas sprite) đặt trên
///    huy hiệu tròn màu hệ có vòng viền - dùng cho HUD, góc tên, đội, màn chọn Pokémon.
///  - tron: chân dung lớn (ảnh _cd) che tròn sẵn (pre-masked: điểm ảnh ngoài vòng tròn trong suốt) + khung
///    viền trắng/tối - dùng cho hộp thoại (không cần shader).
/// Ảnh tạo một lần theo (dex, shiny, cỡ) rồi giữ lại (BucVe -> Texture2D lọc Point, sắc nét khi phóng).
/// Texture sprite/chân dung nạp bằng LoadImage nên đọc được điểm ảnh; lỗi thì quay về paintChanDung cũ.
/// </summary>
public static class ChanDungPoke
{
	private static readonly Dictionary<string, Image> dem = new Dictionary<string, Image>();

	private static readonly HashSet<string> loi = new HashSet<string>();

	private const int TOI_DA = 300;

	private static Image layDem(string k)
	{
		Image a;
		return dem.TryGetValue(k, out a) ? a : null;
	}

	private static Image luu(string k, BucVe b)
	{
		if (dem.Count >= TOI_DA)
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
		dem[k] = a;
		return a;
	}

	private static int argb(Color32 c)
	{
		return c.a << 24 | c.r << 16 | c.g << 8 | c.b;
	}

	/// <summary>Màu hệ chính của loài (bảng HePoke), Thường nếu không biết.</summary>
	public static int mauHe(int dex)
	{
		return KieuO.mauHe(HePoke.he1(dex));
	}

	/// <summary>Huy hiệu tròn đường kính d: vòng tối + vòng màu hệ sáng + đĩa màu hệ, hình cắt sát ở giữa.</summary>
	public static Image huyHieu(int dex, bool shiny, int d)
	{
		if (dex <= 0 || d < 8)
		{
			return null;
		}
		string k = "hh|" + dex + "|" + (shiny ? 1 : 0) + "|" + d;
		Image a = layDem(k);
		if (a != null || loi.Contains(k))
		{
			return a;
		}
		try
		{
			int mau = mauHe(dex);
			BucVe b = new BucVe(d, d);
			b.setColor(GiaoDien.VIEN);
			b.fillArc(0, 0, d, d, 0, 360);
			b.setColor(GiaoDien.pha(mau, GiaoDien.TRANG, 2, 3));
			b.fillArc(1, 1, d - 2, d - 2, 0, 360);
			int dTrong = d - 6;
			// đĩa trong: dải màu hệ trên sáng -> dưới đậm
			for (int y = 0; y < dTrong; y++)
			{
				b.setColor(GiaoDien.pha(GiaoDien.pha(mau, GiaoDien.TRANG, 1, 3), GiaoDien.pha(mau, 0, 1, 3), y, dTrong));
				for (int x = 0; x < dTrong; x++)
				{
					float dx = x + 0.5f - dTrong / 2f;
					float dy = y + 0.5f - dTrong / 2f;
					if (dx * dx + dy * dy <= dTrong * dTrong / 4f)
					{
						b.fillRect(3 + x, 3 + y, 1, 1);
					}
				}
			}
			if (!veHinhCat(b, dex, shiny, 3, 3, dTrong))
			{
				loi.Add(k);
				return null;
			}
			a = luu(k, b);
			return a;
		}
		catch (System.Exception)
		{
			loi.Add(k);
			return null;
		}
	}

	/// <summary>
	/// Hình nhân vật cắt sát vùng có điểm ảnh của khung đứng-hướng-xuống, phóng (lân cận gần nhất) vừa ~85%
	/// đĩa đường kính dd tại (x0, y0); chỉ vẽ điểm nằm trong đĩa (mặt nạ tròn).
	/// </summary>
	private static bool veHinhCat(BucVe b, int dex, bool shiny, int x0, int y0, int dd)
	{
		PokeBody pb = PokeBody.lay(dex, shiny);
		if (pb == null || pb.anh == null || pb.anh.texture == null)
		{
			return false;
		}
		PokeDongTac dt = pb.dl.layDongTac(PokeDuLieu.DUNG);
		Texture2D t = pb.anh.texture;
		int fw = dt.rongKhung;
		int fh = dt.caoKhung;
		int sx = dt.xAtlas;
		int sy = dt.yHang(0);
		if (fw <= 0 || fh <= 0 || sx + fw > t.width || sy + fh > t.height)
		{
			return false;
		}
		// Texture2D: hàng 0 là đáy ảnh
		Color32[] p = t.GetPixels32();
		int tw = t.width;
		int th = t.height;
		int minX = fw;
		int minY = fh;
		int maxX = -1;
		int maxY = -1;
		for (int y = 0; y < fh; y++)
		{
			int hang = (th - 1 - (sy + y)) * tw;
			for (int x = 0; x < fw; x++)
			{
				if (p[hang + sx + x].a > 16)
				{
					if (x < minX)
					{
						minX = x;
					}
					if (x > maxX)
					{
						maxX = x;
					}
					if (y < minY)
					{
						minY = y;
					}
					if (y > maxY)
					{
						maxY = y;
					}
				}
			}
		}
		if (maxX < 0)
		{
			return false;
		}
		int cw = maxX - minX + 1;
		int ch = maxY - minY + 1;
		int dich = dd * 85 / 100;
		// phóng số nguyên khi được (pixel sắc), không thì tỉ lệ thực
		float tl = (float)dich / System.Math.Max(cw, ch);
		if (tl >= 1f)
		{
			tl = Mathf.Floor(tl);
		}
		int rw = System.Math.Max(1, (int)(cw * tl));
		int rh = System.Math.Max(1, (int)(ch * tl));
		int ox = x0 + (dd - rw) / 2;
		int oy = y0 + (dd - rh) / 2 + dd / 20;
		float r2 = dd * dd / 4f;
		for (int y = 0; y < rh; y++)
		{
			int syy = minY + (int)(y / tl);
			int hang = (th - 1 - (sy + syy)) * tw;
			for (int x = 0; x < rw; x++)
			{
				int px = ox + x;
				int py = oy + y;
				float dx = px + 0.5f - (x0 + dd / 2f);
				float dy = py + 0.5f - (y0 + dd / 2f);
				if (dx * dx + dy * dy > r2)
				{
					continue;
				}
				Color32 c = p[hang + sx + minX + (int)(x / tl)];
				if (c.a == 0)
				{
					continue;
				}
				b.setColorA(argb(c));
				b.fillRect(px, py, 1, 1);
			}
		}
		return true;
	}

	/// <summary>Chân dung lớn che tròn sẵn + khung (viền tối ngoài, viền trắng trong), đường kính d.</summary>
	public static Image tron(int dex, bool shiny, int d)
	{
		if (dex <= 0 || d < 8)
		{
			return null;
		}
		string k = "ct|" + dex + "|" + (shiny ? 1 : 0) + "|" + d;
		Image a = layDem(k);
		if (a != null || loi.Contains(k))
		{
			return a;
		}
		try
		{
			Image cd = PokeBody.chanDung(dex, shiny);
			if (cd == null || cd.texture == null)
			{
				loi.Add(k);
				return null;
			}
			Texture2D t = cd.texture;
			Color32[] p = t.GetPixels32();
			int tw = t.width;
			int th = t.height;
			BucVe b = new BucVe(d, d);
			b.setColor(GiaoDien.VIEN);
			b.fillArc(0, 0, d, d, 0, 360);
			b.setColor(GiaoDien.TRANG);
			b.fillArc(1, 1, d - 2, d - 2, 0, 360);
			int dd = d - 6;
			float r2 = dd * dd / 4f;
			for (int y = 0; y < dd; y++)
			{
				for (int x = 0; x < dd; x++)
				{
					float dx = x + 0.5f - dd / 2f;
					float dy = y + 0.5f - dd / 2f;
					if (dx * dx + dy * dy > r2)
					{
						continue;
					}
					int sxx = x * tw / dd;
					int syy = y * th / dd;
					Color32 c = p[(th - 1 - syy) * tw + sxx];
					c.a = 255;
					b.setColorA(argb(c));
					b.fillRect(3 + x, 3 + y, 1, 1);
				}
			}
			a = luu(k, b);
			return a;
		}
		catch (System.Exception)
		{
			loi.Add(k);
			return null;
		}
	}

	/// <summary>Vẽ huy hiệu tâm (cx, cy) đường kính d; false nếu không có hình (người gọi vẽ kiểu cũ).</summary>
	public static bool paintHuyHieu(MyGraphics g, int dex, bool shiny, int cx, int cy, int d)
	{
		Image a = huyHieu(dex, shiny, d);
		if (a == null)
		{
			return PokeBody.paintChanDung(g, dex, shiny, cx, cy, d);
		}
		g.drawImage(a, cx - d / 2, cy - d / 2);
		return true;
	}

	/// <summary>Vẽ chân dung lớn che tròn tâm (cx, cy) đường kính d (hộp thoại).</summary>
	public static bool paintTron(MyGraphics g, int dex, bool shiny, int cx, int cy, int d)
	{
		Image a = tron(dex, shiny, d);
		if (a == null)
		{
			return PokeBody.paintChanDung(g, dex, shiny, cx, cy, d);
		}
		g.drawImage(a, cx - d / 2, cy - d / 2);
		return true;
	}
}
