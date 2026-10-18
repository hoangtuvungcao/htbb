using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Nền banner parallax cho các màn ngoài game (chọn máy chủ, đăng nhập / đăng ký, tạo / chọn Pokémon) ở
/// chế độ Pokémon - thay cảnh bản đồ phía sau (ServerListScr.paintBg).
///
/// Lớp ảnh: Resources/banner/ (chép từ htbb-client/shared/banner/ - BuildCli tự chép trước khi build nếu
/// thư mục Resources/banner chưa có). Thứ tự / tốc độ đọc từ Resources/banner/lop.txt nếu có, mỗi dòng:
///   ten_anh,toc_do_px_giay,do_sau(0..1)        vd: troi,0,0  |  may,6,0.15  |  doi,14,0.5  |  co,28,1
/// Không có lop.txt: nạp lop0, lop1, ... (xa -> gần, tốc độ tăng dần). Không có lớp nào nhưng có ảnh
/// ghép banner/banner (1920x1080): vẽ ảnh đó phủ màn + trôi nhẹ theo chuột. Không có gì: cảnh tự vẽ
/// (trời, mây, núi xa, đồi, cỏ - BucVe) cùng hiệu ứng parallax.
/// Mỗi lớp phóng theo chiều cao màn logic (giữ tỉ lệ), lặp ngang, trôi theo thời gian + lệch theo chuột
/// (do_sau càng lớn càng lệch nhiều).
/// </summary>
public static class BannerParallax
{
	private sealed class Lop
	{
		public Image anh;

		public float tocDo;

		public float doSau;

		/// <summary>true = phóng theo chiều cao màn; false = giữ cỡ gốc * tỉ lệ, neo đáy (lớp tự vẽ).</summary>
		public bool phuCao = true;

		public int yNeo;

		public bool keoGian;
	}

	private static List<Lop> lop;

	private static Image anhGhep;

	private static float lechChuot;

	public static bool bat()
	{
		return PokeBody.CHE_DO_POKEMON;
	}

