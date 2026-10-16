using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Hieu ung chieu kieu Pokemon (viec K3, docs/pokemon/KE_HOACH_CHUYEN_DOI.md) tu particle
/// PMDCollab RawAsset dong goi boi tools/pokemon/tai_hieu_ung_pmd.py:
///   Resources/poke/hu_bang.bytes  bang van ban, moi dong
///       lop,chieu,ten,rong,cao,soKhung,soHuong,soCot,kieuVe,tickMoiKhung
///   Resources/poke/hu_&lt;ten&gt;.bytes  PNG. soHuong = 4: hang d (Char.DOWN/UP/LEFT/RIGHT) chua
///       soKhung khung; soHuong = 1: khung i o cot i % soCot, hang i / soCot.
/// kieuVe: D = no tai muc tieu, B = bay tu nguoi danh toi muc tieu, T = tren nguoi dung chieu.
///
/// Char co pokeDex &gt; 0 dung chieu (lop, chieu) co trong bang thi Char.setCurrentSkillByID lay
/// SkillAnimate o day (layChieu) thay cho hieu ung KPAH cu. Thieu bang/anh thi tra ve null va
/// giu hieu ung cu. Ket qua nap (ke ca that bai) duoc nho, khong Resources.Load moi khung hinh.
/// </summary>
public class PokeHieuUng
{
	public const char VE_TAI_DICH = 'D';

	public const char VE_BAY = 'B';

	public const char VE_TREN_NGUOI = 'T';

	public string ten;

	public int rong;

	public int cao;

	public int soKhung;

	public int soHuong;

	public int soCot;

	public char kieuVe;

	public int tickMoiKhung;

	private static Dictionary<int, PokeHieuUng> bang;

	private static Dictionary<string, Image> cacheAnh = new Dictionary<string, Image>();

	private static Dictionary<int, SkillPokemon> cacheChieu = new Dictionary<int, SkillPokemon>();

	private static int khoa(int lop, int chieu)
	{
		return lop * 100 + chieu;
	}

	private static void napBang()
	{
		if (bang != null)
		{
			return;
		}
		bang = new Dictionary<int, PokeHieuUng>();
		try
		{
			TextAsset ta = (TextAsset)Resources.Load(PokeBody.THU_MUC + "hu_bang", typeof(TextAsset));
			if (ta == null || ta.bytes == null)
			{
				return;
			}
			string[] dong = System.Text.Encoding.UTF8.GetString(ta.bytes).Split('\n');
			for (int i = 0; i < dong.Length; i++)
			{
				string s = dong[i].Trim();
				if (s.Length == 0 || s[0] == '#')
				{
					continue;
				}
				string[] o = s.Split(',');
				if (o.Length < 10)
				{
					continue;
				}
				PokeHieuUng h = new PokeHieuUng();
				h.ten = o[2];
				h.rong = int.Parse(o[3]);
				h.cao = int.Parse(o[4]);
				h.soKhung = int.Parse(o[5]);
				h.soHuong = int.Parse(o[6]);
				h.soCot = int.Parse(o[7]);
				h.kieuVe = (o[8].Length > 0) ? o[8][0] : VE_TAI_DICH;
				h.tickMoiKhung = int.Parse(o[9]);
				if (h.rong <= 0 || h.cao <= 0 || h.soKhung <= 0 || h.soCot <= 0 || h.tickMoiKhung <= 0)
				{
					continue;
				}
				bang[khoa(int.Parse(o[0]), int.Parse(o[1]))] = h;
			}
		}
		catch (System.Exception)
		{
		}
	}

	/// <summary>Thong tin hieu ung cua chieu (null neu khong co).</summary>
	public static PokeHieuUng lay(int lop, int chieu)
	{
		napBang();
		PokeHieuUng h;
		if (bang.TryGetValue(khoa(lop, chieu), out h) && h.anh() != null)
		{
			return h;
		}
		return null;
	}

	public Image anh()
	{
		Image img;
		if (cacheAnh.TryGetValue(ten, out img))
		{
			return img;
		}
		img = null;
		try
		{
			TextAsset ta = (TextAsset)Resources.Load(PokeBody.THU_MUC + ten, typeof(TextAsset));
			if (ta != null && ta.bytes != null && ta.bytes.Length > 0)
			{
				img = Image.createImage(ta.bytes);
				if (img != null && img.w <= 1)
				{
					img = null;
				}
			}
		}
		catch (System.Exception)
		{
			// Loi bat thuong (vd ngoai luong chinh): khong nho, lan sau thu lai.
			return null;
		}
		cacheAnh[ten] = img;
		return img;
	}

