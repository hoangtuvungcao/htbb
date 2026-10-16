using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Ve than Pokemon nguyen con (thay cho paper doll) tu sprite dong goi san trong client:
/// Resources/poke/&lt;khoa&gt;_anh.bytes (atlas PNG), &lt;khoa&gt;_dl.bytes (du lieu khung,
/// xem PokeDuLieu), &lt;khoa&gt;_cd.bytes (chan dung 40x40). khoa = dex 4 chu so, them
/// "s" neu la ban shiny ("0004", "0077s"). Quyet dinh Q5/Q6 trong
/// docs/pokemon/KE_HOACH_CHUYEN_DOI.md.
///
/// Thieu file thi moi ham ve tra ve false de noi goi quay ve cach ve cu. Ket qua nap
/// (ke ca that bai) duoc nho lai, nen thieu file khong gay Resources.Load moi khung hinh.
/// </summary>
public class PokeBody
{
	public const string THU_MUC = "poke/";

	/// <summary>
	/// Cong tac phia client cua co che_do_pokemon (Q8). Chi dung cho cac man TRUOC khi vao
	/// game (tao/chon nhan vat) vi luc do server chua gui gi. Trong game, moi cho ve Pokemon
	/// deu do server dieu khien: server tat co thi khong gui -90/-91, dex = 0, ve kieu cu.
	/// </summary>
	public static bool CHE_DO_POKEMON = true;

	private static Dictionary<int, PokeBody> cache = new Dictionary<int, PokeBody>();

	private static Dictionary<int, Image> cacheChanDung = new Dictionary<int, Image>();

	public Image anh;

	public PokeDuLieu dl;

	private static int khoaCache(int dex, bool shiny)
	{
		return dex * 2 + (shiny ? 1 : 0);
	}

	public static string tenKhoa(int dex, bool shiny)
	{
		return dex.ToString("D4") + (shiny ? "s" : string.Empty);
	}

	private static byte[] docTaiNguyen(string duongDan)
	{
		TextAsset textAsset = (TextAsset)Resources.Load(duongDan, typeof(TextAsset));
		if (textAsset == null || textAsset.bytes == null || textAsset.bytes.Length == 0)
		{
			return null;
		}
		return textAsset.bytes;
	}

	/// <summary>Loi bat thuong khi nap (vd tao Texture2D ngoai luong chinh): khong nho ket qua, lan sau thu lai.</summary>
	private static bool loiTam;

	private static PokeBody napTuFile(int dex, bool shiny)
	{
		try
		{
			string khoa = tenKhoa(dex, shiny);
			byte[] bDl = docTaiNguyen(THU_MUC + khoa + "_dl");
			if (bDl == null)
			{
				return null;
			}
			PokeDuLieu dl = PokeDuLieu.doc(bDl);
			if (dl == null)
			{
				Debug.LogError("PokeBody: du lieu khung hong " + khoa);
				return null;
			}
			byte[] bAnh = docTaiNguyen(THU_MUC + khoa + "_anh");
			if (bAnh == null)
			{
				return null;
			}
			Image anh = Image.createImage(bAnh);
			if (anh == null || anh.w <= 1)
			{
				return null;
			}
			PokeBody pb = new PokeBody();
			pb.anh = anh;
			pb.dl = dl;
			return pb;
		}
		catch (System.Exception)
		{
			loiTam = true;
			return null;
		}
	}

	/// <summary>Lay than Pokemon tu cache (nap lan dau). Shiny thieu file thi dung ban thuong.
	/// Tra ve null neu khong co sprite.</summary>
	public static PokeBody lay(int dex, bool shiny)
	{
		if (dex <= 0)
		{
			return null;
		}
		int k = khoaCache(dex, shiny);
		PokeBody pb;
		if (cache.TryGetValue(k, out pb))
		{
			return pb;
		}
		loiTam = false;
		pb = napTuFile(dex, shiny);
		if (loiTam)
		{
			return null;
		}
		if (pb == null && shiny)
		{
			pb = lay(dex, false);
		}
		cache[k] = pb;
		return pb;
	}

	public static bool coSprite(int dex, bool shiny)
	{
		return lay(dex, shiny) != null;
	}