	private static Image boc(Texture2D t)
	{
		if (t == null)
		{
			return null;
		}
		t.wrapMode = TextureWrapMode.Clamp;
		Image a = new Image();
		UnityEngine.Object.Destroy(a.texture);
		a.texture = t;
		a.w = t.width;
		a.h = t.height;
		return a;
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
					Image a = boc(Resources.Load<Texture2D>("banner/" + c[0].Trim()));
					if (a == null)
					{
						continue;
					}
					Lop l = new Lop();
					l.anh = a;
					l.tocDo = c.Length > 1 ? float.Parse(c[1].Trim(), System.Globalization.CultureInfo.InvariantCulture) : 0f;
					l.doSau = c.Length > 2 ? float.Parse(c[2].Trim(), System.Globalization.CultureInfo.InvariantCulture) : 0f;
					lop.Add(l);
				}
			}
			else
			{
				for (int i = 0; i < 12; i++)
				{
					Image a = boc(Resources.Load<Texture2D>("banner/lop" + i));
					if (a == null)
					{
						break;
					}
					Lop l = new Lop();
					l.anh = a;
					l.tocDo = i * 6f;
					l.doSau = i * 0.12f;
					lop.Add(l);
				}
			}
			if (lop.Count == 0)
			{
				anhGhep = boc(Resources.Load<Texture2D>("banner/banner"));
			}
		}
		catch (System.Exception e)
		{
			Debug.LogWarning("Banner: " + e.Message);
		}
		if (lop.Count == 0 && anhGhep == null)
		{
			taoCanhTuVe();
		}
	}

	// ------------------------------------------------------------------ cảnh tự vẽ (chưa có ảnh banner)

	private static void taoCanhTuVe()
	{
		// trời: dải dọc 1 px kéo giãn ngang
		BucVe troi = new BucVe(1, 300);
		for (int y = 0; y < 300; y++)
		{
			troi.setColor(GiaoDien.pha(0x4A90E2, 0xD6ECFA, y, 300));
			troi.fillRect(0, y, 1, 1);
		}
		Lop lTroi = new Lop();
		lTroi.anh = troi.taoAnh();
		lTroi.keoGian = true;
		lop.Add(lTroi);
		// mây
		BucVe may = new BucVe(640, 140);
		veMay(may, 40, 30, 14);
		veMay(may, 300, 70, 10);
		veMay(may, 500, 20, 12);
		lop.Add(lopTuVe(may.taoAnh(), 8f, 0.1f, 40));
		// núi xa
		BucVe nui = new BucVe(640, 180);
		nui.setColor(0x8DB6D8);
		nui.fillTriangle(-40, 180, 120, 30, 280, 180);
		nui.fillTriangle(200, 180, 380, 10, 560, 180);
		nui.fillTriangle(480, 180, 620, 60, 760, 180);
		nui.setColor(0xF2F6FA);
		nui.fillTriangle(102, 48, 120, 30, 138, 48);
		nui.fillTriangle(358, 32, 380, 10, 402, 32);
		lop.Add(lopTuVe(nui.taoAnh(), 4f, 0.2f, 230));
		// đồi
		BucVe doi = new BucVe(640, 200);
		doi.setColor(0x58A848);
		doi.fillArc(-160, 40, 520, 320, 0, 180);
		doi.setColor(0x4E9A40);
		doi.fillArc(220, 70, 560, 300, 0, 180);
		lop.Add(lopTuVe(doi.taoAnh(), 12f, 0.45f, 330));
		// cỏ gần
		BucVe co = new BucVe(640, 110);
		co.setColor(GiaoDien.LA);
		co.fillRect(0, 10, 640, 100);
		co.setColor(0x9AD870);
		co.fillRect(0, 10, 640, 2);
		co.setColor(0x68B840);
		for (int y = 18; y < 110; y += 12)
		{
			for (int x = (y / 12 % 2) * 11; x < 640; x += 22)
			{
				co.fillRect(x, y, 2, 5);
				co.fillRect(x + 3, y + 1, 2, 4);
			}
		}
		lop.Add(lopTuVe(co.taoAnh(), 26f, 1f, 490));
	}

	private static Lop lopTuVe(Image a, float tocDo, float doSau, int yNeo)
	{
		Lop l = new Lop();
		l.anh = a;
		l.tocDo = tocDo;
		l.doSau = doSau;
		l.phuCao = false;
		l.yNeo = yNeo;
		return l;
	}

	private static void veMay(BucVe g, int x, int y, int s)
	{
		g.setColor(GiaoDien.TRANG);
		g.fillArc(x, y, s * 2, s * 2 - 4, 0, 360);
		g.fillArc(x + s, y - s / 2, s * 2 + 2, s * 2, 0, 360);
		g.fillArc(x + s * 2 + 2, y + 1, s * 2 - 2, s * 2 - 6, 0, 360);
		g.fillRect(x + s / 2, y + s, s * 3, s - 3);
	}

	// ------------------------------------------------------------------ vẽ

	/// <summary>Vẽ nền banner phủ cả màn logic (thay ServerListScr.paintBg ở chế độ Pokémon).</summary>
	public static void ve(MyGraphics g)
	{
		nap();
		Canvas.resetTrans(g);
		float t = Time.realtimeSinceStartup;
		// chuột lệch khỏi tâm (-1..1), làm mượt
		float muc = Screen.width > 0 ? Mathf.Clamp((Input.mousePosition.x / Screen.width) * 2f - 1f, -1f, 1f) : 0f;
		lechChuot += (muc - lechChuot) * 0.08f;
		if (anhGhep != null)
		{
			float tl = Mathf.Max((float)Canvas.w / anhGhep.w, (float)Canvas.h / anhGhep.h) * 1.04f;
			int w = (int)(anhGhep.w * tl);
			int h = (int)(anhGhep.h * tl);
			int x = (Canvas.w - w) / 2 - (int)(lechChuot * (w - Canvas.w) / 2f);
			g.drawImageScale(anhGhep, x, (Canvas.h - h) / 2, w, h, 0);
			return;
		}
		for (int i = 0; i < lop.Count; i++)
		{
			Lop l = lop[i];
			if (l.anh == null)
			{
				continue;
			}
			if (l.keoGian)
			{
				g.drawImageScale(l.anh, 0, 0, Canvas.w, Canvas.h, 0);
				continue;
			}
			float tl = l.phuCao ? (float)Canvas.h / l.anh.h : Canvas.h / 600f;
			int w = Mathf.Max(1, (int)(l.anh.w * tl));
			int h = (int)(l.anh.h * tl);
			int y = l.phuCao ? 0 : (int)(l.yNeo * tl);
			if (!l.phuCao && y + h < Canvas.h && i == lop.Count - 1)
			{
				y = Canvas.h - h; // lớp gần nhất luôn chạm đáy màn
			}
			float dich = t * l.tocDo + lechChuot * 30f * l.doSau;
			int x0 = -(int)(((dich % w) + w) % w);
			for (int x = x0; x < Canvas.w; x += w)
			{
				g.drawImageScale(l.anh, x, y, w, h, 0);
			}
		}
	}
}