	/// <summary>So tick (50/giay) de chay het mot luot hieu ung.</summary>
	public int tongTick()
	{
		return soKhung * tickMoiKhung;
	}

	/// <summary>Ve khung thu f voi TAM tai (x, y). dir: Char.DOWN..RIGHT (chi dung khi soHuong = 4).</summary>
	public void ve(MyGraphics g, int f, int x, int y, int dir)
	{
		Image img = anh();
		if (img == null)
		{
			return;
		}
		if (f < 0)
		{
			f = 0;
		}
		if (f >= soKhung)
		{
			f = soKhung - 1;
		}
		int xNguon;
		int yNguon;
		if (soHuong >= 4)
		{
			if (dir < 0 || dir > 3)
			{
				dir = 0;
			}
			xNguon = f * rong;
			yNguon = dir * cao;
		}
		else
		{
			xNguon = f % soCot * rong;
			yNguon = f / soCot * cao;
		}
		g.drawRegion(img, xNguon, yNguon, rong, cao, 0, x - rong / 2, y - cao / 2, 0);
	}

	/// <summary>Cho Char.setCurrentSkillByID: SkillAnimate kieu Pokemon, hoac null de giu cach cu.</summary>
	public static SkillAnimate layChieu(Char c, int chieu)
	{
		if (c == null || c.pokeDex <= 0 || !PokeBody.CHE_DO_POKEMON)
		{
			return null;
		}
		PokeHieuUng h = lay(c.clazz, chieu);
		if (h == null)
		{
			return null;
		}
		int k = khoa(c.clazz, chieu);
		SkillPokemon sp;
		if (!cacheChieu.TryGetValue(k, out sp))
		{
			sp = new SkillPokemon(h);
			cacheChieu[k] = sp;
		}
		return sp;
	}

	/// <summary>
	/// Chieu bo tro (buff) vua dinh len actor: hieu ung buff cu (CharBuff) van giu de bao
	/// trang thai, them mot luot particle cua chieu tren nguoi actor neu actor la Pokemon.
	/// loaiBuff: so trong SkillManager.EFF_BUFF_SKILL (chieu = 4 + vi tri).
	/// </summary>
	public static void khiNhanBuff(Actor actor, int loaiBuff)
	{
		try
		{
			Char c = actor as Char;
			if (c == null || c.pokeDex <= 0 || !PokeBody.CHE_DO_POKEMON)
			{
				return;
			}
			sbyte[][] eff = SkillManager.EFF_BUFF_SKILL;
			int lop = -1;
			int chieu = -1;
			for (int t = -1; t < eff.Length && lop < 0; t++)
			{
				int l = (t < 0) ? c.clazz : t;
				if (l < 0 || l >= eff.Length)
				{
					continue;
				}
				for (int j = 0; j < eff[l].Length; j++)
				{
					if (eff[l][j] == loaiBuff)
					{
						lop = l;
						chieu = 4 + j;
						break;
					}
				}
			}
			if (lop < 0)
			{
				return;
			}
			PokeHieuUng h = lay(lop, chieu);
			if (h != null)
			{
				EffectManager.addHiEffect(new HieuUngPoke(h, c, null, c.dir));
			}
		}
		catch (System.Exception)
		{
		}
	}
}

/// <summary>Mot luot particle dang chay (them vao EffectManager.hiEffects).</summary>
public class HieuUngPoke : Effect
{
	private PokeHieuUng h;

	private Actor theo;

	private int tick;

	private int dir;

	private int x0;

	private int y0;

	private int x1;

	private int y1;

	private int tickBay;

	/// <summary>Do cao tam hieu ung so voi diem chan actor.</summary>
	public const int CAO_THAN = 18;

	/// <summary>
	/// Kieu D/T: ve tai actor "dich" (di theo actor). Kieu B: bay tu "tu" toi "dich".
	/// </summary>
	public HieuUngPoke(PokeHieuUng h, Actor dich, Actor tu, int dir)
	{
		this.h = h;
		this.dir = dir;
		theo = dich;
		x = (x1 = dich.x);
		y = (y1 = dich.y - CAO_THAN);
		if (tu != null && h.kieuVe == PokeHieuUng.VE_BAY)
		{
			x0 = tu.x;
			y0 = tu.y - CAO_THAN;
			tickBay = thoiGianBay(tu, dich);
			x = x0;
			y = y0;
		}
	}

	public static int thoiGianBay(Actor tu, Actor dich)
	{
		int dx = dich.x - tu.x;
		int dy = dich.y - tu.y;
		int kc = (int)Mathf.Sqrt(dx * dx + dy * dy);
		return Mathf.Clamp(kc / 10, 4, 20);
	}