	/// <summary>
	/// Ve Pokemon sao cho diem chan (tam khung + dxChan/dyChan) trung (x, y).
	/// (x, y) la diem chan cua actor: Char ve paper doll/bong/imgDie quanh (x, y) voi
	/// y la mat dat (Char.cs paint: imgShadow tai y-2, imgDie tai y-27), quai ve bong tai (x, y).
	/// dir: Char.DOWN/UP/LEFT/RIGHT. dongTac: PokeDuLieu.DUNG..GUC_NGA.
	/// tick: so tick game (Canvas.gameTick, 50/giay) ke tu luc bat dau dong tac.
	/// tiLe: phan tram (100 = 1x). doSang: 0 = binh thuong (xem MyGraphics.drawRegionScale).
	/// </summary>
	public static bool paint(MyGraphics g, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null)
		{
			return false;
		}
		pb.ve(g, x, y, dir, dongTac, tick, tiLe, doSang);
		return true;
	}

	public static bool paint(MyGraphics g, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick, int tiLe)
	{
		return paint(g, dex, shiny, x, y, dir, dongTac, tick, tiLe, 0);
	}

	public void ve(MyGraphics g, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang)
	{
		if (dir < 0 || dir > 3)
		{
			dir = 0;
		}
		if (tiLe <= 0)
		{
			tiLe = 100;
		}
		PokeDongTac d = dl.layDongTac(dongTac);
		int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
		int rong = d.rongKhung;
		int cao = d.caoKhung;
		int xNguon = f * rong;
		int yNguon = d.yHang(dir);
		if (tiLe == 100 && doSang <= 0)
		{
			int xVe = x - rong / 2 - d.dxChan[dir];
			int yVe = y - cao / 2 - d.dyChan[dir];
			g.drawRegion(anh, xNguon, yNguon, rong, cao, 0, xVe, yVe, 0);
			return;
		}
		int rongVe = rong * tiLe / 100;
		int caoVe = cao * tiLe / 100;
		int xVe2 = x - (rong / 2 + d.dxChan[dir]) * tiLe / 100;
		int yVe2 = y - (cao / 2 + d.dyChan[dir]) * tiLe / 100;
		g.drawRegionScale(anh, xNguon, yNguon, rong, cao, xVe2, yVe2, rongVe, caoVe, doSang);
	}

	/// <summary>Chieu cao phan hinh nam tren diem chan (de dat ten/icon tren dau).</summary>
	public static int caoTrenChan(int dex, bool shiny, int tiLe)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null)
		{
			return 0;
		}
		PokeDongTac d = pb.dl.layDongTac(PokeDuLieu.DUNG);
		if (tiLe <= 0)
		{
			tiLe = 100;
		}
		return (d.caoKhung / 2 + d.dyChan[0]) * tiLe / 100;
	}

	/// <summary>Chan dung 40x40 (null neu khong co). Shiny thieu thi dung ban thuong.</summary>
	public static Image chanDung(int dex, bool shiny)
	{
		if (dex <= 0)
		{
			return null;
		}
		int k = khoaCache(dex, shiny);
		Image img;
		if (cacheChanDung.TryGetValue(k, out img))
		{
			return img;
		}
		img = null;
		try
		{
			byte[] b = docTaiNguyen(THU_MUC + tenKhoa(dex, shiny) + "_cd");
			if (b != null)
			{
				img = Image.createImage(b);
				if (img != null && img.w <= 1)
				{
					img = null;
				}
			}
		}
		catch (System.Exception)
		{
			img = null;
		}
		if (img == null && shiny)
		{
			img = chanDung(dex, false);
		}
		cacheChanDung[k] = img;
		return img;
	}

	/// <summary>Ve chan dung voi tam (xTam, yTam), canh = kich thuoc ve (40 = goc).</summary>
	public static bool paintChanDung(MyGraphics g, int dex, bool shiny, int xTam, int yTam, int canh)
	{
		Image img = chanDung(dex, shiny);
		if (img == null)
		{
			return false;
		}
		if (canh == img.w && canh == img.h)
		{
			g.drawImage(img, xTam, yTam, MyGraphics.HCENTER | MyGraphics.VCENTER);
		}
		else
		{
			g.drawRegionScale(img, 0, 0, img.w, img.h, xTam - canh / 2, yTam - canh / 2, canh, canh, 0);
		}
		return true;
	}
}
