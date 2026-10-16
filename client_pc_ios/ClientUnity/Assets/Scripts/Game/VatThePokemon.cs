using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Do vat phong cach PMD (RawAsset Object/, Item/) thay hinh KPAH cu cho:
///  - 16 quai phan_loai = vat_the (cong thanh, nguyen lieu, ngoc rong, ruong, thung go):
///    bang QUAI theo id template (viec G7.B1);
///  - NPC su kien (lenh -50) la do vat (cong dich chuyen, bang khu, noi banh): Npc_Server
///    goi veNpc theo idImg (viec G7.B2).
/// Anh: Resources/poke/vt_&lt;id&gt;.bytes va vt_npc&lt;idImg&gt;.bytes (PNG), sinh boi
/// tools/pokemon/tai_vat_the.py; bang doi chieu docs/pokemon/bang_vat_the.csv (sua 2 noi cung
/// luc - test/KiemHinhPokemon so khop). Chi ve khi PokeBody.CHE_DO_POKEMON; thieu file thi
/// moi ham tra ve false de noi goi ve kieu cu.
/// </summary>
public static class VatThePokemon
{
	/// <summary>{id template quai, ti le %, so khung} - anh vt_&lt;id&gt;.bytes.</summary>
	public static readonly short[][] QUAI = new short[][]
	{
		new short[] { 83, 200, 1 },   // cong thanh -> tuong khoi da
		new short[] { 85, 200, 1 },   // Da -> Rock_Gray
		new short[] { 86, 200, 1 },   // Bong -> Silk_White
		new short[] { 87, 100, 1 },   // Go -> Logs_Stacked
		new short[] { 88, 200, 1 },   // Da (thu) -> Belt_Brown
		new short[] { 89, 200, 1 },   // Sat -> Iron_Ball
		new short[] { 119, 200, 1 },  // cong thanh
		new short[] { 121, 100, 1 },  // Ruong ma quai -> Chest tim
		new short[] { 122, 200, 1 },  // Ngoc 1 sao
		new short[] { 123, 200, 1 },  // Ngoc 2 sao
		new short[] { 124, 200, 1 },  // Ngoc 3 sao
		new short[] { 125, 200, 1 },  // Ngoc 4 sao
		new short[] { 126, 200, 1 },  // Ngoc 5 sao
		new short[] { 127, 200, 1 },  // Ngoc 6 sao
		new short[] { 128, 200, 1 },  // Ngoc 7 sao
		new short[] { 131, 200, 1 }   // Thung go -> Box_Brown
	};

	/// <summary>{idImg NPC su kien, ti le %, so khung} - anh vt_npc&lt;idImg&gt;.bytes.</summary>
	public static readonly short[][] NPC = new short[][]
	{
		new short[] { 13, 100, 6 },   // Cong dich chuyen -> Portal
		new short[] { 15, 150, 1 },   // Khu N -> Sign
		new short[] { 25, 250, 1 },   // Noi banh chung -> Pot
		new short[] { 26, 250, 1 }    // Noi banh tet -> Pot
	};

	private class Anh
	{
		public Image img;

		public int tiLe;

		public int soKhung;
	}

	private static Dictionary<string, Anh> cache = new Dictionary<string, Anh>();

	private static short[] tim(short[][] bang, int id)
	{
		for (int i = 0; i < bang.Length; i++)
		{
			if (bang[i][0] == id)
			{
				return bang[i];
			}
		}
		return null;
	}

