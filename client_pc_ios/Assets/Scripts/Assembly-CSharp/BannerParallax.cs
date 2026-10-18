using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Nền key art parallax cho các màn ngoài game (tải, chọn máy chủ, đăng nhập / đăng ký, tạo / chọn Pokémon) ở
/// chế độ Pokémon - cùng một cảnh hoàng hôn với bản J2ME/Android (game.pokemon.Banner), website và ảnh quảng bá
/// (client_jar/tools/tao_banner.py).
///
/// Lớp ảnh: Resources/banner/*.bytes (PNG; BuildCli chép từ client_jar/shared/banner/lop/ + logo từ client_jar/shared/brand/).
/// Thứ tự / chuyển động đọc từ Resources/banner/lop.txt, mỗi dòng: ten,toc_do_px_giay,do_sau(0..1),kieu
///   kieu: phu = phủ màn (khung gốc 880x400, cắt giữa, hiện 360 px chiều cao) | lap = như phu nhưng lặp ngang + trôi
///         | sang = lớp ánh sáng mềm (lọc mượt)
/// Lớp "khoi_dau": vẽ 5 Pokémon khởi đầu bằng PokeBody (thở / chạy động tác đứng) theo đúng đội hình key art; chưa
/// có sprite thì vẽ ảnh tĩnh. Thêm đom đóm lấp lánh. Chuột lệch tâm -> các lớp lệch theo do_sau.
/// Không có lớp nào: cảnh tự vẽ đơn giản (trời, đồi, cỏ).
/// </summary>
public static class BannerParallax
{
	private sealed class Lop
	{
		public string ten;

		public Image anh;

		public float tocDo;

		public float doSau;

		public string kieu = "phu";
	}

	/// <summary>Khung gốc của key art và vùng hiện (16:9 = x120 y20 640x360).</summary>
	private const int GOC_W = 880;

	private const int GOC_H = 400;

	private const int HIEN_Y = 20;

	private const int HIEN_H = 360;

	/// <summary>Đội hình chữ V theo class 0..4 (khớp DOI_HINH trong tao_banner.py và Banner.java), px ở 1x.</summary>
	private static readonly int[] DX = { -64, -124, 0, 64, 122 };

	private static readonly int[] LUI = { 9, 20, 0, 9, 20 };

	private static readonly int[] HUONG = { 3, 0, 0, 2, 0 };

	private static readonly int[] THU_TU = { 1, 4, 0, 3, 2 };

	/// <summary>Tâm đội hình và chân Pokémon gần nhất trong khung gốc (bo_cuc.json: cx, y_khoi).</summary>
	private const int CX = 440;

	private const int Y_KHOI = 344;

	private static List<Lop> lop;

	private static Image logoAnh;

	private static bool daNapLogo;

	private static float lechChuot;

	/// <summary>Pokémon khởi đầu đang chọn (0..4) - màn tạo / chọn Pokémon đặt; -1 = không.</summary>
	public static int chon = -1;

	public static bool bat()
	{
		return PokeBody.CHE_DO_POKEMON;
	}

	private static Image napAnh(string ten)
	{
		try
		{
			if (Resources.Load("banner/" + ten, typeof(TextAsset)) == null)
			{
				return null;
			}
			return Image.createImage("banner/" + ten);
		}
		catch (System.Exception)
		{
			return null;
		}
	}

	/// <summary>Logo wordmark (Resources/banner/logo - client_jar/shared/brand/logo_512.png); null nếu chưa chép.</summary>
	public static Image logo()
	{
		if (!daNapLogo)
		{
			daNapLogo = true;
			logoAnh = napAnh("logo");
			if (logoAnh != null && logoAnh.texture != null)
			{
				logoAnh.texture.filterMode = FilterMode.Bilinear;
			}
		}
		return logoAnh;
	}

	private static void nap()
	{
		if (lop != null)
		{
			return;
		}
		lop = new List<Lop>();
		try
		{
			TextAsset ds = Resources.Load<TextAsset>("banner/lop");
			if (ds != null)
			{
				foreach (string dong0 in ds.text.Split('\n'))
				{
					string dong = dong0.Trim();
					if (dong.Length == 0 || dong.StartsWith("#"))
					{
						continue;
					}
					string[] c = dong.Split(',');
					Image a = napAnh(c[0].Trim());
					if (a == null)
					{
						continue;
					}
					Lop l = new Lop();
					l.ten = c[0].Trim();
					l.anh = a;
					l.tocDo = c.Length > 1 ? float.Parse(c[1].Trim(), System.Globalization.CultureInfo.InvariantCulture) : 0f;
					l.doSau = c.Length > 2 ? float.Parse(c[2].Trim(), System.Globalization.CultureInfo.InvariantCulture) : 0f;
					l.kieu = c.Length > 3 ? c[3].Trim() : "phu";
					if (l.kieu == "sang" && a.texture != null)
					{
						a.texture.filterMode = FilterMode.Bilinear;
					}
					lop.Add(l);
				}
			}
		}
		catch (System.Exception e)
		{
			Debug.LogWarning("Banner: " + e.Message);
		}
	}

	// ------------------------------------------------------------------ vẽ

	/// <summary>Vẽ nền key art phủ cả màn logic (thay ServerListScr.paintBg ở chế độ Pokémon).</summary>
	public static void ve(MyGraphics g)
	{
		ve(g, true);
	}

