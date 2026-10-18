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

	/// <summary>Khoa tep that da nap ("0025", "0036s"; shiny thieu tep thi la khoa ban thuong).</summary>
	public string khoa;

	/// <summary>So thu tu cua than (dung lam khoa bo dem anh vien / trang phuc).</summary>
	private int soThuTu;

	private static int demThan;

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
			pb.khoa = khoa;
			pb.soThuTu = ++demThan;
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

	/// <summary>
	/// Nhu tren, them trangPhuc (1.. = bang mau trang phuc cua PhuKien, 0 = khong): atlas doi mau mot lan roi
	/// nho (theo than + trangPhuc). Shiny giu: doi mau tren nen atlas shiny. Chi ti le 100 (nguoi choi).
	/// </summary>
	public static bool paint(MyGraphics g, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang, int trangPhuc)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null)
		{
			return false;
		}
		veTrangPhuc = trangPhuc;
		try
		{
			pb.ve(g, x, y, dir, dongTac, tick, tiLe, doSang);
		}
		finally
		{
			veTrangPhuc = 0;
		}
		return true;
	}

	// ---------------------------------------------------------------- ngoai hinh trang bi

	/// <summary>Trang phuc cua lan ve dang chay (chi luong chinh ve).</summary>
	private static int veTrangPhuc;

	/// <summary>Atlas da doi mau trang phuc: khoa = soThuTu * 256 + trangPhuc. Atlas to (vai MB) nen chi giu it.</summary>
	private static readonly Dictionary<int, Image> demTrangPhuc = new Dictionary<int, Image>();

	private static readonly List<int> thuTuTrangPhuc = new List<int>();

	private const int TOI_DA_TRANG_PHUC = 12;

	/// <summary>Atlas doi mau trang phuc tp (nho); loi thi tra ve atlas goc.</summary>
	private Image anhTrangPhuc(int tp)
	{
		int k = soThuTu * 256 + (tp & 0xFF);
		Image img;
		if (demTrangPhuc.TryGetValue(k, out img))
		{
			return img ?? anh;
		}
		try
		{
			Texture2D goc = anh.texture;
			Color32[] px = goc.GetPixels32();
			PhuKien.doiMauTrangPhuc(px, tp);
			Texture2D t = new Texture2D(goc.width, goc.height, TextureFormat.RGBA32, false);
			t.filterMode = FilterMode.Point;
			t.wrapMode = TextureWrapMode.Clamp;
			t.SetPixels32(px);
			t.Apply(false, true);
			img = new Image();
			Object.Destroy(img.texture);
			img.texture = t;
			img.w = anh.w;
			img.h = anh.h;
		}
		catch (System.Exception)
		{
			img = null; // atlas khong doc duoc: ve mau goc, khong thu lai
		}
		if (thuTuTrangPhuc.Count >= TOI_DA_TRANG_PHUC)
		{
			int cu = thuTuTrangPhuc[0];
			thuTuTrangPhuc.RemoveAt(0);
			Image a;
			if (demTrangPhuc.TryGetValue(cu, out a))
			{
				PhuKien.huyAnh(a);
				demTrangPhuc.Remove(cu);
			}
		}
		demTrangPhuc[k] = img;
		thuTuTrangPhuc.Add(k);
		return img ?? anh;
	}

	/// <summary>
	/// Diem neo phu kien (Resources/poke/&lt;khoa&gt;_neo.bytes, HTBB tools/pokemon/TinhNeoPoke.java):
	/// neo[dongTac] = soKhung (byte dau) roi 4 huong x soKhung x 8 byte. null = khong co tep.
	/// </summary>
	private byte[][] neoDl;

	private bool daNapNeo;

	public const int BYTE_NEO = 8;

	private byte[][] layNeo()
	{
		if (daNapNeo)
		{
			return neoDl;
		}
		daNapNeo = true;
		try
		{
			byte[] b = docTaiNguyen(THU_MUC + khoa + "_neo");
			if (b == null || b.Length < 3 || b[0] != 'N' || b[1] != 1)
			{
				return null;
			}
			int so = b[2];
			int p = 3;
			byte[][] kq = new byte[so][];
			for (int a = 0; a < so; a++)
			{
				int n = b[p];
				int dai = 1 + 4 * n * BYTE_NEO;
				if (p + dai > b.Length)
				{
					break;
				}
				kq[a] = new byte[dai];
				System.Array.Copy(b, p, kq[a], 0, dai);
				p += dai;
			}
			neoDl = kq;
		}
		catch (System.Exception)
		{
			neoDl = null;
		}
		return neoDl;
	}

	/// <summary>
	/// Diem neo phu kien cua khung DANG ve (cung cach chon khung nhu ve()), da nhan tiLe, so voi diem chan:
	/// out = {dauX, dauY, dinhY, tayX, tayY, tamX, tamY, co}. false = loai khong co neo.
	/// </summary>
	public static bool neo(int dex, bool shiny, int dir, int dongTac, int tick, int tiLe, int[] outNeo)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null)
		{
			return false;
		}
		byte[][] ds = pb.layNeo();
		if (ds == null)
		{
			return false;
		}
		if (dir < 0 || dir > 3)
		{
			dir = 0;
		}
		if (tiLe <= 0)
		{
			tiLe = 100;
		}
		PokeDongTac d = pb.dl.layDongTac(dongTac);
		int a = dongTac < 0 || dongTac >= pb.dl.dongTac.Length ? 0 : dongTac;
		if (a >= ds.Length || ds[a] == null)
		{
			return false;
		}
		byte[] nb = ds[a];
		int n = nb[0];
		if (n <= 0)
		{
			return false;
		}
		int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
		if (f >= n)
		{
			f = n - 1;
		}
		int p = 1 + (dir * n + f) * BYTE_NEO;
		if (p + BYTE_NEO > nb.Length)
		{
			return false;
		}
		for (int i = 0; i < 7; i++)
		{
			outNeo[i] = (sbyte)nb[p + i] * tiLe / 100;
		}
		outNeo[7] = nb[p + 7];
		return true;
	}

	/// <summary>Anh vien sang: khoa = (soThuTu, dongTac, huong, khung, mau).</summary>
	private static readonly Dictionary<long, Image> demVien = new Dictionary<long, Image>();

	/// <summary>
	/// Vien sang 1 px quanh hinh khung dang ve, mau = RGB. Anh vien tinh mot lan cho moi (than, dong tac,
	/// huong, khung, mau) roi nho. Chi ti le 100 (nguoi choi). false = khong ve.
	/// </summary>
	public static bool veVien(MyGraphics g, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick, int mau)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null || pb.anh == null || pb.anh.texture == null)
		{
			return false;
		}
		if (dir < 0 || dir > 3)
		{
			dir = 0;
		}
		PokeDongTac d = pb.dl.layDongTac(dongTac);
		int a = dongTac < 0 || dongTac >= pb.dl.dongTac.Length ? 0 : dongTac;
		int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
		int w = d.rongKhung, h = d.caoKhung;
		int sx = d.xAtlas + f * w, sy = d.yHang(dir);
		long k = ((((long)pb.soThuTu * 8 + a) * 4 + dir) * 256 + (f & 0xFF)) * 0x1000000L + (mau & 0xFFFFFF);
		Image img;
		if (!demVien.TryGetValue(k, out img))
		{
			img = null;
			Texture2D tex = pb.anh.texture;
			if (sx + w <= tex.width && sy + h <= tex.height)
			{
				try
				{
					// GetPixels: hang DUOI truoc
					Color[] src = tex.GetPixels(sx, tex.height - sy - h, w, h);
					bool[] dac = new bool[w * h];
					for (int yy = 0; yy < h; yy++)
					{
						for (int xx = 0; xx < w; xx++)
						{
							dac[yy * w + xx] = src[(h - 1 - yy) * w + xx].a >= 0.5f;
						}
					}
					int W = w + 2, H = h + 2;
					Color32[] dst = new Color32[W * H];
					Color32 c = new Color32((byte)((mau >> 16) & 0xFF), (byte)((mau >> 8) & 0xFF), (byte)(mau & 0xFF), 255);
					for (int yy = 0; yy < H; yy++)
					{
						for (int xx = 0; xx < W; xx++)
						{
							int ox = xx - 1, oy = yy - 1;
							if (laDac(dac, w, h, ox, oy))
							{
								continue; // diem anh cua than: de trong
							}
							if (laDac(dac, w, h, ox - 1, oy) || laDac(dac, w, h, ox + 1, oy) || laDac(dac, w, h, ox, oy - 1) || laDac(dac, w, h, ox, oy + 1))
							{
								dst[yy * W + xx] = c;
							}
						}
					}
					img = PhuKien.taoAnh(dst, W, H);
				}
				catch (System.Exception)
				{
					return false; // vd ngoai luong chinh: lan sau thu lai
				}
			}
			if (demVien.Count > 400)
			{
				foreach (Image cu in demVien.Values)
				{
					PhuKien.huyAnh(cu);
				}
				demVien.Clear();
			}
			demVien[k] = img;
		}
		if (img == null)
		{
			return false;
		}
		int xVe = x - w / 2 - d.dxChan[dir];
		int yVe = y - h / 2 - d.dyChan[dir];
		g.drawImage(img, xVe - 1, yVe - 1);
		return true;
	}

	private static bool laDac(bool[] dac, int w, int h, int x, int y)
	{
		return x >= 0 && y >= 0 && x < w && y < h && dac[y * w + x];
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
		int xNguon = d.xAtlas + f * rong;
		int yNguon = d.yHang(dir);
		Image anhVe = (veTrangPhuc > 0 && tiLe == 100) ? anhTrangPhuc(veTrangPhuc) : anh;
		if (tiLe == 100 && doSang <= 0)
		{
			int xVe = x - rong / 2 - d.dxChan[dir];
			int yVe = y - cao / 2 - d.dyChan[dir];
			g.drawRegion(anhVe, xNguon, yNguon, rong, cao, 0, xVe, yVe, 0);
			return;
		}
		int rongVe = rong * tiLe / 100;
		int caoVe = cao * tiLe / 100;
		int xVe2 = x - (rong / 2 + d.dxChan[dir]) * tiLe / 100;
		int yVe2 = y - (cao / 2 + d.dyChan[dir]) * tiLe / 100;
		g.drawRegionScale(anhVe, xNguon, yNguon, rong, cao, xVe2, yVe2, rongVe, caoVe, doSang);
	}

	/// <summary>Be ngang hinh dung yen (px dac), -1 = chua tinh.</summary>
	private int rongHinhDem = -1;

	/// <summary>
	/// Be ngang hinh dung yen ~ be ngang con Pokemon, 0 neu khong co. Ban jar dung be ngang khung da cat sat
	/// hinh; khung Unity chua cat nen do hop bao diem anh dac (alpha &gt;= 50%) gop moi khung + 4 huong cua dong
	/// tac dung, tinh mot lan cho moi loai.
	/// </summary>
	public static int rongHinh(int dex, bool shiny)
	{
		PokeBody pb = lay(dex, shiny);
		if (pb == null)
		{
			return 0;
		}
		if (pb.rongHinhDem >= 0)
		{
			return pb.rongHinhDem;
		}
		PokeDongTac d = pb.dl.layDongTac(PokeDuLieu.DUNG);
		int kq = d.rongKhung;
		try
		{
			Texture2D tex = pb.anh.texture;
			int W = d.soKhung * d.rongKhung, H = 4 * d.caoKhung;
			if (d.xAtlas + W <= tex.width && d.yAtlas + H <= tex.height)
			{
				// chi doc vung dong tac dung (hang duoi truoc; thu tu hang khong anh huong be ngang)
				Color[] src = tex.GetPixels(d.xAtlas, tex.height - d.yAtlas - H, W, H);
				int trai = int.MaxValue, phai = -1;
				for (int yy = 0; yy < H; yy++)
				{
					int hang = yy * W;
					for (int f = 0; f < d.soKhung; f++)
					{
						int o = hang + f * d.rongKhung;
						for (int xx = 0; xx < d.rongKhung; xx++)
						{
							if (src[o + xx].a >= 0.5f)
							{
								if (xx < trai)
								{
									trai = xx;
								}
								if (xx > phai)
								{
									phai = xx;
								}
							}
						}
					}
				}
				if (phai >= trai)
				{
					kq = phai - trai + 1;
				}
			}
		}
		catch (System.Exception)
		{
			// atlas khong doc duoc: dung be ngang khung
		}
		pb.rongHinhDem = kq;
		return kq;
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