	private static Anh lay(string khoa, short[] dong)
	{
		if (dong == null || !PokeBody.CHE_DO_POKEMON)
		{
			return null;
		}
		Anh a;
		if (cache.TryGetValue(khoa, out a))
		{
			return a;
		}
		a = null;
		try
		{
			TextAsset t = (TextAsset)Resources.Load(PokeBody.THU_MUC + khoa, typeof(TextAsset));
			if (t != null && t.bytes != null && t.bytes.Length > 0)
			{
				Image img = Image.createImage(t.bytes);
				if (img != null && img.w > 1)
				{
					a = new Anh();
					a.img = img;
					a.tiLe = dong[1] > 0 ? dong[1] : 100;
					a.soKhung = dong[2] > 0 ? dong[2] : 1;
				}
			}
		}
		catch (System.Exception)
		{
			// Vd tao Texture2D ngoai luong chinh: khong nho ket qua, lan sau thu lai.
			return null;
		}
		cache[khoa] = a;
		return a;
	}

	private static Anh layQuai(int idTemplate)
	{
		return lay("vt_" + idTemplate, tim(QUAI, idTemplate));
	}

	private static Anh layNpc(int idImg)
	{
		return lay("vt_npc" + idImg, tim(NPC, idImg));
	}

	/// <summary>Template quai nay ve bang do vat PMD (che do Pokemon + co anh).</summary>
	public static bool laVatThe(int idTemplate)
	{
		return layQuai(idTemplate) != null;
	}

	public static bool laNpcVatThe(int idImg)
	{
		return layNpc(idImg) != null;
	}

	/// <summary>Ve day giua tai (x, y) - (x, y) la diem chan cua actor.</summary>
	private static void ve(MyGraphics g, Anh a, int x, int y, int tick)
	{
		int rong = a.img.w / a.soKhung;
		int cao = a.img.h;
		int f = (a.soKhung > 1) ? (tick / 6 % a.soKhung) : 0;
		int rongVe = rong * a.tiLe / 100;
		int caoVe = cao * a.tiLe / 100;
		if (a.tiLe == 100)
		{
			g.drawRegion(a.img, f * rong, 0, rong, cao, 0, x, y, MyGraphics.BOTTOM | MyGraphics.HCENTER);
			return;
		}
		g.drawRegionScale(a.img, f * rong, 0, rong, cao, x - rongVe / 2, y - caoVe, rongVe, caoVe, 0);
	}

	private static int caoVe(Anh a)
	{
		return (a == null) ? 0 : (a.img.h * a.tiLe / 100);
	}

	public static bool veQuai(MyGraphics g, int idTemplate, int x, int y)
	{
		Anh a = layQuai(idTemplate);
		if (a == null)
		{
			return false;
		}
		ve(g, a, x, y, Canvas.gameTick);
		return true;
	}

	public static int caoQuai(int idTemplate)
	{
		return caoVe(layQuai(idTemplate));
	}

	public static bool veNpc(MyGraphics g, int idImg, int x, int y)
	{
		Anh a = layNpc(idImg);
		if (a == null)
		{
			return false;
		}
		ve(g, a, x, y, Canvas.gameTick);
		return true;
	}

	public static int caoNpc(int idImg)
	{
		return caoVe(layNpc(idImg));
	}

	/// <summary>Ve thu nho vua o canh x canh, tam (xTam, yTam) - dung cho khung muc tieu.</summary>
	private static bool veGoc(MyGraphics g, Anh a, int xTam, int yTam, int canh)
	{
		if (a == null)
		{
			return false;
		}
		int rong = a.img.w / a.soKhung;
		int cao = a.img.h;
		int rongVe = canh;
		int caoVe2 = canh;
		if (rong >= cao)
		{
			caoVe2 = cao * canh / rong;
		}
		else
		{
			rongVe = rong * canh / cao;
		}
		g.drawRegionScale(a.img, 0, 0, rong, cao, xTam - rongVe / 2, yTam - caoVe2 / 2, rongVe, caoVe2, 0);
		return true;
	}

	public static bool veGocQuai(MyGraphics g, int idTemplate, int xTam, int yTam, int canh)
	{
		return veGoc(g, layQuai(idTemplate), xTam, yTam, canh);
	}

	public static bool veGocNpc(MyGraphics g, int idImg, int xTam, int yTam, int canh)
	{
		return veGoc(g, layNpc(idImg), xTam, yTam, canh);
	}
}