	public static void ve(MyGraphics g, bool veKhoiDau)
	{
		nap();
		Canvas.resetTrans(g);
		float t = Time.realtimeSinceStartup;
		float muc = Screen.width > 0 ? Mathf.Clamp((Input.mousePosition.x / Screen.width) * 2f - 1f, -1f, 1f) : 0f;
		lechChuot += (muc - lechChuot) * 0.08f;
		if (lop.Count == 0)
		{
			veTuVe(g);
			return;
		}
		// 360 px gốc = chiều cao màn logic (600) -> ở 720p/1080p/1440p toàn màn là phóng nguyên lần 2/3/4
		float s = (float)Canvas.h / HIEN_H;
		int w = Mathf.CeilToInt(GOC_W * s);
		int h = Mathf.CeilToInt(GOC_H * s);
		int y0 = -Mathf.RoundToInt(HIEN_Y * s);
		g.setColor(0x12163D);
		g.fillRect(0, 0, Canvas.w, Canvas.h);
		for (int i = 0; i < lop.Count; i++)
		{
			Lop l = lop[i];
			int x0 = (Canvas.w - w) / 2 - (int)(lechChuot * 14f * s * l.doSau);
			if (l.ten == "khoi_dau" && veKhoiDau)
			{
				if (!veKhoiDauSong(g, x0, y0, s))
				{
					g.drawImageScale(l.anh, x0, y0, w, h, 0);
				}
				veDomDom(g, x0, y0, s);
				continue;
			}
			if (l.ten == "khoi_dau")
			{
				continue;
			}
			if (l.kieu == "lap")
			{
				int dich = (int)(t * l.tocDo * s) % w;
				for (int x = x0 - dich - w; x < Canvas.w; x += w)
				{
					if (x + w > 0)
					{
						g.drawImageScale(l.anh, x, y0, w, h, 0);
					}
				}
				continue;
			}
			g.drawImageScale(l.anh, x0, y0, w, h, 0);
		}
	}

	/// <summary>5 Pokémon khởi đầu sống động theo đội hình; false nếu chưa vẽ được (thiếu sprite).</summary>
	private static bool veKhoiDauSong(MyGraphics g, int x0, int y0, float s)
	{
		int tiLe = Mathf.RoundToInt(200f * s);
		int tick = Canvas.gameTick;
		for (int j = 0; j < THU_TU.Length; j++)
		{
			int i = THU_TU[j];
			int dex = LoaiPokemonClient.khoiDau(i);
			if (dex <= 0)
			{
				return false;
			}
			int x = x0 + (int)((CX + DX[i]) * s);
			int y = y0 + (int)((Y_KHOI - LUI[i]) * s);
			// bóng chân
			g.setColor(0x0E161E, 0.38f);
			int bw = (int)(26 * s);
			int bh = (int)(5 * s);
			g.fillRect(x - bw / 2, y - bh / 2, bw, bh);
			int dy = 0;
			if (i == chon)
			{
				int k = (tick + i * 7) % 24;
				dy = k < 12 ? -(int)((k < 6 ? k : 12 - k) * s) : 0;
			}
			if (!PokeBody.paint(g, dex, false, x, y + dy, HUONG[i], PokeDuLieu.DUNG, tick + i * 9, tiLe))
			{
				return false;
			}
		}
		return true;
	}

	private static void veDomDom(MyGraphics g, int x0, int y0, float s)
	{
		int tick = Canvas.gameTick;
		int tren = y0 + (int)(226 * s);
		int cao = (int)(140 * s);
		for (int i = 0; i < 18; i++)
		{
			int pha = (tick + i * 13) % 48;
			if (pha >= 30)
			{
				continue;
			}
			int x = (i * 211 + tick * (2 + i % 3) / 4) % (Canvas.w + 40) - 20;
			int y = tren + (i * 37 * 3) % cao + (pha < 15 ? pha : 30 - pha) / 2;
			bool to = pha > 8 && pha < 22;
			int c = (int)((to ? 2 : 1) * s * 1.5f);
			g.setColor(to ? 0xFFF7B0 : 0xFFD070, to ? 0.95f : 0.7f);
			g.fillRect(x, y, c, c);
		}
	}

	/// <summary>Chưa có ảnh lớp: trời hoàng hôn + đồi + cỏ vẽ tay (cùng bảng màu key art).</summary>
	private static void veTuVe(MyGraphics g)
	{
		int[] mau = { 0x12163D, 0x272462, 0x553384, 0xA4457F, 0xDE666B, 0xF99B5A, 0xFFD480 };
		int chan = Canvas.h * 58 / 100;
		for (int y = 0; y < chan; y += 4)
		{
			int p = y * (mau.Length - 1) * 100 / chan;
			int i = p / 100;
			g.setColor(GiaoDien.pha(mau[i], mau[Mathf.Min(i + 1, mau.Length - 1)], p % 100, 100));
			g.fillRect(0, y, Canvas.w, 4);
		}
		g.setColor(0x5E4B8E);
		g.fillRect(0, chan - 20, Canvas.w, 20);
		g.setColor(0x3C6539);
		g.fillRect(0, chan, Canvas.w, Canvas.h - chan);
	}

	/// <summary>
	/// Vẽ logo wordmark canh giữa xTam trong khoảng [yTren, yDay] (rộng tối đa rongToiDa, giữ tỉ lệ). false nếu
	/// chưa có ảnh logo.
	/// </summary>
	public static bool veLogo(MyGraphics g, int xTam, int yTren, int yDay, int rongToiDa)
	{
		Image a = logo();
		if (a == null || a.w <= 0)
		{
			return false;
		}
		int w = Mathf.Min(rongToiDa, a.w);
		int h = w * a.h / a.w;
		if (h > yDay - yTren)
		{
			h = Mathf.Max(20, yDay - yTren);
			w = h * a.w / a.h;
		}
		g.drawImageScale(a, xTam - w / 2, yTren, w, h, 0);
		return true;
	}
}