	public override void update()
	{
		tick++;
		if (theo != null)
		{
			x1 = theo.x;
			y1 = theo.y - CAO_THAN;
		}
		if (tickBay > 0)
		{
			int t = (tick < tickBay) ? tick : tickBay;
			x = x0 + (x1 - x0) * t / tickBay;
			y = y0 + (y1 - y0) * t / tickBay;
			// Dan bay: lap lai hoat anh trong luc bay, toi noi thi het.
			if (tick >= tickBay)
			{
				wantDetroy = true;
			}
		}
		else
		{
			x = x1;
			y = y1;
			if (tick >= h.tongTick())
			{
				wantDetroy = true;
			}
		}
	}

	public override void paint(MyGraphics g)
	{
		int f = tick / h.tickMoiKhung;
		if (tickBay > 0)
		{
			f %= h.soKhung;
		}
		h.ve(g, f, x, y, dir);
	}
}

/// <summary>
/// SkillAnimate thay cho Skill_*_Type* khi nguoi danh la Pokemon: cung nhip trang thai
/// (c.state/p1, frame danh, tru mau bay chu, muc tieu nhay) nhung ve particle PMD.
/// Dung chung giua cac Char nhu SkillManager.SKILL_ANIMATE (moi trang thai nam trong Char).
/// </summary>
public class SkillPokemon : SkillAnimate
{
	private PokeHieuUng h;

	/// <summary>Muc tieu phu cua chieu dien rong (setMonster/setActorter), xoa sau khi trung.</summary>
	private mVector mst = new mVector();

	public SkillPokemon(PokeHieuUng h)
		: base(-1)
	{
		this.h = h;
	}

	public override void setMonster(mVector m)
	{
		mst.removeAllElements();
		for (int i = 0; i < m.size(); i++)
		{
			mst.addElement(m.elementAt(i));
		}
	}

	public override void setActorter(mVector m)
	{
		setMonster(m);
	}

	private int tickTrung(Char c)
	{
		if (h.kieuVe == PokeHieuUng.VE_BAY && c.attackTarget != null)
		{
			return HieuUngPoke.thoiGianBay(c, c.attackTarget);
		}
		return 4;
	}

	public override void updateSkill(Char c)
	{
		if (c == null)
		{
			return;
		}
		base.updateSkill(c);
		if (c.state == 0)
		{
			return;
		}
		LiveActor dich = c.attackTarget;
		int trung = tickTrung(c);
		c.frame = (sbyte)((c.p1 < trung) ? 4 : 5);
		c.weapon_frame = 4;
		if (c.p1 == 0)
		{
			if (h.kieuVe == PokeHieuUng.VE_TREN_NGUOI)
			{
				EffectManager.addHiEffect(new HieuUngPoke(h, c, null, c.dir));
			}
			else if (h.kieuVe == PokeHieuUng.VE_BAY)
			{
				EffectManager.addHiEffect(new HieuUngPoke(h, dich, c, c.dir));
			}
			else
			{
				EffectManager.addHiEffect(new HieuUngPoke(h, dich, null, c.dir));
			}
		}
		if (c.p1 == trung)
		{
			trungDon(c, dich, false);
			for (int i = 0; i < mst.size(); i++)
			{
				try
				{
					LiveActor a = mst.elementAt(i) as LiveActor;
					if (a != null && a != dich && !a.isNPC())
					{
						if (h.kieuVe != PokeHieuUng.VE_TREN_NGUOI)
						{
							EffectManager.addHiEffect(new HieuUngPoke(h, a, null, c.dir));
						}
						trungDon(c, a, true);
					}
				}
				catch (System.Exception)
				{
				}
			}
			mst.removeAllElements();
		}
		if (c.p1 >= trung + 8)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
			return;
		}
		c.p1++;
	}

	private void trungDon(Char c, LiveActor a, bool phu)
	{
		if (a == null)
		{
			return;
		}
		if (a.catagory == 1)
		{
			((Monster)a).jump();
		}
		else if (phu)
		{
			a.jump();
		}
		a.realHPSyncTime = 2;
		if (c.attkPower != 0 && c.attkPower != 2000000)
		{
			Canvas.gameScr.startFlyText("-" + c.attkPower, 0, a.x, a.y - 15, -1, -2);
		}
		if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.Length)
		{
			Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, a.x, a.y - 15, 2, -2);
		}
	}

	public override void updateSkill(Monster c)
	{
	}
}
