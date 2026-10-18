using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Du lieu PHU KIEN TRANG BI (Resources/poke/phukien.bytes, sinh boi HTBB tools/pokemon/SinhPhuKien.java -
/// pixel-art tu sinh, khong anh ban quyen): bang mau do hiem, mau co dinh, mau + ten bac, ten do hiem, bang mau
/// trang phuc, hinh tung (o, kieu, huong). Anh phu kien (theo do hiem, lat, ti le, nhip lap lanh) tao mot lan
/// roi nho. Ban PC cua game.pokemon.PhuKien (jar), cung dinh dang tep.
/// </summary>
public static class PhuKien
{
	/// <summary>Mot hinh phu kien (xem SinhPhuKien).</summary>
	public sealed class Hinh
	{
		public int o, kieu, huong, lop, neo, dx, dy, rong, cao, ax, ay, nho;

		/// <summary>chi so mau, rong*cao (hang tren truoc)</summary>
		public byte[] px;
	}

	public const int SAU = 0, TRUOC = 1;

	public const int N_DINH = 0, N_DAU = 1, N_CO = 2, N_TAY = 3, N_TAM = 4;

	private static bool daNap;

	private static bool co;

	private static int[][] hiem;

	private static int[] coDinh;

	private static int[] mauBacTep;

	private static string[] tenBacTep;

	private static string[] tenHiemTep;

	private static int[][] trangPhuc;

	private static string[] tenTrangPhucTep;

	/// <summary>hinh[((o * 16 + kieu) * 3 + huong) * 2 + nho]</summary>
	private static readonly Hinh[] HINH = new Hinh[7 * 16 * 3 * 2];

	/// <summary>Loai ngoai le: dex -&gt; co (1 khong do o co, 2 khong mu, 4 khong do cam, 8 khong sau lung).</summary>
	private static readonly Dictionary<int, int> NGOAI_LE = new Dictionary<int, int>();

	public const int NL_CO = 1, NL_MU = 2, NL_TAY = 4, NL_LUNG = 8;

	/// <summary>Anh vong sang mem duoi chan (elip), khoa so.</summary>
	private static readonly Dictionary<long, Image> ELIP = new Dictionary<long, Image>();

	/// <summary>Anh phu kien da to mau, khoa so nguyen (khong cap phat chuoi moi khung hinh).</summary>
	private static readonly Dictionary<int, Image> ANH = new Dictionary<int, Image>();

	/// <summary>Mau "sang" lap lanh cua do Hoan my (4 nhip).</summary>
	private static readonly int[] LAP_LANH = { 0xFFF8C8, 0xFFD0F0, 0xD0F0FF, 0xD8FFD0 };

	private static readonly int[] CAU_VONG = { 0xFF5050, 0xFFA030, 0xFFE840, 0x58E070, 0x50B8FF, 0xC070FF };

	/// <summary>Nap tep (mot lan). false = khong co phu kien.</summary>
	public static bool nap()
	{
		if (daNap)
		{
			return co;
		}
		daNap = true;
		co = false;
		try
		{
			TextAsset ta = (TextAsset)Resources.Load(PokeBody.THU_MUC + "phukien", typeof(TextAsset));
			if (ta == null || ta.bytes == null || ta.bytes.Length == 0)
			{
				return false;
			}
			byte[] b = ta.bytes;
			int p = 0;
			if (b[p++] != 'K')
			{
				return false;
			}
			int pb = b[p++];
			if (pb != 1 && pb != 2)
			{
				return false;
			}
			int n = b[p++];
			int[][] hiemM = new int[n][];
			for (int i = 0; i < n; i++)
			{
				hiemM[i] = new int[5];
				for (int j = 0; j < 5; j++)
				{
					hiemM[i][j] = rgb(b, ref p);
				}
			}
			n = b[p++];
			int[] coDinhM = new int[n];
			for (int i = 0; i < n; i++)
			{
				coDinhM[i] = rgb(b, ref p);
			}
			n = b[p++];
			int[] mauBacM = new int[n];
			string[] tenBacM = new string[n];
			for (int i = 0; i < n; i++)
			{
				mauBacM[i] = rgb(b, ref p);
				tenBacM[i] = utf(b, ref p);
			}
			n = b[p++];
			string[] tenHiemM = new string[n];
			for (int i = 0; i < n; i++)
			{
				tenHiemM[i] = utf(b, ref p);
			}
			n = b[p++];
			int[][] tpM = new int[n][];
			string[] tenTpM = new string[n];
			for (int i = 0; i < n; i++)
			{
				int[] t = new int[5];
				t[0] = (short)((b[p] << 8) | b[p + 1]);
				p += 2;
				t[1] = b[p++];
				t[2] = b[p++];
				t[3] = rgb(b, ref p);
				t[4] = b[p++];
				tpM[i] = t;
				tenTpM[i] = utf(b, ref p);
			}
			n = (b[p] << 8) | b[p + 1];
			p += 2;
			for (int i = 0; i < n; i++)
			{
				Hinh h = new Hinh();
				h.o = b[p++];
				h.kieu = b[p++];
				h.huong = b[p++];
				h.lop = b[p++];
				h.neo = b[p++];
				h.dx = (sbyte)b[p++];
				h.dy = (sbyte)b[p++];
				h.rong = b[p++];
				h.cao = b[p++];
				h.ax = (sbyte)b[p++];
				h.ay = (sbyte)b[p++];
				h.nho = pb >= 2 ? b[p++] & 1 : 0;
				int sp = h.rong * h.cao;
				h.px = new byte[sp];
				for (int k = 0; k < sp; k += 2)
				{
					int v = b[p++];
					h.px[k] = (byte)(v >> 4);
					if (k + 1 < sp)
					{
						h.px[k + 1] = (byte)(v & 0x0F);
					}
				}
				if (h.o < 7 && h.kieu < 16 && h.huong < 3)
				{
					HINH[((h.o * 16 + h.kieu) * 3 + h.huong) * 2 + h.nho] = h;
				}
			}
			NGOAI_LE.Clear();
			if (pb >= 2)
			{
				int nl = (b[p] << 8) | b[p + 1];
				p += 2;
				for (int i = 0; i < nl; i++)
				{
					int dex = (b[p] << 8) | b[p + 1];
					NGOAI_LE[dex] = b[p + 2];
					p += 3;
				}
			}
			hiem = hiemM;
			coDinh = coDinhM;
			mauBacTep = mauBacM;
			tenBacTep = tenBacM;
			tenHiemTep = tenHiemM;
			trangPhuc = tpM;
			tenTrangPhucTep = tenTpM;
			co = hiem.Length > 0 && coDinh.Length > 0;
		}
		catch (System.Exception)
		{
			co = false;
		}
		return co;
	}

	private static int rgb(byte[] b, ref int p)
	{
		int v = (b[p] << 16) | (b[p + 1] << 8) | b[p + 2];
		p += 3;
		return v;
	}

	/// <summary>Chuoi kieu DataOutputStream.writeUTF: u16 do dai + byte UTF-8.</summary>
	private static string utf(byte[] b, ref int p)
	{
		int n = (b[p] << 8) | b[p + 1];
		p += 2;
		string s = System.Text.Encoding.UTF8.GetString(b, p, n);
		p += n;
		return s;
	}

	/// <summary>Hinh (o, kieu, huong); nho = uu tien ban nho (khong co thi tra ban thuong).</summary>
	public static Hinh hinh(int o, int kieu, int huong, bool nho)
	{
		if (!nap() || o < 0 || o >= 7 || kieu <= 0 || kieu >= 16 || huong < 0 || huong > 2)
		{
			return null;
		}
		int k = ((o * 16 + kieu) * 3 + huong) * 2;
		if (nho && HINH[k + 1] != null)
		{
			return HINH[k + 1];
		}
		return HINH[k];
	}

	/// <summary>Co ngoai le cua loai (0 = binh thuong).</summary>
	public static int ngoaiLe(int dex)
	{
		if (!nap())
		{
			return 0;
		}
		int v;
		return NGOAI_LE.TryGetValue(dex, out v) ? v : 0;
	}

	/// <summary>
	/// Vong sang mem duoi chan: elip rong 2rw x cao 2rh, alpha giam dan tu tam (toi da aMax/255), mau rgb. Mot
	/// anh cho moi (rw, rh, mau, aMax); bang rieng nho.
	/// </summary>
	public static Image elip(int rw, int rh, int mau, int aMax)
	{
		if (rw <= 0 || rh <= 0)
		{
			return null;
		}
		long khoa = ((((long)(rw & 0xFF) * 256 + (rh & 0xFF)) * 256 + (aMax & 0xFF)) << 24) | (long)(mau & 0xFFFFFF);
		Image img;
		if (ELIP.TryGetValue(khoa, out img))
		{
			return img;
		}
		if (ELIP.Count > 200)
		{
			foreach (Image cu in ELIP.Values)
			{
				huyAnh(cu);
			}
			ELIP.Clear();
		}
		int W = rw * 2 + 1, H = rh * 2 + 1;
		Color32[] px = new Color32[W * H];
		long rw2 = rw * rw, rh2 = rh * rh;
		byte cr = (byte)((mau >> 16) & 0xFF), cg = (byte)((mau >> 8) & 0xFF), cb = (byte)(mau & 0xFF);
		for (int y = 0; y < H; y++)
		{
			int dy = y - rh;
			for (int x = 0; x < W; x++)
			{
				int dx = x - rw;
				// d = khoang cach chuan hoa * 256
				long d2 = ((long)dx * dx * 65536) / rw2 + ((long)dy * dy * 65536) / rh2;
				if (d2 >= 65536)
				{
					continue;
				}
				int d = 0;
				while ((long)(d + 1) * (d + 1) <= d2)
				{
					d++;
				}
				int a = aMax * (256 - d) / 256;
				px[y * W + x] = new Color32(cr, cg, cb, (byte)(a < 0 ? 0 : a > 255 ? 255 : a));
			}
		}
		try
		{
			img = taoAnh(px, W, H);
		}
		catch (System.Exception)
		{
			return null;
		}
		ELIP[khoa] = img;
		return img;
	}

	/// <summary>Mau vang sang cua bac (0xRRGGBB); bac 10 (toi thuong) doi mau cau vong theo tick (25/giay).</summary>
	public static int mauBac(int bac, int tick)
	{
		if (bac >= 10)
		{
			return cauVong(tick / 3);
		}
		if (!nap() || bac < 0 || bac >= mauBacTep.Length)
		{
			return 0xFFFFFF;
		}
		return mauBacTep[bac];
	}

	public static int cauVong(int i)
	{
		return CAU_VONG[(i % 6 + 6) % 6];
	}

	public static string tenBac(int b)
	{
		return nap() && b >= 0 && b < tenBacTep.Length ? tenBacTep[b] : string.Empty;
	}

	public static string tenHiem(int h)
	{
		return nap() && h >= 0 && h < tenHiemTep.Length ? tenHiemTep[h] : string.Empty;
	}

	public static int mauHiem(int h)
	{
		return nap() && h >= 0 && h < hiem.Length ? hiem[h][2] : 0xFFFFFF;
	}

	public static string tenTrangPhuc(int tp)
	{
		return nap() && tp >= 1 && tp <= tenTrangPhucTep.Length ? tenTrangPhucTep[tp - 1] : string.Empty;
	}

	public static int soTrangPhuc()
	{
		return nap() ? trangPhuc.Length : 0;
	}

	/// <summary>Dong mang Color32 (hang TREN truoc) thanh Image (Texture2D loc Point, khong doc lai).</summary>
	public static Image taoAnh(Color32[] tren, int w, int h)
	{
		Texture2D t = new Texture2D(w, h, TextureFormat.RGBA32, false);
		t.filterMode = FilterMode.Point;
		t.wrapMode = TextureWrapMode.Clamp;
		Color32[] lat = new Color32[w * h];
		for (int y = 0; y < h; y++)
		{
			System.Array.Copy(tren, y * w, lat, (h - 1 - y) * w, w);
		}
		t.SetPixels32(lat);
		t.Apply(false, true);
		Image a = new Image();
		Object.Destroy(a.texture); // anh 1x1 mac dinh cua Image
		a.texture = t;
		a.w = w;
		a.h = h;
		return a;
	}

	public static void huyAnh(Image a)
	{
		if (a != null && a.texture != null)
		{
			Object.Destroy(a.texture);
		}
	}

	private static Color32 c32(int rgb)
	{
		return new Color32((byte)((rgb >> 16) & 0xFF), (byte)((rgb >> 8) & 0xFF), (byte)(rgb & 0xFF), 255);
	}

	/// <summary>
	/// Anh phu kien: do hiem h, lat guong, phong k lan, nhip lap lanh (chi do Hoan my, 0..3). Nho trong bang
	/// rieng (vai tram anh nho); qua 400 anh thi xoa het.
	/// </summary>
	public static Image anh(Hinh hi, int h, bool lat, int k, int nhay)
	{
		if (hi == null || !nap())
		{
			return null;
		}
		if (h < 0 || h >= hiem.Length)
		{
			h = 0;
		}
		if (h != hiem.Length - 1)
		{
			nhay = 0;
		}
		if (k < 1)
		{
			k = 1;
		}
		int khoa = (((((((hi.o * 16 + hi.kieu) * 3 + hi.huong) * 2 + (hi.nho & 1)) * 16 + h) * 2 + (lat ? 1 : 0)) * 4 + (k & 3)) * 4) + (nhay & 3);
		Image img;
		if (ANH.TryGetValue(khoa, out img))
		{
			return img;
		}
		if (ANH.Count > 400)
		{
			foreach (Image cu in ANH.Values)
			{
				huyAnh(cu);
			}
			ANH.Clear();
		}
		int W = hi.rong * k, H = hi.cao * k;
		if (W <= 0 || H <= 0)
		{
			ANH[khoa] = null;
			return null;
		}
		Color32[] px = new Color32[W * H];
		int[] bang = hiem[h];
		for (int y = 0; y < hi.cao; y++)
		{
			for (int x = 0; x < hi.rong; x++)
			{
				int c = hi.px[y * hi.rong + (lat ? hi.rong - 1 - x : x)];
				if (c == 0)
				{
					continue;
				}
				int rgbM;
				if (c <= 5)
				{
					rgbM = (c == 4 && h == hiem.Length - 1) ? LAP_LANH[nhay & 3] : bang[c - 1];
				}
				else
				{
					rgbM = coDinh[c - 6 < coDinh.Length ? c - 6 : 0];
				}
				Color32 m = c32(rgbM);
				for (int yy = 0; yy < k; yy++)
				{
					for (int xx = 0; xx < k; xx++)
					{
						px[(y * k + yy) * W + x * k + xx] = m;
					}
				}
			}
		}
		try
		{
			img = taoAnh(px, W, H);
		}
		catch (System.Exception)
		{
			return null; // vd ngoai luong chinh: lan sau thu lai
		}
		ANH[khoa] = img;
		return img;
	}

	private static int cosTp = 256, sinTp, tpDangTinh = -1;

	/// <summary>
	/// Doi mau trang phuc tp (1..) tai cho tren mang diem anh: xoay mau (do), bao hoa %, do sang %, phu mau %.
	/// Chi so nguyen + mot lan Math.Cos/Sin cho moi trang phuc. Giu nguyen alpha.
	/// </summary>
	public static void doiMauTrangPhuc(Color32[] px, int tp)
	{
		if (!nap() || tp < 1 || tp > trangPhuc.Length)
		{
			return;
		}
		int[] t = trangPhuc[tp - 1];
		if (tpDangTinh != tp)
		{
			double rad = t[0] * System.Math.PI / 180.0;
			cosTp = (int)(System.Math.Cos(rad) * 256);
			sinTp = (int)(System.Math.Sin(rad) * 256);
			tpDangTinh = tp;
		}
		int sat = t[1], val = t[2], tint = t[3], pct = t[4];
		int tr = (tint >> 16) & 0xFF, tg = (tint >> 8) & 0xFF, tb = tint & 0xFF;
		// ma tran xoay mau quanh truc xam (Paeth / Haeberli, he so *256)
		int c = cosTp, s = sinTp;
		int m00 = (77 * 256 + c * (256 - 77) - s * 77) / 256, m01 = (150 * 256 - c * 150 - s * 150) / 256, m02 = (29 * 256 - c * 29 + s * (256 - 29)) / 256;
		int m10 = (77 * 256 - c * 77 + s * 36) / 256, m11 = (150 * 256 + c * (256 - 150) + s * 36) / 256, m12 = (29 * 256 - c * 29 - s * 72) / 256;
		int m20 = (77 * 256 - c * 77 - s * (256 - 77)) / 256, m21 = (150 * 256 - c * 150 + s * 150) / 256, m22 = (29 * 256 + c * (256 - 29) + s * 29) / 256;
		bool xoay = t[0] != 0;
		for (int i = 0; i < px.Length; i++)
		{
			Color32 p = px[i];
			if (p.a == 0)
			{
				continue;
			}
			int r = p.r, g = p.g, b = p.b;
			if (xoay)
			{
				int r2 = (m00 * r + m01 * g + m02 * b) >> 8;
				int g2 = (m10 * r + m11 * g + m12 * b) >> 8;
				int b2 = (m20 * r + m21 * g + m22 * b) >> 8;
				r = r2;
				g = g2;
				b = b2;
			}
			int xam = (r * 77 + g * 150 + b * 29) >> 8;
			r = xam + (r - xam) * sat / 100;
			g = xam + (g - xam) * sat / 100;
			b = xam + (b - xam) * sat / 100;
			r = r * val / 100;
			g = g * val / 100;
			b = b * val / 100;
			// phu mau theo do sang (giu bong toi: vung toi it bi phu)
			int w = pct * (xam + 64) / 320;
			r += (tr * xam / 255 - r) * w / 100;
			g += (tg * xam / 255 - g) * w / 100;
			b += (tb * xam / 255 - b) * w / 100;
			p.r = (byte)(r < 0 ? 0 : r > 255 ? 255 : r);
			p.g = (byte)(g < 0 ? 0 : g > 255 ? 255 : g);
			p.b = (byte)(b < 0 ? 0 : b > 255 ? 255 : b);
			px[i] = p;
		}
	}
}
